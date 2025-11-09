package nl.tudelft.simulation.simport.terminal;

import java.util.Collection;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.simport.Container;
import nl.tudelft.simulation.simport.truck.Truck;
import nl.tudelft.simulation.simport.util.SimPortRuntimeException;

/**
 * Yard models the handling at the terminal. It is defined as an interface to start simple, and be able to expand the logic
 * later, e.g. to block groups or storage blocks.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface Yard extends Identifiable
{
    /** @return the port facility (terminal or depot) to which this yard belongs. */
    ContainerFacility getContainerFacility();

    /** @return a handling time for a truck dropping off an export container. */
    Duration getHandlingTimeExport();

    /** @return a handling time for a truck picking up an import container. */
    Duration getHandlingTimeImport();

    /** @return a handling time for a dual handling. */
    Duration getHandlingTimeDual();

    /** @return the containers in the yard, as a map from container number to container. */
    Map<Integer, Container> getContainerMap();

    /** @return the trucks at the yard. */
    Collection<Truck> getTrucks();

    /** @return the number of trucks at the yard. */
    default int nrOfTrucks()
    {
        return getTrucks().size();
    }

    /**
     * Have a truck pick up a container.
     * @param truck the truck
     * @param container the container to pick up
     */
    default void pickupContainer(final Truck truck, final Container container)
    {
        Throw.when(!truck.isEmpty(), SimPortRuntimeException.class, "Truck %s is not empty: carries container %s", truck,
                truck.getContainer());
        Throw.when(!getContainerMap().containsKey(container.getNr()), SimPortRuntimeException.class,
                "Container %s not found on yard of facility %s", truck, getContainerFacility());
        truck.loadContainer(container);
        getContainerMap().remove(container.getNr());
    }

    /**
     * Have a truck drop off its container.
     * @param truck the truck
     * @param container the container to drop off
     */
    void dropoffContainer(Truck truck);

}
