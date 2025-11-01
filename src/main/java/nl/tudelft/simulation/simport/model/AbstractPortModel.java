package nl.tudelft.simulation.simport.model;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.io.ResourceResolver;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.gis.GisMapInterface;
import nl.tudelft.simulation.dsol.animation.gis.esri.EsriFileCsvParser;
import nl.tudelft.simulation.dsol.animation.gis.osm.OsmFileCsvParser;
import nl.tudelft.simulation.dsol.model.AbstractDsolModel;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.gis.CoordinateTransformRdNewToWgs84;
import nl.tudelft.simulation.simport.gis.GisHelper;
import nl.tudelft.simulation.simport.gis.MultiGisRenderable2d;
import nl.tudelft.simulation.simport.road.GraphFromGISObjects;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * PortModel is an abstract 'parent' model with key objects such as the terminals and the road network.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class AbstractPortModel extends AbstractDsolModel<Duration, ClockDevsSimulatorInterface> implements PortModel
{
    /** interactive run? */
    private final boolean interactive;

    /** The terminals. */
    private final Map<String, Terminal> terminalMap = new LinkedHashMap<>();

    /** the vessel counter. */
    private final AtomicInteger uniqueVesselNumber = new AtomicInteger(1000);

    /** the container counter. */
    private final AtomicInteger uniqueContainerNumber = new AtomicInteger(1000000);

    /** the GIS map. */
    private MultiGisRenderable2d gisMap;

    /**
     * Create a port model.
     * @param simulator the simulator to use
     * @param interactive indicate whether the model is interactive and should load animation
     */
    public AbstractPortModel(final ClockDevsSimulatorInterface simulator, final boolean interactive)
    {
        super(simulator);
        this.interactive = interactive;
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        if (isInteractive())
        {
            URL csvUrl = ResourceResolver.resolve("/resources/maps/por.csv").asUrl();
            System.out.println("GIS definitions file: " + csvUrl.toString());
            URL osmUrl = ResourceResolver.resolve("/resources/maps/por.osm.pbf").asUrl();
            GisMapInterface osmMap = null;
            System.out.println("GIS data file: " + osmUrl.toString());
            try
            {
                this.gisMap = new MultiGisRenderable2d(getSimulator().getReplication());
                osmMap = OsmFileCsvParser.parseMapFile(csvUrl, osmUrl, "Port of Rotterdam");
                this.gisMap.add(osmMap);
            }
            catch (IOException exception)
            {
                throw new SimRuntimeException(exception);
            }

            URL csvCentroidUrl = ResourceResolver.resolve("/resources/hvm50/centroids2.csv").asUrl();
            GisMapInterface esriMap;
            System.out.println("ESRI-map file: " + csvCentroidUrl.toString());
            try
            {
                esriMap =
                        EsriFileCsvParser.parseMapFile(csvCentroidUrl, "centroids", new CoordinateTransformRdNewToWgs84(0, 0));
                this.gisMap.add(esriMap);
            }
            catch (IOException e)
            {
                throw new SimRuntimeException(e);
            }

            GisHelper.drawMarkers(esriMap, this, csvCentroidUrl);
            new GraphFromGISObjects(esriMap, this, csvCentroidUrl);
        }
    }

    /**
     * @return gisMap
     */
    public MultiGisRenderable2d getGisMap()
    {
        return this.gisMap;
    }

    @Override
    public void addTerminal(final Terminal terminal)
    {
        this.terminalMap.put(terminal.getId(), terminal);
    }

    @Override
    public Terminal getTerminal(final String id)
    {
        return this.terminalMap.get(id);
    }

    @Override
    public int uniqueContainerNr()
    {
        return this.uniqueContainerNumber.incrementAndGet();
    }

    @Override
    public int uniqueVesselNr()
    {
        return this.uniqueVesselNumber.incrementAndGet();
    }

    @Override
    public boolean isInteractive()
    {
        return this.interactive;
    }

}
