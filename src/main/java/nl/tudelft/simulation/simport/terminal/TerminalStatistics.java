package nl.tudelft.simulation.simport.terminal;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.vessel.Vessel;
import nl.tudelft.simulation.simport.vessel.VesselType;

/**
 * TerminalStatistics contains a day snapshot of terminal statistics.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TerminalStatistics implements EventListener
{
    /** The terminal or depot. */
    private final ContainerFacility facility;

    /** The simulator. */
    private final ClockDevsSimulatorInterface simulator;

    /** The periodic, e.g., daily, data. */
    private TerminalData periodic;

    /** The total over the runtime of the simulation. */
    private TerminalData total;

    /** The warmup time, null before warmup. */
    private ClockTime warmupTime = null;

    /**
     * @param simulator
     */
    public TerminalStatistics(final ContainerFacility facility, final ClockDevsSimulatorInterface simulator)
    {
        this.facility = facility;
        this.simulator = simulator;
        simulator.addListener(this, Replication.WARMUP_EVENT);
        resetTotalStatistics();
        resetPeriodicStatistics();
    }

    /** @return the container facility (terminal or depot) */
    public ContainerFacility getFacility()
    {
        return this.facility;
    }

    /** Reset the periodic statistics, e.g., at the end of a day. */
    public void resetPeriodicStatistics()
    {
        this.periodic = new TerminalData();
        this.periodic.nrContainers = this.total.nrContainers.copy();
        this.periodic.nrTeu = this.total.nrTeu.copy();
        this.periodic.setStartTime(this.simulator.getSimulatorClockTime());
    }

    /** Reset the total statistics, e.g., at warmup. */
    public void resetTotalStatistics()
    {
        this.total = new TerminalData();
        this.total.setStartTime(this.simulator.getSimulatorClockTime());
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(Replication.WARMUP_EVENT))
        {
            resetTotalStatistics();
            resetPeriodicStatistics();
            this.warmupTime = getSimulator().getSimulatorClockTime();
        }
    }

    /** @return warmupTime, null before warmup. */
    public ClockTime getWarmupTime()
    {
        return this.warmupTime;
    }

    /** Vessel arrival. */
    public void vesselArrival(final Vessel vessel)
    {
        this.total.incNrVesselArrivals(vessel.getVesselType());
        this.periodic.incNrVesselArrivals(vessel.getVesselType());
    }

    /** Vessel departure. */
    public void vesselDeparture(final Vessel vessel)
    {
        this.total.incNrVesselDepartures(vessel.getVesselType());
        this.periodic.incNrVesselDepartures(vessel.getVesselType());
    }

    /** Add container to yard. */
    public void addContainerYard(final Container container, final TransportMode mode)
    {
        this.total.nrContainers.total++;
        this.total.nrTeu.total += container.is40ft() ? 2 : 1;
        this.periodic.nrContainers.total++;
        this.periodic.nrTeu.total += container.is40ft() ? 2 : 1;

        this.total.nrContainersArr.total++;
        this.total.nrTeuArr.total += container.is40ft() ? 2 : 1;
        this.periodic.nrContainersArr.total++;
        this.periodic.nrTeuArr.total += container.is40ft() ? 2 : 1;

        if (container.isFull())
        {
            this.total.nrContainers.full++;
            this.total.nrTeu.full += container.is40ft() ? 2 : 1;
            this.periodic.nrContainers.full++;
            this.periodic.nrTeu.full += container.is40ft() ? 2 : 1;

            this.total.nrContainersArr.full++;
            this.total.nrTeuArr.full += container.is40ft() ? 2 : 1;
            this.periodic.nrContainersArr.full++;
            this.periodic.nrTeuArr.full += container.is40ft() ? 2 : 1;
        }
        if (container.isGeneral())
        {
            this.total.nrContainers.general++;
            this.total.nrTeu.general += container.is40ft() ? 2 : 1;
            this.periodic.nrContainers.general++;
            this.periodic.nrTeu.general += container.is40ft() ? 2 : 1;

            this.total.nrContainersArr.general++;
            this.total.nrTeuArr.general += container.is40ft() ? 2 : 1;
            this.periodic.nrContainersArr.general++;
            this.periodic.nrTeuArr.general += container.is40ft() ? 2 : 1;
        }
        if (container.is40ft())
        {
            this.total.nrContainers.ft40++;
            this.total.nrTeu.ft40 += 2;
            this.periodic.nrContainers.ft40++;
            this.periodic.nrTeu.ft40 += 2;

            this.total.nrContainersArr.ft40++;
            this.total.nrTeuArr.ft40 += 2;
            this.periodic.nrContainersArr.ft40++;
            this.periodic.nrTeuArr.ft40 += 2;
        }
        this.total.nrContainerArrivals[mode.ordinal()]++;
        this.total.nrTeuArrivals[mode.ordinal()] += container.is40ft() ? 2 : 1;
        this.periodic.nrContainerArrivals[mode.ordinal()]++;
        this.periodic.nrTeuArrivals[mode.ordinal()] += container.is40ft() ? 2 : 1;
    }

    /** Remove container from yard. */
    public void removeContainerYard(final Container container, final TransportMode mode)
    {
        this.total.nrContainers.total--;
        this.total.nrTeu.total -= container.is40ft() ? 2 : 1;
        this.periodic.nrContainers.total++;
        this.periodic.nrTeu.total -= container.is40ft() ? 2 : 1;

        this.total.nrContainersDep.total++;
        this.total.nrTeuDep.total += container.is40ft() ? 2 : 1;
        this.periodic.nrContainersDep.total++;
        this.periodic.nrTeuDep.total += container.is40ft() ? 2 : 1;

        if (container.isFull())
        {
            this.total.nrContainers.full--;
            this.total.nrTeu.full -= container.is40ft() ? 2 : 1;
            this.periodic.nrContainers.full--;
            this.periodic.nrTeu.full -= container.is40ft() ? 2 : 1;

            this.total.nrContainersDep.full++;
            this.total.nrTeuDep.full += container.is40ft() ? 2 : 1;
            this.periodic.nrContainersDep.full++;
            this.periodic.nrTeuDep.full += container.is40ft() ? 2 : 1;
        }
        if (container.isGeneral())
        {
            this.total.nrContainers.general--;
            this.total.nrTeu.general -= container.is40ft() ? 2 : 1;
            this.periodic.nrContainers.general--;
            this.periodic.nrTeu.general -= container.is40ft() ? 2 : 1;

            this.total.nrContainersDep.general++;
            this.total.nrTeuDep.general += container.is40ft() ? 2 : 1;
            this.periodic.nrContainersDep.general++;
            this.periodic.nrTeuDep.general += container.is40ft() ? 2 : 1;
        }
        if (container.is40ft())
        {
            this.total.nrContainers.ft40--;
            this.total.nrTeu.ft40 -= 2;
            this.periodic.nrContainers.ft40--;
            this.periodic.nrTeu.ft40 -= 2;

            this.total.nrContainersDep.ft40++;
            this.total.nrTeuDep.ft40 += 2;
            this.periodic.nrContainersDep.ft40++;
            this.periodic.nrTeuDep.ft40 += 2;
        }
        this.total.nrContainerDepartures[mode.ordinal()]++;
        this.total.nrTeuDepartures[mode.ordinal()] += container.is40ft() ? 2 : 1;
        this.periodic.nrContainerDepartures[mode.ordinal()]++;
        this.periodic.nrTeuDepartures[mode.ordinal()] += container.is40ft() ? 2 : 1;
    }

    public void incTruckVisitPickup()
    {
        this.total.nrTruckVisitsPickup++;
        this.periodic.nrTruckVisitsPickup++;
    }

    public void incTruckVisitDelivery()
    {
        this.total.nrTruckVisitsDelivery++;
        this.periodic.nrTruckVisitsDelivery++;
    }

    public void incTruckVisitDual()
    {
        this.total.nrTruckVisitsDual++;
        this.periodic.nrTruckVisitsDual++;
    }

    /** @return periodic statistics record (e.g., one day). */
    public TerminalData getPeriodic()
    {
        return this.periodic;
    }

    /** @return total statistics record (cumulative). */
    public TerminalData getTotal()
    {
        return this.total;
    }

    /** @return the simulator. */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** Terminal data collector. */
    public class TerminalData
    {
        // @formatter:off
        private ClockTime startTime;
          public ClockTime getStartTime() { return this.startTime; }
          public void setStartTime(final ClockTime startTime) { this.startTime = startTime; }

        private int nrVesselArrivals;
        private int nrDeepseaArrivals;
        private int nrFeederArrivals;
          public int getNrVesselArrivals() { return this.nrVesselArrivals; }
          public int getNrDeepseaArrivals() { return this.nrDeepseaArrivals; }
          public int getNrFeederArrivals() { return this.nrFeederArrivals; }
          public void incNrVesselArrivals(final VesselType vesselType)
          {
              this.nrVesselArrivals++;
              if (vesselType.isDeepSea() ) { this.nrDeepseaArrivals++; } else { this.nrFeederArrivals++; }
          }

        private int nrVesselDepartures;
        private int nrDeepseaDepartures;
        private int nrFeederDepartures;
          public int getNrVesselDepartures() { return this.nrVesselDepartures; }
          public int getNrDeepseaDepartures() { return this.nrDeepseaDepartures; }
          public int getNrFeederDepartures() { return this.nrFeederDepartures; }
          public void incNrVesselDepartures(final VesselType vesselType)
          {
              this.nrVesselDepartures++;
              if (vesselType.isDeepSea() ) { this.nrDeepseaDepartures++; } else { this.nrFeederDepartures++; }
          }

        private ContainerTypeRecord nrContainers = new ContainerTypeRecord();
          public int getNrContainersTotal()   { return this.nrContainers.total; }
          public int getNrContainersFull()    { return this.nrContainers.full; }
          public int getNrContainersEmpty()   { return this.nrContainers.total - this.nrContainers.full; }
          public int getNrContainersGeneral() { return this.nrContainers.general; }
          public int getNrContainersReefer()  { return this.nrContainers.total - this.nrContainers.general; }
          public int getNrContainers40Ft()    { return this.nrContainers.ft40; }
          public int getNrContainers20Ft()    { return this.nrContainers.total - this.nrContainers.ft40; }

        private ContainerTypeRecord nrTeu = new ContainerTypeRecord();
          public int getNrTeuTotal()   { return this.nrTeu.total; }
          public int getNrTeuFull()    { return this.nrTeu.full; }
          public int getNrTeuEmpty()   { return this.nrTeu.total - this.nrTeu.full; }
          public int getNrTeuGeneral() { return this.nrTeu.general; }
          public int getNrTeuReefer()  { return this.nrTeu.total - this.nrTeu.general; }
          public int getNrTeu40Ft()    { return this.nrTeu.ft40; }
          public int getNrTeu20Ft()    { return this.nrTeu.total - this.nrTeu.ft40; }

        private ContainerTypeRecord nrContainersArr = new ContainerTypeRecord();
          public int getNrContainersArrTotal()   { return this.nrContainersArr.total; }
          public int getNrContainersArrFull()    { return this.nrContainersArr.full; }
          public int getNrContainersArrEmpty()   { return this.nrContainersArr.total - this.nrContainersArr.full; }
          public int getNrContainersArrGeneral() { return this.nrContainersArr.general; }
          public int getNrContainersArrReefer()  { return this.nrContainersArr.total - this.nrContainersArr.general; }
          public int getNrContainersArr40Ft()    { return this.nrContainersArr.ft40; }
          public int getNrContainersArr20Ft()    { return this.nrContainersArr.total - this.nrContainersArr.ft40; }

        private ContainerTypeRecord nrTeuArr = new ContainerTypeRecord();
          public int getNrTeuArrTotal()   { return this.nrTeuArr.total; }
          public int getNrTeuArrFull()    { return this.nrTeuArr.full; }
          public int getNrTeuArrEmpty()   { return this.nrTeuArr.total - this.nrTeuArr.full; }
          public int getNrTeuArrGeneral() { return this.nrTeuArr.general; }
          public int getNrTeuArrReefer()  { return this.nrTeuArr.total - this.nrTeuArr.general; }
          public int getNrTeuArr40Ft()    { return this.nrTeuArr.ft40; }
          public int getNrTeuArr20Ft()    { return this.nrTeuArr.total - this.nrTeuArr.ft40; }

        private ContainerTypeRecord nrContainersDep = new ContainerTypeRecord();
          public int getNrContainersDepTotal()   { return this.nrContainersDep.total; }
          public int getNrContainersDepFull()    { return this.nrContainersDep.full; }
          public int getNrContainersDepEmpty()   { return this.nrContainersDep.total - this.nrContainersDep.full; }
          public int getNrContainersDepGeneral() { return this.nrContainersDep.general; }
          public int getNrContainersDepReefer()  { return this.nrContainersDep.total - this.nrContainersDep.general; }
          public int getNrContainersDep40Ft()    { return this.nrContainersDep.ft40; }
          public int getNrContainersDep20Ft()    { return this.nrContainersDep.total - this.nrContainersDep.ft40; }

        private ContainerTypeRecord nrTeuDep = new ContainerTypeRecord();
          public int getNrTeuDepTotal()   { return this.nrTeuDep.total; }
          public int getNrTeuDepFull()    { return this.nrTeuDep.full; }
          public int getNrTeuDepEmpty()   { return this.nrTeuDep.total - this.nrTeuDep.full; }
          public int getNrTeuDepGeneral() { return this.nrTeuDep.general; }
          public int getNrTeuDepReefer()  { return this.nrTeuDep.total - this.nrTeuDep.general; }
          public int getNrTeuDep40Ft()    { return this.nrTeuDep.ft40; }
          public int getNrTeuDep20Ft()    { return this.nrTeuDep.total - this.nrTeuDep.ft40; }

        private int nrTruckVisitsPickup;
          public int getNrTruckVisitsPickup() { return this.nrTruckVisitsPickup; }
          public void incNrTruckVisitsPickup() { this.nrTruckVisitsPickup++; }

        private int nrTruckVisitsDelivery;
          public int getNrTruckVisitsDelivery() { return this.nrTruckVisitsDelivery; }
          public void incNrTruckVisitsDelivery() { this.nrTruckVisitsDelivery++; }

        private int nrTruckVisitsDual;
          public int getNrTruckVisitsDual() { return this.nrTruckVisitsDual; }
          public void incNrTruckVisitsDual() { this.nrTruckVisitsDual++; }

        private int[] nrContainerArrivals = new int[5];
          public int getNrContainerArrivals(final TransportMode mode) { return this.nrContainerArrivals[mode.ordinal()]; }

        private int[] nrContainerDepartures = new int[5];
          public int getNrContainerDepartures(final TransportMode mode) { return this.nrContainerDepartures[mode.ordinal()]; }

        private int[] nrTeuArrivals = new int[5];
          public int getNrTeuArrivals(final TransportMode mode) { return this.nrTeuArrivals[mode.ordinal()]; }

        private int[] nrTeuDepartures = new int[5];
          public int getNrTeuDepartures(final TransportMode mode) { return this.nrTeuDepartures[mode.ordinal()]; }
        // @formatter:on
    }

    /** Information on the yard. Used in TEU and in number of containers. */
    protected class ContainerTypeRecord
    {
        // @formatter:off
        int total;
        int full;
        int general;
        int ft40;
        // @formatter:on

        public ContainerTypeRecord copy()
        {
            ContainerTypeRecord copy = new ContainerTypeRecord();
            copy.total = this.total;
            copy.full = this.full;
            copy.general = this.general;
            copy.ft40 = this.ft40;
            return copy;
        }
    }
}
