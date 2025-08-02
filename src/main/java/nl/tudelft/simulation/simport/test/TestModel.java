package nl.tudelft.simulation.simport.test;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteUniform;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.clocktime.ClockTime;
import nl.tudelft.simulation.simport.dsol.ClockDevsSimulator;
import nl.tudelft.simulation.simport.dsol.ClockSimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.Terminal;
import nl.tudelft.simulation.simport.vessel.VesselGeneratorDist;

/**
 * TestModel.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestModel extends PortModel
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param simulator
     */
    public TestModel(final ClockSimulatorInterface simulator)
    {
        super(simulator);
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        var terminal = new Terminal("APMT-2", this, 20_000);
        var stream = getDefaultStream();
        // @formatter:off
        var vesselGenerator = new VesselGeneratorDist("gen", this, terminal)
                .setShipIatWeekdays(new DistContinuousDuration(new DistTriangular(stream, 7.0, 11.0, 16.0), DurationUnit.HOUR))
                .setShipIatWeekends(new DistContinuousDuration(new DistTriangular(stream, 12.0, 17.0, 22.0), DurationUnit.HOUR))
                .setCallSizeDistLoading(new DistDiscreteUniform(stream, 500, 3000))
                .setCallSizeDistUnloading(new DistDiscreteUniform(stream, 500, 3000))
                .setFraction20ftLoading(0.2)
                .setFraction20ftUnloading(0.2)
                .setFractionEmptyLoading(0.8)
                .setFractionEmptyUnloading(0.2)
                .setFractionReeferLoading(0.02)
                .setFractionReeferUnloading(0.02);
        // @formatter:on
        vesselGenerator.start();
    }

    public static void main(final String[] args)
    {
        var simulator = new ClockDevsSimulator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
        var replication = new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));
        var model = new TestModel(simulator);
        simulator.initialize(model, replication);
        simulator.start();
    }

}
