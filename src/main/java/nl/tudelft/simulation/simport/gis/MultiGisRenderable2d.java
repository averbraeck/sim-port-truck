package nl.tudelft.simulation.simport.gis;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.bounds.Bounds3d;
import org.djutils.draw.point.OrientedPoint3d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.animation.gis.GisMapInterface;
import nl.tudelft.simulation.dsol.animation.gis.GisRenderable2d;
import nl.tudelft.simulation.dsol.animation.gis.transform.CoordinateTransform;
import nl.tudelft.simulation.naming.context.Contextualized;
import nl.tudelft.simulation.naming.context.util.ContextUtil;

/**
 * This renderable contains multiple OSM, ESRI and/or other maps.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class MultiGisRenderable2d implements GisRenderable2d
{
    /** */
    private static final long serialVersionUID = 20200108L;

    /** the map to display. */
    protected List<GisMapInterface> mapList = new ArrayList<>();

    /** the dummy internal renderables. */
    protected List<GisRenderable2d> internalRenderableList = new ArrayList<>();

    /** the image cached image. */
    protected BufferedImage cachedImage = null;

    /** the cached extent. */
    protected Bounds2d cachedExtent = new Bounds2d(0, 0, 0, 0);

    /** the cached screenSize. */
    protected Dimension cachedScreenSize = new Dimension();

    /** the location of the map. */
    protected OrientedPoint3d location = null;

    /** the bounds of the map. */
    protected Bounds3d bounds = null;

    /**
     * constructs a new MultiGisRenderable2d.
     * @param contextProvider the object that can provide the context to store the animation objects
     */
    public MultiGisRenderable2d(final Contextualized contextProvider)
    {
        this(contextProvider, new CoordinateTransform.NoTransform());
    }

    /**
     * constructs a new MultiGisRenderable2d.
     * @param contextProvider the object that can provide the context to store the animation objects
     * @param coordinateTransform the transformation of (x, y) coordinates to (x', y') coordinates.
     */
    public MultiGisRenderable2d(final Contextualized contextProvider, final CoordinateTransform coordinateTransform)
    {
        this(contextProvider, coordinateTransform, -Double.MAX_VALUE);
    }

    /**
     * constructs a new MultiGisRenderable2d based on an existing Map.
     * @param contextProvider the object that can provide the context to store the animation objects
     * @param coordinateTransform the transformation of (x, y) coordinates to (x', y') coordinates.
     * @param z the z-value to use
     */
    public MultiGisRenderable2d(final Contextualized contextProvider, final CoordinateTransform coordinateTransform,
            final double z)
    {
        try
        {
            this.location = new OrientedPoint3d(this.cachedExtent.midPoint().getX(), this.cachedExtent.midPoint().getY(), z);
            this.bounds = new Bounds3d(this.cachedExtent.getDeltaX(), this.cachedExtent.getDeltaY(), 0.0);
            this.bind2Context(contextProvider);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().warn(exception, "<init>");
        }
    }

    /**
     * binds a renderable2D to the context. The reason for specifying this in an independent method instead of adding the code
     * in the constructor is related to the RFE submitted by van Houten that in specific distributed context, such binding must
     * be overwritten.
     * @param contextProvider the object that can provide the context to store the animation objects
     */
    protected void bind2Context(final Contextualized contextProvider)
    {
        try
        {
            ContextUtil.lookupOrCreateSubContext(contextProvider.getContext(), "animation/2D")
                    .bindObject(Integer.toString(System.identityHashCode(this)), this);
        }
        catch (NamingException | RemoteException exception)
        {
            CategoryLogger.always().warn(exception, "<init>");
        }
    }

    /**
     * Add a map to the list of GIS maps.
     * @param map the map to add
     */
    public void add(final GisMapInterface map)
    {
        this.mapList.add(map);
        this.internalRenderableList.add(new InternalGisRenderable2d(this, map));
    }

    @Override
    public void paintComponent(final Graphics2D graphics, final Bounds2d extent, final Dimension screen,
            final RenderableScale renderableScale, final ImageObserver observer)
    {
        try
        {
            boolean same = true;
            for (var map : this.mapList)
                if (!map.isSame())
                    same = false;
            // is the extent or the screen size still the same
            if (extent.equals(this.cachedExtent) && screen.equals(this.cachedScreenSize) && same)
            {
                graphics.drawImage(this.cachedImage, 0, 0, null);
                return;
            }

            for (var map : this.mapList)
            {
                map.setDrawBackground(false);
                map.setExtent(extent);
                map.getImage().setSize(screen);
            }
            this.cacheImage();
            this.paintComponent(graphics, extent, screen, renderableScale, observer);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().warn(exception, "paint");
        }
    }

    /**
     * @return mapList
     */
    public List<GisMapInterface> getMapList()
    {
        return this.mapList;
    }

    /**
     * @return internalRenderableList
     */
    public List<GisRenderable2d> getInternalRenderableList()
    {
        return this.internalRenderableList;
    }

    @Override
    public MultiGisRenderable2d getSource()
    {
        return this;
    }

    @Override
    public Bounds3d getRelativeBounds()
    {
        return this.bounds;
    }

    @Override
    public OrientedPoint3d getLocation()
    {
        return this.location;
    }

    /**
     * @return map the Shapefile map
     */
    @Override
    public GisMapInterface getMap()
    {
        return this.mapList.get(0);
    }

    /**
     * caches the GIS map by creating an image. This prevents continuous rendering.
     * @throws Exception on graphicsProblems and network connection failures.
     */
    private void cacheImage() throws Exception
    {
        this.cachedImage = new BufferedImage((int) this.mapList.get(0).getImage().getSize().getWidth(),
                (int) this.mapList.get(0).getImage().getSize().getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D bg = this.cachedImage.createGraphics();
        for (var map : this.mapList)
        {
            map.drawMap(bg);
        }
        bg.dispose();
        this.cachedScreenSize = (Dimension) this.mapList.get(0).getImage().getSize().clone();
        this.cachedExtent = this.mapList.get(0).getExtent();
        this.location = new OrientedPoint3d(this.cachedExtent.midPoint().getX(), this.cachedExtent.midPoint().getY(),
                -Double.MIN_VALUE);
        this.bounds = new Bounds3d(this.cachedExtent.getDeltaX(), this.cachedExtent.getDeltaY(), 0.0);
    }

    @Override
    public void destroy(final Contextualized contextProvider)
    {
        try
        {
            ContextUtil.lookupOrCreateSubContext(contextProvider.getContext(), "animation/2D")
                    .unbindObject(Integer.toString(System.identityHashCode(this)));
        }
        catch (Throwable throwable)
        {
            CategoryLogger.always().warn(throwable, "finalize");
        }
    }

    @Override
    public boolean contains(final Point2d pointWorldCoordinates, final Bounds2d extent)
    {
        return false;
    }

    @Override
    public boolean contains(final Point2D pointScreenCoordinates, final Bounds2d extent, final Dimension screenSize,
            final RenderableScale scale, final double worldMargin, final double pixelMargin)
    {
        return false;
    }

    @Override
    public long getId()
    {
        return -1; // drawn before the rest in case all z-values are the same
    }

    public static class InternalGisRenderable2d implements GisRenderable2d
    {
        private static final long serialVersionUID = 1L;

        private final GisMapInterface map;

        private final MultiGisRenderable2d parent;

        public InternalGisRenderable2d(final MultiGisRenderable2d parent, final GisMapInterface map)
        {
            this.parent = parent;
            this.map = map;
        }

        @Override
        public boolean contains(final Point2D pointScreenCoordinates, final Bounds2d extent, final Dimension screenSize,
                final RenderableScale scale, final double worldMargin, final double pixelMargin)
        {
            return this.parent.contains(pointScreenCoordinates, extent, screenSize, scale, worldMargin, pixelMargin);
        }

        @Override
        public Point<?> getLocation() throws RemoteException
        {
            return this.parent.getLocation();
        }

        @Override
        public Bounds<?, ?> getRelativeBounds() throws RemoteException
        {
            return this.parent.getRelativeBounds();
        }

        @Override
        public void paintComponent(final Graphics2D graphics, final Bounds2d extent, final Dimension screen,
                final RenderableScale renderableScale, final ImageObserver observer)
        {
            this.parent.paintComponent(graphics, extent, screen, renderableScale, observer);
        }

        @Override
        public GisMapInterface getMap()
        {
            return this.map;
        }

        @Override
        public void destroy(final Contextualized contextProvider)
        {
            this.parent.destroy(contextProvider);
        }
    }

}
