package nl.tudelft.simulation.simport.test;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.Event;
import org.djutils.event.EventListener;

import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.simulators.DevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulator;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * PortApp.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PortApp implements EventListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** simulator. */
    private final DevsSimulatorInterface<Duration> simulator;

    /** model. */
    private final PortModel model;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        var simulator = new ClockDevsSimulator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
        var model = new TestCsvModel(simulator);
        var replication = new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));
        simulator.initialize(model, replication);
        new PortApp(simulator, model);
        simulator.start();
    }

    /**
     * Create the simulation.
     * @param simulator the simulator
     * @param model the model
     */
    public PortApp(final DevsSimulatorInterface<Duration> simulator, final PortModel model)
    {
        this.simulator = simulator;
        this.model = model;
        simulator.addListener(this, Replication.END_REPLICATION_EVENT);
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(Replication.END_REPLICATION_EVENT))
        {
            reportStats();
        }
        else if (event.getType().equals(Experiment.END_EXPERIMENT_EVENT))
        {
            reportFinalStats();
        }
    }

    /**
     * Report statistics at the end of a replication.
     */
    protected void reportStats()
    {
        // this.model.getPersistentUtilization().endObservations(this.model.getSimulator().getReplication().getRunLength());
        // this.model.getPersistentQueueLength().endObservations(this.model.getSimulator().getReplication().getRunLength());

        System.out.println("\nStatistics of replication : " + this.model.getSimulator().getReplication().getId());
        // System.out.println(SimCounter.reportHeader());
        // System.out.println(this.model.getContainerCounter().reportLine());
        // System.out.println(this.model.getVesselCounter().reportLine());
        // System.out.println(SimCounter.reportFooter());

        // System.out.println(SimTally.reportHeader());
        // System.out.println(this.model.getTallyTimeInQueue().reportLine());
        // System.out.println(this.model.getTallyTimeInSystem().reportLine());
        // System.out.println(SimTally.reportFooter());

        // System.out.println(SimPersistent.reportHeader());
        // System.out.println(this.model.getPersistentQueueLength().reportLine());
        // System.out.println(this.model.getPersistentUtilization().reportLine());
        // System.out.println(SimPersistent.reportFooter());
    }

    /**
     * Report the final statistics
     */
    protected void reportFinalStats()
    {
        /*-
        System.out.println("\nFinal statistics:");
        SortedMap<String, SortedMap<String, Tally>> stats = this.experiment.getSummaryStatistics();
        for (String statMapKey : stats.keySet())
        {
            System.out.println("\nSummary statistic for: " + statMapKey);
            System.out.println(Tally.reportHeader());
            SortedMap<String, Tally> statMap = stats.get(statMapKey);
            for (String statKey : statMap.keySet())
            {
                Tally stat = statMap.get(statKey);
                System.out.println(stat.reportLine());
            }
            System.out.println(Tally.reportFooter());
        }
        */
    }

}
