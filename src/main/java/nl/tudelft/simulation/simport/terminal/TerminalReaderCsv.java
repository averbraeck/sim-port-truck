package nl.tudelft.simulation.simport.terminal;

import java.io.IOException;
import java.io.InputStreamReader;

import org.djutils.io.ResourceResolver;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Read the terminals from a CSV-file. TODO: Clarify 'In' and 'Out'; standardize on import and export seen from the container.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TerminalReaderCsv
{
    /**
     * Read the terminals from a CSV file.
     * @param model the simulation model with access to, e.g., terminals
     * @param terminalCsvPath path to the file with terminal parameters
     */
    public static void readTerminals(final PortModel model, final String terminalCsvPath)
    {
        try (NamedCsvReader csvReader =
                NamedCsvReader.builder().build(new InputStreamReader(ResourceResolver.resolve(terminalCsvPath).openStream())))
        {
            for (NamedCsvRow row : csvReader)
            {
                var terminal = new TerminalStandard(row.getField("id"), model, Double.parseDouble(row.getField("lon")),
                        Double.parseDouble(row.getField("lat")));
                var truckFraction = Double.parseDouble(row.getField("truckIn"));
                var bargeFraction = Double.parseDouble(row.getField("bargeIn"));
                var railFraction = Double.parseDouble(row.getField("railIn"));
                var shortSeaFraction = Double.parseDouble(row.getField("shortSeaIn"));
                terminal.setModalSplitExport(new ModalSplit(truckFraction, bargeFraction, railFraction, shortSeaFraction));
                truckFraction = Double.parseDouble(row.getField("truckOut"));
                bargeFraction = Double.parseDouble(row.getField("bargeOut"));
                railFraction = Double.parseDouble(row.getField("railOut"));
                shortSeaFraction = Double.parseDouble(row.getField("shortSeaOut"));
                terminal.setModalSplitImport(new ModalSplit(truckFraction, bargeFraction, railFraction, shortSeaFraction));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
