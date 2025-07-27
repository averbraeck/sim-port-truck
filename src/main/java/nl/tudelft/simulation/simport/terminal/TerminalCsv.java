package nl.tudelft.simulation.simport.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.djutils.io.URLResource;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
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
    }

}
