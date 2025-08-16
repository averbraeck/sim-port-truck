package nl.tudelft.simulation.simport.ndw;

import java.io.File;

/**
 * TestReadNdwData.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestReadNdwData
{

    /**
     *
     */
    public TestReadNdwData()
    {
    }

    /**
     * @param args not used
     * @throws Exception on error
     */
    public static void main(final String[] args) throws Exception
    {
        File config = new File("E:/NDW/expert-avg-export-test-week1-ma-zo-config.zip");
        File data = new File("E:/NDW/expert-avg-export-test-week1-ma-zo-data.zip");

        var metadataMap = MeasurementSiteTableParser.parse(config);
        var dataProcessor = new DailyDataProcessor(metadataMap, 60 * 60 * 1000); // 1 hour
        dataProcessor.processZip(data);
    }

}
