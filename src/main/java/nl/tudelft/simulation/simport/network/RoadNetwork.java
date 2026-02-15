package nl.tudelft.simulation.simport.network;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nl.tudelft.simulation.dsol.animation.gis.FeatureInterface;
import nl.tudelft.simulation.dsol.animation.gis.LayerInterface;
import nl.tudelft.simulation.dsol.animation.gis.esri.ShapeFileReader;
import nl.tudelft.simulation.dsol.animation.gis.map.Feature;
import nl.tudelft.simulation.dsol.animation.gis.map.Layer;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.animation.CentroidAnimation;
import nl.tudelft.simulation.simport.animation.LinkAnimation;
import nl.tudelft.simulation.simport.animation.NodeAnimation;
import nl.tudelft.simulation.simport.animation.TurnAnimation;
import nl.tudelft.simulation.simport.gis.CoordinateTransformRdNewToWgs84;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.util.SimPortRuntimeException;

/**
 * The logical RoadNetwork with all relevant elements.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadNetwork
{
    /** the model. */
    private final PortModel model;

    /** The nodes. */
    private List<Node> nodeList = new ArrayList();

    /** the road nodes. */
    private Map<RoadNode, RoadNode> roadNodeMap = new HashMap<>();

    /** the links. */
    private List<RoadLink> roadLinkList = new ArrayList<>();

    /** the turns. */
    private List<RoadTurn> turnList = new ArrayList<>();

    /** The centroids. */
    private List<Centroid> centroidList = new ArrayList<>();

    /** The loop detectors. */
    private List<Detector> detectorList = new ArrayList<>();

    /** The centroid aread. */
    private List<CentroidArea> centroidAreaList = new ArrayList<>();

    /**
     *
     */
    public RoadNetwork(final PortModel model)
    {
        this.model = model;
    }

    /**
     * Read the nodes and their properties, store them, and animate if needed.
     * @param url the location of the nodes file
     */
    public void readNodes(final URL nodesUrl)
    {
        LayerInterface layer = new Layer();
        List<FeatureInterface> features = new ArrayList<>();
        FeatureInterface feature = new Feature(layer);
        feature.setKey("*");
        feature.setValue("*");
        features.add(feature);
        boolean animate = getSimulator() instanceof AnimatorInterface;
        var simulator = getSimulator();
        try
        {
            var shpReader = new ShapeFileReader(nodesUrl, new CoordinateTransformRdNewToWgs84(0.0, 0.0), features);
            shpReader.readAllShapes(feature);
            for (int i = 0; i < feature.getNumPoints(); i++)
            {
                Point2D p = feature.getPoint(i);
                Node node = new Node(p.getX(), p.getY());
                this.nodeList.add(node);
                if (animate)
                    new NodeAnimation(node, simulator);
            }
        }
        catch (Exception e)
        {
            throw new SimPortRuntimeException(e);
        }
    }

    /**
     * Read the sections and their properties, store them, and animate if needed.
     * @param url the location of the nodes file
     */
    public void readSections(final URL linksUrl)
    {
        LayerInterface layer = new Layer();
        List<FeatureInterface> features = new ArrayList<>();
        FeatureInterface feature = new Feature(layer);
        feature.setKey("*");
        feature.setValue("*");
        features.add(feature);
        boolean animate = getSimulator() instanceof AnimatorInterface;
        var simulator = getSimulator();
        try
        {
            var shpReader = new ShapeFileReader(linksUrl, new CoordinateTransformRdNewToWgs84(0.0, 0.0), features);
            shpReader.readAllShapes(feature);
            for (int i = 0; i < feature.getNumShapes(); i++)
            {
                Path2D path = feature.getShape(i);
                Point2D[] fromto = getFirstAndLastPoints(path).get();
                RoadNode nodeFrom = new RoadNode(fromto[0].getX(), fromto[0].getY());
                RoadNode nodeTo = new RoadNode(fromto[1].getX(), fromto[1].getY());
                if (this.roadNodeMap.containsKey(nodeFrom))
                    nodeFrom = this.roadNodeMap.get(nodeFrom);
                else
                    this.roadNodeMap.put(nodeFrom, nodeFrom);
                if (this.roadNodeMap.containsKey(nodeTo))
                    nodeTo = this.roadNodeMap.get(nodeTo);
                else
                    this.roadNodeMap.put(nodeTo, nodeTo);
                RoadLink link = new RoadLink(nodeFrom, nodeTo);
                this.roadLinkList.add(link);
                if (animate)
                    new LinkAnimation(link, simulator);
            }
        }
        catch (Exception e)
        {
            throw new SimPortRuntimeException(e);
        }
    }

    /**
     * Read the turns and their properties, store them, and animate if needed.
     * @param url the location of the nodes file
     */
    public void readTurns(final URL turnsUrl)
    {
        LayerInterface layer = new Layer();
        List<FeatureInterface> features = new ArrayList<>();
        FeatureInterface feature = new Feature(layer);
        feature.setKey("*");
        feature.setValue("*");
        features.add(feature);
        boolean animate = getSimulator() instanceof AnimatorInterface;
        var simulator = getSimulator();
        try
        {
            var shpReader = new ShapeFileReader(turnsUrl, new CoordinateTransformRdNewToWgs84(0.0, 0.0), features);
            shpReader.readAllShapes(feature);
            for (int i = 0; i < feature.getNumShapes(); i++)
            {
                Path2D path = feature.getShape(i);
                Point2D[] fromto = getFirstAndLastPoints(path).get();
                RoadNode nodeFrom = new RoadNode(fromto[0].getX(), fromto[0].getY());
                RoadNode nodeTo = new RoadNode(fromto[1].getX(), fromto[1].getY());
                if (this.roadNodeMap.containsKey(nodeFrom))
                    nodeFrom = this.roadNodeMap.get(nodeFrom);
                else
                    this.roadNodeMap.put(nodeFrom, nodeFrom);
                if (this.roadNodeMap.containsKey(nodeTo))
                    nodeTo = this.roadNodeMap.get(nodeTo);
                else
                    this.roadNodeMap.put(nodeTo, nodeTo);
                RoadTurn turn = new RoadTurn(nodeFrom, nodeTo);
                this.turnList.add(turn);
                if (animate)
                    new TurnAnimation(turn, simulator);
            }
        }
        catch (Exception e)
        {
            throw new SimPortRuntimeException(e);
        }
    }

    /**
     * Read the centroids and their properties, store them, and animate if needed.
     * @param url the location of the centroids file
     */
    public void readCentroids(final URL centroidsUrl)
    {
        LayerInterface layer = new Layer();
        List<FeatureInterface> features = new ArrayList<>();
        FeatureInterface feature = new Feature(layer);
        feature.setKey("*");
        feature.setValue("*");
        features.add(feature);
        boolean animate = getSimulator() instanceof AnimatorInterface;
        var simulator = getSimulator();
        try
        {
            var shpReader = new ShapeFileReader(centroidsUrl, new CoordinateTransformRdNewToWgs84(0.0, 0.0), features);
            shpReader.readAllShapes(feature);
            for (int i = 0; i < feature.getNumPoints(); i++)
            {
                Point2D p = feature.getPoint(i);
                Centroid centroid = new Centroid(p.getX(), p.getY());
                this.centroidList.add(centroid);
                if (animate)
                    new CentroidAnimation(centroid, simulator);
            }
        }
        catch (Exception e)
        {
            throw new SimPortRuntimeException(e);
        }
    }

    /**
     * @return model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /**
     * @return simulator
     */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return getModel().getSimulator();
    }

    ///////////////////////////////////////// HELPER METHODS ////////////////////////////////////

    /**
     * Returns the first and last points of the given Path2D, if present. The first point is from the first SEG_MOVETO. The last
     * point is the end of the last drawing segment; if the path ends with SEG_CLOSE, the last point is the start of that
     * subpath (i.e., the last MOVETO).
     * @param path the path (Path2D.Double or Path2D.Float)
     * @return an Optional containing a 2-element array: [firstPoint, lastPoint]; empty if path has no points
     */

    public static Optional<Point2D[]> getFirstAndLastPoints(final Path2D path)
    {
        if (path == null)
            return Optional.empty();

        PathIterator it = path.getPathIterator(null);
        double[] c = new double[6];

        // Trackers
        Point2D firstPoint = null; // start of first drawable segment
        Point2D lastPoint = null; // end of the last drawable segment
        Point2D subpathStart = null; // start of current subpath

        // The "current point" as Java2D would track it. If there is no MOVETO before the first
        // drawable segment, the current point is implicitly (0,0).
        double currX = 0.0, currY = 0.0;

        while (!it.isDone())
        {
            int seg = it.currentSegment(c);
            switch (seg)
            {
                case PathIterator.SEG_MOVETO:
                {
                    currX = c[0];
                    currY = c[1];
                    subpathStart = new Point2D.Double(currX, currY);
                    break;
                }
                case PathIterator.SEG_LINETO:
                {
                    // Start is current point, end is (c[0], c[1])
                    if (firstPoint == null)
                    {
                        firstPoint = new Point2D.Double(currX, currY);
                    }
                    currX = c[0];
                    currY = c[1];
                    lastPoint = new Point2D.Double(currX, currY);
                    break;
                }
                case PathIterator.SEG_QUADTO:
                {
                    // End is (c[2], c[3])
                    if (firstPoint == null)
                    {
                        firstPoint = new Point2D.Double(currX, currY);
                    }
                    currX = c[2];
                    currY = c[3];
                    lastPoint = new Point2D.Double(currX, currY);
                    break;
                }
                case PathIterator.SEG_CUBICTO:
                {
                    // End is (c[4], c[5])
                    if (firstPoint == null)
                    {
                        firstPoint = new Point2D.Double(currX, currY);
                    }
                    currX = c[4];
                    currY = c[5];
                    lastPoint = new Point2D.Double(currX, currY);
                    break;
                }
                case PathIterator.SEG_CLOSE:
                {
                    // Closes to subpathStart, if any
                    if (subpathStart != null)
                    {
                        if (firstPoint == null)
                        {
                            firstPoint = new Point2D.Double(currX, currY);
                        }
                        currX = subpathStart.getX();
                        currY = subpathStart.getY();
                        lastPoint = new Point2D.Double(currX, currY);
                    }
                    break;
                }
                default:
                    // ignore
            }
            it.next();
        }

        if (firstPoint == null || lastPoint == null)
        {
            // No drawable segments found
            return Optional.empty();
        }

        return Optional.of(new Point2D[] {firstPoint, lastPoint});
    }

    // Convenience helpers if you only need one side
    public static Optional<Point2D> getFirstPoint(final Path2D path)
    {
        return getFirstAndLastPoints(path).map(arr -> arr[0]);
    }

    public static Optional<Point2D> getLastPoint(final Path2D path)
    {
        return getFirstAndLastPoints(path).map(arr -> arr[1]);
    }

}
