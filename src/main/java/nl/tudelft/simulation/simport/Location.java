package nl.tudelft.simulation.simport;

/**
 * Location, e.g. a terminal, truck or out of a terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum Location
{
    /** unused = 0. */
    UNUSED,

    /** deepsea = 1. */
    DEEPSEA,

    /** feeder = 2. */
    FEEDER,

    /** rail = 3. */
    RAIL,

    /** barge = 4. */
    BARGE,

    /** truck = 5. */
    TRUCK,

    /** truck = 6. */
    TERMINAL,

    /** truck = 7. */
    DEPOT,

    /** truck = 8. */
    HINTERLAND,

    /** truck = 9. */
    PORT;

    public byte asByte()
    {
        return (byte) (this.ordinal());
    }

    public static Location of(final int transportModeNr)
    {
        return values()[transportModeNr];
    }

    public boolean isUnused()
    {
        return this.equals(UNUSED);
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

    public boolean isTerminal()
    {
        return this.equals(TERMINAL);
    }

    public boolean isDepot()
    {
        return this.equals(DEPOT);
    }

    public boolean isHinterland()
    {
        return this.equals(HINTERLAND);
    }

    public boolean isPort()
    {
        return this.equals(PORT);
    }

}
