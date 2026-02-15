package nl.tudelft.simulation.simport.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;

import org.djutils.draw.bounds.Bounds2d;

import nl.tudelft.simulation.dsol.animation.SimRenderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.simport.network.RoadTurn;

/**
 * NodeAnimation.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TurnAnimation extends SimRenderable2d<RoadTurn>
{
    /**
     *
     */
    public TurnAnimation(final RoadTurn turn, final SimulatorInterface<?> simulator)
    {
        super(turn, simulator);
        setScaleY(true);
    }

    @Override
    public boolean contains(final Point2D pointScreenCoordinates, final Bounds2d extent, final Dimension screenSize,
            final RenderableScale scale, final double worldMargin, final double pixelMargin)
    {
        return super.contains(pointScreenCoordinates, extent, screenSize, scale, 0.0, 5.0);
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        double dx = getSource().getDx();
        double dy = getSource().getDy();
        graphics.setColor(Color.blue);
        graphics.setStroke(new BasicStroke(0.00005f));
        var line = new Path2D.Double();
        if (dx > 0 && dy > 0 || dx < 0 && dy < 0)
        {
            line.moveTo(0.0, 0.0);
            line.lineTo(Math.abs(dx), -Math.abs(dy));
        }
        else
        {
            line.moveTo(0.0, -Math.abs(dy));
            line.lineTo(Math.abs(dx), 0.0);
        }
        graphics.draw(line);
    }

}
