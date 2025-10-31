package nl.tudelft.simulation.simport.model;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.model.DsolModel;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * PortModel defines the important functions of a model for simulating (truck) traffic in a large container port.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface PortModel extends DsolModel<Duration, ClockDevsSimulatorInterface>
{
    /**
     * Get a unique container number.
     * @return a unique container number
     */
    int uniqueContainerNr();

    /**
     * Get a unique vessel number.
     * @return a unique vessel number
     */
    int uniqueVesselNr();

    /**
     * Indicate whether the simulation is interactive or not.
     * @param interactive to indicate whether the simulation is interactive or not
     */
    void setInteractive(boolean interactive);

    /**
     * Return whether the simulation is interactive or not.
     * @return whether the simulation is interactive or not
     */
    boolean isInteractive();

    /**
     * Add a terminal to the model.
     * @param Terminal the terminal to add
     */
    void addTerminal(Terminal terminal);

    /**
     * Get a terminal based on its id.
     * @param id the terminal id to search for
     * @return the terminal or null if it could not be found
     */
    Terminal getTerminal(String id);

}
