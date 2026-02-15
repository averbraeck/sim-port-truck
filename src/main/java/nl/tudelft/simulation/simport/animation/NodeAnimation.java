package nl.tudelft.simulation.simport.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import org.djutils.draw.bounds.Bounds2d;

import nl.tudelft.simulation.dsol.animation.SimRenderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.simport.network.Node;

/**
 * NodeAnimation.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class NodeAnimation extends SimRenderable2d<Node>
{
    /**
     *
     */
    public NodeAnimation(final Node node, final SimulatorInterface<?> simulator)
    {
        super(node, simulator);
        setScale(false);
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
        var rect = new Rectangle2D.Double(-2.0, -2.0, 4, 4);
        graphics.setColor(Color.red);
        graphics.fill(rect);
    }

}
