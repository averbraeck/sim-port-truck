package nl.tudelft.simulation.simport.appointment;

import java.util.Objects;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.terminal.slot.Slot;

/**
 * SlotBooking is an appointment for a slot at a terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SlotBooking extends Appointment
{
    /** The slot that has been booked. */
    private final Slot slot;

    /**
     * Create a slot booking for a terminal. The regular interval for arrival at the terminal is: <br>
     * [targetTime - regularDurationBeforeTarget, targetTime + regularDurationAfterTarget]. <br>
     * The allowed interval (grace period) for arriving at the terminal is: <br>
     * [targetTime - graceDurationBeforeTarget, targetTime + graceDurationAfterTarget]. <br>
     * Note that the targetTime does not have to be in the middle of the interval. Very often, the target time is at the start
     * of the regular interval, since that is a safe time for arrival.
     * @param targetTime the target arrival time at terminal
     * @param slot the slot that has been booked
     */
    public SlotBooking(final ClockTime targetTime, final Slot slot)
    {
        super(targetTime);
        this.slot = slot;
    }

    /**
     * @return slot
     */
    public Slot getSlot()
    {
        return this.slot;
    }

    /**
     * @return regularDurationBeforeTarget
     */
    public Duration getRegularDurationBeforeTarget()
    {
        return getTargetTime().minus(this.slot.getRegularSlotStart());
    }

    /**
     * @return regularDurationAfterTarget
     */
    public Duration getRegularDurationAfterTarget()
    {
        return this.slot.getRegularSlotEnd().minus(getTargetTime());
    }

    /**
     * @return graceDurationBeforeTarget
     */
    public Duration getGraceDurationBeforeTarget()
    {
        return getRegularDurationBeforeTarget().plus(this.slot.getGraceDurationBeforeStart());
    }

    /**
     * @return graceDurationAfterTarget
     */
    public Duration getGraceDurationAfterTarget()
    {
        return getRegularDurationAfterTarget().plus(this.slot.getGraceDurationAfterEnd());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(this.slot);
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SlotBooking other = (SlotBooking) obj;
        return Objects.equals(this.slot, other.slot);
    }

    @Override
    public String toString()
    {
        return "SlotBooking [slot=" + this.slot + ", targetTime=" + this.getTargetTime() + "]";
    }

}
