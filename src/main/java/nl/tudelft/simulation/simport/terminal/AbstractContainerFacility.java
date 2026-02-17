package nl.tudelft.simulation.simport.terminal;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point3d;
import org.djutils.event.LocalEventProducer;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * AbstractContainerFacility.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class AbstractContainerFacility extends LocalEventProducer implements ContainerFacility
{
    /** Facility id. */
    private final String id;

    /** Facility name. */
    private final String name;

    /** Pointer to the model. */
    private final PortModel model;

    /** Facility latitude (y). */
    private final double lat;

    /** Facility longitude (s). */
    private final double lon;

    /** TEU capacity. */
    private int capacityTeu;

    /** Terminal gate. */
    private Gate gate;

    /** Terminal yard for handling. */
    private Yard yard;

    /** Terminal statistics. */
    protected TerminalStatistics statistics;

    /**
     * Create a new container facility for the port model.
     * @param name the name of the facility
     * @param id the id of the container facility
     * @param model the port model
     * @param lat latitude
     * @param lon longitude
     */
    public AbstractContainerFacility(final String id, final String name, final PortModel model, final double lat,
            final double lon)
    {
        this.id = id;
        this.name = name;
        this.model = model;
        this.lat = lat;
        this.lon = lon;
        this.statistics = new TerminalStatistics(this, model.getSimulator());

        // schedule statistics reporting once a day.
        model.getSimulator().scheduleEventNow(() -> reportStatistics());
    }

    protected void reportStatistics()
    {
        getModel().fireEvent(PortModel.DAILY_TERMINAL_EVENT, this.statistics);
        getSimulator().scheduleEventRel(new Duration(1.0, DurationUnit.DAY), () -> reportStatistics());
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public TerminalStatistics getStatistics()
    {
        return this.statistics;
    }

    @Override
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
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
    public Point3d getLocation()
    {
        return new Point3d(this.lon, this.lat, 1.0);
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return new Bounds2d(0.0015, 0.0009);
    }

    @Override
    public PortModel getModel()
    {
        return this.model;
    }

    @Override
    public ContainerFacility setGate(final Gate gate)
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
    public ContainerFacility setYard(final Yard yard)
    {
        this.yard = yard;
        return this;
    }

    @Override
    public Yard getYard()
    {
        return this.yard;
    }

    @Override
    public ContainerFacility setCapacityTeu(final int capacityTeu)
    {
        this.capacityTeu = capacityTeu;
        return this;
    }

    @Override
    public int getCapacityTeu()
    {
        return this.capacityTeu;
    }

}
