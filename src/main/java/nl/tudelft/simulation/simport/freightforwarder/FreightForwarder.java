package nl.tudelft.simulation.simport.freightforwarder;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.event.LocalEventProducer;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.Location;
import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.truck.Truck;
import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * The FreightForwarder plans the mode of transport for a container, as well as the pickup and dropoff time of the cargo. The
 * FreightForwarder books trucks at one of the trucking firms, and (in case it is modelled in detail) the trains and barges. The
 * model can work with one 'central' freight forwarder, a freight forwarder per terminal, or a large number of competing freight
 * forwarders.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FreightForwarder extends LocalEventProducer implements Identifiable
{
    /** The id. */
    private final String id;

    /** The port model. */
    private final PortModel model;

    /** The simulator. */
    private final ClockDevsSimulatorInterface simulator;

    /** The import lead time distributions for truck per terminal. */
    private Map<Terminal, Map<String, DistContinuousDuration>> importLeadTimeTruckDist = new HashMap<>();

    /** The import lead time distributions for barge per terminal. */
    private Map<Terminal, Map<String, DistContinuousDuration>> importLeadTimeBargeDist = new HashMap<>();

    /** The import lead time distributions for rail per terminal. */
    private Map<Terminal, Map<String, DistContinuousDuration>> importLeadTimeRailDist = new HashMap<>();

    /** The export lead time distributions for truck per terminal. */
    private Map<Terminal, Map<String, DistContinuousDuration>> exportLeadTimeTruckDist = new HashMap<>();

    /** The export lead time distributions for barge per terminal. */
    private Map<Terminal, Map<String, DistContinuousDuration>> exportLeadTimeBargeDist = new HashMap<>();

    /** The export lead time distributions for rail per terminal. */
    private Map<Terminal, Map<String, DistContinuousDuration>> exportLeadTimeRailDist = new HashMap<>();

    /**
     * Instantiate a new Freight Forwarder.
     * @param id the id of the FF
     * @param model the port model
     */
    public FreightForwarder(final String id, final PortModel model)
    {
        this.id = id;
        this.model = model;
        this.simulator = model.getSimulator();
    }

    private String containerType(final boolean reefer, final boolean empty)
    {
        return (reefer ? "R" : "G") + (empty ? "E" : "F");
    }

    /**
     * Set a time distributions for import, leading to dwell times on the terminals.
     * @param terminal the terminal for which this is the lead time distribution
     * @param transportMode the transport mode
     * @param reefer true if reefer, false if general
     * @param empty true is empty, false if full
     * @param leadTimeDist the lead time distribution
     */
    public void setLeadTimeImport(final Terminal terminal, final TransportMode transportMode, final boolean reefer,
            final boolean empty, final DistContinuousDuration leadTimeDist)
    {
        switch (transportMode)
        {
            case TRUCK -> {
                this.importLeadTimeTruckDist.putIfAbsent(terminal, new HashMap<>());
                this.importLeadTimeTruckDist.get(terminal).put(containerType(reefer, empty), leadTimeDist);
            }
            case RAIL -> {
                this.importLeadTimeRailDist.putIfAbsent(terminal, new HashMap<>());
                this.importLeadTimeRailDist.get(terminal).put(containerType(reefer, empty), leadTimeDist);
            }
            case BARGE -> {
                this.importLeadTimeBargeDist.putIfAbsent(terminal, new HashMap<>());
                this.importLeadTimeBargeDist.get(terminal).put(containerType(reefer, empty), leadTimeDist);
            }
        }
    }

    /**
     * Set a time distributions for export, leading to dwell times on the terminals.
     * @param terminal the terminal for which this is the lead time distribution
     * @param transportMode the transport mode
     * @param reefer true if reefer, false if general
     * @param empty true is empty, false if full
     * @param leadTimeDist the lead time distribution
     */
    public void setLeadTimeExport(final Terminal terminal, final TransportMode transportMode, final boolean reefer,
            final boolean empty, final DistContinuousDuration leadTimeDist)
    {
        switch (transportMode)
        {
            case TRUCK -> {
                this.exportLeadTimeTruckDist.putIfAbsent(terminal, new HashMap<>());
                this.exportLeadTimeTruckDist.get(terminal).put(containerType(reefer, empty), leadTimeDist);
            }
            case RAIL -> {
                this.exportLeadTimeRailDist.putIfAbsent(terminal, new HashMap<>());
                this.exportLeadTimeRailDist.get(terminal).put(containerType(reefer, empty), leadTimeDist);
            }
            case BARGE -> {
                this.exportLeadTimeBargeDist.putIfAbsent(terminal, new HashMap<>());
                this.exportLeadTimeBargeDist.get(terminal).put(containerType(reefer, empty), leadTimeDist);
            }
        }
    }

    ///////////////////////////////////////////////// TRUCK /////////////////////////////////////////////////

    /**
     * Plan the export (TO terminal) of a container by truck. TODO: day of week, time of day, trucking company
     * @param terminal the terminal the container has to be brought to
     * @param vessel the outgoing vessel on which the container is booked
     * @param container the container that has to be transported
     */
    public void planExportContainerTruck(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Duration leadTime =
                this.exportLeadTimeTruckDist.get(terminal).get(containerType(container.isReefer(), container.isEmpty())).draw();
        this.simulator.scheduleEventAbs(new ClockTime(vessel.getEta().minus(leadTime)),
                () -> truckDepartureToTerminal(terminal, vessel, container));
    }

    /**
     * Departure of truck (TO terminal) to transport a container. TODO: this will be done by the trucking company
     * @param terminal the terminal the container has to be brought to
     * @param vessel the outgoing vessel on which the container is booked
     * @param container the container that has to be transported
     */
    protected void truckDepartureToTerminal(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Truck truck = new Truck(this.model.uniqueTruckNr(), this.model);
        truck.loadContainer(container);
        this.simulator.scheduleEventRel(new Duration(2.0, DurationUnit.HOUR), () -> terminal.getYard().dropoffContainer(truck));
    }

    /**
     * Plan the import (FROM terminal) of a container by truck. TODO: day of week, time of day, trucking company
     * @param terminal the terminal the container has to be collected from
     * @param vessel the incoming vessel on which the container was booked
     * @param container the container that has to be transported
     */
    public void planImportContainerTruck(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Duration leadTime =
                this.importLeadTimeTruckDist.get(terminal).get(containerType(container.isReefer(), container.isEmpty())).draw();
        this.simulator.scheduleEventAbs(new ClockTime(vessel.getEta().plus(leadTime)),
                () -> truckDepartureFromTerminal(terminal, vessel, container));
    }

    /**
     * Departure of truck (FROM terminal) to transport a container. TODO: this will be done by the trucking company
     * @param terminal the terminal the container has to be brought to
     * @param vessel the outgoing vessel on which the container is booked
     * @param container the container that has to be transported
     */
    protected void truckDepartureFromTerminal(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Truck truck = new Truck(this.model.uniqueTruckNr(), this.model);
        terminal.getYard().pickupContainer(truck, container);
        this.simulator.scheduleEventRel(new Duration(2.0, DurationUnit.HOUR), () -> unloadContainerHinterland(truck));
    }

    /**
     * Dropoff a container in the hinterland. TODO: this will become part of the truck model
     * @param truck the truck that has driven to the hinterland
     */
    protected void unloadContainerHinterland(final Truck truck)
    {
        var container = truck.unloadContainer();
        container.addLocation(Location.HINTERLAND);
        // TODO statistics
    }

    ///////////////////////////////////////////////// BARGE /////////////////////////////////////////////////

    /**
     * Plan the export (TO terminal) of a container by barge.
     * @param terminal the terminal the container has to be brought to
     * @param vessel the outgoing vessel on which the container is booked
     * @param container the container that has to be transported
     */
    public void planExportContainerBarge(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Duration leadTime =
                this.exportLeadTimeBargeDist.get(terminal).get(containerType(container.isReefer(), container.isEmpty())).draw();
        this.simulator.scheduleEventAbs(new ClockTime(vessel.getEta().minus(leadTime)),
                () -> bargeDepartureToTerminal(terminal, container));
    }

    /**
     * Departure of barge (TO terminal) to transport a container.
     * @param terminal the terminal the container has to be brought to
     * @param container the container that has to be transported
     */
    protected void bargeDepartureToTerminal(final Terminal terminal, final Container container)
    {
        container.addLocation(Location.BARGE);
        this.simulator.scheduleEventRel(new Duration(16.0, DurationUnit.HOUR),
                () -> dropoffContainerBargeTerminal(terminal, container));
    }

    /**
     * Dropoff of the barge container at the terminal.
     * @param terminal the terminal the container has to be brought to
     * @param container the container that has to be transported
     */
    protected void dropoffContainerBargeTerminal(final Terminal terminal, final Container container)
    {
        terminal.getYard().addContainer(container);
    }

    /**
     * Plan the import (FROM terminal) of a container by barge.
     * @param terminal the terminal the container has to be collected from
     * @param vessel the incoming vessel on which the container was booked
     * @param container the container that has to be transported
     */
    public void planImportContainerBarge(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Duration leadTime =
                this.importLeadTimeBargeDist.get(terminal).get(containerType(container.isReefer(), container.isEmpty())).draw();
        this.simulator.scheduleEventAbs(new ClockTime(vessel.getEta().plus(leadTime)),
                () -> bargeDepartureFromTerminal(terminal, container));
    }

    /**
     * Departure of barge (FROM terminal) to transport a container.
     * @param terminal the terminal the container has to be picked up
     * @param container the container that has to be transported
     */
    protected void bargeDepartureFromTerminal(final Terminal terminal, final Container container)
    {
        container.addLocation(Location.BARGE);
        terminal.getYard().removeContainer(container);
        this.simulator.scheduleEventRel(new Duration(16.0, DurationUnit.HOUR),
                () -> dropoffContainerBargeHinterland(container));
    }

    /**
     * Dropoff of the barge container at the terminal.
     * @param terminal the terminal the container has to be brought to
     * @param container the container that has to be transported
     */
    protected void dropoffContainerBargeHinterland(final Container container)
    {
        container.addLocation(Location.HINTERLAND);
        // TODO: statistics
    }

    ///////////////////////////////////////////////// RAIL /////////////////////////////////////////////////

    /**
     * Plan the export (TO terminal) of a container by rail.
     * @param terminal the terminal the container has to be brought to
     * @param vessel the outgoing vessel on which the container is booked
     * @param container the container that has to be transported
     */
    public void planExportContainerRail(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Duration leadTime =
                this.exportLeadTimeRailDist.get(terminal).get(containerType(container.isReefer(), container.isEmpty())).draw();
        this.simulator.scheduleEventAbs(new ClockTime(vessel.getEta().minus(leadTime)),
                () -> railDepartureToTerminal(terminal, container));
    }

    /**
     * Departure of rail (TO terminal) to transport a container.
     * @param terminal the terminal the container has to be brought to
     * @param container the container that has to be transported
     */
    protected void railDepartureToTerminal(final Terminal terminal, final Container container)
    {
        container.addLocation(Location.RAIL);
        this.simulator.scheduleEventRel(new Duration(16.0, DurationUnit.HOUR),
                () -> dropoffContainerRailTerminal(terminal, container));
    }

    /**
     * Dropoff of the rail container at the terminal.
     * @param terminal the terminal the container has to be brought to
     * @param container the container that has to be transported
     */
    protected void dropoffContainerRailTerminal(final Terminal terminal, final Container container)
    {
        terminal.getYard().addContainer(container);
    }

    /**
     * Plan the import (FROM terminal) of a container by rail.
     * @param terminal the terminal the container has to be collected from
     * @param vessel the incoming vessel on which the container was booked
     * @param container the container that has to be transported
     */
    public void planImportContainerRail(final Terminal terminal, final Vessel vessel, final Container container)
    {
        Duration leadTime =
                this.importLeadTimeRailDist.get(terminal).get(containerType(container.isReefer(), container.isEmpty())).draw();
        this.simulator.scheduleEventAbs(new ClockTime(vessel.getEta().plus(leadTime)),
                () -> railDepartureFromTerminal(terminal, container));
    }

    /**
     * Departure of rail (FROM terminal) to transport a container.
     * @param terminal the terminal the container has to be picked up
     * @param container the container that has to be transported
     */
    protected void railDepartureFromTerminal(final Terminal terminal, final Container container)
    {
        container.addLocation(Location.RAIL);
        terminal.getYard().removeContainer(container);
        this.simulator.scheduleEventRel(new Duration(16.0, DurationUnit.HOUR), () -> dropoffContainerRailHinterland(container));
    }

    /**
     * Dropoff of the rail container at the terminal.
     * @param terminal the terminal the container has to be brought to
     * @param container the container that has to be transported
     */
    protected void dropoffContainerRailHinterland(final Container container)
    {
        container.addLocation(Location.HINTERLAND);
        // TODO: statistics
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * @return model
     */
    public PortModel getModel()
    {
        return this.model;
    }

}
