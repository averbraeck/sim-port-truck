package nl.tudelft.simulation.simport.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.util.SimPortRuntimeException;
import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * OutputWriter writes output of the simulation that it obtains through pub/sub to a number of output files.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OutputWriter implements EventListener
{
    /** the model. */
    private final PortModel model;

    /** the container writer. */
    private PrintWriter containerWriter;

    /** the truck writer. */
    private PrintWriter truckWriter;

    /** the terminal writer. */
    private PrintWriter terminalWriter;

    /** the loop detector writer. */
    private PrintWriter loopDetectorWriter;

    /** the tds writer. */
    private PrintWriter tdsWriter;

    /** the terminal queue writer. */
    private PrintWriter terminalQueueWriter;

    /** the terminal visit writer. */
    private PrintWriter terminalVisitWriter;

    /**
     * OutputWriter writes output of the simulation that it obtains through pub/sub to a number of output files.
     * @param model the model for pub/sub
     * @param outputPath the output path to which the filenames will be appended
     */
    public OutputWriter(final PortModel model, final String outputPath)
    {
        this.model = model;
        makeOutputDirectory(outputPath);

        try
        {
            this.containerWriter = new PrintWriter(outputPath + "/container.csv");
            writeContainerHeader();
            model.addListener(this, PortModel.CONTAINER_EVENT);
        }
        catch (IOException ioe)
        {
            throw new SimPortRuntimeException(ioe);
        }
    }

    /**
     * Make the output path + directory.
     * @param directory string; the full path to the output directory to create
     */
    private void makeOutputDirectory(final String directory)
    {
        // try to create directory
        File f = new File(directory);
        if (!f.exists())
        {
            try
            {
                if (!f.mkdirs())
                {
                    throw new Exception("Could not create directory for output: " + directory);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }
    }

    private void writeContainerHeader()
    {
        this.containerWriter.print("\"container_nr\"");
        this.containerWriter.print(",\"terminal_in\"");
        this.containerWriter.print(",\"vessel_in\"");
        this.containerWriter.print(",\"date_vessel_in\"");
        this.containerWriter.print(",\"terminal_out\"");
        this.containerWriter.print(",\"vessel_out\"");
        this.containerWriter.print(",\"date_vessel_out\"");
        this.containerWriter.print(",\"container_type\"");
        this.containerWriter.print(",\"empty_full\"");
        this.containerWriter.print(",\"location_chain\"");
        this.containerWriter.print(",\"direction\"");
        this.containerWriter.print(",\"transport_mode\"");
        this.containerWriter.println();
        this.containerWriter.flush();
    }

    private void writeContainerLine(final Container container)
    {
        int vesselInNr = container.getVesselInNr();
        Vessel vesselIn = vesselInNr < 0 ? null : getModel().getVesselMap().get(vesselInNr);
        int vesselOutNr = container.getVesselOutNr();
        Vessel vesselOut = vesselOutNr < 0 ? null : getModel().getVesselMap().get(vesselOutNr);
        this.containerWriter.print("\"" + container.getId() + "\"");
        this.containerWriter.print(",\"" + (vesselIn == null ? "" : vesselIn.getTerminal().getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselIn == null ? "" : vesselIn.getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselIn == null ? "" : vesselIn.getAta().ymdhm()) + "\"");
        this.containerWriter.print(",\"" + (vesselOut == null ? "" : vesselOut.getTerminal().getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselOut == null ? "" : vesselOut.getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselOut == null ? "" : vesselOut.getAta().ymdhm()) + "\"");
        this.containerWriter.print("\"" + container.getType() + "\"");
        this.containerWriter.print("\"" + (container.isEmpty() ? "E" : "F") + "\"");
        String locations = container.getLocations().toString();
        this.containerWriter.print(",\"" + locations + "\"");
        String direction = "domestic";
        if (vesselIn == null && vesselOut != null)
            direction = "export";
        else if (vesselIn != null && vesselOut == null)
            direction = "import";
        else if (vesselIn != null && vesselOut != null)
            direction = "transshipment";
        this.containerWriter.print(",\"" + direction + "\"");
        String transportMode = "TRANSSHIP";
        if (locations.contains("TRUCK"))
            transportMode = "TRUCK";
        else if (locations.contains("BARGE"))
            transportMode = "BARGE";
        else if (locations.contains("RAIL"))
            transportMode = "RAIL";
        this.containerWriter.print(",\"" + transportMode + "\"");
        this.containerWriter.println();
        this.containerWriter.flush();
    }

    /**
     * Return the model.
     * @return the model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /* ****************************************** NOTIFY ******************************************** */

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(PortModel.CONTAINER_EVENT))
        {
            writeContainerLine((Container) event.getContent());
        }
    }
}
