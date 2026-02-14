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
    /** fraction truck. */
    private final double truckFraction;

    /** fraction barge. */
    private final double bargeFraction;

    /** fraction rail. */
    private final double railFraction;

    /**
     * Create a modal split. The fractions will be normalized, so they can be given in TEU, as a percentage, or as a fraction.
     * @param truckFraction fraction truck
     * @param bargeFraction fraction barge
     * @param railFraction fraction rail
     * @param shortSeaFraction fraction short sea / feeder
     */
    public ModalSplit(final double truckFraction, final double bargeFraction, final double railFraction)
    {
        var sum = bargeFraction + railFraction + truckFraction;
        this.bargeFraction = bargeFraction / sum;
        this.railFraction = railFraction / sum;
        this.truckFraction = truckFraction / sum;
    }

    /**
     * @return truckFraction
     */
    public double getTruckFraction()
    {
        return this.truckFraction;
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
     * @return cumulative truckFraction
     */
    public double cumulativeTruckFractionTBR()
    {
        return this.truckFraction;
    }

    /**
     * @return cumulative bargeFraction
     */
    public double cumulativeBargeFractionTBR()
    {
        return Math.min(1.0, this.truckFraction + this.bargeFraction);
    }

    /**
     * @return cumulative railFraction
     */
    public double cumulativeRailFractionTBR()
    {
        return 1.0;
    }


}
