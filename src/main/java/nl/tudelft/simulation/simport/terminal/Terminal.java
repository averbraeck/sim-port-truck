package nl.tudelft.simulation.simport.terminal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point3d;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.Container;
import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Terminal models a deepsea terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DeepseaTerminal implements PortFacility
{
    /** Terminal id. */
    private final String id;

    /** Pointer to the model. */
    private final PortModel model;

    /** Terminal latitude (y). */
    private final double lat;

    /** Terminal longitude (s). */
    private final double lon;

    /** Week pattern. If not present, 1/52 for each week. Key is coded as yyyyww, e.g., 202201. */
    private Map<Integer, Double> weekPatternMap = new LinkedHashMap<>();

    /** Day pattern. If not present, 1/7 for each day. Key is coded as 1 for Monday to 7 for Sunday (ISO-8601 standard). */
    private Map<Integer, Double> dayPatternMap = new LinkedHashMap<>();

    /** TEU capacity. */
    private int capacityTeu;

    /** Terminal gate. */
    private Gate gate;

    private ModalSplit modalSplitImport;

    private ModalSplit modalSplitExport;

    private List<Container> containersImport = new ArrayList<>();

    private List<Container> containersExport = new ArrayList<>();

    private List<Container> truckContainersImport = new ArrayList<>();

    private List<Container> truckContainersExport = new ArrayList<>();

    private int teu = 0;

    /**
     * Create a new terminal for the port model.
     * @param id the id of the terminal
     * @param model the port model
     * @param lat latitude
     * @param lon longitude
     */
    public DeepseaTerminal(final String id, final PortModel model, final double lat, final double lon)
    {
        this.id = id;
        this.model = model;
        this.model.addTerminal(this);
        this.lat = lat;
        this.lon = lon;
        if (model.isInteractive())
            new TerminalAnimation(this, model.getSimulator());
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

    @Override
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
    }

    @Override
    public Point3d getLocation()
    {
        return new Point3d(this.lon, this.lat, 1.0);
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
    public PortModel getModel()
    {
        return this.model;
    }

    @Override
    public DeepseaTerminal setGate(final Gate gate)
    {
        this.gate = gate;
        return this;
    }

    @Override
    public Gate getGate()
    {
        return this.gate;
    }

    @Override
    public double getLat()
    {
        return this.lat;
    }

    @Override
    public double getLon()
    {
        return this.lon;
    }

    @Override
    public DeepseaTerminal setCapacityTeu(final int capacityTeu)
    {
        this.capacityTeu = capacityTeu;
        return this;
    }

    @Override
    public int getCapacityTeu()
    {
        return this.capacityTeu;
    }

    @Override
    public String toString()
    {
        return this.id;
    }

}
