package nl.tudelft.simulation.simport.truck;

/**
 * TruckState indicates what a truck is doing.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum TruckState
{
    /** driving. */
    DRIVING,

    /** waiting at a parking. */
    WAITING_PARKING,

    /** waiting at deepsea terminal to enter. */
    WAITING_DS_ENTER,

    /** loading at deepsea terminal. */
    LOADING_DS,

    /** unloading at deepsea terminal. */
    UNLOADING_DS,

    /** waiting at deepsea terminal to leave. */
    WAITING_DS_LEAVE,

    /** waiting at depot to enter. */
    WAITING_DEPOT_ENTER,

    /** loading at depot. */
    LOADING_DEPOT,

    /** unloading at depot. */
    UNLOADING_DEPOT,

    /** waiting at depot to leave. */
    WAITING_DEPOT_LEAVE;

    /**
     * Return whether the truck is driving.
     * @return whether the truck is driving
     */
    public boolean isDriving()
    {
        return this.equals(DRIVING);
    }

    /**
     * Return whether the truck is waiting.
     * @return whether the truck is waiting
     */
    public boolean isWaiting()
    {
        return this.equals(WAITING_PARKING) || this.equals(WAITING_DS_ENTER) || this.equals(WAITING_DS_LEAVE)
                || this.equals(WAITING_DEPOT_ENTER) || this.equals(WAITING_DEPOT_LEAVE);
    }

    /**
     * Return whether the truck is handling.
     * @return whether the truck is handling
     */
    public boolean isHandling()
    {
        return this.equals(LOADING_DS) || this.equals(UNLOADING_DS) || this.equals(LOADING_DEPOT)
                || this.equals(UNLOADING_DEPOT);
    }

    /**
     * Return whether the truck is loading.
     * @return whether the truck is loading
     */
    public boolean isLoading()
    {
        return this.equals(LOADING_DS) || this.equals(LOADING_DEPOT);
    }

    /**
     * Return whether the truck is unloading.
     * @return whether the truck is unloading
     */
    public boolean isUnloading()
    {
        return this.equals(UNLOADING_DS) || this.equals(UNLOADING_DEPOT);
    }

    /**
     * Return whether the truck is at a terminal.
     * @return whether the truck is at a terminal
     */
    public boolean isAtTerminal()
    {
        return this.equals(LOADING_DS) || this.equals(UNLOADING_DS) || this.equals(WAITING_DS_ENTER)
                || this.equals(WAITING_DS_LEAVE);
    }

    /**
     * Return whether the truck is at a depot.
     * @return whether the truck is at a depot
     */
    public boolean isAtDepot()
    {
        return this.equals(LOADING_DEPOT) || this.equals(UNLOADING_DEPOT) || this.equals(WAITING_DEPOT_ENTER)
                || this.equals(WAITING_DEPOT_LEAVE);
    }

}
