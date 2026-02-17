package nl.tudelft.simulation.simport.terminal;

import org.djutils.event.EventProducer;
import org.djutils.event.EventType;

import nl.tudelft.simulation.simport.Facility;

/**
 * ContainerFacility is the interface for deepsea and shortsea terminals, as well as empty depots.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface ContainerFacility extends Facility, EventProducer
{
    /** A daily terminal update event with the number of TEU as (day, number). */
    EventType getDailyYardTeuEventType();

    /** @return the name */
    String getName();

    /**
     * Set the gate for the terminal.
     * @param gate the gate for the terminal
     * @return the terminal for method chaining
     */
    ContainerFacility setGate(Gate gate);

    /** @return the gate. */
    Gate getGate();

    /**
     * Set the yard for the terminal.
     * @param yard the yard for the terminal
     * @return the terminal for method chaining
     */
    ContainerFacility setYard(Yard yard);

    /** @return the yard. */
    Yard getYard();

    /**
     * Set the capacity in TEU for the terminal.
     * @param capacityTeu the capacity in TEU for the terminal
     * @return the terminal for method chaining
     */
    ContainerFacility setCapacityTeu(int capacityTeu);

    /** @return the capacity in TEU. */
    int getCapacityTeu();

    /** @return the statistics object. */
    TerminalStatistics getStatistics();
}
