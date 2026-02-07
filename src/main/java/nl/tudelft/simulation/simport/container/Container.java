package nl.tudelft.simulation.simport.container;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.simport.Location;

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
     * The transport modes of the container for statistics.
     * <ul>
     * <li>0000 = Unused</li>
     * <li>0001 = Deepsea</li>
     * <li>0010 = Feeder</li>
     * <li>0011 = Rail</li>
     * <li>0100 = Barge</li>
     * <li>0101 = Truck</li>
     * <li>0110 = Terminal</li>
     * <li>0111 = Depot</li>
     * <li>1000 = Hinterland</li>
     * <li>1001 = Port</li>
     * </ul>
     * Example: Deepsea - Terminal - Truck - Hinterland.
     */
    private byte[] locations = new byte[6];

    /** index of the current location. */
    private byte currentIndex = 0;

    /**
     * Create a container for the model.
     * @param nr container number
     * @param size size in ft (20/40/45)
     * @param empty true if empty; false if full
     * @param reefer true if reefer; false if normal container
     * @param location the current location of the container
     */
    public Container(final int nr, final int size, final boolean empty, final boolean reefer, final Location location)
    {
        super(nr, size, empty, reefer);
        this.locations[0] = location.asByte();
    }

    /**
     * Add a new location of the container.
     * @param location the new location
     */
    public void addLocation(final Location location)
    {
        this.locations[++this.currentIndex] = location.asByte();
    }

    /**
     * Get the current location.
     * @return the current location
     */
    public Location getLocation()
    {
        return Location.of(this.locations[this.currentIndex]);
    }

    /**
     * Get a list of locations for the container.
     * @return a list of locations for the container
     */
    public List<Location> getLocations()
    {
        List<Location> ret = new ArrayList<>();
        for (int i = 0; i < this.currentIndex; i++)
            ret.add(Location.of(this.locations[i]));
        return ret;
    }

    @Override
    public String toString()
    {
        return "Container [nr=" + this.getNr() + ", type=" + this.getType() + ", locations=" + this.getLocations() + "]";
    }

}
