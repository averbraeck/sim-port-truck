package nl.tudelft.simulation.simport.terminal;

import java.util.Objects;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;

/**
 * Slot is a reservation in the terminal system.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Slot
{
    /** The terminal to which this slot belongs. */
    private final Terminal terminal;

    /** The regular slot start time. */
    private final ClockTime regularSlotStart;

    /** The regular slot duration relative to the start time. */
    private final Duration regularSlotDuration;

    /** The grace slot duration before the slot start time. */
    private final Duration graceDurationBeforeStart;

    /** The grace slot duration after the slot end time. */
    private final Duration graceDurationAfterEnd;

    /**
     * Create a slot for a terminal.
     * @param terminal terminal to which this slot belongs
     * @param regularSlotStart the regular slot start time
     * @param regularSlotDuration the regular slot duration relative to the start time
     * @param graceDurationBeforeStart the grace slot duration before the slot start time
     * @param graceDurationAfterEnd the grace slot duration after the slot end time
     */
    public Slot(final Terminal terminal, final ClockTime regularSlotStart, final Duration regularSlotDuration,
            final Duration graceDurationBeforeStart, final Duration graceDurationAfterEnd)
    {
        this.terminal = terminal;
        this.regularSlotStart = regularSlotStart;
        this.regularSlotDuration = regularSlotDuration;
        this.graceDurationBeforeStart = graceDurationBeforeStart;
        this.graceDurationAfterEnd = graceDurationAfterEnd;
    }

    /**
     * @return terminal
     */
    public Terminal getTerminal()
    {
        return this.terminal;
    }

    /**
     * @return regularSlotStart
     */
    public ClockTime getRegularSlotStart()
    {
        return this.regularSlotStart;
    }

    /**
     * @return regularSlotEnd
     */
    public ClockTime getRegularSlotEnd()
    {
        return new ClockTime(this.regularSlotStart.plus(this.regularSlotDuration));
    }

    /**
     * @return regularSlotDuration
     */
    public Duration getRegularSlotDuration()
    {
        return this.regularSlotDuration;
    }

    /**
     * @return graceDurationBeforeStart
     */
    public Duration getGraceDurationBeforeStart()
    {
        return this.graceDurationBeforeStart;
    }

    /**
     * @return graceDurationAfterEnd
     */
    public Duration getGraceDurationAfterEnd()
    {
        return this.graceDurationAfterEnd;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.graceDurationAfterEnd, this.graceDurationBeforeStart, this.regularSlotDuration,
                this.regularSlotStart, this.terminal);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Slot other = (Slot) obj;
        return Objects.equals(this.graceDurationAfterEnd, other.graceDurationAfterEnd)
                && Objects.equals(this.graceDurationBeforeStart, other.graceDurationBeforeStart)
                && Objects.equals(this.regularSlotDuration, other.regularSlotDuration)
                && Objects.equals(this.regularSlotStart, other.regularSlotStart)
                && Objects.equals(this.terminal, other.terminal);
    }

    @Override
    public String toString()
    {
        return "Slot [terminal=" + this.terminal + ", regularSlotStart=" + this.regularSlotStart + ", regularSlotDuration="
                + this.regularSlotDuration + ", graceDurationBeforeStart=" + this.graceDurationBeforeStart
                + ", graceDurationAfterEnd=" + this.graceDurationAfterEnd + "]";
    }

}
