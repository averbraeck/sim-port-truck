package nl.tudelft.simulation.simport.terminal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.truck.Truck;
import nl.tudelft.simulation.simport.util.SimPortRuntimeException;
import nl.tudelft.simulation.simport.vessel.Vessel;

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

    /**
     * @return the import containers for a Vessel in the yard, as a map from vessel to container. These are the unloaded
     *         containers from the Vessel, that will be transshipped or transported by one of the transport modes.
     */
    Map<Vessel, List<Container>> getImportVesselContainerMap();

    /**
     * @return the export containers for a Vessel in the yard, as a map from vessel to container. These are the containers that
     *         have to be loaded onto the Vessel, and came into the terminal with one of the transport modes (or transshipment).
     */
    Map<Vessel, List<Container>> getExportVesselContainerMap();

    /** @return the trucks at the yard. */
    Collection<Truck> getTrucks();

    /** @return the number of trucks at the yard. */
    default int nrOfTrucks()
    {
        return getTrucks().size();
    }

    /**
     * Add a container from a vessel to the yard.
     * @param vessel the vessel
     * @param container the container
     */
    default void dropoffContainer(final Vessel vessel, final Container container)
    {
        getContainerMap().put(container.getNr(), container);
    }

    /**
     * Have a truck pick up a container.
     * @param truck the truck
     * @param container the container to pick up
     */
    default void pickupContainer(final Truck truck, final Container container)
    {
        Throw.when(!truck.isEmpty(), SimPortRuntimeException.class, "Truck %s is not empty: it carries container %s", truck,
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
    default void dropoffContainer(final Truck truck)
    {
        Throw.when(truck.isEmpty(), SimPortRuntimeException.class, "Truck %s is empty: it does not carry a container", truck);
        Container container = truck.unloadContainer();
        getContainerMap().put(container.getNr(), container);
    }

}
