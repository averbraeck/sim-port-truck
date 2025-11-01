package nl.tudelft.simulation.simport.statistics;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.Event;
import org.djutils.event.EventListener;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.dsol.statistics.SimCounter;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * StatisticsCollector collects statistics on the state of the simulation that it obtains through pub/sub.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StatisticsCollector implements EventListener
{
    /** the model. */
    private final PortModel model;

    /** Counter for the number of containers in the model. */
    private Map<ClockTime, Integer> x;

    /** Counter for the number of vessels in the model. */
    private SimCounter<Duration> vesselCounter;

    /**
     * StatisticsCollector collects statistics on the state of the simulation that it obtains through pub/sub.
     * @param model the model for pub/sub
     */
    public StatisticsCollector(final PortModel model)
    {
        this.model = model;
    }

    /**
     * Return the model.
     * @return the model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /* ****************************************** NOTIFY ******************************************** */

    @Override
    public void notify(final Event event)
    {
    }
}
