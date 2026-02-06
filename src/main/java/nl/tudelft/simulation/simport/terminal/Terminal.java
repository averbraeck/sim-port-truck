package nl.tudelft.simulation.simport.terminal;

import java.util.LinkedHashMap;
import java.util.Map;

import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.vessel.VesselGenerator;

/**
 * Terminal is a container facility that gets visits from short sea or deep sea vessels.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class Terminal extends AbstractContainerFacility
{
    /** The vessel generators for this terminal. */
    private Map<String, VesselGenerator> vesselgeneratorMap = new LinkedHashMap<>();

    /**
     * Create a new terminal for the port model.
     * @param id the id of the terminal
     * @param model the port model
     * @param lat latitude
     * @param lon longitude
     */
    public Terminal(final String id, final PortModel model, final double lat, final double lon)
    {
        super(id, model, lat, lon);
        model.addTerminal(this);
    }

    /**
     * Add a vessel generator to the terminal, and start scheduling arrivals of vessels.
     * @param vesselGenerator the new VesselGenerator to add
     */
    public void addVesselGenerator(final VesselGenerator vesselGenerator)
    {
        this.vesselgeneratorMap.put(vesselGenerator.getId(), vesselGenerator);
        vesselGenerator.start();
    }

    /**
     * Remove a vessel generator from the terminal, and stop scheduling arrivals of vessels.
     * @param vesselGenerator the VesselGenerator to remove
     * @return whether the removal was successful; false if it the generator was not found
     */
    public boolean removeVesselGenerator(final VesselGenerator vesselGenerator)
    {
        return removeVesselGenerator(vesselGenerator.getId());
    }

    /**
     * Remove a vessel generator from the terminal, and stop scheduling arrivals of vessels.
     * @param is the id of the VesselGenerator to remove
     * @return whether the removal was successful; false if it the generator was not found
     */
    public boolean removeVesselGenerator(final String id)
    {
        if (this.vesselgeneratorMap.containsKey(id))
        {
            var vesselGenerator = this.vesselgeneratorMap.remove(id);
            vesselGenerator.stop();
            return true;
        }
        return false;
    }

    /**
     * Return a map of the vessel generators for this terminal, based on their id.
     * @return a map of the vessel generators for this terminal
     */
    public Map<String, VesselGenerator> getVesselGeneratorMap()
    {
        return this.vesselgeneratorMap;
    }

}
