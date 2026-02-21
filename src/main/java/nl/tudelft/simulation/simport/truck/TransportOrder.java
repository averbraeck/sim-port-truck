package nl.tudelft.simulation.simport.truck;

import java.util.concurrent.atomic.AtomicInteger;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.network.Centroid;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * TransportOrder contains an order to transport a container from A to B.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public record TransportOrder(int uniqueId, Vessel vessel, Container container, Centroid loadCentroid, Terminal loadTerminal,
        Centroid unloadCentroid, Terminal unloadTerminal, ClockTime targetTime, Duration marginBefore, Duration marginAfter)
{
    /** the static uniqueId counter. */
    private static AtomicInteger uniqueIdCounter = new AtomicInteger(0);

    // Convenience constructor
    public TransportOrder(final Vessel vessel, final Container container, final Centroid loadCentroid,
            final Terminal loadTerminal, final Centroid unloadCentroid, final Terminal unloadTerminal,
            final ClockTime targetTime, final Duration marginBefore, final Duration marginAfter)
    {
        this(uniqueIdCounter.incrementAndGet(), vessel, container, loadCentroid, loadTerminal, unloadCentroid, unloadTerminal,
                targetTime, marginBefore, marginAfter);
    }
}
