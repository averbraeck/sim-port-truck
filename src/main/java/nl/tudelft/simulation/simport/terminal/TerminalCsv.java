package nl.tudelft.simulation.simport.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.djutils.io.URLResource;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Read the terminals from a CSV-file.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TerminalCsv
{
    /**
     * Read the terminals from a CSV file.
     * @param model the simulation model with access to, e.g., terminals
     * @param terminalCsvPath path to the file with terminal parameters
     */
    public static void readTerminals(final PortModel model, final String terminalCsvPath)
    {
        InputStream terminalCsvStream = URLResource.getResourceAsStream(terminalCsvPath);
        try (NamedCsvReader csvReader = NamedCsvReader.builder().build(new InputStreamReader(terminalCsvStream)))
        {
            for (NamedCsvRow row : csvReader)
            {
                var terminal = new Terminal(row.getField("id"), model);
                var bargeFraction = Double.parseDouble(row.getField("bargeIn"));
                var railFraction = Double.parseDouble(row.getField("railIn"));
                var truckFraction = Double.parseDouble(row.getField("truckIn"));
                terminal.setModalSplitIn(new ModalSplit(bargeFraction, railFraction, truckFraction));
                bargeFraction = Double.parseDouble(row.getField("bargeOut"));
                railFraction = Double.parseDouble(row.getField("railOut"));
                truckFraction = Double.parseDouble(row.getField("truckOut"));
                terminal.setModalSplitOut(new ModalSplit(bargeFraction, railFraction, truckFraction));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
