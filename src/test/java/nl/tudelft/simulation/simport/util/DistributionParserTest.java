package nl.tudelft.simulation.simport.util;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * DistributionParserTest.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DistributionParserTest
{

    /** */
    public DistributionParserTest()
    {
        var stream = new MersenneTwister(100L);
        var s = "empirical(0.0,0, 0.0247,1, 0.141,482, 0.3393,1204, 0.5135,1926, 0.6406,2649, 0.7256,3371, 0.7813,4093, 0.8185,4816, "
                + "0.844,5538, 0.8628,6260, 0.8805,6983, 0.9035,7705, 0.9327,8427, 0.9611,9150, 0.9808,9872, 0.9908,10594, 1,11798))";
        var dist = DistributionParser.parseDistContinuous(s, stream);
        for (int i = 0; i < 1000; i++)
            System.out.println((int) dist.draw());
    }

    /**
     * @param args none
     */
    public static void main(final String[] args)
    {
        new DistributionParserTest();
    }

}
