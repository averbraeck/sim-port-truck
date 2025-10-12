package nl.tudelft.simulation.simport;

/**
 * TransportMode.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum TransportMode
{
    /** none = 0. */
    NONE,

    /** truck = 1. */
    TRUCK,

    /** barge = 2. */
    BARGE,

    /** rail = 3. */
    RAIL,

    /** short-sea / feeder = 4. */
    SHORTSEA;

    public byte asByte()
    {
        return (byte) (this.ordinal());
    }

    public static TransportMode fromByte(final byte transportModeNr)
    {
        return values()[transportModeNr];
    }
}
