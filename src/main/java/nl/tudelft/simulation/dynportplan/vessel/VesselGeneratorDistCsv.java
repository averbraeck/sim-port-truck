package nl.tudelft.simulation.dynportplan.vessel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.djunits.unit.DurationUnit;
import org.djutils.io.URLResource;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import nl.tudelft.simulation.dynportplan.Terminal;
import nl.tudelft.simulation.dynportplan.model.PortModel;
import nl.tudelft.simulation.dynportplan.util.DistributionParser;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Create vessel visits based on parameters in CSV files.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VesselGeneratorDistCsv
{
    /**
     * Create VesselGenerators based on data from a csv-file.
     * @param model the simulation model with access to, e.g., terminals
     * @param terminalCsvPath path to the file with terminal parameters
     * @param vesselDistCsvPath path to the file with vessel distribution parameters
     */
    public static void generateVesselsCsv(final PortModel model, final String terminalCsvPath, final String vesselDistCsvPath)
    {
        InputStream terminalCsvStream = URLResource.getResourceAsStream(terminalCsvPath);
        try (CsvReader csvReader = CsvReader.builder().build(new InputStreamReader(terminalCsvStream)))
        {
            boolean first = true;
            for (CsvRow row : csvReader)
            {
                if (first)
                    first = false;
                else
                {
                    new Terminal(row.getField(0), model);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        InputStream vesseLDistCsvStream = URLResource.getResourceAsStream(vesselDistCsvPath);
        try (CsvReader csvReader = CsvReader.builder().build(new InputStreamReader(vesseLDistCsvStream)))
        {
            boolean first = true;
            for (CsvRow row : csvReader)
            {
                if (first)
                    first = false;
                else
                {
                    var terminal = model.getTerminal(row.getField(0));
                    DistContinuousDuration iatWd = parseDcd(row.getField(1), DurationUnit.HOUR, model.getDefaultStream());
                    DistContinuousDuration iatWe = parseDcd(row.getField(2), DurationUnit.HOUR, model.getDefaultStream());
                    DistDiscrete callSizeU = DistributionParser.parseDistDiscrete(row.getField(3), model.getDefaultStream());
                    DistDiscrete callSizeL = DistributionParser.parseDistDiscrete(row.getField(4), model.getDefaultStream());
                    double ft20FractionU = Double.parseDouble(row.getField(5));
                    double ft20FractionL = Double.parseDouble(row.getField(6));
                    double emptyFractionU = Double.parseDouble(row.getField(7));
                    double emptyFractionL = Double.parseDouble(row.getField(8));
                    double reeferFractionU = Double.parseDouble(row.getField(9));
                    double reeferFractionL = Double.parseDouble(row.getField(10));
                    // @formatter:off
                    var vesselGenerator = new VesselGeneratorDist("gen_" + terminal, model.getSimulator(), terminal)
                            .setShipIatWeekdays(iatWd)
                            .setShipIatWeekends(iatWe)
                            .setCallSizeDistLoading(callSizeL)
                            .setCallSizeDistUnloading(callSizeU)
                            .setFraction20ftLoading(ft20FractionL)
                            .setFraction20ftUnloading(ft20FractionU)
                            .setFractionEmptyLoading(emptyFractionL)
                            .setFractionEmptyUnloading(emptyFractionU)
                            .setFractionReeferLoading(reeferFractionL)
                            .setFractionReeferUnloading(reeferFractionU);
                    // @formatter:on
                    vesselGenerator.start();
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static DistContinuousDuration parseDcd(final String distStr, final DurationUnit unit, final StreamInterface stream)
    {
        DistContinuous dc = DistributionParser.parseDistContinuous(distStr, stream);
        return new DistContinuousDuration(dc, unit);
    }

}
