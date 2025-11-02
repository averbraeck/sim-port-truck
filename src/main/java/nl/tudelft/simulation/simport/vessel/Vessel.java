package nl.tudelft.simulation.simport.vessel;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.logger.Cat;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * Vessel models a container ship with an unloading plan and loading plan.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Vessel implements Identifiable
{
    /** the id of the ship. */
    private final String id;

    /** The estimated time of arrival. */
    private final ClockTime eta;

    /** The actual time of arrival. */
    private ClockTime ata;

    /** The estimated time of departure. */
    private final ClockTime etd;

    /** The actual time of departure. */
    private ClockTime atd;

    /** The call size information for unloading at the terminal. */
    private final VesselLoadInfo vesselUnloadInfo;

    /** The call size information for loading at the terminal. */
    private final VesselLoadInfo vesselLoadInfo;

    /** load container list. */
    private List<Container> loadList = new ArrayList<>();

    /** unloadload container list. */
    private List<Container> unloadList = new ArrayList<>();

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

    /**
     * Create a Vessel.
     * @param id the id of the ship
     * @param model the port model
     * @param eta estimated time of arrival
     * @param etd estimated time of departure
     * @param vesselUnloadInfo call size information for unloading at the terminal
     * @param vesselLoadInfo call size information for loading at the terminal
     * @param terminal the terminal to visit
     */
    public Vessel(final String id, final PortModel model, final ClockTime eta, final ClockTime etd,
            final VesselLoadInfo vesselUnloadInfo, final VesselLoadInfo vesselLoadInfo, final Terminal terminal)
    {
        this.id = "Vessel:" + model.uniqueVesselNr();
        this.model = model;
        this.simulator = model.getSimulator();
        this.eta = eta;
        setAta(eta);
        this.etd = etd;
        setAtd(etd);
        this.vesselLoadInfo = vesselLoadInfo;
        this.vesselUnloadInfo = vesselUnloadInfo;
        makeList(this.vesselLoadInfo, this.loadList);
        makeList(this.vesselUnloadInfo, this.unloadList);
        this.terminal = terminal;
    }

    @Override
    public String getId()
    {
        return this.id;
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
        CategoryLogger.with(Cat.DSOL).debug("Vessel {} arrived at    terminal {}", this.id, this.terminal);
        getSimulator().scheduleEventRel(this.etd.minus(this.ata).times(0.25), () -> unloadContainers());
        getSimulator().scheduleEventRel(this.etd.minus(this.ata).times(0.75), () -> loadContainers());
    }

    protected void vesselDeparture()
    {
        CategoryLogger.with(Cat.DSOL).debug("Vessel {} departed from terminal {}", this.id, this.terminal);
    }

    protected void makeList(final VesselLoadInfo vli, final List<Container> ll)
    {
        // #cont = #teu / (2.0 - frac20), because c.f + 2.c.(1-f) = t => c = t / (2 - f)
        int nrContainers = (int) (vli.callSizeTEU() / (2.0 - vli.fraction20ft()));
        StreamInterface rng = getModel().getDefaultStream();
        for (int i = 0; i < nrContainers; i++)
        {
            byte size = rng.nextDouble() < vli.fraction20ft() ? (byte) 20 : (byte) 40;
            boolean empty = rng.nextDouble() < vli.fractionEmpty();
            boolean reefer = rng.nextDouble() < vli.fractionReefer();
            ll.add(new Container(this.model.uniqueContainerNr(), size, empty, reefer));
        }
    }

    protected void unloadContainers()
    {
        this.terminal.addImportContainers(this.unloadList);
    }

    protected void loadContainers()
    {
        this.terminal.removeExportContainers(this.loadList);
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
     * Return call size information for unloading at the terminal.
     * @return call size information for unloading at the terminal
     */
    public VesselLoadInfo getVesselLoadInfoUnloading()
    {
        return this.vesselUnloadInfo;
    }

    /**
     * Return call size information for loading at the terminal.
     * @return call size information for loading at the terminal
     */
    public VesselLoadInfo getVesselLoadInfoLoading()
    {
        return this.vesselLoadInfo;
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

}
