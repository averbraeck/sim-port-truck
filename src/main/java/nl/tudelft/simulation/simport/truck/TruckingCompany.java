package nl.tudelft.simulation.simport.truck;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.appointment.Appointment;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.network.RoadNetwork;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.truck.activity.PlannedDrivingActivity;
import nl.tudelft.simulation.simport.truck.activity.PlannedTerminalActivity;
import nl.tudelft.simulation.simport.truck.activity.PlannedTerminalActivity.TerminalActivityType;

/**
 * TruckingCompany is the company that owns one or more trucks.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TruckingCompany implements Identifiable
{
    /** The id. */
    private final String id;

    /** The model. */
    private final PortModel model;

    /** the truck counter. */
    private final AtomicInteger uniqueTruckNr = new AtomicInteger(1000);

    /** The unplanned transport orders. */
    private final NavigableSet<TransportOrder> unplannedOrderSet = new TreeSet<>(new TransportOrderComparator());

    /** Fraction Saturday trips of trips planned on Saturday. */
    private final double fractionSaturday;

    /** Fraction Sunday trips of trips planned on Sunday. */
    private final double fractionSunday;

    /** Cumulative distribution probabilities for the weights per hour of the day. */
    private final NavigableMap<Double, Integer> cumulativeHourProbabilities = new TreeMap<>();

    /**
     * Instantiate a trucking company.
     * @param id the id
     * @param model the model
     * @param nrTruck the number of trucks
     */
    public TruckingCompany(final String id, final PortModel model, final int nrTrucks)
    {
        this.id = id;
        this.model = model;
        getSimulator().scheduleEventRel(new Duration(24.0, DurationUnit.HOUR), () -> planTrips());

        this.fractionSaturday = model.getInputParameterDouble("truck.FractionSaturday");
        this.fractionSunday = model.getInputParameterDouble("truck.FractionSunday");
        setHourWeights();
    }

    protected void setHourWeights()
    {
        String[] stringWeights = this.model.getInputParameterString("truck.HourWeights").split(",");
        Throw.when(stringWeights.length != 24, IllegalArgumentException.class,
                "HourWeights parameter does not have 24 entries");
        double total = 0.0;
        for (String w : stringWeights)
        {
            total += Double.parseDouble(w);
        }
        double cumTotal = 0.0;
        for (int hour = 0; hour < 24; hour++)
        {
            cumTotal += Double.parseDouble(stringWeights[hour]);
            this.cumulativeHourProbabilities.put(cumTotal / total, hour);
        }
    }

    public void bookTrip(final TransportOrder transportOrder)
    {
        planWeekDistribution(transportOrder); // move to preferred day or hour
        this.unplannedOrderSet.add(transportOrder);
    }

    protected void planTrips()
    {
        // Already schedule the next planning cycle
        getSimulator().scheduleEventRel(new Duration(24.0, DurationUnit.HOUR), () -> planTrips());

        // Find the TransportOrders that need to be planned in the next 36 hours
        RoadNetwork roadNetwork = getModel().getRoadNetwork();
        ClockTime cutoff = new ClockTime(getSimulator().getSimulatorClockTime().plus(new Duration(36.0, DurationUnit.HOUR)));
        Set<TransportOrder> planSet = new LinkedHashSet<>();
        for (var it = this.unplannedOrderSet.iterator(); it.hasNext();)
        {
            var to = it.next();
            if (to.targetTime().gt(cutoff))
                break;
            planSet.add(to);
            it.remove();
        }

        // See if there is work to do
        int nrTotal = planSet.size();
        if (nrTotal == 0)
            return;

        int nrCombinedOneTerminal = 0;
        int nrCombinedTwoTerminals = 0;
        for (var it = planSet.iterator(); it.hasNext();)
        {
            var transportOrder = it.next();

            // Plan faraway trips as single trips (import or export)
            if (roadNetwork.getFarCentroids().contains(transportOrder.loadCentroid())
                    || roadNetwork.getFarCentroids().contains(transportOrder.unloadCentroid()))
            {
                planSingleTrip(transportOrder);
                it.remove();
                continue;
            }

            // Try to find dropoff and pickup order at same terminal, up to the given fraction
            if (1.0 * nrCombinedOneTerminal / nrTotal > getModel().getTargetCombinedOneTerminalFraction())
            {
                // Find a matching transport order; transportOrder is always the part of the trip WITH container
                boolean planned = false;
                for (var it2 = planSet.iterator(); it2.hasNext();)
                {
                    var transportOrder2 = it2.next();
                    if (transportOrder == transportOrder2)
                        continue;
                    if (transportOrder.unloadCentroid().equals(transportOrder2.loadCentroid())
                            && getModel().getTerminalCentroids().contains(transportOrder.unloadCentroid()))
                    {
                        planCombinedTripOneTerminal(transportOrder, transportOrder2);
                        it2.remove();
                        it.remove();
                        nrCombinedOneTerminal++;
                        planned = true;
                        break;
                    }
                    else if (transportOrder2.unloadCentroid().equals(transportOrder.loadCentroid())
                            && getModel().getTerminalCentroids().contains(transportOrder2.unloadCentroid()))
                    {
                        planCombinedTripOneTerminal(transportOrder2, transportOrder);
                        it2.remove();
                        it.remove();
                        nrCombinedOneTerminal++;
                        planned = true;
                        break;
                    }
                }
                if (planned)
                    continue;
            }

            // Try to find dropoff and pickup order at different terminals, up to the given fraction
            if (1.0 * nrCombinedTwoTerminals / nrTotal > getModel().getTargetCombinedTwoTerminalFraction())
            {
                // Find a matching transport order
                boolean planned = false;
                for (var it2 = planSet.iterator(); it2.hasNext();)
                {
                    var transportOrder2 = it2.next();
                    if (transportOrder == transportOrder2)
                        continue;
                    if (transportOrder.unloadCentroid().equals(transportOrder2.loadCentroid())
                            && getModel().getTerminalCentroids().contains(transportOrder.unloadCentroid()))
                    {
                        planCombinedTripTwoTerminals(transportOrder, transportOrder2);
                        it2.remove();
                        it.remove();
                        nrCombinedTwoTerminals++;
                        planned = true;
                        break;
                    }
                    else if (transportOrder2.unloadCentroid().equals(transportOrder.loadCentroid())
                            && getModel().getTerminalCentroids().contains(transportOrder.unloadCentroid()))
                    {
                        planCombinedTripTwoTerminals(transportOrder2, transportOrder);
                        it2.remove();
                        it.remove();
                        nrCombinedTwoTerminals++;
                        planned = true;
                        break;
                    }
                }
                if (planned)
                    continue;
            }

            // Single trip
            planSingleTrip(transportOrder);
            it.remove();
        }

    }

    protected void planSingleTrip(final TransportOrder transportOrder)
    {
        Truck truck = generateTruck();

        // unloading, so export container.
        if (transportOrder.loadTerminal() == null)
        {
            this.model.getTruckingStatistics().incExportTrips();

            // (1) Drive full towards terminal
            Terminal terminal = transportOrder.unloadTerminal();
            Appointment appointment = transportOrder.unloadTerminal().bookAppointment(transportOrder);
            ClockTime targetTime = appointment.getTargetTime();
            Duration drivingTime = getModel().getRoadNetwork().drivingTime(transportOrder.loadCentroid(),
                    transportOrder.unloadCentroid(), new Speed(50.0, SpeedUnit.KM_PER_HOUR)); // straight distance
            Duration margin = new Duration(15.0, DurationUnit.MINUTE);
            ClockTime departureTime1 = new ClockTime(targetTime.minus(drivingTime.plus(margin)));
            Length distance = RoadNetwork.haversine(transportOrder.loadCentroid().getLocation(),
                    transportOrder.unloadCentroid().getLocation());
            PlannedDrivingActivity pda1 = new PlannedDrivingActivity(truck, transportOrder.loadCentroid(),
                    transportOrder.unloadCentroid(), transportOrder.container(), null, departureTime1, targetTime, distance);
            truck.getPlannedActivityList().add(pda1);

            // (2) unload container at terminal
            PlannedTerminalActivity pta = new PlannedTerminalActivity(truck, terminal, appointment,
                    TerminalActivityType.DROPOFF, null, null, transportOrder.container(), null);
            truck.getPlannedActivityList().add(pta);

            // (3) drive back empty
            ClockTime departureTime2 = new ClockTime(targetTime.plus(terminal.getYard().getAvgHandlingTimeExport())
                    .plus(terminal.getGate().getAvgGateTimeIn()).plus(terminal.getGate().getAvgGateTimeOut()));
            ClockTime arrivalTime2 = new ClockTime(departureTime2.plus(drivingTime.plus(margin)));
            PlannedDrivingActivity pda2 = new PlannedDrivingActivity(truck, transportOrder.unloadCentroid(),
                    transportOrder.loadCentroid(), null, null, departureTime2, arrivalTime2, distance);
            truck.getPlannedActivityList().add(pda2);
        }

        // loading, so import container.
        else
        {
            this.model.getTruckingStatistics().incImportTrips();

            // (1) Drive empty towards terminal
            Terminal terminal = transportOrder.loadTerminal();
            Appointment appointment = transportOrder.loadTerminal().bookAppointment(transportOrder);
            ClockTime targetTime = appointment.getTargetTime();
            Duration drivingTime = getModel().getRoadNetwork().drivingTime(transportOrder.unloadCentroid(),
                    transportOrder.loadCentroid(), new Speed(50.0, SpeedUnit.KM_PER_HOUR)); // straight distance
            Duration margin = new Duration(15.0, DurationUnit.MINUTE);
            ClockTime departureTime1 = new ClockTime(targetTime.minus(drivingTime.plus(margin)));
            Length distance = RoadNetwork.haversine(transportOrder.unloadCentroid().getLocation(),
                    transportOrder.loadCentroid().getLocation());
            PlannedDrivingActivity pda1 = new PlannedDrivingActivity(truck, transportOrder.unloadCentroid(),
                    transportOrder.loadCentroid(), null, null, departureTime1, targetTime, distance);
            truck.getPlannedActivityList().add(pda1);

            // (2) unload container at terminal
            PlannedTerminalActivity pta = new PlannedTerminalActivity(truck, terminal, appointment, TerminalActivityType.PICKUP,
                    transportOrder.container(), null, null, null);
            truck.getPlannedActivityList().add(pta);

            // (3) drive back full
            ClockTime departureTime2 = new ClockTime(targetTime.plus(terminal.getYard().getAvgHandlingTimeImport())
                    .plus(terminal.getGate().getAvgGateTimeIn()).plus(terminal.getGate().getAvgGateTimeOut()));
            ClockTime arrivalTime2 = new ClockTime(departureTime2.plus(drivingTime.plus(margin)));
            PlannedDrivingActivity pda2 = new PlannedDrivingActivity(truck, transportOrder.loadCentroid(),
                    transportOrder.unloadCentroid(), transportOrder.container(), null, departureTime2, arrivalTime2, distance);
            truck.getPlannedActivityList().add(pda2);
        }

        truck.startPlan();
    }

    protected void planCombinedTripOneTerminal(final TransportOrder transportOrder1, final TransportOrder transportOrder2)
    {
        // this.model.getTruckingStatistics().incCombinedTrips1Terminal();
        // Truck truck = generateTruck();

        planSingleTrip(transportOrder1);
        planSingleTrip(transportOrder2);
    }

    protected void planCombinedTripTwoTerminals(final TransportOrder transportOrder1, final TransportOrder transportOrder2)
    {
        // this.model.getTruckingStatistics().incCombinedTrips2Terminals();
        // Truck truck = generateTruck();

        planSingleTrip(transportOrder1);
        planSingleTrip(transportOrder2);
    }

    // constants
    private static final Duration H24 = new Duration(24.0, DurationUnit.HOUR);

    private static final Duration H48 = new Duration(48.0, DurationUnit.HOUR);

    private static final Duration H72 = new Duration(72.0, DurationUnit.HOUR);

    protected void planWeekDistribution(final TransportOrder transportOrder)
    {
        ClockTime targetTime = transportOrder.targetTime();
        int weekday = targetTime.dayOfWeekInt();

        // 6 = Saturday
        if (weekday == 6 && this.model.getU01().draw() > this.fractionSaturday)
        {
            if (transportOrder.loadTerminal() != null)
            {
                // import order, pick up later, preferably to Monday (or Tuesday)
                if (transportOrder.marginAfter().getInUnit(DurationUnit.HOUR) > 72.0 && this.model.getU01().draw() < 0.33)
                    targetTime = new ClockTime(targetTime.plus(H72));
                else if (transportOrder.marginAfter().getInUnit(DurationUnit.HOUR) > 48.0)
                    targetTime = new ClockTime(targetTime.plus(H48));
            }
            else
            {
                // export order, bring earlier, preferably to Friday
                if (transportOrder.marginBefore().getInUnit(DurationUnit.HOUR) > 48.0 && this.model.getU01().draw() < 0.33)
                    targetTime = new ClockTime(targetTime.minus(H48));
                else if (transportOrder.marginBefore().getInUnit(DurationUnit.HOUR) > 24.0)
                    targetTime = new ClockTime(targetTime.minus(H24));
            }
        }

        // 7 = Sunday
        else if (weekday == 7 && this.model.getU01().draw() > this.fractionSunday)
        {
            if (transportOrder.loadTerminal() != null)
            {
                // import order, shift later, preferably to Monday
                if (transportOrder.marginAfter().getInUnit(DurationUnit.HOUR) > 48.0 && this.model.getU01().draw() < 0.33)
                    targetTime = new ClockTime(targetTime.plus(H48));
                else if (transportOrder.marginAfter().getInUnit(DurationUnit.HOUR) > 24.0)
                    targetTime = new ClockTime(targetTime.plus(H24));
            }
            else
            {
                // export order, shift earlier, preferably to Friday
                if (transportOrder.marginBefore().getInUnit(DurationUnit.HOUR) > 72.0 && this.model.getU01().draw() < 0.33)
                    targetTime = new ClockTime(targetTime.minus(H72));
                else if (transportOrder.marginBefore().getInUnit(DurationUnit.HOUR) > 48.0)
                    targetTime = new ClockTime(targetTime.minus(H48));
            }
        }

        weekday = targetTime.dayOfWeekInt();

        // determine hour of the day
        int hourOfTheDay = this.cumulativeHourProbabilities.ceilingEntry(this.model.getU01().draw()).getValue();
        var localDate = targetTime.localDateTime().toLocalDate();
        int minute = (int) Math.floor(59.9 * this.model.getU01().draw());
        var localDateTime = LocalDateTime.of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                hourOfTheDay, minute);
        ClockTime newTime = ClockTime.ofLocalDateTime(localDateTime);
        if (newTime.minus(getSimulator().getSimulatorClockTime()).gt(H24))
            transportOrder.setTargetTime(newTime);
    }

    protected Truck generateTruck()
    {
        return new Truck(getModel().uniqueTruckNr(), this, this.uniqueTruckNr.incrementAndGet());
    }

    /**
     * @return the port model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /**
     * @return the simulator
     */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return getModel().getSimulator();
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Comparator for two transport orders, only yielding 0 (equal) when both target time and unique id are equal.
     */
    protected class TransportOrderComparator implements Comparator<TransportOrder>
    {
        @Override
        public int compare(final TransportOrder o1, final TransportOrder o2)
        {
            int ct = o1.targetTime().compareTo(o2.targetTime());
            if (ct != 0)
                return ct;
            return Integer.compare(o1.uniqueId(), o2.uniqueId());
        }
    }
}
