package nl.tudelft.simulation.simport.truck;

import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;
import org.djutils.draw.point.Point3d;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.Facility;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Truck can transport a container from A to B, and load/unload a container.<br>
 * TODO: Note that in theory the truck can pick up a 2nd 20-ft container after a 20 ft container was already loaded. (in
 * reality: only if weight permits of course).
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Truck implements Identifiable, Locatable
{
    /** The truck id. */
    private final int id;

    /** the model. */
    private final PortModel model;

    /** The state of the truck. */
    private TruckState state;

    /** The origin when driving (null when not driving). */
    private Point2d origin;

    /** The destination when driving (null when not driving). */
    private Point2d destination;

    /** The facility when waiting or (un)loading. */
    private Facility facility;

    /** The container on board or null if none. */
    private Container container;

    /** Departure time. */
    private ClockTime departureTime;

    /** Arrival time, given gongestion. */
    private ClockTime arrivalTime;

    public Truck(final int id, final PortModel model)
    {
        this.id = id;
        this.model = model;
    }

    /**
     * Load a container onto the truck.
     * @param container the container to load
     */
    public void loadContainer(final Container container)
    {
        this.container = container;
    }

    /**
     * Unload a container from the truck.
     * @return the container unloaded container
     */
    public Container unloadContainer()
    {
        Container unloaded = this.container;
        this.container = null;
        return unloaded;
    }

    public boolean isEmpty()
    {
        return this.container == null;
    }

    /**
     * Return the container on the truck, or null if the truck is not loaded.
     * @return the container on the truck, or null if the truck is not loaded
     */
    public Container getContainer()
    {
        return this.container;
    }

    @Override
    public Point3d getLocation()
    {
        if (this.state.isDriving())
            return interpolateLocation();
        return this.facility.getLocation();
    }

    /**
     * For now, without a map service: straight line interpolation for the truck.
     * @return the location of the ruck while driving
     */
    public Point3d interpolateLocation()
    {
        double fraction = interpolateFraction();
        return new Point3d(this.origin.x + fraction * (this.destination.x - this.origin.x),
                this.origin.y + fraction * (this.destination.y - this.origin.y), 1.0);
    }

    /**
     * Interpolate the location between origin and destination, given the departure and arrival times.
     * @return fraction of the route
     */
    public double interpolateFraction()
    {
        return (getSimulator().getSimulatorClockTime().si - this.departureTime.si)
                / (this.arrivalTime.si - this.departureTime.si);
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return new Bounds2d(0.000146, 0.00009); // 10x10 m at Rotterdam (51.9 deg latitude)
    }

    @Override
    public String getId()
    {
        return String.valueOf(this.id);
    }

    /** @return the simulator. */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
    }
}
