package nl.tudelft.simulation.simport;

/**
 * Container contains the information about a container.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Container
{
    /** Container number. */
    private final int nr;

    /** Size in ft (20/40/45). */
    private final byte size;

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
    public Container(final int nr, final byte size, final boolean empty, final boolean reefer)
    {
        this.nr = nr;
        this.size = size;
        this.empty = empty;
        this.reefer = reefer;
    }

    public String getType()
    {
        if (this.reefer)
            return this.size == 40 ? "42R1" : Byte.toString(this.size) + "R1";
        return Byte.toString(this.size) + "G1";
    }

    /**
     * Return container number.
     * @return container number
     */
    public int getNr()
    {
        return this.nr;
    }

    /**
     * Return size in ft (20/40/45).
     * @return size in ft (20/40/45)
     */
    public byte getSize()
    {
        return this.size;
    }

    /**
     * Return true if empty; false if full.
     * @return true if empty; false if full
     */
    public boolean isEmpty()
    {
        return this.empty;
    }

    /**
     * Return true if reefer; false if normal container.
     * @return true if reefer; false if normal container
     */
    public boolean isReefer()
    {
        return this.reefer;
    }

}
