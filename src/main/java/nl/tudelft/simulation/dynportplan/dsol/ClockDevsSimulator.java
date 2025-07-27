package nl.tudelft.simulation.dynportplan.dsol;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.DevsSimulator;
import nl.tudelft.simulation.dynportplan.clocktime.ClockTime;

/**
 * TimeDevsSimulator is an extension of the DevsSimulator that works with Duration and Time.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ClockDevsSimulator extends DevsSimulator<Duration> implements ClockSimulatorInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The absolute startTime of the simulator. */
    private final ClockTime startTime;

    /**
     * Instantiate a TimeDevsSimulator.
     * @param id the id of the simulator
     * @param startTime the startTime of the simulator
     */
    public ClockDevsSimulator(final Serializable id, final ClockTime startTime)
    {
        super(id);
        this.startTime = startTime;
    }

    @Override
    public ClockTime getStartClockTime()
    {
        return this.startTime;
    }

}
