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
public class TransportOrder
{
    // @formatter:off
    private final int uniqueId;             public int uniqueId() { return this.uniqueId; }
    private final Vessel vessel;            public Vessel vessel() { return this.vessel; }
    private final Container container;      public Container container() { return this.container; }
    private final Centroid loadCentroid;    public Centroid loadCentroid() { return this.loadCentroid; }
    private final Terminal loadTerminal;    public Terminal loadTerminal() { return this.loadTerminal; }
    private final Centroid unloadCentroid;  public Centroid unloadCentroid() { return this.unloadCentroid; }
    private final Terminal unloadTerminal;  public Terminal unloadTerminal() { return this.unloadTerminal; }
    private ClockTime targetTime;           public ClockTime targetTime() { return this.targetTime; }
    private final Duration marginBefore;    public Duration marginBefore() { return this.marginBefore; }
    private final Duration marginAfter;     public Duration marginAfter() { return this.marginAfter; }
    // @formatter:on

    /** the static uniqueId counter. */
    private static AtomicInteger uniqueIdCounter = new AtomicInteger(0);

    public TransportOrder(final Vessel vessel, final Container container, final Centroid loadCentroid,
            final Terminal loadTerminal, final Centroid unloadCentroid, final Terminal unloadTerminal,
            final ClockTime targetTime, final Duration marginBefore, final Duration marginAfter)
    {
        this.uniqueId = uniqueIdCounter.incrementAndGet();
        this.vessel = vessel;
        this.container = container;
        this.loadCentroid = loadCentroid;
        this.loadTerminal = loadTerminal;
        this.unloadCentroid = unloadCentroid;
        this.unloadTerminal = unloadTerminal;
        this.targetTime = targetTime;
        this.marginBefore = marginBefore;
        this.marginAfter = marginAfter;
    }

    public void setTargetTime(final ClockTime targetTime)
    {
        this.targetTime = targetTime;
    }

}
