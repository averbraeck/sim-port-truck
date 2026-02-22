package nl.tudelft.simulation.simport.truck;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;

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

    /** The currently driving truck fleet. */
    // private final List<Truck> currentFleet = new ArrayList<>();

    /** the truck counter. */
    private final AtomicInteger uniqueTruckNr = new AtomicInteger(1000);

    /** The unplanned transport orders. */
    private final NavigableSet<TransportOrder> unplannedOrderSet = new TreeSet<>(new TransportOrderComparator());

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
    }

    public void bookTrip(final TransportOrder transportOrder)
    {
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
        // Truck truck = generateTruck();
        // TODO
        planSingleTrip(transportOrder1);
        planSingleTrip(transportOrder2);
    }

    protected void planCombinedTripTwoTerminals(final TransportOrder transportOrder1, final TransportOrder transportOrder2)
    {
        // Truck truck = generateTruck();
        planSingleTrip(transportOrder1);
        planSingleTrip(transportOrder2);
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
