package nl.tudelft.simulation.simport.truck.activity;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.truck.Truck;

/**
 * A truck visit is a part of a truck trip, visiting a terminal or depot (or shipper).
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Visit
{
    // @formatter:off
    private final Truck truck;
    public Terminal terminal;
    public VisitType visitType;
    public Container dropoffContainer1;
    public Container dropoffContainer2;
    public Container pickupContainer1;
    public Container pickupContainer2;
    public String slotNr;
    public ClockTime earliestGrace;
    public ClockTime erliestStandard;
    public ClockTime latestStandard;
    public ClockTime latestGrace;
    public ClockTime ata;
    public Duration waitTimeGateIn;
    public Duration handlingTime;
    public Duration waitTimeGateOut;
    // @formatter:on

    /**
     *
     */
    public Visit(final Truck truck)
    {
        this.truck = truck;
    }

    /**
     * @return truck
     */
    public Truck getTruck()
    {
        return this.truck;
    }

    /** The visit type. */
    public enum VisitType
    {
        PICKUP,
        DROPOFF,
        DUAL;
    }
}
