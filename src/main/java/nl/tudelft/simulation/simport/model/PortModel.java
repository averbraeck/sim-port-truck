package nl.tudelft.simulation.simport.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.model.AbstractDsolModel;
import nl.tudelft.simulation.simport.dsol.ClockSimulatorInterface;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * PortModel is an abstract 'parent' model with key objects such as the terminals and the road network.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class PortModel extends AbstractDsolModel<Duration, ClockSimulatorInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The terminals. */
    private final Map<String, Terminal> terminalMap = new LinkedHashMap<>();

    /**
     * Create a port model.
     * @param simulator the simulator to use
     */
    public PortModel(final ClockSimulatorInterface simulator)
    {
        super(simulator);
    }

    /**
     * Add a terminal to the model.
     * @param terminal the terminal to add
     */
    public void addTerminal(final Terminal terminal)
    {
        this.terminalMap.put(terminal.getId(), terminal);
    }

    /**
     * Get a terminal based on its id.
     * @param id the terminal id to search for
     * @return the terminal or null if it could not be found
     */
    public Terminal getTerminal(final String id)
    {
        return this.terminalMap.get(id);
    }

}
