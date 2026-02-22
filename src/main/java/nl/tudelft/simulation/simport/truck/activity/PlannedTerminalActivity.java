package nl.tudelft.simulation.simport.truck.activity;

import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.simport.appointment.Appointment;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.truck.Truck;

/**
 * TerminalActivity.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PlannedTerminalActivity extends PlannedTruckActivity
{
    /** The terminal. */
    final Terminal terminal;

    /** The activity type. */
    final TerminalActivityType terminalActivityType;

    /** The appointment. */
    final Appointment appointment;

    /** The first container to be picked up. */
    final Container containerPickup1;

    /** The second container to be picked up. */
    final Container containerPickup2;

    /** The first container to be dropped off. */
    final Container containerDropoff1;

    /** The second container to be dropped off. */
    final Container containerDropoff2;

    /**
     * Make a terminal activity for a truck.
     * @param truck the truck
     * @param terminal the terminal
     * @param appointment the appointment at the terminal (regular or slot)
     * @param terminalActivityType the activity type (pickup or dropoff)
     * @param containerPickup1 the first container to be picked up
     * @param containerPickup2 the second container to be picked up
     * @param containerDropoff1 the first container to be dropped off
     * @param containerDropoff2 the second container to be dropped off
     */
    public PlannedTerminalActivity(final Truck truck, final Terminal terminal, final Appointment appointment,
            final TerminalActivityType terminalActivityType, final Container containerPickup1, final Container containerPickup2,
            final Container containerDropoff1, final Container containerDropoff2)
    {
        super(truck);
        Throw.when(
                terminalActivityType.isDropoff()
                        && (containerDropoff1 == null || containerPickup1 != null || containerPickup2 != null),
                IllegalStateException.class, "Dropoff activity %s has no dropoff container or has a pickup container", this);
        Throw.when(
                terminalActivityType.isPickup()
                        && (containerPickup1 == null || containerDropoff1 != null || containerDropoff2 != null),
                IllegalStateException.class, "Pickup activity %s has no pickup container or has a dropoff container", this);
        Throw.when(terminalActivityType.isDual() && (containerPickup1 == null || containerDropoff1 == null),
                IllegalStateException.class, "Dual activity %s has no pickup container or dropoff container", this);
        this.terminal = terminal;
        this.appointment = appointment;
        this.terminalActivityType = terminalActivityType;
        this.containerPickup1 = containerPickup1;
        this.containerPickup2 = containerPickup2;
        this.containerDropoff1 = containerDropoff1;
        this.containerDropoff2 = containerDropoff2;
    }

    /**
     * @return terminal
     */
    public Terminal getTerminal()
    {
        return this.terminal;
    }

    /**
     * @return appointment
     */
    public Appointment getAppointment()
    {
        return this.appointment;
    }

    /**
     * @return activityType
     */
    public TerminalActivityType getTerminalActivityType()
    {
        return this.terminalActivityType;
    }

    /**
     * @return containerPickup1
     */
    public Container getContainerPickup1()
    {
        return this.containerPickup1;
    }

    /**
     * @return containerPickup2
     */
    public Container getContainerPickup2()
    {
        return this.containerPickup2;
    }

    /**
     * @return containerDropoff1
     */
    public Container getContainerDropoff1()
    {
        return this.containerDropoff1;
    }

    /**
     * @return containerDropoff2
     */
    public Container getContainerDropoff2()
    {
        return this.containerDropoff2;
    }

    /** The activity type. */
    public enum TerminalActivityType
    {
        PICKUP,
        DROPOFF,
        DUAL;

        public boolean isPickup()
        {
            return this.equals(PICKUP);
        }

        public boolean isDropoff()
        {
            return this.equals(DROPOFF);
        }

        public boolean isDual()
        {
            return this.equals(DUAL);
        }
    }
}
