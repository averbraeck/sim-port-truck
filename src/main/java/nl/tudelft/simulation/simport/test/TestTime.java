package nl.tudelft.simulation.simport.test;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;

/**
 * TestTime.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestTime
{

    /**
     *
     */
    public TestTime()
    {
    }

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        var ct = ClockTime.ofIso("2025-07-27T13:24:17");
        System.out.println(ct);
        System.out.println(ct.dayOfWeek());
    }

}
