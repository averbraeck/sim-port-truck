package nl.tudelft.simulation.simport.model;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;

import nl.tudelft.simulation.dsol.model.DsolModel;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.freightforwarder.FreightForwarder;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * PortModel defines the important functions of a model for simulating (truck) traffic in a large container port.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface PortModel extends DsolModel<Duration, ClockDevsSimulatorInterface>, EventProducer
{
    /** A container has been delivered and is about to leave the model. */
    static EventType CONTAINER_EVENT = new EventType("CONTAINER_EVENT");

    /** A truck has been completed a trip and is about to dropoff the container. */
    static EventType TRUCK_EVENT = new EventType("TRUCK_EVENT");

    /** A daily terminal update event. */
    static EventType DAILY_TERMINAL_EVENT = new EventType("DAILY_TERMINAL_EVENT");

    /**
     * Return the standard random stream of the model.
     * @return the standard random stream of the model
     */
    StreamInterface getRandomStream();

    /**
     * Return the standard uniform distribution based on the standard random stream of the model.
     * @return the standard uniform distribution based on the standard random stream of the model
     */
    DistUniform getU01();

    /**
     * Get a unique container number.
     * @return a unique container number
     */
    int uniqueContainerNr();

    /**
     * Get a unique booking number.
     * @return a unique booking number
     */
    int uniqueBookingNr();

    /**
     * Get a unique vessel number.
     * @return a unique vessel number
     */
    int uniqueVesselNr();

    /**
     * Get a unique truck number.
     * @return a unique truck number
     */
    int uniqueTruckNr();

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

    /**
     * Get the map of terminals.
     * @return the map of terminals
     */
    Map<String, Terminal> getTerminalMap();

    /**
     * Return the correct Freight Forwarder. This can be dependent on the terminal and the vessel (e.g., the liner).
     * @param terminal the terminal that requests a freight forwarder
     * @param vessel the vessel for which a freight forwarder is requested
     */
    FreightForwarder getFreightForwarder(Terminal terminal, Vessel vessel);

    /**
     * Add a vessel to the model.
     * @param Vessel the vessel to add
     */
    void addVessel(Vessel vessel);

    /**
     * Get a vessel based on its id.
     * @param id the vessel id to search for
     * @return the vessel or null if it could not be found
     */
    Vessel getVessel(int id);

    /**
     * Get the map of vessels.
     * @return the map of vessels
     */
    Map<Integer, Vessel> getVesselMap();

}
