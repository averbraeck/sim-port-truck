package nl.tudelft.simulation.simport.vessel;

import java.io.IOException;
import java.io.InputStreamReader;

import org.djunits.unit.DurationUnit;
import org.djutils.io.ResourceResolver;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.util.DistributionParser;

/**
 * Create vessel visits based on parameters in CSV files.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ReadVesselDistCsv
{
    /**
     * Create VesselGenerators based on data from a csv-file.
     * @param model the simulation model with access to, e.g., terminals
     * @param vesselDistCsvPath path to the file with vessel distribution parameters
     */
    public static void readVesselDist(final PortModel model, final String vesselDistCsvPath)
    {
        try (NamedCsvReader csvReader =
                NamedCsvReader.builder().build(new InputStreamReader(ResourceResolver.resolve(vesselDistCsvPath).openStream())))
        {
            for (NamedCsvRow row : csvReader)
            {
                var terminal = model.getTerminal(row.getField("terminal"));
                DistContinuousDuration iatWd = DistributionParser.parseDistContinuousDuration(row.getField("iatDistWeekdays"),
                        DurationUnit.HOUR, model.getDefaultStream());
                DistContinuousDuration iatWe = DistributionParser.parseDistContinuousDuration(row.getField("iatDistWeekends"),
                        DurationUnit.HOUR, model.getDefaultStream());
                DistDiscrete callSizeU =
                        DistributionParser.parseDistDiscrete(row.getField("callSizeDistUnloading"), model.getDefaultStream());
                DistDiscrete callSizeL =
                        DistributionParser.parseDistDiscrete(row.getField("callSizeDistLoading"), model.getDefaultStream());
                double ft20FractionU = Double.parseDouble(row.getField("20ftFractionUnloading"));
                double ft20FractionL = Double.parseDouble(row.getField("20ftFractionLoading"));
                double emptyFractionU = Double.parseDouble(row.getField("emptyFractionUnloading"));
                double emptyFractionL = Double.parseDouble(row.getField("emptyFractionLoading"));
                double reeferFractionU = Double.parseDouble(row.getField("reeferFractionUnloading"));
                double reeferFractionL = Double.parseDouble(row.getField("reeferFractionLoading"));
                var vesselType = VesselType.DEEPSEA; // TODO: read DS/SS from file
                // @formatter:off
                var vesselGenerator = new VesselGeneratorDist("gen_" + terminal, model, terminal, vesselType)
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
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
