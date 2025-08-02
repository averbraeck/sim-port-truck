package nl.tudelft.simulation.simport.vessel;

/**
 * VesselLoadInfo contains information on the load plan or unload plan of a vessel.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param callSizeTEU the call size in TEU for (un)loading
 * @param fraction20ft the 20/40 ft ratio for (un)loading
 * @param fractionEmpty the fraction empty for (un)loading
 * @param fractionReefer the fraction reefers for (un)loading
 */
public record VesselLoadInfo(int callSizeTEU, double fraction20ft, double fractionEmpty, double fractionReefer)
{
}
