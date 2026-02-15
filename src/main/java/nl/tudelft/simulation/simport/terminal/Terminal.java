package nl.tudelft.simulation.simport.terminal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.simport.Location;
import nl.tudelft.simulation.simport.animation.TerminalAnimation;
import nl.tudelft.simulation.simport.container.Booking;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.vessel.Vessel;
import nl.tudelft.simulation.simport.vessel.VesselGenerator;

/**
 * Terminal is a container facility that gets visits from short sea or deep sea vessels.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Terminal extends AbstractContainerFacility
{
    /** The vessel generators for this terminal. */
    private Map<String, VesselGenerator> vesselGeneratorMap = new LinkedHashMap<>();

    /** Unallocated export bookings that have to be allocated to a mode. These come in 7-14 days before vessel arrival. */
    private final SortedMap<Vessel, List<Booking>> unallocatedExportMap;

    /** Unallocated import bookings that have to be allocated to a mode. These come in 7-14 days before vessel arrival. */
    private final SortedMap<Vessel, List<Booking>> unallocatedImportMap;

    /** Allocated export bookings that have been allocated to the truck MODE, not necessarily to a truck instance yet. */
    private final SortedMap<Vessel, List<Booking>> allocatedExportTruckMap;

    /** Allocated export bookings that have been allocated to the barge MODE, not necessarily to a barge instance yet. */
    private final SortedMap<Vessel, List<Booking>> allocatedExportBargeMap;

    /** Allocated export bookings that have been allocated to the rail MODE, not necessarily to a train instance yet. */
    private final SortedMap<Vessel, List<Booking>> allocatedExportRailMap;

    /** Allocated export bookings that have been allocated to transshipment. The deepsea vessel is fixed. */
    private final SortedMap<Vessel, List<Booking>> allocatedExportTransshipMap;

    /** Allocated import bookings that have been allocated to the truck MODE, not necessarily to a truck instance yet. */
    private final SortedMap<Vessel, List<Booking>> allocatedImportTruckMap;

    /** Allocated import bookings that have been allocated to the barge MODE, not necessarily to a barge instance yet. */
    private final SortedMap<Vessel, List<Booking>> allocatedImportBargeMap;

    /** Allocated import bookings that have been allocated to the rail MODE, not necessarily to a train instance yet. */
    private final SortedMap<Vessel, List<Booking>> allocatedImportRailMap;

    /** Allocated import bookings that have been allocated to transshipment. The deepsea vessel is fixed. */
    private final SortedMap<Vessel, List<Booking>> allocatedImportTransshipMap;

    /** The transshipment fraction for import containers. */
    private double transshipmentFractionImport;

    /** The transshipment fraction for export containers. */
    private double transshipmentFractionExport;

    /** The modal split for import containers. */
    private ModalSplit modalSplitImport;

    /** The modal split for export containers. */
    private ModalSplit modalSplitExport;

    /**
     * Create a new terminal for the port model.
     * @param id the id of the terminal
     * @param name the longer name of the terminal
     * @param model the port model
     * @param lat latitude
     * @param lon longitude
     */
    public Terminal(final String id, final String name, final PortModel model, final double lat, final double lon)
    {
        super(id, name, model, lat, lon);
        model.addTerminal(this);
        this.unallocatedExportMap = new TreeMap<>(new VesselComparator());
        this.unallocatedImportMap = new TreeMap<>(new VesselComparator());
        this.allocatedExportTruckMap = new TreeMap<>(new VesselComparator());
        this.allocatedExportBargeMap = new TreeMap<>(new VesselComparator());
        this.allocatedExportRailMap = new TreeMap<>(new VesselComparator());
        this.allocatedExportTransshipMap = new TreeMap<>(new VesselComparator());
        this.allocatedImportTruckMap = new TreeMap<>(new VesselComparator());
        this.allocatedImportBargeMap = new TreeMap<>(new VesselComparator());
        this.allocatedImportRailMap = new TreeMap<>(new VesselComparator());
        this.allocatedImportTransshipMap = new TreeMap<>(new VesselComparator());

        // schedule a matchmaking activity once a day.
        allocateBookings();

        // animation?
        if (getSimulator() instanceof AnimatorInterface)
        {
            new TerminalAnimation(this, getSimulator());
        }
    }

    /**
     * Add a vessel generator to the terminal, and start scheduling arrivals of vessels.
     * @param vesselGenerator the new VesselGenerator to add
     */
    public void addVesselGenerator(final VesselGenerator vesselGenerator)
    {
        this.vesselGeneratorMap.put(vesselGenerator.getId(), vesselGenerator);
        vesselGenerator.start();
    }

    /**
     * Remove a vessel generator from the terminal, and stop scheduling arrivals of vessels.
     * @param vesselGenerator the VesselGenerator to remove
     * @return whether the removal was successful; false if it the generator was not found
     */
    public boolean removeVesselGenerator(final VesselGenerator vesselGenerator)
    {
        return removeVesselGenerator(vesselGenerator.getId());
    }

    /**
     * Remove a vessel generator from the terminal, and stop scheduling arrivals of vessels.
     * @param is the id of the VesselGenerator to remove
     * @return whether the removal was successful; false if it the generator was not found
     */
    public boolean removeVesselGenerator(final String id)
    {
        if (this.vesselGeneratorMap.containsKey(id))
        {
            var vesselGenerator = this.vesselGeneratorMap.remove(id);
            vesselGenerator.stop();
            return true;
        }
        return false;
    }

    /**
     * Return a map of the vessel generators for this terminal, based on their id.
     * @return a map of the vessel generators for this terminal
     */
    public Map<String, VesselGenerator> getVesselGeneratorMap()
    {
        return this.vesselGeneratorMap;
    }

    /**
     * Add import containers and plan to allocate mode of transport. For a feeder, the transport mode can de decided directly.
     * For a deepsea vessel we have to wait to see how much transloading has to take place with feeder vessels that have not yet
     * been generated.
     * @param vessel the vessel to add the unallocated import containers for
     */
    public void addToUnallocatedImportMap(final Vessel vessel)
    {
        this.unallocatedImportMap.put(vessel, new ArrayList<>(vessel.getUnloadList()));
        this.allocatedImportTruckMap.put(vessel, new ArrayList<>());
        this.allocatedImportBargeMap.put(vessel, new ArrayList<>());
        this.allocatedImportRailMap.put(vessel, new ArrayList<>());
        this.allocatedImportTransshipMap.put(vessel, new ArrayList<>());

        // if the vessel is a feeder, distribute the modes of transport
        if (!vessel.getVesselType().isFeeder())
            return;
        int index = 0;
        var list = this.unallocatedImportMap.get(vessel);
        while (index < list.size())
        {
            Booking booking = list.get(index);
            if (getModel().getU01().draw() < this.transshipmentFractionImport)
                index++;
            else
            {
                drawImportMode(vessel, booking);
                list.remove(index);
            }
        }
    }

    /**
     * Add export containers and plan to allocate mode of transport. For a feeder, the transport mode can de decided directly.
     * For a deepsea vessel we have to wait to see how much transloading has to take place with feeder vessels that have not yet
     * been generated.
     * @param vessel the vessel to add the unallocated export containers for
     */
    public void addToUnallocatedExportMap(final Vessel vessel)
    {
        this.unallocatedExportMap.put(vessel, new ArrayList<>(vessel.getLoadList()));
        this.allocatedExportTruckMap.put(vessel, new ArrayList<>());
        this.allocatedExportBargeMap.put(vessel, new ArrayList<>());
        this.allocatedExportRailMap.put(vessel, new ArrayList<>());
        this.allocatedExportTransshipMap.put(vessel, new ArrayList<>());

        // if the vessel is a feeder, distribute the modes of transport
        if (!vessel.getVesselType().isFeeder())
            return;
        int index = 0;
        var list = this.unallocatedExportMap.get(vessel);
        while (index < list.size())
        {
            Booking booking = list.get(index);
            if (getModel().getU01().draw() < this.transshipmentFractionExport)
                index++;
            else
            {
                drawExportMode(vessel, booking);
                list.remove(index);
            }
        }
    }

    /**
     * Draw an export mode of transport and add the booking to the correct map. Note that the booking is not yet removed from
     * the unallocated booking map. Add a container in the hinterland for the export booking.
     * @param vessel the vessel the booking belongs to; the container is LEAVING with this vessel
     * @param booking the booking, without a container
     */
    protected void drawExportMode(final Vessel vessel, final Booking booking)
    {
        Container container = new Container(getModel().uniqueContainerNr(), booking.getSize(), booking.isEmpty(),
                booking.isReefer(), Location.HINTERLAND);
        booking.setContainer(container);
        var freightForwarder = getModel().getFreightForwarder(this, vessel);

        double d = getModel().getU01().draw();
        if (d < this.modalSplitExport.cumulativeTruckFractionTBR())
        {
            this.allocatedExportTruckMap.get(vessel).add(booking);
            freightForwarder.planExportContainerTruck(this, vessel, container);
        }
        else if (d < this.modalSplitExport.cumulativeBargeFractionTBR())
        {
            this.allocatedExportBargeMap.get(vessel).add(booking);
            freightForwarder.planExportContainerBarge(this, vessel, container);
        }
        else
        {
            this.allocatedExportRailMap.get(vessel).add(booking);
            freightForwarder.planExportContainerRail(this, vessel, container);
        }
    }

    /**
     * Draw an import mode of transport and add the booking to the correct map. Note that the booking is not yet removed from
     * the unallocated booking map.
     * @param vessel the vessel the booking belongs to; the container is ARRIVING with this vessel
     * @param booking the booking, including a container since it is import (the container is already linked to the vessel)
     */
    protected void drawImportMode(final Vessel vessel, final Booking booking)
    {
        Container container = booking.getContainer();
        var freightForwarder = getModel().getFreightForwarder(this, vessel);

        double d = getModel().getU01().draw();
        if (d < this.modalSplitImport.cumulativeTruckFractionTBR())
        {
            this.allocatedImportTruckMap.get(vessel).add(booking);
            freightForwarder.planImportContainerTruck(this, vessel, container);
        }
        else if (d < this.modalSplitImport.cumulativeBargeFractionTBR())
        {
            this.allocatedImportBargeMap.get(vessel).add(booking);
            freightForwarder.planImportContainerBarge(this, vessel, container);
        }
        else
        {
            this.allocatedImportRailMap.get(vessel).add(booking);
            freightForwarder.planImportContainerRail(this, vessel, container);
        }
    }

    /**
     * Check once a day how to match transloading bookings for vessels arriving and departing. When all transloading has been
     * booked, bookings are distributed over the modes of transport. Containers that are transloaded stay a few days on the
     * yard. This means that there has to be a minimum number of days between arrival of vessel and departure on the next
     * vessel. This number is a property for the simulation.
     */
    protected void allocateBookings()
    {
        Duration cutoffDS = Duration.valueOf(getModel().getInputParameterString("terminal.CutoffTransloadingDeepsea"));
        Duration cutoffFF = Duration.valueOf(getModel().getInputParameterString("terminal.CutoffTransloadingFeeder"));
        Duration bufferTime = Duration.valueOf(getModel().getInputParameterString("terminal.TransloadingBufferTime"));
        double maxTransloadFraction = getModel().getInputParameterDouble("terminal.MaxDeepseaTransloadFraction");

        // Feeder -> Deepsea
        for (Vessel feeder : this.unallocatedImportMap.keySet())
        {
            if (!feeder.getVesselType().isFeeder() || this.unallocatedImportMap.get(feeder).size() == 0)
                continue;
            for (Vessel deepsea : this.unallocatedExportMap.keySet())
            {
                if (deepsea.getVesselType().isFeeder() || this.unallocatedExportMap.get(deepsea).size() == 0)
                    continue;
                if (deepsea.getEta().minus(getSimulator().getSimulatorClockTime()).lt(cutoffDS))
                    continue;
                if (deepsea.getEta().minus(feeder.getEta()).lt(bufferTime))
                    continue;
                while (deepsea.getNrContainersTransshippedLoaded() <= maxTransloadFraction * deepsea.getLoadList().size())
                {
                    if (this.unallocatedImportMap.get(feeder).size() == 0)
                        break;
                    var booking = this.unallocatedImportMap.get(feeder).remove(0);
                    this.allocatedImportTransshipMap.get(feeder).add(booking);
                    this.allocatedExportTransshipMap.get(deepsea).add(booking);
                    feeder.incNrContainersTransshippedUnloaded(1);
                    deepsea.incNrContainersTransshippedLoaded(1);
                }
            }
        }

        // Deepsea -> Feeder
        for (Vessel feeder : this.unallocatedExportMap.keySet())
        {
            if (!feeder.getVesselType().isFeeder() || this.unallocatedExportMap.get(feeder).size() == 0)
                continue;
            for (Vessel deepsea : this.unallocatedImportMap.keySet())
            {
                if (deepsea.getVesselType().isFeeder() || this.unallocatedImportMap.get(deepsea).size() == 0)
                    continue;
                if (deepsea.getEta().minus(getSimulator().getSimulatorClockTime()).lt(cutoffDS))
                    continue;
                if (feeder.getEta().minus(deepsea.getEta()).lt(bufferTime))
                    continue;
                while (deepsea.getNrContainersTransshippedUnloaded() <= maxTransloadFraction * deepsea.getUnloadList().size())
                {
                    if (this.unallocatedExportMap.get(feeder).size() == 0)
                        break;
                    var booking = this.unallocatedExportMap.get(feeder).remove(0);
                    this.allocatedExportTransshipMap.get(feeder).add(booking);
                    this.allocatedImportTransshipMap.get(deepsea).add(booking);
                    feeder.incNrContainersTransshippedLoaded(1);
                    deepsea.incNrContainersTransshippedUnloaded(1);
                }
            }
        }

        // Ships where allocation is ready or where cutoff date has passed
        for (var it = this.unallocatedExportMap.keySet().iterator(); it.hasNext();)
        {
            Vessel deepsea = it.next();
            if (!deepsea.getVesselType().isDeepSea())
                continue;
            if (deepsea.getEta().minus(getSimulator().getSimulatorClockTime()).lt(cutoffDS))
            {
                for (var booking : this.unallocatedExportMap.get(deepsea))
                {
                    drawExportMode(deepsea, booking);
                }
                it.remove();
            }
        }
        for (var it = this.unallocatedImportMap.keySet().iterator(); it.hasNext();)
        {
            Vessel deepsea = it.next();
            if (!deepsea.getVesselType().isDeepSea())
                continue;
            if (deepsea.getEta().minus(getSimulator().getSimulatorClockTime()).lt(cutoffDS))
            {
                for (var booking : this.unallocatedImportMap.get(deepsea))
                {
                    drawImportMode(deepsea, booking);
                }
                it.remove();
            }
        }

        for (var it = this.unallocatedExportMap.keySet().iterator(); it.hasNext();)
        {
            Vessel feeder = it.next();
            if (!feeder.getVesselType().isFeeder())
                continue;
            if (feeder.getEta().minus(getSimulator().getSimulatorClockTime()).lt(cutoffFF))
            {
                for (var booking : this.unallocatedExportMap.get(feeder))
                {
                    drawExportMode(feeder, booking);
                }
                it.remove();
            }
        }
        for (var it = this.unallocatedImportMap.keySet().iterator(); it.hasNext();)
        {
            Vessel feeder = it.next();
            if (!feeder.getVesselType().isFeeder())
                continue;
            if (feeder.getEta().minus(getSimulator().getSimulatorClockTime()).lt(cutoffFF))
            {
                for (var booking : this.unallocatedImportMap.get(feeder))
                {
                    drawImportMode(feeder, booking);
                }
                it.remove();
            }
        }

        getSimulator().scheduleEventRel(
                Duration.valueOf(getModel().getInputParameterString("terminal.TransloadingMatchInterval")),
                () -> allocateBookings());
    }

    /**
     * Comparator for two vessels based on ETA.
     */
    protected static class VesselComparator implements Comparator<Vessel>
    {
        @Override
        public int compare(final Vessel v1, final Vessel v2)
        {
            return Double.compare(v1.getEta().doubleValue(), v2.getEta().doubleValue());
        }
    }

    //////////////////////////////////////////// GETTERS AND SETTERS //////////////////////////////////////////////////

    /**
     * Return the modal split for import containers for this terminal.
     * @return the modal split for import containers for this terminal
     */
    public ModalSplit getModalSplitImport()
    {
        return this.modalSplitImport;
    }

    /**
     * Set the modal split for import containers for this terminal.
     * @param modalSplitImport the modal split for import containers for this terminal
     */
    public void setModalSplitImport(final ModalSplit modalSplitImport)
    {
        this.modalSplitImport = modalSplitImport;
    }

    /**
     * Return the modal split for export containers for this terminal.
     * @return the modal split for export containers for this terminal
     */
    public ModalSplit getModalSplitExport()
    {
        return this.modalSplitExport;
    }

    /**
     * Set the modal split for export containers for this terminal.
     * @param modalSplitExport the modal split for export containers for this terminal
     */
    public void setModalSplitExport(final ModalSplit modalSplitExport)
    {
        this.modalSplitExport = modalSplitExport;
    }

    /**
     * @return transshipmentFractionImport
     */
    public double getTransshipmentFractionImport()
    {
        return this.transshipmentFractionImport;
    }

    /**
     * @param transshipmentFractionImport set transshipmentFractionImport
     */
    public void setTransshipmentFractionImport(final double transshipmentFractionImport)
    {
        this.transshipmentFractionImport = transshipmentFractionImport;
    }

    /**
     * @return transshipmentFractionExport
     */
    public double getTransshipmentFractionExport()
    {
        return this.transshipmentFractionExport;
    }

    /**
     * @param transshipmentFractionExport set transshipmentFractionExport
     */
    public void setTransshipmentFractionExport(final double transshipmentFractionExport)
    {
        this.transshipmentFractionExport = transshipmentFractionExport;
    }

    /**
     * @return unallocatedExportMap
     */
    public SortedMap<Vessel, List<Booking>> getUnallocatedExportMap()
    {
        return this.unallocatedExportMap;
    }

    /**
     * @return unallocatedImportMap
     */
    public SortedMap<Vessel, List<Booking>> getUnallocatedImportMap()
    {
        return this.unallocatedImportMap;
    }

    /**
     * @return allocatedExportTruckMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedExportTruckMap()
    {
        return this.allocatedExportTruckMap;
    }

    /**
     * @return allocatedExportBargeMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedExportBargeMap()
    {
        return this.allocatedExportBargeMap;
    }

    /**
     * @return allocatedExportRailMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedExportRailMap()
    {
        return this.allocatedExportRailMap;
    }

    /**
     * @return allocatedExportTransshipMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedExportTransshipMap()
    {
        return this.allocatedExportTransshipMap;
    }

    /**
     * @return allocatedImportTruckMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedImportTruckMap()
    {
        return this.allocatedImportTruckMap;
    }

    /**
     * @return allocatedImportBargeMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedImportBargeMap()
    {
        return this.allocatedImportBargeMap;
    }

    /**
     * @return allocatedImportRailMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedImportRailMap()
    {
        return this.allocatedImportRailMap;
    }

    /**
     * @return allocatedImportTransshipMap
     */
    public SortedMap<Vessel, List<Booking>> getAllocatedImportTransshipMap()
    {
        return this.allocatedImportTransshipMap;
    }

    @Override
    public String toString()
    {
        return "Terminal [id=" + this.getId() + "]";
    }

}
