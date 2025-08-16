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

        // 15-minute buckets, flow-weighted speed, bbox over NL (example coords)
        // 51.997346972229955, 3.964056685829537
        // 51.8647871885184, 4.337355551237825

        // var bbox = new DailyDataProcessor.BoundingBox(51.8647871885184, 3.964056685829537, 51.997346972229955,
        // 4.337355551237825);
        var proc = new DailyDataProcessor(metadataMap, java.time.Duration.ofMinutes(15).toMillis(), true, 51.8647871885184,
                3.964056685829537, 51.997346972229955, 4.337355551237825);

        // Parallel is recommended
        proc.processZipParallel(data, Runtime.getRuntime().availableProcessors());

        // FastCSV write
        try (var out = new java.io.BufferedWriter(new java.io.FileWriter("E:/NDW/ndw_week01_15min.csv"), 1 << 20))
        {
            proc.writeCsvFastCsv(out);
        }
    }

}
