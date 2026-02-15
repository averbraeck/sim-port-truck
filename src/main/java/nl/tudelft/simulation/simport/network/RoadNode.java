package nl.tudelft.simulation.simport.network;

import java.util.Objects;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The RoadNode is a vertex in the road network connecting Links and Turns.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadNode implements Locatable
{
    /** x in WGS84. */
    private final double x;

    /** y in WGS84. */
    private final double y;

    /**
     *
     */
    public RoadNode(final double x, final double y)
    {
        this.x = x;
        this.y = y;
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
        return new Bounds2d(0.01, 0.01);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.x, this.y);
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
        RoadNode other = (RoadNode) obj;
        return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(other.x)
                && Double.doubleToLongBits(this.y) == Double.doubleToLongBits(other.y);
    }

    @Override
    public String toString()
    {
        return "RoadNode [x=" + this.x + ", y=" + this.y + "]";
    }

}
