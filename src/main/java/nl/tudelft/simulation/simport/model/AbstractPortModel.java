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
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.statistics.SimCounter;
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
    protected boolean interactive = true;

    /** The terminals. */
    private final Map<String, Terminal> terminalMap = new LinkedHashMap<>();

    /** the vessel counter. */
    private final AtomicInteger uniqueVesselCounter = new AtomicInteger(1000);

    /** the container counter. */
    private final AtomicInteger uniqueContainerCounter = new AtomicInteger(1000000);

    /** the GIS map. */
    private MultiGisRenderable2d gisMap;

    /** Statistic for the number of containers in the model. */
    private SimCounter<Duration> containerCounter;

    /** Statistic for the number of vessels in the model. */
    private SimCounter<Duration> vesselCounter;

    /**
     * Create a port model.
     * @param simulator the simulator to use
     */
    public AbstractPortModel(final ClockDevsSimulatorInterface simulator)
    {
        super(simulator);
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        if (getSimulator() instanceof AnimatorInterface)
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

        this.containerCounter = new SimCounter<>("Generated containers", "Generated containers", this);
        this.containerCounter.initialize();
        this.vesselCounter = new SimCounter<>("Generated vessels", "Generated vessels", this);
        this.vesselCounter.initialize();
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

    public Terminal getTerminal(final String id)
    {
        return this.terminalMap.get(id);
    }

    @Override
    public int uniqueContainerNr()
    {
        this.containerCounter.register(1);
        return this.uniqueContainerCounter.incrementAndGet();
    }

    @Override
    public int uniqueVesselNr()
    {
        this.vesselCounter.register(1);
        return this.uniqueVesselCounter.incrementAndGet();
    }

    /**
     * @return containerCounter
     */
    public SimCounter<Duration> getContainerCounter()
    {
        return this.containerCounter;
    }

    /**
     * @return vesselCounter
     */
    public SimCounter<Duration> getVesselCounter()
    {
        return this.vesselCounter;
    }

    @Override
    public void setInteractive(final boolean interactive)
    {
        this.interactive = interactive;
    }

    @Override
    public boolean isInteractive()
    {
        return this.interactive;
    }

}
