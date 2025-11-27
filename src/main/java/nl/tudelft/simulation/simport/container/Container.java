package nl.tudelft.simulation.simport.container;

/**
 * Compact implementation of a container (all info in 1 byte).
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Container implements Shipment
{
    /** Container number. */
    private final int nr;

    /**
     * The status of the container.
     * <ul>
     * <li>bit 6+7: 00 = 20 ft, 01 = 40 ft, 10 = 45 ft</li>
     * <li>bit 5: empty = 0, full = 1</li>
     * <li>bit 4: normal = 0, reefer = 1</li>
     * </ul>
     */
    private final byte status;

    /**
     * Create a container for the model.
     * @param nr container number
     * @param size size in ft (20/40/45)
     * @param empty true if empty; false if full
     * @param reefer true if reefer; false if normal container
     */
    public Container(final int nr, final int size, final boolean empty, final boolean reefer)
    {
        this.nr = nr;
        int s = 0;
        s = size == 40 ? 1 : size > 40 ? 2 : 0;
        if (!empty)
            s |= 0x4;
        if (reefer)
            s |= 8;
        this.status = (byte) s;
    }

    @Override
    public int getNr()
    {
        return this.nr;
    }

    @Override
    public int getSize()
    {
        int s = this.status & 0x3;
        return s == 1 ? 40 : s == 2 ? 45 : 20;
    }

    @Override
    public boolean isEmpty()
    {
        return (this.status & 0x4) == 0;
    }

    @Override
    public boolean isReefer()
    {
        return (this.status & 0x8) == 0;
    }

    @Override
    public String toString()
    {
        return "Container [nr=" + this.nr + ", size=" + getSize() + "]";
    }

}
