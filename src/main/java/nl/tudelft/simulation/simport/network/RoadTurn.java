package nl.tudelft.simulation.simport.network;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The Link is a connector in the road network to connect two segments (roadlink).
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadTurn implements Locatable
{
    private final RoadNode nodeFrom;

    private final RoadNode nodeTo;

    private final double dx;

    private final double dy;

    private Speed maxSpeed = new Speed(20.0, SpeedUnit.KM_PER_HOUR);

    private int nrLanes = 1;

    /**
     *
     */
    public RoadTurn(final RoadNode nodeFrom, final RoadNode nodeTo)
    {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.dx = nodeTo.getX() - nodeFrom.getX();
        this.dy = nodeTo.getY() - nodeFrom.getY();
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
}
