package nl.tudelft.simulation.simport.terminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.simulation.simport.container.Booking;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * Tos is the terminal operating system with the planning of the yard and berth.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Tos
{
    /** Unallocated incoming containers. */
    private Map<Vessel, List<Container>> unallocatedIn = new HashMap<>();

    /** Unallocated outgoing bookings. */
    private Map<Vessel, List<Booking>> unallocatedOut = new HashMap<>();

    /** Allocated containers Vessel - Vessel (DS -- FF or FF -- DS). */
    private Map<Integer, List<Container>> allocatedVV = new HashMap<>();

    /** Allocated containers Vessel -- Rail. */
    private Map<Vessel, List<Container>> allocatedVR = new HashMap<>();

    /** Allocated containers Vessel -- Barge. */
    private Map<Vessel, List<Container>> allocatedVB = new HashMap<>();

    /** Allocated containers Vessel -- Truck. */
    private Map<Vessel, List<Container>> allocatedVT = new HashMap<>();

    /** Allocated bookinga Rail -- Vessel. */
    private Map<Vessel, List<Booking>> allocatedRV = new HashMap<>();

    /** Allocated bookings Barge -- Vessel. */
    private Map<Vessel, List<Booking>> allocatedBV = new HashMap<>();

    /** Allocated bookings Truck -- Vessel. */
    private Map<Vessel, List<Booking>> allocatedTV = new HashMap<>();

    /**
     * Calculate a unique ID for a vessel-to-vessel transfer.
     * @param vesselFrom the vessel the container originates from
     * @param vesselTo the vessel the container is transferred to
     * @return a unique id based on the from and to vessel
     */
    private int calculateVV(final Vessel vesselFrom, final Vessel vesselTo)
    {
        return (vesselFrom.getVesselNr() << 16) + vesselTo.getVesselNr();
    }

    //////////////////////////////////////// UNALLOCATED IN ///////////////////////////////////////////

    public void addUnallocatedIn(final Vessel vessel, final Container container)
    {
        this.unallocatedIn.putIfAbsent(vessel, new ArrayList<>()).add(container);
    }

    public void removeUnallocatedIn(final Vessel vessel, final Container container)
    {
        this.unallocatedIn.putIfAbsent(vessel, new ArrayList<>()).remove(container);
    }

    public List<Container> getUnallocatedIn(final Vessel vessel)
    {
        return this.unallocatedIn.get(vessel);
    }

    public List<Vessel> getUnallocatedInVessels()
    {
        return new ArrayList<>(this.unallocatedIn.keySet());
    }

    //////////////////////////////////////// UNALLOCATED OUT ///////////////////////////////////////////

    public void addUnallocatedOut(final Vessel vessel, final Booking booking)
    {
        this.unallocatedOut.putIfAbsent(vessel, new ArrayList<>()).add(booking);
    }

    public void removeUnallocatedOut(final Vessel vessel, final Booking booking)
    {
        this.unallocatedOut.putIfAbsent(vessel, new ArrayList<>()).remove(booking);
    }

    public List<Booking> getUnallocatedOut(final Vessel vessel)
    {
        return this.unallocatedOut.get(vessel);
    }

    public List<Vessel> getUnallocatedOutVessels()
    {
        return new ArrayList<>(this.unallocatedOut.keySet());
    }

    ///////////////////////////////////////////// IN RAIL ////////////////////////////////////////////////

    public void addAllocatedInRail(final Vessel vessel, final Container container)
    {
        removeUnallocatedIn(vessel, container);
        this.allocatedVR.putIfAbsent(vessel, new ArrayList<>()).add(container);
    }

    public void removeAllocatedInRail(final Vessel vessel, final Container container)
    {
        this.allocatedVR.putIfAbsent(vessel, new ArrayList<>()).remove(container);
    }

    public List<Container> getAllocatedInRail(final Vessel vessel)
    {
        return this.allocatedVR.get(vessel);
    }

    public List<Vessel> getAllocatedInRailVessels()
    {
        return new ArrayList<>(this.allocatedVR.keySet());
    }

    ///////////////////////////////////////////// IN BARGE ////////////////////////////////////////////////

    public void addAllocatedInBarge(final Vessel vessel, final Container container)
    {
        removeUnallocatedIn(vessel, container);
        this.allocatedVB.putIfAbsent(vessel, new ArrayList<>()).add(container);
    }

    public void removeAllocatedInBarge(final Vessel vessel, final Container container)
    {
        this.allocatedVB.putIfAbsent(vessel, new ArrayList<>()).remove(container);
    }

    public List<Container> getAllocatedInBarge(final Vessel vessel)
    {
        return this.allocatedVB.get(vessel);
    }

    public List<Vessel> getAllocatedInBargeVessels()
    {
        return new ArrayList<>(this.allocatedVB.keySet());
    }

    ///////////////////////////////////////////// IN TRUCK ////////////////////////////////////////////////

    public void addAllocatedInTruck(final Vessel vessel, final Container container)
    {
        removeUnallocatedIn(vessel, container);
        this.allocatedVT.putIfAbsent(vessel, new ArrayList<>()).add(container);
    }

    public void removeAllocatedInTruck(final Vessel vessel, final Container container)
    {
        this.allocatedVT.putIfAbsent(vessel, new ArrayList<>()).remove(container);
    }

    public List<Container> getAllocatedInTruck(final Vessel vessel)
    {
        return this.allocatedVT.get(vessel);
    }

    public List<Vessel> getAllocatedInTruckVessels()
    {
        return new ArrayList<>(this.allocatedVT.keySet());
    }

    ///////////////////////////////////////////// OUT RAIL ////////////////////////////////////////////////

    public void addAllocatedOutRail(final Vessel vessel, final Booking booking)
    {
        removeUnallocatedOut(vessel, booking);
        this.allocatedRV.putIfAbsent(vessel, new ArrayList<>()).add(booking);
    }

    public void removeAllocatedOutRail(final Vessel vessel, final Booking booking)
    {
        this.allocatedRV.putIfAbsent(vessel, new ArrayList<>()).remove(booking);
    }

    public List<Booking> getAllocatedOutRail(final Vessel vessel)
    {
        return this.allocatedRV.get(vessel);
    }

    public List<Vessel> getAllocatedOutRailVessels()
    {
        return new ArrayList<>(this.allocatedRV.keySet());
    }

    ///////////////////////////////////////////// OUT BARGE ////////////////////////////////////////////////

    public void addAllocatedOutBarge(final Vessel vessel, final Booking booking)
    {
        removeUnallocatedOut(vessel, booking);
        this.allocatedBV.putIfAbsent(vessel, new ArrayList<>()).add(booking);
    }

    public void removeAllocatedOutBarge(final Vessel vessel, final Booking booking)
    {
        this.allocatedBV.putIfAbsent(vessel, new ArrayList<>()).remove(booking);
    }

    public List<Booking> getAllocatedOutBarge(final Vessel vessel)
    {
        return this.allocatedBV.get(vessel);
    }

    public List<Vessel> getAllocatedOutBargeVessels()
    {
        return new ArrayList<>(this.allocatedBV.keySet());
    }

    ///////////////////////////////////////////// OUT TRUCK ////////////////////////////////////////////////

    public void addAllocatedOutTruck(final Vessel vessel, final Booking booking)
    {
        removeUnallocatedOut(vessel, booking);
        this.allocatedTV.putIfAbsent(vessel, new ArrayList<>()).add(booking);
    }

    public void removeAllocatedOutTruck(final Vessel vessel, final Booking booking)
    {
        this.allocatedTV.putIfAbsent(vessel, new ArrayList<>()).remove(booking);
    }

    public List<Booking> getAllocatedOutTruck(final Vessel vessel)
    {
        return this.allocatedTV.get(vessel);
    }

    public List<Vessel> getAllocatedOutTruckVessels()
    {
        return new ArrayList<>(this.allocatedTV.keySet());
    }

    ///////////////////////////////////////////// OUT VESSEL ////////////////////////////////////////////////

}
