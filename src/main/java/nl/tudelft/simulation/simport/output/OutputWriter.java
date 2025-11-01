package nl.tudelft.simulation.simport.output;

import java.io.File;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

import nl.tudelft.simulation.simport.model.PortModel;

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

    /**
     * OutputWriter writes output of the simulation that it obtains through pub/sub to a number of output files.
     * @param model the model for pub/sub
     * @param outputPath the output path to which the filenames will be appended
     */
    public OutputWriter(final PortModel model, final String outputPath)
    {
        this.model = model;
        makeOutputDirectory(outputPath);
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
    }
}
