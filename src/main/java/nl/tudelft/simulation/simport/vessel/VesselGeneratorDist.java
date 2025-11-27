package nl.tudelft.simulation.simport.vessel;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
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

    /** The call size distribution for unloading. */
    private DistDiscrete callSizeDistUnloading;

    /** The call size distribution for loading. */
    private DistDiscrete callSizeDistLoading;

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
        this.stopped = false;
        Throw.whenNull(this.vesselIatWeekdays, "vesselIatWeekdays");
        Throw.whenNull(this.vesselIatWeekends, "vesselIatWeekends");
        Throw.whenNull(this.callSizeDistUnloading, "callSizeDistUnloading");
        Throw.whenNull(this.callSizeDistLoading, "callSizeDistLoading");
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
        this.stopped = true;
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
        String id = "";
        var eta = new ClockTime(getSimulator().getSimulatorClockTime()); // .plus(new Duration(1.0, DurationUnit.MINUTE)));
        var etd = new ClockTime(eta.plus(new Duration(1.0, DurationUnit.DAY)));
        var unloadInfo = new VesselLoadInfo((int) this.callSizeDistUnloading.draw(), this.fraction20ftUnloading,
                this.fractionEmptyUnloading, this.fractionReeferUnloading);
        var loadInfo = new VesselLoadInfo((int) this.callSizeDistLoading.draw(), this.fraction20ftLoading,
                this.fractionEmptyLoading, this.fractionReeferLoading);
        generateVessel(id, eta, etd, unloadInfo, loadInfo);
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
    public DistDiscrete getCallSizeDistUnloading()
    {
        return this.callSizeDistUnloading;
    }

    /**
     * @param callSizeDistUnloading set callSizeDistUnloading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setCallSizeDistUnloading(final DistDiscrete callSizeDistUnloading)
    {
        this.callSizeDistUnloading = callSizeDistUnloading;
        return this;
    }

    /**
     * @return callSizeDistLoading
     */
    public DistDiscrete getCallSizeDistLoading()
    {
        return this.callSizeDistLoading;
    }

    /**
     * @param callSizeDistLoading set callSizeDistLoading
     * @return the object itself for method chaining
     */
    public VesselGeneratorDist setCallSizeDistLoading(final DistDiscrete callSizeDistLoading)
    {
        this.callSizeDistLoading = callSizeDistLoading;
        return this;
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
