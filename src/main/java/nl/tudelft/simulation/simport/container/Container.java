package nl.tudelft.simulation.simport.container;

import nl.tudelft.simulation.simport.TransportMode;

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
     * <li>bit 0-1 (0x3): 00 = 20 ft, 01 = 40 ft, 10 = 45 ft</li>
     * <li>bit 2 (0x4): empty = 0, full = 1</li>
     * <li>bit 3 (0x8): normal = 0, reefer = 1</li>
     * </ul>
     */
    private final byte status;

    /**
     * The transport modes of the container.
     * <ul>
     * <li>bit 0-2 (0x7): Terminal IN via 000 = DS, 001 = Feeder, 010 = Truck, 011 = Barge, 100 = Rail</li>
     * <li>bit 3-5 (0x37): Terminal OUT via 000 = DS, 001 = Feeder, 010 = Truck, 011 = Barge, 100 = Rail</li>
     * <li>bit 6-7 (0xC0): 00 = no transshipment, 01 = internal transshipment, 10 = external transshipment, 11 = depot MV2</li>
     * </ul>
     */
    private byte modes;

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
        int s = size == 40 ? 0x01 : size > 40 ? 0x02 : 0x00;
        if (!empty)
            s |= 0x04;
        if (reefer)
            s |= 0x08;
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
        int s = this.status & 0x03;
        return s == 0x01 ? 40 : s == 0x02 ? 45 : 20;
    }

    @Override
    public boolean isEmpty()
    {
        return (this.status & 0x04) == 0;
    }

    @Override
    public boolean isReefer()
    {
        return (this.status & 0x08) == 0;
    }

    /**
     * Set the transport mode INTO the deepsea terminal.
     * @param transportMode the transport mode INTO the deepsea terminal
     */
    public void setTransportModeIntoDS(final TransportMode transportMode)
    {
        this.modes |= transportMode.asByte();
    }

    /**
     * Get the transport mode of the container INTO the deepsea terminal.
     * @return the transport mode INTO the deepsea terminal
     */
    public TransportMode getTransportModeIntoDS()
    {
        return TransportMode.of(this.modes & 0x07);
    }

    /**
     * Set the transport mode OUT OF the deepsea terminal.
     * @param transportMode the transport mode OUT OF the deepsea terminal
     */
    public void setTransportModeFromDS(final TransportMode transportMode)
    {
        this.modes |= (transportMode.ordinal() << 3);
    }

    /**
     * Get the transport mode of the container OUT OF the deepsea terminal.
     * @return the transport mode OUT OF the deepsea terminal
     */
    public TransportMode getTransportModeFromDS()
    {
        return TransportMode.of((this.modes >> 3) & 0x07);
    }

    /**
     * Set the transshipment status to None.
     */
    public void setNoTransshipment()
    {
        this.modes &= 0x37;
    }

    /**
     * Set the internal transshipment status: Deepsea -> Feeder or Feeder -> Deepsea on the same terminal.
     */
    public void setInternalTransshipment()
    {
        setNoTransshipment();
        this.modes |= 0x40;
    }

    /**
     * Return whether the container is an internal transshipment: Deepsea -> Feeder or Feeder -> Deepsea on the same terminal.
     * @return whether the container has an internal transshipment status
     */
    public boolean isInternalTransshipment()
    {
        return (this.modes & 0x40) == 0x40;
    }

    /**
     * Set the external transshipment status: Deepsea -> Feeder or Feeder -> Deepsea on different terminals.
     */
    public void setExternalTransshipment()
    {
        setNoTransshipment();
        this.modes |= 0x80;
    }

    /**
     * Return whether the container has the external transshipment status: Deepsea -> Feeder or Feeder -> Deepsea.
     * @return whether the container has the external transshipment status
     */
    public boolean isExternalTransshipment()
    {
        return (this.modes & 0x80) == 0x80;
    }

    /**
     * Set the "depot transshipment" status: Deepsea -> MV2-Depot or MV2-Depot -> Deepsea.
     */
    public void setDepotTransshipment()
    {
        this.modes |= 0xC0;
    }

    /**
     * Return whether the container has the "depot transshipment" status: Deepsea -> MV2-Depot or MV2-Depot -> Deepsea.
     * @return whether the container has the "depot transshipment" status
     */
    public boolean isDepotTransshipment()
    {
        return (this.modes & 0xC0) == 0xC0;
    }

    @Override
    public String toString()
    {
        return "Container [nr=" + this.nr + ", size=" + getSize() + "]";
    }

}
