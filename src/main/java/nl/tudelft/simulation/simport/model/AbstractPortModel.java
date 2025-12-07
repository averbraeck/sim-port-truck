package nl.tudelft.simulation.simport.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.io.ResourceResolver;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.AbstractDsolModel;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterLocalDateTime;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterLong;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterString;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.terminal.TerminalStandard;

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

    /** the standard random stream of the model. */
    protected StreamInterface randomStream;

    /** the standard uniform distribution based on the standard random stream of the model. */
    protected DistUniform u01;

    /** The terminals. */
    private final Map<String, Terminal> terminalMap = new LinkedHashMap<>();

    /** the vessel counter. */
    private final AtomicInteger uniqueVesselNumber = new AtomicInteger(1000);

    /** the container counter. */
    private final AtomicInteger uniqueContainerNumber = new AtomicInteger(1000000);

    /** the booking counter. */
    private final AtomicInteger uniqueBookingNumber = new AtomicInteger(2000000);

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
            genericMap.add(new InputParameterLocalDateTime("StartDate", "Model start date", "yyyy-MM-ddThh:mm:ss",
                    LocalDateTime.parse("2024-07-01T00:00:00"), 3.0));
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
            volumeMap.add(new InputParameterBoolean("WeekPattern", "Use week/month pattern file?",
                    "If week/month pattern file, define below", false, 7.0));
            volumeMap.add(new InputParameterString("WeekPatternPath", "File path week/month pattern",
                    "File path weekpattern.csv", "", 8.0));
            volumeMap.add(new InputParameterBoolean("DayPattern", "Use day pattern file?", "If day pattern file, define below",
                    false, 9.0));
            volumeMap.add(new InputParameterString("DayPatternPath", "File path weekday pattern", "File path daypattern.csv",
                    "", 10.0));

            InputParameterMap terminalMap = new InputParameterMap("terminal", "Terminals", "Terminal parameters", 3.0);
            terminalMap.add(new InputParameterString("TerminalDefinitionPath", "File path terminal definitions",
                    "File path terminals.csv", "", 1.0));
            terminalMap.add(new InputParameterString("VolumePath", "File path terminal volumes",
                    "Volumes and call size csv file", "", 2.0));
            terminalMap.add(new InputParameterString("DepotDefinitionPath", "File path empty depot definitions",
                    "File path depots.csv", "", 2.5));
            terminalMap.add(new InputParameterBoolean("WeekPattern", "Use terminal week/month pattern file?",
                    "If week/month pattern file, define below", false, 3.0));
            terminalMap.add(new InputParameterString("WeekPatternPath", "File path terminal week/month pattern",
                    "File path terminal-weekpattern.csv", "", 4.0));
            terminalMap.add(new InputParameterBoolean("DayPattern", "Use terminal day pattern file?",
                    "If day pattern file, define below", false, 5.0));
            terminalMap.add(new InputParameterString("DayPatternPath", "File path terminal day pattern",
                    "File path terminal-daypattern.csv", "", 6.0));
            terminalMap.add(
                    new InputParameterBoolean("ContainerTypeOverride", "Use terminal override for DS/SS, I/E, 20/40, G/R, E/F?",
                            "If container type file, define below", false, 7.0));
            terminalMap.add(new InputParameterString("ContainerTypeOverridePath",
                    "File path for override container types per terminal", "File path terminal-containertype.csv", "", 8.0));
            root.add(terminalMap);

            InputParameterMap vesselMap = new InputParameterMap("vessel", "Vessels", "Vessel parameters", 4.0);
            vesselMap.add(new InputParameterDouble("CallSizeFraction", "Fraction to apply on call size", "1.0 is no change",
                    1.0, 0.1, 10.0, true, true, "%d", 1.0));
            vesselMap.add(new InputParameterInteger("CallSizeOverrideDS", "Replacement average callsize for deepsea vessels",
                    "-1 is no override", -1, 2.0));
            vesselMap.add(new InputParameterInteger("CallSizeOverrideSS", "Replacement average callsize for shortsea vessels",
                    "-1 is no override", -1, 3.0));
            vesselMap.add(new InputParameterString("DeviationATA", "Deviation distribution ATA in days",
                    "CONST(0.0) is no change", "TRIA(-2.0, 0.0, 2.0)", 4.0));
            root.add(vesselMap);

            InputParameterMap modalSplitMap = new InputParameterMap("modalsplit", "Modal split", "Modal split parameters", 5.0);
            modalSplitMap.add(new InputParameterDouble("Barge", "Global weight for barge modal split",
                    "(volume, percentage, fraction)", 34.0, 1.0));
            modalSplitMap.add(new InputParameterDouble("Truck", "Global weight for truck modal split",
                    "(volume, percentage, fraction)", 58.0, 2.0));
            modalSplitMap.add(new InputParameterDouble("Rail", "Global weight for rail modal split",
                    "(volume, percentage, fraction)", 8.0, 3.0));
            modalSplitMap.add(new InputParameterBoolean("TerminalOverride", "Modal split defined per deepsea terminal?",
                    "If terminal modal split override file, define below", false, 4.0));
            modalSplitMap.add(new InputParameterString("TerminalOverridePath", "File path modal split per terminal",
                    "File path to terminal-modalsplit.csv file", "", 5.0));
            root.add(modalSplitMap);

            InputParameterMap dwellTimeMap = new InputParameterMap("dwelltime", "Dwell time", "Dwell time parameters", 6.0);
            dwellTimeMap.add(new InputParameterString("fig", "Dwell time distribution full import general container",
                    "Distribution function (days)", "UNIF(1,7)", 1.0));
            dwellTimeMap.add(new InputParameterString("fir", "Dwell time distribution full import reefer container",
                    "Distribution function (days)", "UNIF(1,7)", 2.0));
            dwellTimeMap.add(new InputParameterString("feg", "Dwell time distribution full export general container",
                    "Distribution function (days)", "UNIF(1,7)", 3.0));
            dwellTimeMap.add(new InputParameterString("fer", "Dwell time distribution full export reefer container",
                    "Distribution function (days)", "UNIF(1,7)", 4.0));
            dwellTimeMap.add(new InputParameterString("ei", "Dwell time distribution empty import container",
                    "Distribution function (days)", "UNIF(1,2)", 5.0));
            dwellTimeMap.add(new InputParameterString("ee", "Dwell time distribution empty export container",
                    "Distribution function (days)", "UNIF(1,2)", 6.0));
            root.add(dwellTimeMap);

            InputParameterMap odMap = new InputParameterMap("od", "O/D", "O/D parameters", 6.0);
            odMap.add(new InputParameterString("FilePathHVM", "Origins and destinations file from HVM",
                    "(typically a centroids.dbf file)", "", 1.0));
            odMap.add(new InputParameterString("NetworkFolder", "Network folder with nodes, sections and turning shape files",
                    "(typically contains nodes, section, turning ESRI files)", "", 2.0));
            odMap.add(new InputParameterString("CongestionFolderHVM", "Folder with congestion data from HVM",
                    "(csv files per vehicle type and part of day)", "", 3.0));
            odMap.add(new InputParameterString("CongestionFolderNDW", "Folder with congestion data from NDW",
                    "(use either this file or the HVM file, not both)", "", 4.0));
            odMap.add(new InputParameterString("DetectorLoopFile", "File with detector loop locations (NDW and Port)",
                    "(can be blank - no loop detection)", "", 5.0));
            root.add(odMap);

            InputParameterMap truckMap = new InputParameterMap("truck", "Truck", "Truck parameters", 7.0);
            truckMap.add(new InputParameterString("SlotBookingDist", "Distribution of slot booking", "(hours before truck ETA)",
                    "CONST(24.0)", 1.0));
            truckMap.add(new InputParameterDouble("FractionCombi", "Fraction planned combi trips", "[0,1]", 0.4, 0.0, 1.0, true,
                    true, "%d", 2.0));
            truckMap.add(new InputParameterDouble("CombiSafetyMarginHours", "Safety margin between two trips when combi trip",
                    "[0,24]", 2.0, 0.0, 24.0, true, true, "%d", 3.0));
            root.add(truckMap);

            InputParameterMap planningMap = new InputParameterMap("planning", "Planning", "Planning parameters", 8.0);
            planningMap.add(new InputParameterString("FilePathTerminalSlots", "File with slot management regime per terminal",
                    "(on/off, window size, quick rebooking on/off)", "", 1.0));
            planningMap.add(new InputParameterDouble("PortAlertFraction", "Fraction of trucks with Port Alert", "[0,1]", 0.2,
                    0.0, 1.0, true, true, "%d", 2.0));
            root.add(planningMap);

            InputParameterMap disruptionMap = new InputParameterMap("disruption", "Disruption", "Disruption parameters", 9.0);
            disruptionMap.add(new InputParameterString("FilePathRoadDisruption", "File with road disruptions",
                    "(road location, direction, start time, duration)", "", 1.0));
            disruptionMap.add(new InputParameterString("FilePathTerminalDisruption", "File with terminal disruptions",
                    "(terminal, start time, fraction throughput, duration)", "", 2.0));
            disruptionMap.add(new InputParameterString("FilePathDepotDisruption", "File with depot disruptions",
                    "(depot, start time, fraction throughput, duration)", "", 3.0));
            root.add(disruptionMap);

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

    /**
     * Build the terminals, e.g. by reading them from a file.
     */
    protected abstract void buildTerminals();

    /**
     * Build the vessel generators, can be per terminal, or by reading vessel information from a file.
     */
    protected abstract void buildVesselGenerators();

    /**
     * Draw maps for the interactive version of the model.
     */
    protected abstract void drawMaps();

    @Override
    public void constructModel() throws SimRuntimeException
    {
        System.out.println("Used seed in the PortModel: " + getInputParameterLong("generic.Seed"));
        this.randomStream = new MersenneTwister(getInputParameterLong("generic.Seed") + 1L);
        this.streamInformation.addStream("default", new MersenneTwister(getInputParameterLong("generic.Seed")));
        this.u01 = new DistUniform(this.randomStream, 0.0, 1.0);

        buildTerminals();
        buildVesselGenerators();

        if (isInteractive())
        {
            drawMaps();
        }
    }

    /**
     * Read the terminals from a CSV file.
     * @param terminalCsvPath path to the file with terminal parameters
     */
    public void readTerminalsFromCsv(final String terminalCsvPath)
    {
        try (NamedCsvReader csvReader =
                NamedCsvReader.builder().build(new InputStreamReader(ResourceResolver.resolve(terminalCsvPath).openStream())))
        {
            for (NamedCsvRow row : csvReader)
            {
                var terminal = new TerminalStandard(row.getField("id"), this, Double.parseDouble(row.getField("lat")),
                        Double.parseDouble(row.getField("lon")));
                addTerminal(terminal);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* ***************************************************************************************************************** */
    /* ********************************************** GETTERS AND SETTERS ********************************************** */
    /* ***************************************************************************************************************** */

    @Override
    public StreamInterface getRandomStream()
    {
        return this.randomStream;
    }

    @Override
    public DistUniform getU01()
    {
        return this.u01;
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
    public Map<String, Terminal> getTerminalMap()
    {
        return this.terminalMap;
    }

    @Override
    public int uniqueContainerNr()
    {
        return this.uniqueContainerNumber.incrementAndGet();
    }

    @Override
    public int uniqueBookingNr()
    {
        return this.uniqueBookingNumber.incrementAndGet();
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
