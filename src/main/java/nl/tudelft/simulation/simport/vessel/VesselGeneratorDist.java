package nl.tudelft.simulation.simport.vessel;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.jstats.distributions.Dist;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.simport.Location;
import nl.tudelft.simulation.simport.container.Booking;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;

/**
 * VesselGeneratorDist creates vessel visits based on a distribution for:
 * <ul>
 * <li>ship interarrival times on weekdays</li>
 * <li>ship interarrival times on weekends</li>
 * <li>call size distribution for unloading</li>
 * <li>call size distribution for loading</li>
 * <li>20/40 ft ratio for unloading</li>
 * <li>20/40 ft ratio for loading</li>
 * <li>fraction empty for unloading</li>
 * <li>fraction empty for loading</li>
 * <li>fraction reefers for unloading</li>
 * <li>fraction reefers for loading</li>
 * </ul>
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VesselGeneratorDist extends VesselGenerator
{
    /** The vessel interarrival times on weekdays. */
    private DistContinuousDuration vesselIatWeekdays;

    /** The vessel interarrival times on weekends. */
    private DistContinuousDuration vesselIatWeekends;

    /** The call size distribution (discrete) for unloading. */
    private DistDiscrete callSizeDistDiscUnloading;

    /** The call size distribution (discrete) for loading. */
    private DistDiscrete callSizeDistDiscLoading;

    /** The call size distribution (continuous) for unloading. */
    private DistContinuous callSizeDistContUnloading;

    /** The call size distribution (continuous) for loading. */
    private DistContinuous callSizeDistContLoading;

    /** The 20/40 ft ratio for unloading. */
    private double fraction20ftUnloading = Double.NaN;

    /** The 20/40 ft ratio for loading. */
    private double fraction20ftLoading = Double.NaN;

    /** The fraction empty for unloading. */
    private double fractionEmptyUnloading = Double.NaN;

    /** The fraction empty for loading. */
    private double fractionEmptyLoading = Double.NaN;

    /** The fraction reefers for unloading. */
    private double fractionReeferUnloading = Double.NaN;

    /** The fraction reefers for loading. */
    private double fractionReeferLoading = Double.NaN;

    /** stopped? */
    private boolean stopped = true;

    /**
     * Create a VesselGenerator based on distributions for one Terminal.
     * @param id the generator's id
     * @param model the model
     * @param terminal the terminal to generate ships for
     * @param vesselType the vessel type to generate
     */
    public VesselGeneratorDist(final String id, final PortModel model, final Terminal terminal, final VesselType vesselType)
    {
        super(id, model, terminal, vesselType);
    }

    @Override
    public void start()
    {
        Throw.when(!this.stopped, IllegalStateException.class, "Starting an already started vessel generator " + getId());
        this.stopped = false;
        Throw.whenNull(this.vesselIatWeekdays, "vesselIatWeekdays");
        Throw.whenNull(this.vesselIatWeekends, "vesselIatWeekends");
        Throw.when(this.callSizeDistDiscUnloading == null && this.callSizeDistContUnloading == null, NullPointerException.class,
                "callSizeDistUnloading not defined");
        Throw.when(this.callSizeDistDiscLoading == null && this.callSizeDistContLoading == null, NullPointerException.class,
                "callSizeDistLoading not defined");
        Throw.whenNaN(this.fraction20ftUnloading, "fraction20ftUnloading");
        Throw.whenNaN(this.fraction20ftLoading, "fraction20ftLoading");
        Throw.whenNaN(this.fractionEmptyUnloading, "fractionEmptyUnloading");
        Throw.whenNaN(this.fractionEmptyLoading, "fractionEmptyLoading");
        Throw.whenNaN(this.fractionReeferUnloading, "fractionReeferUnloading");
        Throw.whenNaN(this.fractionReeferLoading, "fractionReeferLoading");
        getSimulator().scheduleEventRel(this.vesselIatWeekdays.draw(), () -> nextWeekday());
        getSimulator().scheduleEventRel(this.vesselIatWeekends.draw(), () -> nextWeekend());
    }

    @Override
    public void stop()
    {
        Throw.when(this.stopped, IllegalStateException.class, "Stopping an already stopped vessel generator " + getId());
        this.stopped = true;
    }

    /**
     * Make the unload list for a vessel, and put the physical containers on the vessel.
     * @param vessel the vessel (deepsea or feeder)
     * @param containerList the list of containers on the vessel
     * @return the unload bookings for the vessel
     */
    protected List<Booking> makeUnloadList(final Vessel vessel, final List<Container> containerList)
    {
        List<Booking> bookingList = new ArrayList<>();
        int callSizeTEU = drawCallSizeUnloading();
        // #cont = #teu / (2.0 - frac20), because c.f + 2.c.(1-f) = t => c = t / (2 - f)
        int nrContainers = (int) (callSizeTEU / (2.0 - this.fraction20ftUnloading));
        StreamInterface rng = getModel().getDefaultStream();
        for (int i = 0; i < nrContainers; i++)
        {
            byte size = rng.nextDouble() < this.fraction20ftUnloading ? (byte) 20 : (byte) 40;
            boolean empty = rng.nextDouble() < this.fractionEmptyUnloading;
            boolean reefer = rng.nextDouble() < this.fractionReeferUnloading;
            Location loc = getVesselType().isDeepSea() ? Location.DEEPSEA : Location.FEEDER;
            Container container = new Container(getModel().uniqueContainerNr(), size, empty, reefer, loc);
            container.setVesselInNr(vessel.getVesselNr());
            Booking booking = new Booking(vessel, true, getModel().uniqueBookingNr(), size, empty, reefer);
            booking.setContainer(container);
            bookingList.add(booking);
            containerList.add(container);
        }
        return bookingList;
    }

    /**
     * Make the load list for a vessel (bookings only).
     * @param vessel the vessel (deepsea or feeder)
     * @return the load bookings for the vessel
     */
    protected List<Booking> makeLoadList(final Vessel vessel)
    {
        List<Booking> bookingList = new ArrayList<>();
        int callSizeTEU = drawCallSizeLoading();
        // #cont = #teu / (2.0 - frac20), because c.f + 2.c.(1-f) = t => c = t / (2 - f)
        int nrBookings = (int) (callSizeTEU / (2.0 - this.fraction20ftLoading));
        StreamInterface rng = getModel().getDefaultStream();
        for (int i = 0; i < nrBookings; i++)
        {
            byte size = rng.nextDouble() < this.fraction20ftLoading ? (byte) 20 : (byte) 40;
            boolean empty = rng.nextDouble() < this.fractionEmptyLoading;
            boolean reefer = rng.nextDouble() < this.fractionReeferLoading;
            Booking booking = new Booking(vessel, true, getModel().uniqueBookingNr(), size, empty, reefer);
            bookingList.add(booking);
        }
        return bookingList;
    }

    protected void nextWeekday()
    {
        if (this.stopped)
            return;
        // is it a weekday?
        if (getSimulator().getSimulatorClockTime().dayOfWeekInt() <= 5)
        {
            generateVessel();
        }
        getSimulator().scheduleEventRel(this.vesselIatWeekdays.draw(), () -> nextWeekday());
    }

    protected void nextWeekend()
    {
        if (this.stopped)
            return;
        // is it a weekend?
        if (getSimulator().getSimulatorClockTime().dayOfWeekInt() >= 6)
        {
            generateVessel();
        }
        getSimulator().scheduleEventRel(this.vesselIatWeekends.draw(), () -> nextWeekend());
    }

    protected void generateVessel()
    {
        Duration etaAdvance = Duration.valueOf(getModel()
                .getInputParameterString(getVesselType().isDeepSea() ? "terminal.GenerateDeepsea" : "terminal.GenerateFeeder"));
        var eta = new ClockTime(getSimulator().getSimulatorClockTime().plus(etaAdvance));
        var etd = new ClockTime(eta.plus(new Duration(1.0, DurationUnit.DAY)));
        var vessel = new Vessel(getVesselType(), getModel(), eta, etd, getTerminal());
        getModel().addVessel(vessel);
        vessel.setLoadList(makeLoadList(vessel));
        getTerminal().addToUnallocatedExportMap(vessel);
        vessel.setUnloadList(makeUnloadList(vessel, vessel.getContainerList()));
        getTerminal().addToUnallocatedImportMap(vessel);
    }

    /**
     * @return shipIatWeekdays
     */
    public DistContinuousDuration getShipIatWeekdays()
    {
        return this.vesselIatWeekdays;
    }

    /**
     * @param shipIatWeekdays set shipIatWeekdays
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setShipIatWeekdays(final DistContinuousDuration shipIatWeekdays)
    {
        this.vesselIatWeekdays = shipIatWeekdays;
        return this;
    }

    /**
     * @return shipIatWeekends
     */
    public DistContinuousDuration getShipIatWeekends()
    {
        return this.vesselIatWeekends;
    }

    /**
     * @param shipIatWeekends set shipIatWeekends
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setShipIatWeekends(final DistContinuousDuration shipIatWeekends)
    {
        this.vesselIatWeekends = shipIatWeekends;
        return this;
    }

    /**
     * @return callSizeDistUnloading
     */
    public Dist getCallSizeDistUnloading()
    {
        return this.callSizeDistContUnloading != null ? this.callSizeDistContUnloading : this.callSizeDistDiscUnloading;
    }

    /**
     * @param callSizeDistUnloading set callSizeDistUnloading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setCallSizeDistUnloading(final DistDiscrete callSizeDistUnloading)
    {
        this.callSizeDistDiscUnloading = callSizeDistUnloading;
        this.callSizeDistContUnloading = null;
        return this;
    }

    /**
     * @param callSizeDistUnloading set callSizeDistUnloading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setCallSizeDistUnloading(final DistContinuous callSizeDistUnloading)
    {
        this.callSizeDistDiscUnloading = null;
        this.callSizeDistContUnloading = callSizeDistUnloading;
        return this;
    }

    public int drawCallSizeUnloading()
    {
        return this.callSizeDistContUnloading != null ? (int) this.callSizeDistContUnloading.draw()
                : (int) this.callSizeDistDiscUnloading.draw();
    }

    /**
     * @return callSizeDistLoading
     */
    public Dist getCallSizeDistLoading()
    {
        return this.callSizeDistContLoading != null ? this.callSizeDistContLoading : this.callSizeDistDiscLoading;
    }

    /**
     * @param callSizeDistLoading set callSizeDistLoading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setCallSizeDistLoading(final DistDiscrete callSizeDistLoading)
    {
        this.callSizeDistDiscLoading = callSizeDistLoading;
        this.callSizeDistContLoading = null;
        return this;
    }

    /**
     * @param callSizeDistLoading set callSizeDistLoading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setCallSizeDistLoading(final DistContinuous callSizeDistLoading)
    {
        this.callSizeDistDiscLoading = null;
        this.callSizeDistContLoading = callSizeDistLoading;
        return this;
    }

    public int drawCallSizeLoading()
    {
        return this.callSizeDistContLoading != null ? (int) this.callSizeDistContLoading.draw()
                : (int) this.callSizeDistDiscLoading.draw();
    }

    /**
     * @return fraction20ftUnloading
     */
    public double getFraction20ftUnloading()
    {
        return this.fraction20ftUnloading;
    }

    /**
     * @param fraction20ftUnloading set fraction20ftUnloading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setFraction20ftUnloading(final double fraction20ftUnloading)
    {
        this.fraction20ftUnloading = fraction20ftUnloading;
        return this;
    }

    /**
     * @return fraction20ftLoading
     */
    public double getFraction20ftLoading()
    {
        return this.fraction20ftLoading;
    }

    /**
     * @param fraction20ftLoading set fraction20ftLoading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setFraction20ftLoading(final double fraction20ftLoading)
    {
        this.fraction20ftLoading = fraction20ftLoading;
        return this;
    }

    /**
     * @return fractionEmptyUnloading
     */
    public double getFractionEmptyUnloading()
    {
        return this.fractionEmptyUnloading;
    }

    /**
     * @param fractionEmptyUnloading set fractionEmptyUnloading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setFractionEmptyUnloading(final double fractionEmptyUnloading)
    {
        this.fractionEmptyUnloading = fractionEmptyUnloading;
        return this;
    }

    /**
     * @return fractionEmptyLoading
     */
    public double getFractionEmptyLoading()
    {
        return this.fractionEmptyLoading;
    }

    /**
     * @param fractionEmptyLoading set fractionEmptyLoading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setFractionEmptyLoading(final double fractionEmptyLoading)
    {
        this.fractionEmptyLoading = fractionEmptyLoading;
        return this;
    }

    /**
     * @return fractionReeferUnloading
     */
    public double getFractionReeferUnloading()
    {
        return this.fractionReeferUnloading;
    }

    /**
     * @param fractionReeferUnloading set fractionReeferUnloading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setFractionReeferUnloading(final double fractionReeferUnloading)
    {
        this.fractionReeferUnloading = fractionReeferUnloading;
        return this;
    }

    /**
     * @return fractionReeferLoading
     */
    public double getFractionReeferLoading()
    {
        return this.fractionReeferLoading;
    }

    /**
     * @param fractionReeferLoading set fractionReeferLoading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setFractionReeferLoading(final double fractionReeferLoading)
    {
        this.fractionReeferLoading = fractionReeferLoading;
        return this;
    }

}
