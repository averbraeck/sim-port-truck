package nl.tudelft.simulation.simport.vessel;

import org.djutils.base.Identifiable;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * VesselGenerator generates visits for Vessels in the Port. The class is abstract, so deriving classes can work based on visit
 * data from files, or visits based on a random distribution. The generator generates vessels for one Terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class VesselGenerator implements Identifiable
{
    /** The generator's id. */
    private final String id;

    /** The simulator. */
    private final PortModel model;

    /** The terminal to generate ships for. */
    private final Terminal terminal;

    /** The vessel type to generate. */
    private final VesselType vesselType;

    /**
     * Create a VesselGenerator for one Terminal.
     * @param id the generator's id
     * @param model the model
     * @param terminal the terminal to generate ships for
     * @param vesselType the vessel type to generate
     */
    public VesselGenerator(final String id, final PortModel model, final Terminal terminal, final VesselType vesselType)
    {
        this.id = id;
        this.model = model;
        this.terminal = terminal;
        this.vesselType = vesselType;
    }

    /**
     * Generate a vessel with the (un)loading information.
     * @param eta the ETA of the vessel
     * @param etd the ETD of the vessel
     */
    public void generateVessel(final ClockTime eta, final ClockTime etd)
    {
        Vessel vessel = new Vessel(this.vesselType, this.model, eta, etd, this.terminal);
        getModel().addVessel(vessel);
    }

    /**
     * Start the generator.
     */
    public abstract void start();

    /**
     * Stop the generator.
     */
    public abstract void stop();

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * @return vesselType
     */
    public VesselType getVesselType()
    {
        return this.vesselType;
    }

    /**
     * Return the simulator.
     * @return the simulator
     */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
    }

    /**
     * Return the model.
     * @return the model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /**
     * Return the terminal to generate ships for.
     * @return the terminal to generate ships for
     */
    public Terminal getTerminal()
    {
        return this.terminal;
    }

}
