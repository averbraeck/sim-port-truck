package nl.tudelft.simulation.simport.vessel;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.container.ContainerStandard;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * Vessel models a container ship with an unloading plan and loading plan.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VesselStandard extends Vessel
{
    /** The call size information for unloading at the terminal. */
    private final VesselLoadInfo vesselUnloadInfo;

    /** The call size information for loading at the terminal. */
    private final VesselLoadInfo vesselLoadInfo;

    /** load container list. */
    private List<Container> loadList = new ArrayList<>();

    /** unloadload container list. */
    private List<Container> unloadList = new ArrayList<>();

    /**
     * Create a Vessel.
     * @param id the id of the ship
     * @param vesselType the vessel type
     * @param model the port model
     * @param eta estimated time of arrival
     * @param etd estimated time of departure
     * @param vesselUnloadInfo call size information for unloading at the terminal
     * @param vesselLoadInfo call size information for loading at the terminal
     * @param terminal the terminal to visit
     */
    public VesselStandard(final String id, final VesselType vesselType, final PortModel model, final ClockTime eta,
            final ClockTime etd, final VesselLoadInfo vesselUnloadInfo, final VesselLoadInfo vesselLoadInfo,
            final Terminal terminal)
    {
        super(id, vesselType, model, eta, etd, terminal);
        this.vesselLoadInfo = vesselLoadInfo;
        this.vesselUnloadInfo = vesselUnloadInfo;
        makeList(this.vesselLoadInfo, this.loadList);
        makeList(this.vesselUnloadInfo, this.unloadList);
    }

    protected void makeList(final VesselLoadInfo vli, final List<Container> ll)
    {
        // #cont = #teu / (2.0 - frac20), because c.f + 2.c.(1-f) = t => c = t / (2 - f)
        int nrContainers = (int) (vli.callSizeTEU() / (2.0 - vli.fraction20ft()));
        StreamInterface rng = getModel().getDefaultStream();
        for (int i = 0; i < nrContainers; i++)
        {
            byte size = rng.nextDouble() < vli.fraction20ft() ? (byte) 20 : (byte) 40;
            boolean empty = rng.nextDouble() < vli.fractionEmpty();
            boolean reefer = rng.nextDouble() < vli.fractionReefer();
            ll.add(new ContainerStandard(getModel().uniqueContainerNr(), size, empty, reefer));
        }
    }

    /*-
    protected void unloadContainers()
    {
        this.terminal.addImportContainers(this.unloadList);
    }

    protected void loadContainers()
    {
        this.terminal.removeExportContainers(this.loadList);
    }
    */

    /**
     * Return call size information for unloading at the terminal.
     * @return call size information for unloading at the terminal
     */
    public VesselLoadInfo getVesselLoadInfoUnloading()
    {
        return this.vesselUnloadInfo;
    }

    /**
     * Return call size information for loading at the terminal.
     * @return call size information for loading at the terminal
     */
    public VesselLoadInfo getVesselLoadInfoLoading()
    {
        return this.vesselLoadInfo;
    }

}
