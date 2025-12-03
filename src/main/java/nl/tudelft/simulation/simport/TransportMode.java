package nl.tudelft.simulation.simport;

/**
 * TransportMode, e.g. into a terminal or out of a terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum TransportMode
{
    /** deepsea = 0. */
    DEEPSEA,

    /** feeder = 1. */
    FEEDER,

    /** truck = 2. */
    TRUCK,

    /** barge = 3. */
    BARGE,

    /** rail = 4. */
    RAIL;

    public byte asByte()
    {
        return (byte) (this.ordinal());
    }

    public static TransportMode of(final int transportModeNr)
    {
        return values()[transportModeNr];
    }

    public boolean isDeepsea()
    {
        return this.equals(DEEPSEA);
    }

    public boolean isFeeder()
    {
        return this.equals(FEEDER);
    }

    public boolean isTruck()
    {
        return this.equals(TRUCK);
    }

    public boolean isBarge()
    {
        return this.equals(BARGE);
    }

    public boolean isRail()
    {
        return this.equals(RAIL);
    }

}
