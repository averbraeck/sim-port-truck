package nl.tudelft.simulation.simport.vessel;

import org.djutils.base.Identifiable;

import nl.tudelft.simulation.simport.clocktime.ClockTime;
import nl.tudelft.simulation.simport.dsol.ClockSimulatorInterface;
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

    /**
     * Create a VesselGenerator for one Terminal.
     * @param id the generator's id
     * @param model the model
     * @param terminal the terminal to generate ships for
     */
    public VesselGenerator(final String id, final PortModel model, final Terminal terminal)
    {
        this.id = id;
        this.model = model;
        this.terminal = terminal;
    }

    /**
     * Generate a vessel with the (un)loading information.
     * @param id the id of the ship
     * @param eta the ETA of the vessel
     * @param etd the ETD of the vessel
     * @param unloadInfo the info for containers to unload
     * @param loadInfo the info for containers to load
     */
    public void generateVessel(final String id, final ClockTime eta, final ClockTime etd, final VesselLoadInfo unloadInfo,
            final VesselLoadInfo loadInfo)
    {
        new Vessel(id, this.model, eta, etd, unloadInfo, loadInfo, this.terminal);
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Return the simulator.
     * @return the simulator
     */
    public ClockSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
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
