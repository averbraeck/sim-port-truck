package nl.tudelft.simulation.simport.container;

import java.util.Objects;

import nl.tudelft.simulation.simport.TransportMode;

/**
 * Implementation of a physical container.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Container extends Shipment
{
    /**
     * The transport modes of the container.
     * <ul>
     * <li>bit 0-2 (0x07): Terminal IN via 000 = DS, 001 = Feeder, 010 = Truck, 011 = Barge, 100 = Rail</li>
     * <li>bit 3-5 (0x38): Terminal OUT via 000 = DS, 001 = Feeder, 010 = Truck, 011 = Barge, 100 = Rail</li>
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
        super(nr, size, empty, reefer);
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
        this.modes &= 0xC3;
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
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(this.modes);
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Container other = (Container) obj;
        return this.modes == other.modes;
    }

    @Override
    public String toString()
    {
        return "Container [nr=" + getNr() + ", size=" + getSize() + "]";
    }

}
