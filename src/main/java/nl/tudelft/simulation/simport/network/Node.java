package nl.tudelft.simulation.simport.network;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The Node is a vertex in the road network that is a POI.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Node implements Locatable
{
    /** x in WGS84. */
    private final double x;

    /** y in WGS84. */
    private final double y;

    /**
     *
     */
    public Node(final double x, final double y)
    {
        this.x = x;
        this.y = y;
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

}
