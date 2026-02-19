package nl.tudelft.simulation.simport.gis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.io.ResourceResolver;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.SimRenderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.animation.gis.GisMapInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * GisHelper.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class GisHelper
{
    public static void drawMarkers(final GisMapInterface map, final PortModel model, final URL csvCentroidUrl)
    {
        Map<String, String> shapeFileMap = new LinkedHashMap<>();
        try
        {
            NamedCsvReader csvCentroid = NamedCsvReader.builder().build(Paths.get(csvCentroidUrl.toURI()));
            csvCentroid.forEach((row) -> shapeFileMap.put(row.getField("layer"), row.getField("shapeFile")));
            URL centroidUrl = ResourceResolver.resolve(shapeFileMap.get("centroids").replace(".shp", ".dbf")).asUrl();
            DbfReader dbfReader = new DbfReader(centroidUrl);
            String[] colNames = dbfReader.getColumnNames();
            String[][] data = dbfReader.getRows();
            System.out.println(Arrays.asList(colNames));

            var layerMap = map.getLayerMap();
            var centroidLayer = layerMap.get("centroids");
            for (var feature : centroidLayer.getFeatures())
            {
                for (int i = 0; i < feature.getNumPoints(); i++)
                {
                    Point2D point = feature.getPoint(i);
                    // new Marker(point, data[i][1], model.getSimulator());
                }
            }

            URL nodesUrl = ResourceResolver.resolve(shapeFileMap.get("nodes").replace(".shp", ".dbf")).asUrl();
            DbfReader dbfReaderNodes = new DbfReader(nodesUrl);
            String[] colNamesNodes = dbfReaderNodes.getColumnNames();
            String[][] dataNodes = dbfReaderNodes.getRows();
            System.out.println(Arrays.asList(colNamesNodes));

            var nodesLayer = layerMap.get("nodes");
            for (var feature : nodesLayer.getFeatures())
            {
                for (int i = 0; i < feature.getNumPoints(); i++)
                {
                    Point2D point = feature.getPoint(i);
                    // new Node(point, colNamesNodes, dataNodes[i], model.getSimulator());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static class Marker implements Locatable
    {
        private final Point2d point;

        private final String key;

        /**
         * @param point
         * @param key
         */
        public Marker(final Point2D point, final String key, final SimulatorInterface<?> simulator)
        {
            this.point = new Point2d(point.getX(), point.getY());
            this.key = key;
            new MarkerAnimation(this, simulator);
        }

        @Override
        public Point<?> getLocation()
        {
            return this.point;
        }

        @Override
        public Bounds<?, ?> getRelativeBounds()
        {
            return new Bounds2d(0.007, 0.0004);
        }

        /**
         * @return key
         */
        public String getKey()
        {
            return this.key;
        }

    }

    public static class MarkerAnimation extends SimRenderable2d<Marker>
    {
        /**
         * @param source
         * @param contextProvider
         */
        public MarkerAnimation(final Marker source, final SimulatorInterface<?> simulator)
        {
            super(source, simulator);
            setScale(false);
            setScaleY(false);
            setScaleObject(false);
        }

        @Override
        public boolean contains(final Point2D pointScreenCoordinates, final Bounds2d extent, final Dimension screenSize,
                final RenderableScale scale, final double worldMargin, final double pixelMargin)
        {
            return super.contains(pointScreenCoordinates, extent, screenSize, scale, 0.0, 10.0);
        }

        @Override
        public void paint(final Graphics2D graphics, final ImageObserver observer)
        {
            var rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
            graphics.setColor(Color.red);
            graphics.fill(rect);
        }
    }

    public static class Node implements Locatable
    {
        private final Point2d point;

        private final String[] dataNodes;

        /**
         * @param point
         * @param key
         */
        public Node(final Point2D point, final String[] colNamesNodes, final String[] dataNodes,
                final SimulatorInterface<?> simulator)
        {
            this.point = new Point2d(point.getX(), point.getY());
            this.dataNodes = dataNodes;
            new NodeAnimation(this, simulator);
        }

        @Override
        public Point<?> getLocation()
        {
            return this.point;
        }

        @Override
        public Bounds<?, ?> getRelativeBounds()
        {
            return new Bounds2d(0.0035, 0.0002);
        }

        /**
         * @return key
         */
        public String[] getDataNodes()
        {
            return this.dataNodes;
        }

    }

    public static class NodeAnimation extends SimRenderable2d<Node>
    {
        /**
         * @param source
         * @param contextProvider
         */
        public NodeAnimation(final Node source, final SimulatorInterface<?> simulator)
        {
            super(source, simulator);
            setScale(false);
            setScaleY(false);
            setScaleObject(false);
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
            var rect = new Rectangle2D.Double(0.0, 0.0, 5.0, 5.0);
            graphics.setColor(Color.blue);
            graphics.fill(rect);
        }

    }

}
