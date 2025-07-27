package nl.tudelft.simulation.dynportplan.test;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dynportplan.clocktime.ClockTime;
import nl.tudelft.simulation.dynportplan.dsol.ClockDevsSimulator;
import nl.tudelft.simulation.dynportplan.dsol.ClockSimulatorInterface;
import nl.tudelft.simulation.dynportplan.model.PortModel;
import nl.tudelft.simulation.dynportplan.vessel.VesselGeneratorDistCsv;

/**
 * TestModel.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestCsvModel extends PortModel
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param simulator
     */
    public TestCsvModel(final ClockSimulatorInterface simulator)
    {
        super(simulator);
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        VesselGeneratorDistCsv.generateVesselsCsv(this, "/resources/test/terminals.csv", "/resources/test/vesselDist.csv");
    }

    public static void main(final String[] args)
    {
        var simulator = new ClockDevsSimulator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
        var replication = new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));
        var model = new TestCsvModel(simulator);
        simulator.initialize(model, replication);
        simulator.start();
    }

}
