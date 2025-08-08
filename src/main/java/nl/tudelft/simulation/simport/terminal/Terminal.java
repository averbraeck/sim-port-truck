package nl.tudelft.simulation.simport.terminal;

import java.util.ArrayList;
import java.util.List;

import org.djutils.base.Identifiable;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.vessel.VesselLoadInfo;

/**
 * Terminal.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Terminal implements Identifiable
{
    private final String id;

    private final PortModel model;

    private ModalSplit modalSplitImport;

    private ModalSplit modalSplitExport;

    private List<Container> truckContainersImport = new ArrayList<>();

    private List<Container> truckContainersExport = new ArrayList<>();

    private int teu = 0;

    private StreamInterface stream;

    /**
     * Create a new terminal for the port model
     * @param id the id of the terminal
     * @param model the port model
     * @param initialTEU initial number of TEU on terminal
     */
    public Terminal(final String id, final PortModel model, final int initialTEU)
    {
        this.id = id;
        this.model = model;
        this.model.addTerminal(this);
        this.teu = initialTEU;
        this.stream = model.getDefaultStream();
    }

    public void addImportContainers(final VesselLoadInfo vesselLoadInfo)
    {
        this.teu += vesselLoadInfo.callSizeTEU();
        var truckTEU = vesselLoadInfo.callSizeTEU() * this.modalSplitImport.getTruckFraction();
        for (int i = 0; i < Math.round(truckTEU); i++)
        {
            var container = new Container(this.model.uniqueContainerNr(),
                    this.stream.nextDouble() < vesselLoadInfo.fraction20ft() ? (byte) 20 : (byte) 40,
                    this.stream.nextDouble() < vesselLoadInfo.fractionEmpty(),
                    this.stream.nextDouble() < vesselLoadInfo.fractionReefer());
            this.truckContainersImport.add(container);
        }
    }

    public void removeExportContainers(final VesselLoadInfo vesselLoadInfo)
    {
        this.teu -= vesselLoadInfo.callSizeTEU();

    }

    /**
     * @return modalSplitImport
     */
    public ModalSplit getModalSplitImport()
    {
        return this.modalSplitImport;
    }

    /**
     * @param modalSplitImport set modalSplitImport
     */
    public void setModalSplitIn(final ModalSplit modalSplitImport)
    {
        this.modalSplitImport = modalSplitImport;
    }

    /**
     * @return modalSplitExport
     */
    public ModalSplit getModalSplitExport()
    {
        return this.modalSplitExport;
    }

    /**
     * @param modalSplitExport set modalSplitExport
     */
    public void setModalSplitOut(final ModalSplit modalSplitExport)
    {
        this.modalSplitExport = modalSplitExport;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
    }

    @Override
    public String toString()
    {
        return this.id;
    }

}
