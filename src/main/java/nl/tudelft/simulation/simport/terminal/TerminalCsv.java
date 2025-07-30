package nl.tudelft.simulation.simport.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.djutils.io.URLResource;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

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
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new InputStreamReader(terminalCsvStream)))
        {
            Map<String, String> row;
            while ((row = reader.readMap()) != null)
            {
                var terminal = new Terminal(row.get("id"), model);
                var bargeFraction = Double.parseDouble(row.get("bargeIn"));
                var railFraction = Double.parseDouble(row.get("railIn"));
                var truckFraction = Double.parseDouble(row.get("truckIn"));
                terminal.setModalSplitIn(new ModalSplit(bargeFraction, railFraction, truckFraction));
                bargeFraction = Double.parseDouble(row.get("bargeOut"));
                railFraction = Double.parseDouble(row.get("railOut"));
                truckFraction = Double.parseDouble(row.get("truckOut"));
                terminal.setModalSplitOut(new ModalSplit(bargeFraction, railFraction, truckFraction));
            }
        }
        catch (IOException | CsvException e)
        {
            throw new RuntimeException(e);
        }
    }

}
