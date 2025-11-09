package nl.tudelft.simulation.simport.terminal;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.Truck;

/**
 * GateConstant is a gate with a constant number of lanes and a constant processing time.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class GateConstant implements Gate
{
    /** The terminal or depot to which the gate belongs. */
    private final PortFacility portFacility;

    /** the (sub) id for the gate. */
    private final String id;

    /** the (fixed) number of lanes-in. */
    private int lanesIn;

    /** the (fixed) number of lanes-out. */
    private int lanesOut;

    /** the fixed handling time distribution for entering the terminal. */
    private DistContinuousDuration timeInDist;

    /** the fixed handling time distribution for leaving the terminal. */
    private DistContinuousDuration timeOutDist;

    /** the truck queue for entering the terminal. */
    private List<Truck> truckQueueIn;

    /** the truck queue for leaving the terminal. */
    private List<Truck> truckQueueOut;

    /**
     * Create a gate for a terminal with a constant number of lanes and constant handling times.
     * @param portFacility the terminal or depot
     * @param id the unique id within the portFacility; will be appended to the terminal id for display.
     */
    public GateConstant(final PortFacility portFacility, final String id)
    {
        this.portFacility = portFacility;
        this.id = id;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public PortFacility getPortFacility()
    {
        return this.portFacility;
    }

    /**
     * Set the number of lanes into the terminal.
     * @param lanesIn the number of lanes into the terminal
     * @return the facility instance for method chaining
     */
    public GateConstant setLanesIn(final int lanesIn)
    {
        this.lanesIn = lanesIn;
        return this;
    }

    @Override
    public int getCurrentLanesIn()
    {
        return this.lanesIn;
    }

    /**
     * Set the number of lanes out of the terminal.
     * @param lanesOut the number of lanes out of the terminal
     * @return the facility instance for method chaining
     */
    public GateConstant setLanesOut(final int lanesOut)
    {
        this.lanesOut = lanesOut;
        return this;
    }
    @Override
    public int getCurrentLanesOut()
    {
        return this.lanesOut;
    }

    /**
     * Set the fixed handling time distribution for entering the terminal.
     * @param timeInDist the fixed handling time distribution for entering the terminal
     * @return the facility instance for method chaining
     */
    public GateConstant setTimeInDist(final DistContinuousDuration timeInDist)
    {
        this.timeInDist = timeInDist;
        return this;
    }

    @Override
    public Duration getCurrentHandlingTimeIn()
    {
        return this.timeInDist.draw();
    }

    /**
     * Set the fixed handling time distribution for leaving the terminal.
     * @param timeInDist the fixed handling time distribution for leaving the terminal
     * @return the facility instance for method chaining
     */
    public GateConstant setTimeOutDist(final DistContinuousDuration timeOutDist)
    {
        this.timeOutDist = timeOutDist;
        return this;
    }

    @Override
    public Duration getCurrentHandlingTimeOut()
    {
        return this.timeOutDist.draw();
    }

    @Override
    public List<Truck> getTruckQueueIn()
    {
        return this.truckQueueIn;
    }

    @Override
    public List<Truck> getTruckQueueOut()
    {
        return this.truckQueueOut;
    }

    @Override
    public void addTruckIn(final Truck truck)
    {
        this.truckQueueIn.add(truck);
    }

    @Override
    public void addTruckOut(final Truck truck)
    {
        this.truckQueueOut.add(truck);
    }

    @Override
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.portFacility.getSimulator();
    }

}
