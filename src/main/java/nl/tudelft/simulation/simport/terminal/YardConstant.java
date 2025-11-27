package nl.tudelft.simulation.simport.terminal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.truck.Truck;

/**
 * YardConstant is an implementation of the Yard interface with constant parameters.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class YardConstant implements Yard
{
    /** The terminal or depot to which the yard belongs. */
    private final ContainerFacility portFacility;

    /** the (sub) id for the yard. */
    private final String id;

    /** handling time distribution for export. */
    private DistContinuousDuration handlingTimeExportDist;

    /** handling time distribution for import. */
    private DistContinuousDuration handlingTimeImportDist;

    /** handling time distribution for dual visit. */
    private DistContinuousDuration handlingTimeDualDist;

    /** Map with containers in the yard (Container number -> Container). */
    private final Map<Integer, Container> containerMap = new LinkedHashMap<>();

    /** Set of trucks on the yard. */
    private Collection<Truck> trucks = new LinkedHashSet<Truck>();

    /**
     * Create a yard for a terminal with constant stack size and dwell times.
     * @param portFacility the terminal or depot
     * @param id the unique id within the portFacility; will be appended to the terminal id for display.
     */
    public YardConstant(final ContainerFacility portFacility, final String id)
    {
        this.portFacility = portFacility;
        this.id = id;
    }

    @Override
    public Duration getHandlingTimeExport()
    {
        return this.handlingTimeExportDist.draw();
    }

    @Override
    public Duration getHandlingTimeImport()
    {
        return this.handlingTimeImportDist.draw();
    }

    @Override
    public Duration getHandlingTimeDual()
    {
        return this.handlingTimeDualDist.draw();
    }

    @Override
    public Map<Integer, Container> getContainerMap()
    {
        return this.containerMap;
    }

    @Override
    public Collection<Truck> getTrucks()
    {
        return this.trucks;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public ContainerFacility getContainerFacility()
    {
        return this.portFacility;
    }

    @Override
    public String toString()
    {
        return "Yard [id=" + getContainerFacility().getId() + "." + this.id + "]";
    }

}
