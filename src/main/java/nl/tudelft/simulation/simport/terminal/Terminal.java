package nl.tudelft.simulation.simport.terminal;

import org.djutils.base.Identifiable;

import nl.tudelft.simulation.simport.dsol.ClockSimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Terminal.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Terminal implements Identifiable
{
    private final String id;

    private final PortModel model;

    private ModalSplit modalSplitIn;

    private ModalSplit modalSplitOut;

    /**
     * Create a new terminal for the port model
     * @param id the id of the terminal
     * @param model the port model
     */
    public Terminal(final String id, final PortModel model)
    {
        this.id = id;
        this.model = model;
        this.model.addTerminal(this);
    }

    /**
     * @return modalSplitIn
     */
    public ModalSplit getModalSplitIn()
    {
        return this.modalSplitIn;
    }

    /**
     * @param modalSplitIn set modalSplitIn
     */
    public void setModalSplitIn(final ModalSplit modalSplitIn)
    {
        this.modalSplitIn = modalSplitIn;
    }

    /**
     * @return modalSplitOut
     */
    public ModalSplit getModalSplitOut()
    {
        return this.modalSplitOut;
    }

    /**
     * @param modalSplitOut set modalSplitOut
     */
    public void setModalSplitOut(final ModalSplit modalSplitOut)
    {
        this.modalSplitOut = modalSplitOut;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    public ClockSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
    }

    @Override
    public String toString()
    {
        return this.id;
    }

}
