package nl.tudelft.simulation.simport.terminal;

import java.util.Map;

import nl.tudelft.simulation.simport.vessel.VesselGenerator;

/**
 * Terminal is a container facility that gets visits from short sea or deep sea vessels.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface Terminal extends ContainerFacility
{
    /**
     * Add a vessel generator to the terminal, and start scheduling arrivals of vessels.
     * @param vesselGenerator the new VesselGenerator to add
     */
    void addVesselGenerator(VesselGenerator vesselGenerator);

    /**
     * Remove a vessel generator from the terminal, and stop scheduling arrivals of vessels.
     * @param vesselGenerator the VesselGenerator to remove
     * @return whether the removal was successful; false if it the generator was not found
     */
    default boolean removeVesselGenerator(final VesselGenerator vesselGenerator)
    {
        return removeVesselGenerator(vesselGenerator.getId());
    }

    /**
     * Remove a vessel generator from the terminal, and stop scheduling arrivals of vessels.
     * @param is the id of the VesselGenerator to remove
     * @return whether the removal was successful; false if it the generator was not found
     */
    boolean removeVesselGenerator(String id);

    /**
     * Return a map of the vessel generators for this terminal, based on their id.
     * @return a map of the vessel generators for this terminal
     */
    Map<String, VesselGenerator> getVesselGeneratorMap();

}
