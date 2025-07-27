package nl.tudelft.simulation.simport.vessel;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.simport.clocktime.ClockTime;
import nl.tudelft.simulation.simport.dsol.ClockSimulatorInterface;
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
    /** the static id counter. */
    private static int idCounter = 0;

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
    private final VesselLoadInfo vesselLoadInfoUnloading;

    /** The call size information for loading at the terminal. */
    private final VesselLoadInfo vesselLoadInfoLoading;

    /** Terminal. */
    private final Terminal terminal;

    /** The simulator to schedule vessel arrival and departure. */
    private final ClockSimulatorInterface simulator;

    /** Event for ATA. */
    private SimEventInterface<Duration> ataEvent;

    /** Event for ATD. */
    private SimEventInterface<Duration> atdEvent;

    /**
     * Create a Vessel.
     * @param id the id of the ship
     * @param simulator the simulator to schedule vessel arrival and departure
     * @param eta estimated time of arrival
     * @param etd estimated time of departure
     * @param vesselLoadInfoUnloading call size information for unloading at the terminal
     * @param vesselLoadInfoLoading call size information for loading at the terminal
     * @param terminal the terminal to visit
     */
    public Vessel(final String id, final ClockSimulatorInterface simulator, final ClockTime eta, final ClockTime etd,
            final VesselLoadInfo vesselLoadInfoUnloading, final VesselLoadInfo vesselLoadInfoLoading, final Terminal terminal)
    {
        this.id = "Vessel:" + (++idCounter);
        this.simulator = simulator;
        this.eta = eta;
        setAta(eta);
        this.etd = etd;
        setAtd(etd);
        this.vesselLoadInfoUnloading = vesselLoadInfoUnloading;
        this.vesselLoadInfoLoading = vesselLoadInfoLoading;
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
        System.out.println("Vessel " + this.id + " arrived at    terminal " + this.terminal + " at t="
                + this.simulator.getSimulatorClockTime().dayOfWeek().toString().substring(0, 3) + " "
                + this.simulator.getSimulatorClockTime());
    }

    protected void vesselDeparture()
    {
        System.out.println("Vessel " + this.id + " departed from terminal " + this.terminal + " at t="
                + this.simulator.getSimulatorClockTime().dayOfWeek().toString().substring(0, 3) + " "
                + this.simulator.getSimulatorClockTime());
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
        return this.vesselLoadInfoUnloading;
    }

    /**
     * Return call size information for loading at the terminal.
     * @return call size information for loading at the terminal
     */
    public VesselLoadInfo getVesselLoadInfoLoading()
    {
        return this.vesselLoadInfoLoading;
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
