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
        var s = "empirical(0.0,0, 0.0483,1, 0.1665,482, 0.3626,1204, 0.5316,1926, 0.6548,2649, 0.7377,3371, 0.7917,4093, 0.8273,4816, "
                + "0.8516,5538, 0.8695,6260, 0.8862,6983, 0.9081,7705, 0.9359,8427, 0.9631,9150, 0.9819,9872, 0.9913,10594, 1,11798)";
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
