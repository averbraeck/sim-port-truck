package nl.tudelft.simulation.simport.container;

import nl.tudelft.simulation.simport.TransportMode;

/**
 * Container contains the information about a container.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ContainerStandard implements Container
{
    /** Container number. */
    private final int nr;

    /** Size in ft (20/40/45). */
    private final byte size;

    /** Transport Mode. 0 = none, 1 = truck, 2 = barge, 3 = rail, 4 - short sea / feeder. */
    private byte mode = 0;

    /** Empty or full. */
    private final boolean empty;

    /** Reefer or not. */
    private final boolean reefer;

    /**
     * Create a container for the model.
     * @param nr container number
     * @param size size in ft (20/40/45)
     * @param empty true if empty; false if full
     * @param reefer true if reefer; false if normal container
     */
    public ContainerStandard(final int nr, final int size, final boolean empty, final boolean reefer)
    {
        this.nr = nr;
        this.size = (byte) size;
        this.empty = empty;
        this.reefer = reefer;
    }

    @Override
    public int getNr()
    {
        return this.nr;
    }

    @Override
    public int getSize()
    {
        return this.size;
    }

    @Override
    public boolean isEmpty()
    {
        return this.empty;
    }

    @Override
    public boolean isReefer()
    {
        return this.reefer;
    }

    /**
     * Return transport mode for this container.
     * @return the transport mode for this container
     */
    public TransportMode getMode()
    {
        return TransportMode.fromByte(this.mode);
    }

    /**
     * Set the transport mode for this container.
     * @param mode the new transport mode
     */
    public void setMode(final TransportMode transportMode)
    {
        this.mode = transportMode.asByte();
    }

    @Override
    public String toString()
    {
        return "Container [nr=" + this.nr + ", size=" + this.size + "]";
    }

}
