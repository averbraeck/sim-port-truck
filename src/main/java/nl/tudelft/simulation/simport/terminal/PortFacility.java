package nl.tudelft.simulation.simport.terminal;

import org.djutils.base.Identifiable;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * PortFacility is the interface for deepsea and shortsea terminals, as well as empty depots.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface PortFacility extends Identifiable, Locatable
{
    /** @return the simulator. */
    ClockDevsSimulatorInterface getSimulator();

    /** @return the model. */
    PortModel getModel();

    /**
     * Set the gate for the terminal.
     * @param gate the gate for the terminal
     * @return the terminal for method chaining
     */
    PortFacility setGate(Gate gate);

    /** @return the gate. */
    Gate getGate();

    /** @return the latitude. */
    double getLat();

    /** @return the longitude. */
    double getLon();

    /**
     * Set the capacity in TEU for the terminal.
     * @param capacityTeu the capacity in TEU for the terminal
     * @return the terminal for method chaining
     */
    PortFacility setCapacityTeu(int capacityTeu);

    /** @return the capacity in TEU. */
    int getCapacityTeu();

}
