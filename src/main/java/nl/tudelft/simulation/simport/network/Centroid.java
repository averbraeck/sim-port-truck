package nl.tudelft.simulation.simport.network;

import java.util.Objects;

import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * A Centroid is an origin/destination of goods in a CentroidArea. It has a connected Node that lies on the road network.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Centroid implements Locatable, Identifiable
{
    /** x in WGS84. */
    private final double x;

    /** y in WGS84. */
    private final double y;

    private final String id;

    private String name;

    private String eid;

    private String type;

    /**
     *
     */
    public Centroid(final String id, final double x, final double y)
    {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getId()
    {
        return this.id;
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

    /**
     * @return eid
     */
    public String getEid()
    {
        return this.eid;
    }

    /**
     * @param eid set eid
     */
    public void setEid(final String eid)
    {
        this.eid = eid;
    }

    /**
     * @return type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @param type set type
     */
    public void setType(final String type)
    {
        this.type = type;
    }

    /**
     * @return x
     */
    public double getX()
    {
        return this.x;
    }

    /**
     * @return y
     */
    public double getY()
    {
        return this.y;
    }

    @Override
    public Point<?> getLocation()
    {
        return new Point2d(this.x, this.y);
    }

    @Override
    public Bounds<?, ?> getRelativeBounds()
    {
        return new Bounds2d(0.02, 0.02);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.eid, this.id, this.name, this.type, this.x, this.y);
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
        Centroid other = (Centroid) obj;
        return Objects.equals(this.eid, other.eid) && Objects.equals(this.id, other.id) && Objects.equals(this.name, other.name)
                && Objects.equals(this.type, other.type) && Double.doubleToLongBits(this.x) == Double.doubleToLongBits(other.x)
                && Double.doubleToLongBits(this.y) == Double.doubleToLongBits(other.y);
    }

    @Override
    public String toString()
    {
        return "Centroid [" + this.eid + "]";
    }

}
