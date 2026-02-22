package nl.tudelft.simulation.simport.truck.activity;

import nl.tudelft.simulation.simport.truck.Truck;

/**
 * TruckActivity is the abstract parent class of a truck activity.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RealizedTruckActivity
{
    /** The truck. */
    protected final Truck truck;

    /**
     * Make a truck activity for a truck.
     * @param truck the truck
     */
    public RealizedTruckActivity(final Truck truck)
    {
        this.truck = truck;
    }

    /**
     * @return truck
     */
    public Truck getTruck()
    {
        return this.truck;
    }

}
