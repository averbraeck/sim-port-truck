package nl.tudelft.simulation.simport.ndw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
        File config = new File("E:/NDW/NDW-2024-01-08-config.zip");
        File data = new File("E:/NDW/NDW-2024-01-08-data.zip");

        var metadataMap = MeasurementSiteTableParser.parse(config);

        // 15-minute buckets, flow-weighted speed, bbox over NL (example coords)
        // 51.997346972229955, 3.964056685829537
        // 51.8647871885184, 4.337355551237825

        var bbox =
                new DailyDataProcessor.BoundingBox(51.8647871885184, 3.964056685829537, 51.997346972229955, 4.337355551237825);
        var proc = new DailyDataProcessor(metadataMap, java.time.Duration.ofMinutes(15).toMillis(), bbox, true);

        // 1) Sequential (baseline)
        try (var out = new BufferedWriter(new FileWriter("E:/NDW/seq.csv"), 1 << 20))
        {
            proc.processZipSequential(data, out);
        }

        // 2) Ordered parallel (barrier per bucket)
//        try (var out = new BufferedWriter(new FileWriter("E:/NDW/par.csv"), 1 << 20))
//        {
            // true argument for fastcsv
            // proc.processZipParallelOrderedAndStreamCsv(data, Runtime.getRuntime().availableProcessors(), out, true);
//            proc.processZipParallel(data, Runtime.getRuntime().availableProcessors(), out);
//        }
    }

}
