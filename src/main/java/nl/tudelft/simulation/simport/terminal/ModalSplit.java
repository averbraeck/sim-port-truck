package nl.tudelft.simulation.simport.terminal;

/**
 * ModalSplit models the split between modes of transport.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ModalSplit
{
    /** fraction barge. */
    private final double bargeFraction;

    /** fraction rail. */
    private final double railFraction;

    /** fraction truck. */
    private final double truckFraction;

    /**
     * Create a modal split. The fractions will be normalized, so they can be given in TEU, as a percentage, or as a fraction.
     * @param bargeFraction fraction barge
     * @param railFraction fraction rail
     * @param truckFraction fraction truck
     */
    public ModalSplit(final double bargeFraction, final double railFraction, final double truckFraction)
    {
        var sum = bargeFraction + railFraction + truckFraction;
        this.bargeFraction = bargeFraction / sum;
        this.railFraction = railFraction / sum;
        this.truckFraction = truckFraction / sum;
    }

    /**
     * @return bargeFraction
     */
    public double getBargeFraction()
    {
        return this.bargeFraction;
    }

    /**
     * @return railFraction
     */
    public double getRailFraction()
    {
        return this.railFraction;
    }

    /**
     * @return truckFraction
     */
    public double getTruckFraction()
    {
        return this.truckFraction;
    }

}
