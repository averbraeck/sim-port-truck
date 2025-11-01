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
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterLong;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterString;
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
        makeInputParameterMap();
    }

    /**
     * Make the initial input parameter map with the parameters that are used by the generic PortModel such as the name for the
     * experiment, the output path for the results (or write no results at all), and the need for animation. The method
     * extendInputParameterMap() is called at the end, enabling extensions of this abstract model to easily add parameters.
     */
    protected void makeInputParameterMap()
    {
        try
        {
            InputParameterMap root = this.inputParameterMap;
            InputParameterMap genericMap = new InputParameterMap("generic", "Generic", "Generic parameters", 1.0);
            root.add(genericMap);
            String inputPath = "/";
            genericMap.add(new InputParameterString("Name", "Model name", "Model name", "", 1.0));
            genericMap.add(new InputParameterString("Description", "Model description", "Model description", "", 2.0));
            genericMap.add(new InputParameterString("StartDate", "Model start date", "yyyy-MM-ddThh:mm:ss",
                    "2022-01-01T00:00:00", 3.0));
            genericMap.add(new InputParameterString("InputPath", "Input path", "Input path", inputPath, 4.0));
            genericMap.add(new InputParameterBoolean("WriteOutput", "Write output?", "Output writing on or off", true, 5.0));
            String outputPath = getExecutionPath();
            genericMap.add(new InputParameterString("OutputPath", "Output path", "Output path", outputPath, 6.0));
            genericMap.add(new InputParameterInteger("RunLengthDays", "Run length in days", "Run length in days", 365, 1, 3650,
                    "%d", 7.0));
            genericMap.add(new InputParameterLong("Seed", "Seed for the RNG", "Seed for the Random Number Generator", 111L, 1,
                    Long.MAX_VALUE, "%d", 8.0));

            InputParameterMap volumeMap = new InputParameterMap("volume", "Volume", "Volume parameters", 2.0);
            root.add(volumeMap);
            volumeMap.add(new InputParameterInteger("TEU", "Number of TEU per year", "Only for deepsea terminals", 10_000_000,
                    1, 100_000_000, "%d", 1.0));
            volumeMap.add(new InputParameterDouble("TEUFactor", "TEU-factor", "Average TEU-length of container", 1.7, 1.0, 2.0,
                    true, true, "%d", 2.0));
            volumeMap.add(new InputParameterDouble("ImportFraction", "Fraction import containers", "[0,1]", 0.5, 0.0, 1.0, true,
                    true, "%d", 3.0));
            volumeMap.add(new InputParameterDouble("EmptyFraction", "Fraction empty containers", "[0,1]", 0.2, 0.0, 1.0, true,
                    true, "%d", 4.0));
            volumeMap.add(new InputParameterDouble("DeepseaFraction", "Fraction deepsea containers", "[0,1]", 0.5, 0.0, 1.0,
                    true, true, "%d", 5.0));
            volumeMap.add(new InputParameterDouble("ShortseaTransshipFraction", "Fraction shortsea transshipment", "[0,1]", 0.9,
                    0.0, 1.0, true, true, "%d", 6.0));

            extendInputParameterMap();
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Extend the input parameters with extra tabs and parameters. The base parameters are already there.
     * @throws InputParameterException on not being able to find a key or submap
     */
    protected abstract void extendInputParameterMap() throws InputParameterException;

    /**
     * @return Execution Path
     */
    private String getExecutionPath()
    {
        String absolutePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
        absolutePath = absolutePath.replaceAll("%20", " ");
        return absolutePath;
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
