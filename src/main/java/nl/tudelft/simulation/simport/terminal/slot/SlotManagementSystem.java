package nl.tudelft.simulation.simport.terminal.slot;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.simport.appointment.SlotBooking;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.truck.TransportOrder;

/**
 * Slot Management System for a Terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class SlotManagementSystem
{
    /** The Terminal. */
    protected final Terminal terminal;

    /** The slot duration. */
    protected final Duration slotDuration;

    /** The grace period before the slot start. */
    protected final Duration gracePeriodBefore;

    /** The grace period after the slot end. */
    protected final Duration gracePeriodAfter;

    /**
     * Instantiate a Slot Management System for a terminal.
     * @param terminal the terminal
     * @param slotDuration the slot duration
     * @param gracePeriodBefore grace period before the slot start
     * @param gracePeriodAfter grace period after the slot end
     */
    public SlotManagementSystem(final Terminal terminal, final Duration slotDuration, final Duration gracePeriodBefore,
            final Duration gracePeriodAfter)
    {
        this.terminal = terminal;
        this.slotDuration = slotDuration;
        this.gracePeriodBefore = gracePeriodBefore;
        this.gracePeriodAfter = gracePeriodAfter;
    }

    /**
     * Book a slot in the slot management system.
     * @param transportOrder the data on the transport
     */
    public abstract SlotBooking bookSlot(final TransportOrder transportOrder);

    /**
     * @return terminal
     */
    public Terminal getTerminal()
    {
        return this.terminal;
    }

    /**
     * @return slotDuration
     */
    public Duration getSlotDuration()
    {
        return this.slotDuration;
    }

    /**
     * @return gracePeriodBefore
     */
    public Duration getGracePeriodBefore()
    {
        return this.gracePeriodBefore;
    }

    /**
     * @return gracePeriodAfter
     */
    public Duration getGracePeriodAfter()
    {
        return this.gracePeriodAfter;
    }

}
