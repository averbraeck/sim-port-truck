package nl.tudelft.simulation.simport.terminal;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.truck.Truck;

/**
 * The Gate simulates the process at the gate. Gate times and number of lanes can be constant, or fluctuate over the day.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface Gate extends Identifiable
{
    /** @return the port facility (terminal or depot) to which this gate belongs. */
    ContainerFacility getPortFacility();

    /** @return the current number of lanes into the terminal or depot. */
    int getCurrentLanesIn();

    /** @return the current number of lanes out of the terminal or depot. */
    int getCurrentLanesOut();

    /** @return the current handling time when entering the terminal or depot (can be drawn from a distribution). */
    Duration getCurrentHandlingTimeIn();

    /** @return the current handling time when leaving the terminal or depot (can be drawn from a distribution). */
    Duration getCurrentHandlingTimeOut();

    /** @return the queue of trucks that want to enter. */
    List<Truck> getTruckQueueIn();

    /** @return the queue of trucks that want to leave. */
    List<Truck> getTruckQueueOut();

    /**
     * Add a truck that wants to enter the terminal.
     * @param truck the truck that wants to enter
     */
    void addTruckIn(Truck truck);

    /**
     * Add a truck that wants to leave the terminal.
     * @param truck the truck that wants to leave
     */
    void addTruckOut(Truck truck);

    /** @return the simulator. */
    ClockDevsSimulatorInterface getSimulator();
}
