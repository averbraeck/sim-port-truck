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

    /** load container list. */
    private List<Booking> loadList = new ArrayList<>();

    /** unloadload container list. */
    private List<Container> unloadList = new ArrayList<>();

    /**
     * Create a Vessel.
     * @param id the id of the ship
     * @param model the port model
     * @param vesselType the vessel type
     * @param eta estimated time of arrival
     * @param etd estimated time of departure
     * @param terminal the terminal to visit
     */
    public Vessel(final String id, final VesselType vesselType, final PortModel model, final ClockTime eta, final ClockTime etd,
            final Terminal terminal)
    {
        this.id = id;
        this.vesselType = vesselType;
        this.model = model;
        this.simulator = model.getSimulator();
        this.eta = eta;
        setAta(eta);
        this.etd = etd;
        setAtd(etd);
        this.terminal = terminal;
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
        this.atdEvent = this.simulator.scheduleEventAbs(this.atd, this, "vesselDeparture", null);
    }

    protected void vesselArrival()
    {
        CategoryLogger.with(Cat.DSOL).debug("Vessel {} arrived at terminal {}", this.id, this.terminal);
        // TODO: getSimulator().scheduleEventRel(this.etd.minus(this.ata).times(0.25), () -> unloadContainers());
        // TODO: getSimulator().scheduleEventRel(this.etd.minus(this.ata).times(0.75), () -> loadContainers());
    }

    protected void vesselDeparture()
    {
        CategoryLogger.with(Cat.DSOL).debug("Vessel {} departed from terminal {}", this.id, this.terminal);
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

}
