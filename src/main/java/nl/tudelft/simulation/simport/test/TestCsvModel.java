package nl.tudelft.simulation.simport.test;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulator;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.TerminalCsv;
import nl.tudelft.simulation.simport.vessel.VesselDistCsv;

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
    public TestCsvModel(final ClockDevsSimulatorInterface simulator)
    {
        super(simulator);
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        TerminalCsv.readTerminals(this, "/resources/test/terminals.csv");
        VesselDistCsv.readVesselDist(this, "/resources/test/vesselDist.csv");
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
