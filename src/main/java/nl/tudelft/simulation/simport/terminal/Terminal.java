package nl.tudelft.simulation.simport.terminal;

import java.util.ArrayList;
import java.util.List;

import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point3d;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.Container;
import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Terminal.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Terminal implements Identifiable, Locatable
{
    private final String id;

    private final PortModel model;

    private ModalSplit modalSplitImport;

    private ModalSplit modalSplitExport;

    private List<Container> containersImport = new ArrayList<>();

    private List<Container> containersExport = new ArrayList<>();

    private List<Container> truckContainersImport = new ArrayList<>();

    private List<Container> truckContainersExport = new ArrayList<>();

    private int teu = 0;

    private final double x, y;

    private StreamInterface stream;

    /**
     * Create a new terminal for the port model
     * @param id the id of the terminal
     * @param model the port model
     * @param initialTEU initial number of TEU on terminal
     */
    public Terminal(final String id, final PortModel model, final double x, final double y, final int initialTEU)
    {
        this.id = id;
        this.model = model;
        this.model.addTerminal(this);
        this.teu = initialTEU;
        this.stream = model.getDefaultStream();
        this.x = x;
        this.y = y;
        if (model.getSimulator() instanceof AnimatorInterface)
        {
            new TerminalAnimation(this, model.getSimulator());
        }
    }

    public void addImportContainers(final List<Container> containerList)
    {
        StreamInterface rng = this.model.getDefaultStream();
        for (var container : containerList)
        {
            this.containersImport.add(container);
            var r = rng.nextDouble();
            if (r < this.modalSplitImport.cumulativeTruckFraction())
            {
                container.setMode(TransportMode.TRUCK);
                this.truckContainersImport.add(container);
            }
            else if (r < this.modalSplitImport.cumulativeBargeFraction())
                container.setMode(TransportMode.BARGE);
            else if (r < this.modalSplitImport.cumulativeRailFraction())
                container.setMode(TransportMode.RAIL);
            else
                container.setMode(TransportMode.SHORTSEA);
            this.teu += (container.getSize() > 20) ? 2 : 1;
        }
    }

    public void removeExportContainers(final List<Container> containerList)
    {
        for (var container : containerList)
        {
            this.teu -= (container.getSize() > 20) ? 2 : 1;
        }
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
    public Point3d getLocation()
    {
        return new Point3d(this.x, this.y, 1.0);
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return new Bounds2d(0.0015, 0.0009);
    }

    /**
     * @return containersImport
     */
    public List<Container> getContainersImport()
    {
        return this.containersImport;
    }

    /**
     * @param containersImport set containersImport
     */
    public void setContainersImport(final List<Container> containersImport)
    {
        this.containersImport = containersImport;
    }

    /**
     * @return containersExport
     */
    public List<Container> getContainersExport()
    {
        return this.containersExport;
    }

    /**
     * @return truckContainersImport
     */
    public List<Container> getTruckContainersImport()
    {
        return this.truckContainersImport;
    }

    /**
     * @return truckContainersExport
     */
    public List<Container> getTruckContainersExport()
    {
        return this.truckContainersExport;
    }

    /**
     * @return teu
     */
    public int getTeu()
    {
        return this.teu;
    }

    @Override
    public String toString()
    {
        return this.id;
    }

}
