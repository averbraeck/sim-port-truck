package nl.tudelft.simulation.simport.truck.activity;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.appointment.Appointment;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.truck.activity.PlannedTerminalActivity.TerminalActivityType;

/**
 * TerminalActivity.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RealizedTerminalActivity extends RealizedTruckActivity
{
    /** The planned terminal activity. */
    private final PlannedTerminalActivity plannedTerminalActivity;

    /** Actual arrival time at the terminal. */
    private final ClockTime actualArrivalTime;

    /** Waiting time before terminal could be entered. */
    private Duration waitingTimeIn;

    /** Actual gate time in. */
    private Duration actualGateTimeIn;

    /** Actual gate time out. */
    private Duration actualGateTimeOut;

    /** Actual handling time. */
    private Duration actualHandlingTime;

    /**
     * Make a terminal activity for a truck.
     * @param truck the truck
     * @param terminal the terminal
     * @param appointment the appointment at the terminal (regular or slot)
     * @param activityType the activity type (pickup or dropoff)
     */
    public RealizedTerminalActivity(final PlannedTerminalActivity plannedTerminalActivity)
    {
        super(plannedTerminalActivity.getTruck());
        this.plannedTerminalActivity = plannedTerminalActivity;
        this.actualArrivalTime = getTerminal().getSimulator().getSimulatorClockTime();
    }

    /**
     * @return terminal
     */
    public Terminal getTerminal()
    {
        return this.plannedTerminalActivity.getTerminal();
    }

    /**
     * @return appointment
     */
    public Appointment getAppointment()
    {
        return this.plannedTerminalActivity.getAppointment();
    }

    /**
     * @return activityType
     */
    public TerminalActivityType getTerminalActivityType()
    {
        return this.plannedTerminalActivity.getTerminalActivityType();
    }

    /**
     * @return actualGateTimeIn
     */
    public Duration getActualGateTimeIn()
    {
        return this.actualGateTimeIn;
    }

    /**
     * @param actualGateTimeIn set actualGateTimeIn
     */
    public void setActualGateTimeIn(final Duration actualGateTimeIn)
    {
        this.actualGateTimeIn = actualGateTimeIn;
    }

    /**
     * @return actualGateTimeOut
     */
    public Duration getActualGateTimeOut()
    {
        return this.actualGateTimeOut;
    }

    /**
     * @param actualGateTimeOut set actualGateTimeOut
     */
    public void setActualGateTimeOut(final Duration actualGateTimeOut)
    {
        this.actualGateTimeOut = actualGateTimeOut;
    }

    /**
     * @return actualHandlingTime
     */
    public Duration getActualHandlingTime()
    {
        return this.actualHandlingTime;
    }

    /**
     * @param actualHandlingTime set actualHandlingTime
     */
    public void setActualHandlingTime(final Duration actualHandlingTime)
    {
        this.actualHandlingTime = actualHandlingTime;
    }

    /**
     * @return waitingTimeIn
     */
    public Duration getWaitingTimeIn()
    {
        return this.waitingTimeIn;
    }

    /**
     * @param waitingTimeIn set waitingTimeIn
     */
    public void setWaitingTimeIn(final Duration waitingTimeIn)
    {
        this.waitingTimeIn = waitingTimeIn;
    }

    /**
     * @return plannedTerminalActivity
     */
    public PlannedTerminalActivity getPlannedTerminalActivity()
    {
        return this.plannedTerminalActivity;
    }

    /**
     * @return containerPickup1
     */
    public Container getContainerPickup1()
    {
        return this.plannedTerminalActivity.getContainerPickup1();
    }

    /**
     * @return containerPickup2
     */
    public Container getContainerPickup2()
    {
        return this.plannedTerminalActivity.getContainerPickup2();
    }

    /**
     * @return containerDropoff1
     */
    public Container getContainerDropoff1()
    {
        return this.plannedTerminalActivity.getContainerDropoff1();
    }

    /**
     * @return containerDropoff2
     */
    public Container getContainerDropoff2()
    {
        return this.plannedTerminalActivity.getContainerDropoff2();
    }

    /**
     * @return actualArrivalTime
     */
    public ClockTime getActualArrivalTime()
    {
        return this.actualArrivalTime;
    }

}
