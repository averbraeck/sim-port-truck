package nl.tudelft.simulation.simport.truck.activity;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.network.Centroid;
import nl.tudelft.simulation.simport.truck.Truck;

/**
 * PlannedDrivingActivity contains information about the intended trip from A to B.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PlannedDrivingActivity extends PlannedTruckActivity
{
    /** the start location. */
    private final Centroid origCentroid;

    /** the end location. */
    private final Centroid destCentroid;

    /** the first container on the truck (can be null). */
    private final Container container1;

    /** the second container on the truck (can be null). */
    private final Container container2;

    /** the departure time. */
    private final ClockTime departureTime;

    /** the arrival time. */
    private final ClockTime arrivalTime;

    /** the length of the route. */
    private final Length distance;

    /**
     * Make a driving activity for a truck.
     * @param truck the truck
     * @param origCentroid the start location
     * @param destCentroid the end location
     * @param container1 the first container on the truck (can be null)
     * @param container2 the second container on the truck (can be null)
     * @param departureTime the departure time
     * @param arrivalTime the arrival time
     * @param distance the length of the route
     */
    public PlannedDrivingActivity(final Truck truck, final Centroid origCentroid, final Centroid destCentroid,
            final Container container1, final Container container2, final ClockTime departureTime, final ClockTime arrivalTime,
            final Length distance)
    {
        super(truck);
        this.origCentroid = origCentroid;
        this.destCentroid = destCentroid;
        this.container1 = container1;
        this.container2 = container2;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.distance = distance;
    }

    /**
     * @return whether the truck is empty
     */
    public boolean isEmpty()
    {
        return this.container1 == null && this.container2 == null;
    }

    /**
     * @return whether the truck is loaded
     */
    public boolean isLoaded()
    {
        return !isEmpty();
    }

    /**
     * @return the duration of the trip
     */
    public Duration getDuration()
    {
        return this.getArrivalTime().minus(this.getDepartureTime());
    }

    /**
     * @return the average speed of the trip
     */
    public Speed getAvgSpeed()
    {
        return this.distance.divide(getDuration());
    }

    /**
     * @return origCentroid
     */
    public Centroid getOrigCentroid()
    {
        return this.origCentroid;
    }

    /**
     * @return destCentroid
     */
    public Centroid getDestCentroid()
    {
        return this.destCentroid;
    }

    /**
     * @return container1
     */
    public Container getContainer1()
    {
        return this.container1;
    }

    /**
     * @return container2
     */
    public Container getContainer2()
    {
        return this.container2;
    }

    /**
     * @return departureTime
     */
    public ClockTime getDepartureTime()
    {
        return this.departureTime;
    }

    /**
     * @return arrivalTime
     */
    public ClockTime getArrivalTime()
    {
        return this.arrivalTime;
    }

    /**
     * @return distance
     */
    public Length getDistance()
    {
        return this.distance;
    }

}
