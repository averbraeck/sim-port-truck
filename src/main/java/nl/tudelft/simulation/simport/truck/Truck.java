package nl.tudelft.simulation.simport.truck;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point3d;
import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.logger.Cat;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.Location;
import nl.tudelft.simulation.simport.animation.TruckAnimation;
import nl.tudelft.simulation.simport.appointment.SlotBooking;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.truck.activity.PlannedDrivingActivity;
import nl.tudelft.simulation.simport.truck.activity.PlannedTerminalActivity;
import nl.tudelft.simulation.simport.truck.activity.PlannedTruckActivity;
import nl.tudelft.simulation.simport.truck.activity.RealizedDrivingActivity;
import nl.tudelft.simulation.simport.truck.activity.RealizedTerminalActivity;
import nl.tudelft.simulation.simport.truck.activity.RealizedTruckActivity;

/**
 * Truck can transport a container from A to B, and load/unload a container.<br>
 * TODO: Note that in theory the truck can pick up a 2nd 20-ft container after a 20 ft container was already loaded. (in
 * reality: only if weight permits of course, e.g., in case of 2 empties).
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Truck implements Identifiable, Locatable
{
    /** The truck id. */
    private final int uniqueId;

    /** The trucking company to which the truck belongs. */
    private final TruckingCompany truckingCompany;

    /** The id within the trucking cmpany. */
    private final int idWithinCompany;

    /** the model. */
    private final PortModel model;

    /** The first container that is carried. */
    private Container container1;

    /** The second container that is carried. */
    private Container container2;

    /** Planned truck activities (driving and loading/unloading). */
    private List<PlannedTruckActivity> plannedActivityList = new ArrayList<>();

    /** Realized truck activities (driving and loading/unloading). */
    private List<RealizedTruckActivity> realizedActivityList = new ArrayList<>();

    /** Current truck activity index. */
    private int currentActivityIndex = -1;

    /** Realized truck activity index. */
    private int realizedActivityIndex = -1;

    /** truck animation. */
    private TruckAnimation truckAnimation = null;

    /**
     * Instantiate a truck.
     * @param id the overall unique id
     * @param truckingCompany the trucking company
     * @param idWithinCompany the unique id within the company
     */
    public Truck(final int id, final TruckingCompany truckingCompany, final int idWithinCompany)
    {
        this.uniqueId = id;
        this.truckingCompany = truckingCompany;
        this.idWithinCompany = idWithinCompany;
        this.model = truckingCompany.getModel();

        // animation?
        if (getSimulator() instanceof AnimatorInterface)
        {
            this.truckAnimation = new TruckAnimation(this, getSimulator());
        }
    }

    /**
     * Start the plan.
     */
    public void startPlan()
    {
        if (this.plannedActivityList.size() == 0)
        {
            CategoryLogger.with(Cat.DSOL).error("Truck {} asked to start, but does not have a plan", toString());
            return;
        }
        PlannedTruckActivity pta = this.plannedActivityList.get(0);
        if (pta instanceof PlannedDrivingActivity pda)
        {
            // TODO: see if there is a delay when starting to drive.
            Duration delay = Duration.ofSI(0.0);
            getSimulator().scheduleEventAbs(new ClockTime(pda.getDepartureTime().plus(delay)), () -> startDrivingFirst(pda));
        }
        else
            CategoryLogger.with(Cat.DSOL).error("Truck {} has an unknown or illegal first activity {}", toString(),
                    this.plannedActivityList.get(0));
    }

    /**
     * Start a driving activity as a first activity, see if containers need to be loaded onto the truck.
     */
    protected void startDrivingFirst(final PlannedDrivingActivity pda)
    {
        if (pda.getContainer1() != null)
        {
            loadContainer(pda.getContainer1());
        }
        if (pda.getContainer2() != null)
        {
            loadContainer(pda.getContainer2());
        }
        this.currentActivityIndex++;
        startDriving(pda);
    }

    /**
     * Start a driving activity.
     */
    protected void startDriving(final PlannedDrivingActivity pda)
    {
        // TODO: stochastic driving time, especially in the rush hours
        double factor = 1.0;
        ClockTime tnow = getSimulator().getSimulatorClockTime();
        RealizedDrivingActivity rda = new RealizedDrivingActivity(pda);
        rda.setActualDepartureTime(tnow);
        ClockTime actualArrivalTime = new ClockTime(tnow.plus(pda.getDuration().times(factor)));
        rda.setActualArrivalTime(actualArrivalTime);
        this.realizedActivityList.add(rda);
        this.realizedActivityIndex++;
        getSimulator().scheduleEventAbs(actualArrivalTime, () -> endDriving(rda));
    }

    /**
     * End a driving activity
     */
    protected void endDriving(final RealizedDrivingActivity rda)
    {
        // if the end location is not a terminal (so, hinterland), dropoff any container and trigger statistics
        if (!isEmpty() && !this.model.getTerminalCentroids().contains(rda.getDestCentroid()))
        {
            var containers = unloadContainers();
            for (int i = 0; i < containers.length; i++)
            {
                containers[i].addLocation(Location.HINTERLAND);
                this.model.fireEvent(PortModel.CONTAINER_EVENT, containers[i]);
            }
        }
        rda.setActualArrivalTime(getSimulator().getSimulatorClockTime());
        getSimulator().scheduleEventNow(() -> startNextActivity());
    }

    /**
     * Start a next activity. The currentActivityIndex has not yet been increased.
     */
    protected void startNextActivity()
    {
        // see if there is still another activity
        this.currentActivityIndex++;
        if (this.currentActivityIndex >= this.plannedActivityList.size())
        {
            // end of activities; log statistics to truck file
            this.model.fireEvent(PortModel.TRUCK_EVENT, this);
            if (this.truckAnimation != null)
                this.truckAnimation.destroy(getSimulator());
            return;
        }

        PlannedTruckActivity plannedActivity = this.plannedActivityList.get(this.currentActivityIndex);
        if (plannedActivity instanceof PlannedDrivingActivity pda)
        {
            getSimulator().scheduleEventNow(() -> startDriving(pda));
        }
        else if (plannedActivity instanceof PlannedTerminalActivity pta)
        {
            getSimulator().scheduleEventNow(() -> startTerminalActivity(pta));
        }
        else
            CategoryLogger.with(Cat.DSOL).error("Truck {} has an unknown or illegal first activity {}", toString(),
                    this.plannedActivityList.get(0));
    }

    /**
     * Start a terminal activity.
     */
    protected void startTerminalActivity(final PlannedTerminalActivity pta)
    {
        // see if we can get in -- are we within the slot time?
        ClockTime tnow = getSimulator().getSimulatorClockTime();
        Duration delay = Duration.ZERO;
        if (pta.getAppointment() instanceof SlotBooking slotBooking)
        {
            Duration early = slotBooking.getTargetTime().minus(slotBooking.getGraceDurationBeforeTarget()).minus(tnow);
            if (early.gt0())
            {
                // too early - wait
                delay = early;
            }
            Duration late = tnow.minus(slotBooking.getTargetTime().plus(slotBooking.getGraceDurationAfterTarget()));
            if (late.gt0())
            {
                // missed the slot: replan the visit
                // TODO: replanning
                delay = new Duration(1.0, DurationUnit.HOUR);
            }
        }
        RealizedTerminalActivity rta = new RealizedTerminalActivity(pta);
        rta.setWaitingTimeIn(delay);
        this.realizedActivityList.add(rta);
        this.realizedActivityIndex++;
        getSimulator().scheduleEventRel(delay, () -> gateInActivity(rta));
    }

    /**
     * Gate in activity. TODO: Check whether the terminal has capacity.
     */
    protected void gateInActivity(final RealizedTerminalActivity rta)
    {
        Duration gateTimeIn = rta.getTerminal().getGate().drawCurrentGateTimeIn();
        rta.setActualGateTimeIn(gateTimeIn);
        getSimulator().scheduleEventRel(gateTimeIn, () -> startTerminalHandling(rta));
    }

    /**
     * Terminal handling. Load/unload container(s).
     */
    protected void startTerminalHandling(final RealizedTerminalActivity rta)
    {
        Duration handlingTime = Duration.ZERO;
        switch (rta.getTerminalActivityType())
        {
            case DROPOFF -> {
                handlingTime = rta.getTerminal().getYard().drawHandlingTimeExport();
                rta.getTerminal().getYard().dropoffContainers(this);
            }
            case PICKUP -> {
                handlingTime = rta.getTerminal().getYard().drawHandlingTimeImport();
                rta.getTerminal().getYard().pickupContainer(this, rta.getPlannedTerminalActivity().getContainerPickup1());
                if (rta.getPlannedTerminalActivity().getContainerPickup2() != null)
                    rta.getTerminal().getYard().pickupContainer(this, rta.getPlannedTerminalActivity().getContainerPickup2());
            }
            case DUAL -> {
                handlingTime = rta.getTerminal().getYard().drawHandlingTimeDual();
                rta.getTerminal().getYard().dropoffContainers(this);
                rta.getTerminal().getYard().pickupContainer(this, rta.getPlannedTerminalActivity().getContainerPickup1());
                if (rta.getPlannedTerminalActivity().getContainerPickup2() != null)
                    rta.getTerminal().getYard().pickupContainer(this, rta.getPlannedTerminalActivity().getContainerPickup2());
            }
        }
        rta.setActualHandlingTime(handlingTime);
        getSimulator().scheduleEventRel(handlingTime, () -> gateOutActivity(rta));
    }

    /**
     * Gate out activity.
     */
    protected void gateOutActivity(final RealizedTerminalActivity rta)
    {
        Duration gateTimeOut = rta.getTerminal().getGate().drawCurrentGateTimeOut();
        rta.setActualGateTimeOut(gateTimeOut);
        getSimulator().scheduleEventRel(gateTimeOut, () -> startNextActivity());
    }

    /**
     * Load a container onto the truck.
     * @param container the container to load
     */
    public void loadContainer(final Container container)
    {
        if (this.container1 == null)
            this.container1 = container;
        else if (this.container1 == null)
            this.container2 = container;
        else
        {
            CategoryLogger.with(Cat.DSOL).warn("Full truck {} wanted to pick up container {}", this, container);
            return;
        }
        if (this.container1.getSize() + (this.container2 == null ? 0 : this.container2.getSize()) > 45)
        {
            CategoryLogger.with(Cat.DSOL).warn("Truck {} has more than 45 ft loaded: {} and {}", this, this.container1,
                    (this.container2 == null ? "" : this.container2.toString()));
            return;
        }
        container.addLocation(Location.TRUCK);
    }

    /**
     * Unload one or two containers from the truck.
     * @return the unloaded containers
     */
    public Container[] unloadContainers()
    {
        Container[] containers;
        if (this.container2 != null)
            containers = new Container[] {this.container1, this.container2};
        else
            containers = new Container[] {this.container1};
        this.container1 = null;
        this.container2 = null;
        return containers;
    }

    public boolean isEmpty()
    {
        return this.container1 == null && this.container2 == null;
    }

    /**
     * Return the container on the truck, or null if the truck is not loaded.
     * @return the first container on the truck, or null if the truck is not loaded
     */
    public Container getContainer1()
    {
        return this.container1;
    }

    /**
     * Return the container on the truck, or null if the truck is not loaded.
     * @return the second container on the truck, or null if the second container is not present
     */
    public Container getContainer2()
    {
        return this.container2;
    }

    @Override
    public Point<?> getLocation()
    {
        if (this.realizedActivityIndex >= 0)
        {
            // terminal -- return terminal location
            if (this.realizedActivityList.get(this.realizedActivityIndex) instanceof RealizedTerminalActivity ta)
                return ta.getTerminal().getLocation();

            // driving -- interpolate location based on last activity
            if (this.realizedActivityList.get(this.realizedActivityIndex) instanceof RealizedDrivingActivity ra)
                return interpolateLocation(ra.getOrigCentroid().getLocation(), ra.getDestCentroid().getLocation(),
                        ra.getActualDepartureTime(), ra.getActualArrivalTime());
        }

        return new Point3d(0.0, 0.0, 1.0);
    }

    /**
     * For now, without a map service: straight line interpolation for the truck.
     * @return the location of the truck while driving
     */
    public Point3d interpolateLocation(final Point<?> origin, final Point<?> destination, final ClockTime startTime,
            final ClockTime endTime)
    {
        double fraction = interpolateFraction(startTime, endTime);
        return new Point3d(origin.getX() + fraction * (destination.getX() - origin.getX()),
                origin.getY() + fraction * (destination.getY() - origin.getY()), 1.0);
    }

    /**
     * Interpolate the location between origin and destination, given the departure and arrival times.
     * @return fraction of the route
     */
    public double interpolateFraction(final ClockTime startTime, final ClockTime endTime)
    {
        return (getSimulator().getSimulatorClockTime().si - startTime.si) / (endTime.si - startTime.si);
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return new Bounds2d(0.000146, 0.00009); // 10x10 m at Rotterdam (51.9 deg latitude)
    }

    /**
     * @return the uniqueId
     */
    public int getUniqueId()
    {
        return this.uniqueId;
    }

    @Override
    public String getId()
    {
        return this.truckingCompany.getId() + "." + this.idWithinCompany;
    }

    /**
     * @return truckingCompany
     */
    public TruckingCompany getTruckingCompany()
    {
        return this.truckingCompany;
    }

    /** @return the simulator. */
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.model.getSimulator();
    }

    /**
     * @return plannedActivityList
     */
    public List<PlannedTruckActivity> getPlannedActivityList()
    {
        return this.plannedActivityList;
    }

    /**
     * @return realizedActivityList
     */
    public List<RealizedTruckActivity> getRealizedActivityList()
    {
        return this.realizedActivityList;
    }

    @Override
    public String toString()
    {
        return "Truck [id=" + getId() + ", container=" + this.container1 + "]";
    }

}
