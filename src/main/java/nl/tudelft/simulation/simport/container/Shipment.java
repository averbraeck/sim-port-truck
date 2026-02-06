package nl.tudelft.simulation.simport.container;

import java.util.Objects;

import org.djutils.base.Identifiable;

/**
 * Superclass of container and booking.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class Shipment implements Identifiable
{
    /** Container number. */
    private final int nr;

    /**
     * The status of the shipment.
     * <ul>
     * <li>bit 0-1 (0x3): 00 = 20 ft, 01 = 40 ft, 10 = 45 ft</li>
     * <li>bit 2 (0x4): empty = 0, full = 1</li>
     * <li>bit 3 (0x8): normal = 0, reefer = 1</li>
     * </ul>
     */
    private final byte status;

    /**
     * Create a shipment for the model.
     * @param nr container number
     * @param size size in ft (20/40/45)
     * @param empty true if empty; false if full
     * @param reefer true if reefer; false if normal container
     */
    public Shipment(final int nr, final int size, final boolean empty, final boolean reefer)
    {
        this.nr = nr;
        int s = size == 40 ? 0x01 : size > 40 ? 0x02 : 0x00;
        if (!empty)
            s |= 0x04;
        if (reefer)
            s |= 0x08;
        this.status = (byte) s;
    }

    @Override
    public String getId()
    {
        return String.valueOf(getNr());
    }

    /** @return the container number or booking number */
    public int getNr()
    {
        return this.nr;
    }

    /** @return size in ft (20/40/45) */
    public int getSize()
    {
        int s = this.status & 0x03;
        return s == 0x01 ? 40 : s == 0x02 ? 45 : 20;
    }

    /** @return get the container type as a String, e.g., "40G1". */
    public String getType()
    {
        int size = getSize();
        if (isReefer())
            return size == 40 ? "42R1" : size + "R1";
        return size + "G1";
    }

    /** return true if 20ft, false if not */
    public boolean is20ft()
    {
        return getSize() == 20;
    }

    /** return true if 40ft, false if not */
    public boolean is40ft()
    {
        return getSize() == 40;
    }

    /** return the number of teu of this container */
    public double teu()
    {
        return getSize() / 20.0;
    }

    /** return the integer number of teu of this container, 1 for 20 ft, 2 for 40 ft or more */
    public int teuInt()
    {
        return getSize() <= 20 ? 1 : 2;
    }

    /** @return true if empty; false if full */
    public boolean isEmpty()
    {
        return (this.status & 0x04) == 0;
    }

    /** @return true if full; false if empty */
    public boolean isFull()
    {
        return !isEmpty();
    }

    /** @return true if reefer; false if normal container */
    public boolean isReefer()
    {
        return (this.status & 0x08) == 0;
    }

    /** @return true if normal container; false if reefer */
    public boolean isNormal()
    {
        return !isReefer();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.nr);
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
        Shipment other = (Shipment) obj;
        return this.nr == other.nr;
    }

}
