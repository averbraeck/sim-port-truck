package nl.tudelft.simulation.simport.truck.activity;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.network.Centroid;

/**
 * RealizedDrivingActivity contains information about the actual trip from A to B.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RealizedDrivingActivity extends RealizedTruckActivity
{
    /** the planned driving activity. */
    private final PlannedDrivingActivity plannedDrivingActivity;

    /** the actual departure time. */
    private ClockTime actualDepartureTime;

    /** the actual arrival time. */
    private ClockTime actualArrivalTime;

    /**
     * Make a realized driving activity for a truck.
     * @param plannedDrivingActivity the planned driving activity
     * @param actualDepartureTime the actual departure time
     * @param actualArrivalTime the actual arrival time
     */
    public RealizedDrivingActivity(final PlannedDrivingActivity plannedDrivingActivity)
    {
        super(plannedDrivingActivity.getTruck());
        this.plannedDrivingActivity = plannedDrivingActivity;
    }

    /**
     * @return whether the truck is empty
     */
    public boolean isEmpty()
    {
        return this.plannedDrivingActivity.isEmpty();
    }

    /**
     * @return whether the truck is loaded
     */
    public boolean isLoaded()
    {
        return !isEmpty();
    }

    /**
     * @return the planned duration of the trip
     */
    public Duration getPlannedDuration()
    {
        return getPlannedArrivalTime().minus(getPlannedDepartureTime());
    }

    /**
     * @return the actual duration of the trip
     */
    public Duration getActualDuration()
    {
        return this.getActualArrivalTime().minus(this.getActualDepartureTime());
    }

    /**
     * @return the planned average speed of the trip
     */
    public Speed getPlannedAvgSpeed()
    {
        return getDistance().divide(getPlannedDuration());
    }

    /**
     * @return the actual average speed of the trip
     */
    public Speed getActualAvgSpeed()
    {
        return getDistance().divide(getActualDuration());
    }

    /**
     * @return origCentroid
     */
    public Centroid getOrigCentroid()
    {
        return this.plannedDrivingActivity.getOrigCentroid();
    }

    /**
     * @return destCentroid
     */
    public Centroid getDestCentroid()
    {
        return this.plannedDrivingActivity.getDestCentroid();
    }

    /**
     * @return container1
     */
    public Container getContainer1()
    {
        return this.plannedDrivingActivity.getContainer1();
    }

    /**
     * @return container2
     */
    public Container getContainer2()
    {
        return this.plannedDrivingActivity.getContainer2();
    }

    /**
     * @return planned departure time
     */
    public ClockTime getPlannedDepartureTime()
    {
        return this.plannedDrivingActivity.getDepartureTime();
    }

    /**
     * @return planned arrival time
     */
    public ClockTime getPlannedArrivalTime()
    {
        return this.plannedDrivingActivity.getArrivalTime();
    }

    /**
     * @param actualDepartureTime set actualDepartureTime
     */
    public void setActualDepartureTime(final ClockTime actualDepartureTime)
    {
        this.actualDepartureTime = actualDepartureTime;
    }

    /**
     * @param actualArrivalTime set actualArrivalTime
     */
    public void setActualArrivalTime(final ClockTime actualArrivalTime)
    {
        this.actualArrivalTime = actualArrivalTime;
    }

    /**
     * @return actual departure time
     */
    public ClockTime getActualDepartureTime()
    {
        return this.actualDepartureTime;
    }

    /**
     * @return actual arrival time
     */
    public ClockTime getActualArrivalTime()
    {
        return this.actualArrivalTime;
    }

    /**
     * @return distance
     */
    public Length getDistance()
    {
        return this.plannedDrivingActivity.getDistance();
    }

    /**
     * @return plannedDrivingActivity
     */
    public PlannedDrivingActivity getPlannedDrivingActivity()
    {
        return this.plannedDrivingActivity;
    }

}
