package nl.tudelft.simulation.simport.network;

import java.util.Objects;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The Link is an edge in the road network with O/D and usage intensity.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadLink implements Locatable, Identifiable
{
    private final RoadNode nodeFrom;

    private final RoadNode nodeTo;

    private final double dx;

    private final double dy;

    private Speed maxSpeed = new Speed(80.0, SpeedUnit.KM_PER_HOUR);

    private int nrLanes = 2;

    private String name = "";

    private final String id;

    /**
     *
     */
    public RoadLink(final String id, final RoadNode nodeFrom, final RoadNode nodeTo)
    {
        this.id = id;
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.dx = nodeTo.getX() - nodeFrom.getX();
        this.dy = nodeTo.getY() - nodeFrom.getY();
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * @return nodeFrom
     */
    public RoadNode getNodeFrom()
    {
        return this.nodeFrom;
    }

    /**
     * @return nodeTo
     */
    public RoadNode getNodeTo()
    {
        return this.nodeTo;
    }

    /**
     * @return dx
     */
    public double getDx()
    {
        return this.dx;
    }

    /**
     * @return dy
     */
    public double getDy()
    {
        return this.dy;
    }

    /**
     * @return maxSpeed
     */
    public Speed getMaxSpeed()
    {
        return this.maxSpeed;
    }

    /**
     * @param maxSpeed set maxSpeed
     */
    public void setMaxSpeed(final Speed maxSpeed)
    {
        this.maxSpeed = maxSpeed;
    }

    /**
     * @return nrLanes
     */
    public int getNrLanes()
    {
        return this.nrLanes;
    }

    /**
     * @param nrLanes set nrLanes
     */
    public void setNrLanes(final int nrLanes)
    {
        this.nrLanes = nrLanes;
    }

    /**
     * @return name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name set name
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public Point<?> getLocation()
    {
        return new Point2d(Math.min(this.nodeFrom.getX(), this.nodeTo.getX()),
                Math.min(this.nodeFrom.getY(), this.nodeTo.getY()));
    }

    @Override
    public Bounds<?, ?> getRelativeBounds()
    {
        return new Bounds2d(0.0, Math.abs(this.dx), 0.0, Math.abs(this.dy));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.nodeFrom, this.nodeTo, this.nrLanes);
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
        RoadLink other = (RoadLink) obj;
        return Objects.equals(this.id, other.id) && Objects.equals(this.nodeFrom, other.nodeFrom)
                && Objects.equals(this.nodeTo, other.nodeTo) && this.nrLanes == other.nrLanes;
    }

    @Override
    public String toString()
    {
        return "RoadLink [id=" + this.id + ", name=" + this.name + "]";
    }

}
