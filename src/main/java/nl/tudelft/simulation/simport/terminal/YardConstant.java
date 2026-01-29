package nl.tudelft.simulation.simport.terminal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.truck.Truck;
import nl.tudelft.simulation.simport.vessel.Vessel;

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

    /**
     * The import containers for a Vessel in the yard, as a map from vessel to container. These are the unloaded containers from
     * the Vessel, that will be transshipped or transported by one of the transport modes.
     */
    Map<Vessel, List<Container>> importVesselContainerMap = new LinkedHashMap<>();

    /**
     * The export containers for a Vessel in the yard, as a map from vessel to container. These are the containers that have to
     * be loaded onto the Vessel, and came into the terminal with one of the transport modes (or transshipment).
     */
    Map<Vessel, List<Container>> exportVesselContainerMap = new LinkedHashMap<>();

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

    /**
     * Set the distribution for the handling time for dropping off an export container.
     * @param handlingTimeExportDist the distribution for the handling time for dropping off an export container
     */
    public void setHandlingTimeExportDist(final DistContinuousDuration handlingTimeExportDist)
    {
        this.handlingTimeExportDist = handlingTimeExportDist;
    }

    /**
     * Set the distribution for the handling time for picking up an import container.
     * @param handlingTimeImportDist the distribution for the handling time for picking up an import container
     */
    public void setHandlingTimeImportDist(final DistContinuousDuration handlingTimeImportDist)
    {
        this.handlingTimeImportDist = handlingTimeImportDist;
    }

    /**
     * Set the distribution for the handling time for a dual move (dropping off and picking up a container).
     * @param handlingTimeDualDist the distribution for the handling time for a dual move (dropping off and picking up a
     *            container)
     */
    public void setHandlingTimeDualDist(final DistContinuousDuration handlingTimeDualDist)
    {
        this.handlingTimeDualDist = handlingTimeDualDist;
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
    public Map<Vessel, List<Container>> getImportVesselContainerMap()
    {
        return this.importVesselContainerMap;
    }

    @Override
    public Map<Vessel, List<Container>> getExportVesselContainerMap()
    {
        return this.exportVesselContainerMap;
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
