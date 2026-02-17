package nl.tudelft.simulation.simport.vessel;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.logger.Cat;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.Location;
import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.container.Booking;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * The Vessel interface is the contract for a Vessel class.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Vessel extends LocalEventProducer implements Identifiable
{
    /** the id of the ship. */
    private final String id;

    /** the unique vessel nr. */
    private final int vesselNr;

    /** Vessel type. */
    private final VesselType vesselType;

    /** The estimated time of arrival. */
    private final ClockTime eta;

    /** The actual time of arrival. */
    private ClockTime ata;

    /** The estimated time of departure. */
    private final ClockTime etd;

    /** The actual time of departure. */
    private ClockTime atd;

    /** Terminal. */
    private final Terminal terminal;

    /** the simulation model. */
    private final PortModel model;

    /** The simulator to schedule vessel arrival and departure. */
    private final ClockDevsSimulatorInterface simulator;

    /** Event for ATA. */
    private SimEventInterface<Duration> ataEvent;

    /** Event for ATD. */
    private SimEventInterface<Duration> atdEvent;

    /** Booking list for containers to be loaded onto the vessel. */
    private List<Booking> loadList = new ArrayList<>();

    /** Booking list for containers to be unloaded from the vessel. */
    private List<Booking> unloadList = new ArrayList<>();

    /** List of containers physically present on the vessel. */
    private List<Container> containerList = new ArrayList<>();

    /** Number of loaded containers that have been marked as transshipped. */
    private int nrContainersTransshippedLoaded = 0;

    /** Number of unloaded containers that have been marked as transshipped. */
    private int nrContainersTransshippedUnloaded = 0;

    /**
     * Create a Vessel.
     * @param id the id of the ship
     * @param model the port model
     * @param vesselType the vessel type
     * @param eta estimated time of arrival
     * @param etd estimated time of departure
     * @param terminal the terminal to visit
     */
    public Vessel(final VesselType vesselType, final PortModel model, final ClockTime eta, final ClockTime etd,
            final Terminal terminal)
    {
        this.vesselNr = model.uniqueVesselNr();
        this.id = terminal.getId() + "." + (vesselType.equals(VesselType.DEEPSEA) ? "DS." : "FF.") + this.vesselNr;
        this.vesselType = vesselType;
        this.model = model;
        this.simulator = model.getSimulator();
        this.eta = eta;
        setAta(eta);
        this.etd = etd;
        setAtd(etd);
        this.terminal = terminal;
    }

    /**
     * Return the unique vessel number.
     * @return the unique vesselNr
     */
    public int getVesselNr()
    {
        return this.vesselNr;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Return the ETA for the vessel.
     * @return the ETA for the vessel
     */
    public ClockTime getEta()
    {
        return this.eta;
    }

    /**
     * Return the ETD for the vessel.
     * @return the ETD for the vessel
     */
    public ClockTime getEtd()
    {
        return this.etd;
    }

    /**
     * Return the ATA for the vessel.
     * @return the ATA for the vessel
     */
    public ClockTime getAta()
    {
        return this.ata;
    }

    /**
     * Set a new ATA and (re)schedule the vessel arrival event.
     * @param ata new ATA
     */
    public void setAta(final ClockTime ata)
    {
        Throw.whenNull(ata, "ata");
        Throw.when(this.ata != null && this.simulator.getSimulatorClockTime().gt(this.ata), IllegalStateException.class,
                "Cannot set ATA for a vessel when ATA has already passed");
        Throw.when(this.simulator.getSimulatorClockTime().gt(ata), IllegalArgumentException.class,
                "Cannot set ATA for a vessel in the past");
        this.ata = ata;
        if (this.ataEvent != null)
            this.simulator.cancelEvent(this.ataEvent);
        this.ataEvent = this.simulator.scheduleEventAbs(this.ata, () -> vesselArrival());
    }

    /**
     * Return the ATD for the vessel
     * @return the ATD for the vessel
     */
    public ClockTime getAtd()
    {
        return this.atd;
    }

    /**
     * Set a new ATD and (re)schedule the vessel departure event.
     * @param atd new ATD
     */
    public void setAtd(final ClockTime atd)
    {
        Throw.whenNull(atd, "atd");
        Throw.when(this.atd != null && this.simulator.getSimulatorClockTime().gt(this.atd), IllegalStateException.class,
                "Cannot set ATD for a vessel when ATD has already passed");
        Throw.when(this.simulator.getSimulatorClockTime().gt(atd), IllegalArgumentException.class,
                "Cannot set ATD for a vessel in the past");
        this.atd = atd;
        if (this.atdEvent != null)
            this.simulator.cancelEvent(this.atdEvent);
        this.atdEvent = this.simulator.scheduleEventAbs(this.atd, () -> vesselDeparture());
    }

    /**
     * Return the booking list for containers to be loaded onto the vessel
     * @return the booking list for containers to be loaded onto the vessel
     */
    public List<Booking> getLoadList()
    {
        return this.loadList;
    }

    /**
     * Set the booking list for containers to be loaded onto the vessel
     * @param loadList the booking list for containers to be loaded onto the vessel
     */
    public void setLoadList(final List<Booking> loadList)
    {
        this.loadList = loadList;
    }

    /**
     * Return the booking list for containers to be unloaded from the vessel
     * @return the booking list for containers to be unloaded from the vessel
     */
    public List<Booking> getUnloadList()
    {
        return this.unloadList;
    }

    /**
     * Set the booking list for containers to be unloaded from the vessel
     * @param unloadList the booking list for containers to be unloaded from the vessel
     */
    public void setUnloadList(final List<Booking> unloadList)
    {
        this.unloadList = unloadList;
    }

    /**
     * Return the list of containers physically present on the ship. This is NOT a safe copy, so containers can be added or
     * removed.
     * @return list of containers physically present on the ship
     */
    public List<Container> getContainerList()
    {
        return this.containerList;
    }

    /**
     * Set the list of containers physically present on the ship.
     * @param containerList set the list of containers physically present on the ship
     */
    public void setContainerList(final List<Container> containerList)
    {
        this.containerList = containerList;
    }

    /**
     * Vessel arrival. Unload containers first, then load containers. Schedule vessel departure after unloading and loading.
     */
    protected void vesselArrival()
    {
        CategoryLogger.with(Cat.DSOL).debug("Vessel {} arrived at terminal {}", this.id, this.terminal);
        getSimulator().scheduleEventNow(() -> unloadContainers());
        getSimulator().scheduleEventRel(this.etd.minus(this.ata).times(0.5), () -> loadContainers());
        getTerminal().getStatistics().vesselArrival(this);
    }

    /**
     * Unload all containers from the vessel.
     */
    protected void unloadContainers()
    {
        Duration unloadTime = this.etd.minus(this.ata).times(0.5);
        Duration deltaT = unloadTime.divide(Math.max(1.0, this.unloadList.size()));
        if (this.unloadList.size() > 0)
        {
            unloadContainer(0, deltaT);
        }
    }

    /**
     * Unload one container from the vessel, and place it in the yard.
     * @param index the index of the container in the booking unload list to be unloaded
     * @param deltaT the time till the next container is unloaded
     */
    protected void unloadContainer(final int index, final Duration deltaT)
    {
        Container container = this.unloadList.get(index).getContainer();
        if (container != null)
        {
            getTerminal().getYard().addContainer(container,
                    getVesselType().isDeepSea() ? TransportMode.DEEPSEA : TransportMode.FEEDER);
            if (!this.containerList.remove(container))
            {
                CategoryLogger.with(Cat.DSOL).trace("Container {} not on vessel {} for booking {}", container, this,
                        this.unloadList.get(index));
            }
        }
        else
        {
            CategoryLogger.with(Cat.DSOL).trace("Container not found for booking " + this.unloadList.get(index));
        }
        if (index < this.unloadList.size() - 1)
        {
            getSimulator().scheduleEventRel(deltaT, () -> unloadContainer(index + 1, deltaT));
        }
    }

    /**
     * Load all containers onto the vessel.
     */
    protected void loadContainers()
    {
        Duration loadTime = this.etd.minus(this.ata).times(0.5);
        Duration deltaT = loadTime.divide(this.loadList.size());
        if (this.loadList.size() > 0)
        {
            loadContainer(0, deltaT);
        }
    }

    /**
     * Load one container from the yard into the vessel.
     * @param index the index of the container in the booking load list to be loaded
     * @param deltaT the time till the next container is loaded
     */
    protected void loadContainer(final int index, final Duration deltaT)
    {
        Container container = this.loadList.get(index).getContainer();
        if (container != null)
        {
            if (!getTerminal().getYard().removeContainer(container,
                    getVesselType().isDeepSea() ? TransportMode.DEEPSEA : TransportMode.FEEDER))
            {
                CategoryLogger.with(Cat.DSOL).trace("Container {} not in the yard for booking {}", container,
                        this.loadList.get(index));
            }
            else
            {
                this.containerList.add(container);
                container.addLocation(getVesselType().isDeepSea() ? Location.DEEPSEA : Location.FEEDER);
            }
        }
        else
        {
            CategoryLogger.with(Cat.DSOL).trace("Container not allocated for booking " + this.loadList.get(index));
        }
        if (index < this.loadList.size() - 1)
        {
            getSimulator().scheduleEventRel(deltaT, () -> loadContainer(index + 1, deltaT));
        }
    }

    protected void vesselDeparture()
    {
        for (Container container : getContainerList())
        {
            container.setVesselOutNr(getVesselNr());
            getModel().fireEvent(PortModel.CONTAINER_EVENT, container);
        }
        getTerminal().getStatistics().vesselDeparture(this);
        CategoryLogger.with(Cat.DSOL).debug("Vessel {} departed from terminal {}", this.id, this.terminal);
        this.loadList = null;
        this.unloadList = null;
        this.containerList = null;
        this.ataEvent = null;
        this.atdEvent = null;
    }

    /**
     * Return the model.
     * @return the model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /**
     * Return the simulator.
     * @return the simulator
     */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Return the terminal where the vessel will (un)load.
     * @return the terminal where the vessel will (un)load
     */
    public Terminal getTerminal()
    {
        return this.terminal;
    }

    /**
     * Return the vessel type (deepsea or shortsea/feeder).
     * @return the vessel type
     */
    public VesselType getVesselType()
    {
        return this.vesselType;
    }

    /**
     * @return nrContainersTransshippedLoaded
     */
    public int getNrContainersTransshippedLoaded()
    {
        return this.nrContainersTransshippedLoaded;
    }

    /**
     * @return nrContainersTransshippedUnloaded
     */
    public int getNrContainersTransshippedUnloaded()
    {
        return this.nrContainersTransshippedUnloaded;
    }

    /**
     * Increment the number of loaded containers that have been transshipped.
     * @param increment the number of loaded containers that have been transshipped
     */
    public void incNrContainersTransshippedLoaded(final int increment)
    {
        this.nrContainersTransshippedLoaded += increment;
    }

    /**
     * Increment the number of unloaded containers that have been transshipped.
     * @param increment the number of unloaded containers that have been transshipped
     */
    public void incNrContainersTransshippedUnloaded(final int increment)
    {
        this.nrContainersTransshippedUnloaded += increment;
    }

}
