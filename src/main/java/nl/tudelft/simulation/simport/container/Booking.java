package nl.tudelft.simulation.simport.container;

import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * Compact implementation of a booking of a (future) container on a vessel.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Booking extends Shipment
{
    /** Vessel for which this is a booking. */
    private final Vessel vessel;

    /** Container for this booking, can be null when it still has to be allocated. */
    private Container container = null;

    /** Whether the container id unloaded from the ship (false) or is loaded onto the ship (true). */
    private final boolean loading;

    /**
     * Create a booking for the model, and create an associated container.
     * @param vessel the vessel for which this is a booking
     * @param loading whether the container id unloaded from the ship (false) or is loaded onto the ship (true)
     * @param nr booking number
     * @param size size in ft (20/40/45)
     * @param empty true if empty; false if full
     * @param reefer true if reefer; false if normal container
     */
    public Booking(final Vessel vessel, final boolean loading, final int nr, final int size, final boolean empty,
            final boolean reefer)
    {
        super(nr, size, empty, reefer);
        this.vessel = vessel;
        this.loading = loading;
    }

    /**
     * @return the vessel for which this is a booking
     */
    public Vessel getVessel()
    {
        return this.vessel;
    }

    /**
     * @param container set the container for the booking
     */
    public void setContainer(final Container container)
    {
        Throw.whenNull(container, "container cannot be null for booking %s", this);
        Throw.when(this.container != null, IllegalStateException.class, "%s already has %s, cannot add %s", this,
                this.container, container);
        this.container = container;
    }

    /**
     * @return the container for this booking, can be null when still to be allocated.
     */
    public Container getContainer()
    {
        return this.container;
    }

    /**
     * Return whether this booking has a container allocated.
     * @return whether this booking has a container allocated
     */
    public boolean hasContainer()
    {
        return this.container != null;
    }

    /**
     * @return whether the container id unloaded from the ship (false) or is loaded onto the ship (true)
     */
    public boolean isLoading()
    {
        return this.loading;
    }

    @Override
    public String toString()
    {
        return "Booking [nr=" + getNr() + ", size=" + getSize() + "]";
    }

}
