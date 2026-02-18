package nl.tudelft.simulation.simport.terminal;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point3d;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.network.Centroid;
import nl.tudelft.simulation.simport.network.RoadLink;
import nl.tudelft.simulation.simport.util.SimPortRuntimeException;

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

    /** Centroid associated with the terminal. */
    private Centroid centroid;

    /** RoadLink closest to the gate-in. */
    private RoadLink roadLinkGateIn;

    /** RoadLink closest to the gate-out. */
    private RoadLink roadLinkGateOut;

    /** TEU capacity. */
    private int capacityTeu;

    /** Terminal gate. */
    private Gate gate;

    /** Terminal yard for handling. */
    private Yard yard;

    /** Terminal statistics. */
    protected final TerminalStatistics statistics;

    /** Event type for daily TEU statistics. */
    private final EventType dailyYardTeuEventType;

    /** The centroids of the destinations and weight for this container facility. */
    private Map<String, Double> terminalDestinations;

    /** The cumulative probabilities for destination centroids. */
    private NavigableMap<Double, Centroid> destinationProbabilities = new TreeMap<>();

    /** The centroids of the origins and weight for this container facility. */
    private Map<String, Double> terminalOrigins;

    /** The cumulative probabilities for origin centroids. */
    private NavigableMap<Double, Centroid> originProbabilities = new TreeMap<>();

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
        this.dailyYardTeuEventType = new EventType("DAILY_YARD_TEU_EVENT_TYPE_" + id);

        // schedule statistics reporting once a day.
        model.getSimulator().scheduleEventNow(() -> reportStatistics());

        // totals 1 second before end of simulation
        model.getSimulator().scheduleEventRel(model.getSimulator().getReplication().getEndTime().minus(Duration.ofSI(1.0)),
                () -> getModel().fireEvent(PortModel.TOTAL_TERMINAL_EVENT, this.statistics));
    }

    protected void reportStatistics()
    {
        if (this.statistics.getWarmupTime() != null)
        {
            getModel().fireEvent(PortModel.DAILY_TERMINAL_EVENT, this.statistics);
            double day = Math.round(
                    getSimulator().getSimulatorClockTime().minus(this.statistics.getWarmupTime()).getInUnit(DurationUnit.DAY));
            fireEvent(getDailyYardTeuEventType(), new double[] {day, this.statistics.getTotal().getNrTeuTotal()});
        }
        this.statistics.resetPeriodicStatistics();
        getSimulator().scheduleEventRel(new Duration(1.0, DurationUnit.DAY), () -> reportStatistics());
    }

    /**
     * Set the O/D matrix for this terminal. The method should be called AFTER the network has been read and the centroids have
     * been set for the terminal.
     */
    public void setTerminalOD()
    {
        // get the row (origins) and column (destinations) for the terminal centroids
        var od = getModel().getRoadNetwork().getOdMatrix();
        var centroid = getCentroid();
        Throw.when(!od.containsOrigin(centroid.getEid()), SimPortRuntimeException.class,
                "OD does not containn terminal origin %s for %s", centroid.getEid(), this);
        Throw.when(!od.containsDestination(centroid.getEid()), SimPortRuntimeException.class,
                "OD does not containn terminal destination %s for %s", centroid.getEid(), this);
        Map<String, Double> terminalDestinations = od.getAllDestinationsForOrigin(centroid.getEid());
        Map<String, Double> terminalOrigins = od.getAllOriginsForDestination(centroid.getEid());
        setTerminalDestinations(terminalDestinations);
        setTerminalOrigins(terminalOrigins);
    }

    protected void makeCumulativeProbabilities(final NavigableMap<Double, Centroid> probabilities,
            final Map<String, Double> weights)
    {
        var centroidMap = getModel().getRoadNetwork().getCentroidMap();
        probabilities.clear();
        probabilities.put(0.0, null);

        double sum = 0.0;
        for (var centroidName : weights.keySet())
        {
            if (centroidMap.containsKey(centroidName))
                sum += weights.get(centroidName);
        }

        double cumulative = 0.0;
        for (var centroidName : weights.keySet())
        {
            if (!centroidMap.containsKey(centroidName))
                continue;
            double weight = weights.get(centroidName);
            if (weight == 0.0)
                continue;
            cumulative += weight;
            probabilities.put(cumulative / sum, centroidMap.get(centroidName));
        }
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
    public EventType getDailyYardTeuEventType()
    {
        return this.dailyYardTeuEventType;
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
        Throw.whenNull(gate, "gate");
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
        Throw.whenNull(yard, "yard");
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

    /**
     * @return centroid
     */
    public Centroid getCentroid()
    {
        return this.centroid;
    }

    /**
     * @param centroid set centroid
     */
    public void setCentroid(final Centroid centroid)
    {
        Throw.whenNull(centroid, "centroid");
        this.centroid = centroid;
    }

    /**
     * @return roadLinkGateIn
     */
    public RoadLink getRoadLinkGateIn()
    {
        return this.roadLinkGateIn;
    }

    /**
     * @param roadLinkGateIn set roadLinkGateIn
     */
    public void setRoadLinkGateIn(final RoadLink roadLinkGateIn)
    {
        Throw.whenNull(roadLinkGateIn, "roadLinkGateIn");
        this.roadLinkGateIn = roadLinkGateIn;
    }

    /**
     * @return roadLinkGateOut
     */
    public RoadLink getRoadLinkGateOut()
    {
        return this.roadLinkGateOut;
    }

    /**
     * @param roadLinkGateOut set roadLinkGateOut
     */
    public void setRoadLinkGateOut(final RoadLink roadLinkGateOut)
    {
        Throw.whenNull(roadLinkGateOut, "roadLinkGateOut");
        this.roadLinkGateOut = roadLinkGateOut;
    }

    /**
     * @return terminalDestinations
     */
    public Map<String, Double> getTerminalDestinations()
    {
        return this.terminalDestinations;
    }

    /**
     * @param terminalDestinations set terminalDestinations
     */
    public void setTerminalDestinations(final Map<String, Double> terminalDestinations)
    {
        this.terminalDestinations = terminalDestinations;
        makeCumulativeProbabilities(this.destinationProbabilities, this.terminalDestinations);
    }

    /**
     * @return terminalOrigins
     */
    public Map<String, Double> getTerminalOrigins()
    {
        return this.terminalOrigins;
    }

    /**
     * @param terminalOrigins set terminalOrigins
     */
    public void setTerminalOrigins(final Map<String, Double> terminalOrigins)
    {
        this.terminalOrigins = terminalOrigins;
        makeCumulativeProbabilities(this.originProbabilities, this.terminalOrigins);
    }

}
