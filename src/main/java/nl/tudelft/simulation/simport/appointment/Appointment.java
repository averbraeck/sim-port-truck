package nl.tudelft.simulation.simport.appointment;

import java.util.Objects;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;

/**
 * Appointment for a terminal visit.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Appointment
{
    /** Target arrival time at terminal. */
    private final ClockTime targetTime;

    /**
     * Create an appointment at a terminal (no slots).
     * @param targetTime the target arrival time at terminal
     */
    public Appointment(final ClockTime targetTime)
    {
        this.targetTime = targetTime;
    }

    /**
     * @return targetTime
     */
    public ClockTime getTargetTime()
    {
        return this.targetTime;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.targetTime);
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
        Appointment other = (Appointment) obj;
        return Objects.equals(this.targetTime, other.targetTime);
    }

    @Override
    public String toString()
    {
        return "Appointment [targetTime=" + this.targetTime + "]";
    }

}
