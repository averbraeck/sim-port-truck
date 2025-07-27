package nl.tudelft.simulation.simport.dsol;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.Executable;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DevsSimulatorInterface;
import nl.tudelft.simulation.simport.clocktime.ClockTime;

/**
 * TimeSimulatorInterface is an interface to use the TimeSimEvent with ClockTime + Duration rather than a pure duration-based
 * simulator.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface ClockSimulatorInterface extends DevsSimulatorInterface<Duration>
{
    /**
     * Return the start time of the simulator as a 'calendar-aware' ClockTime.
     * @return the start time of the simulator as a 'calendar-aware' ClockTime
     */
    ClockTime getStartClockTime();

    /**
     * Return the simulator time as a 'ClockTime' object rather than a 'Duration' object.
     * @return the simulator time as a 'ClockTime' object rather than a 'Duration' object
     */
    default ClockTime getSimulatorClockTime()
    {
        return new ClockTime(getStartClockTime().plus(getSimulatorTime()));
    }

    /**
     * schedules a lambda expression at an absolute time with normal priority.
     * @param clockTime the exact time to schedule the method on the simulator.
     * @param executable the lambda expression to execute
     * @return the simulation event so it can be cancelled later
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    default SimEventInterface<Duration> scheduleEventAbs(final ClockTime clockTime, final Executable executable)
            throws SimRuntimeException
    {
        return scheduleEventAbs(clockTime, SimEventInterface.NORMAL_PRIORITY, executable);
    }

    /**
     * schedules a lambda expression at an absolute time.
     * @param clockTime the exact time to schedule the method on the simulator.
     * @param priority the priority compared to other events scheduled at the same time.
     * @param executable the lambda expression to execute
     * @return the simulation event so it can be cancelled later
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    default SimEventInterface<Duration> scheduleEventAbs(final ClockTime clockTime, final short priority,
            final Executable executable) throws SimRuntimeException
    {
        if (clockTime.eq(getSimulatorClockTime()))
            return scheduleEventNow(priority, executable);
        return scheduleEventAbs(clockTime.minus(getStartClockTime()), priority, executable);
    }

    /**
     * schedules a methodCall at an absolute time with normal priority.
     * @param clockTime the exact time to schedule the method on the simulator.
     * @param target the target
     * @param method the method
     * @param args the arguments.
     * @return the simulation event so it can be cancelled later
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    default SimEventInterface<Duration> scheduleEventAbs(final ClockTime clockTime, final Object target, final String method,
            final Object[] args) throws SimRuntimeException
    {
        return scheduleEventAbs(clockTime, SimEventInterface.NORMAL_PRIORITY, target, method, args);
    }

    /**
     * schedules a methodCall at an absolute time.
     * @param clockTime the exact time to schedule the method on the simulator.
     * @param priority the priority compared to other events scheduled at the same time.
     * @param target the target
     * @param method the method
     * @param args the arguments.
     * @return the simulation event so it can be cancelled later
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    default SimEventInterface<Duration> scheduleEventAbs(final ClockTime clockTime, final short priority, final Object target,
            final String method, final Object[] args) throws SimRuntimeException
    {
        if (clockTime.eq(getSimulatorClockTime()))
            return scheduleEventNow(priority, target, method, args);
        return scheduleEventAbs(clockTime.minus(getStartClockTime()), priority, target, method, args);
    }

}
