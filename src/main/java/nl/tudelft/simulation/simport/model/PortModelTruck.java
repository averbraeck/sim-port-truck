package nl.tudelft.simulation.simport.model;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;

/**
 * PortModelTruck. is the base model to run..
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PortModelTruck extends AbstractPortModel
{

    /**
     * Create a port model instance for trucking.
     * @param simulator the simulator to use
     * @param interactive indicate whether the model is interactive and should load animation
     */
    public PortModelTruck(final ClockDevsSimulatorInterface simulator, final boolean interactive)
    {
        super(simulator, interactive);
    }

    @Override
    protected void extendInputParameterMap() throws InputParameterException
    {
        // no additions at the moment
    }

    @Override
    protected void buildTerminals()
    {
        readTerminalsFromCsv(getInputParameterString("terminal.TerminalDefinitionPath"));
    }

}
