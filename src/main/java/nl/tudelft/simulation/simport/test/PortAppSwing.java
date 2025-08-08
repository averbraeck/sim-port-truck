package nl.tudelft.simulation.simport.test;

import java.rmi.RemoteException;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.bounds.Bounds2d;
import org.pmw.tinylog.Level;

import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.model.DsolModel;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.DevsSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleLogger;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleOutput;
import nl.tudelft.simulation.dsol.swing.gui.DsolPanel;
import nl.tudelft.simulation.dsol.swing.gui.animation.DsolAnimationApplication;
import nl.tudelft.simulation.dsol.swing.gui.animation.DsolAnimationTab;
import nl.tudelft.simulation.dsol.swing.gui.control.ClockPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.RealTimeControlPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.RunSpeedSliderPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.RunUntilPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.SpeedPanel;
import nl.tudelft.simulation.language.DsolException;

/**
 * PortAppSwing.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PortAppSwing extends DsolAnimationApplication
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param title
     * @param panel panel
     * @throws DsolException
     * @throws IllegalArgumentException
     * @throws ArithmeticException
     * @throws RemoteException
     */
    public PortAppSwing(final String title, final DsolPanel panel)
            throws DsolException, RemoteException, ArithmeticException, IllegalArgumentException
    {
        super(panel, title, DsolAnimationTab.createAutoPanTab(new Bounds2d(-120, 120, -120, 120), panel.getSimulator()));
        panel.enableSimulationControlButtons();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        var simulator = new ClockDevsRealTimeAnimator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
        var model = new TestCsvModel(simulator);
        var replication = new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));
        // new TabbedParameterDialog(model.getInputParameterMap());
        simulator.initialize(model, replication);
        List<Double> speeds =
                List.of(1.0, 10.0, 60.0, 600.0, 3600.0, 6 * 3600.0, 24 * 3600.0, 5 * 24 * 3600.0, 30 * 24 * 3600.0, 1.0E9);
        List<String> labels = List.of("1s", "10s", "1m", "10m", "1h", "6h", "1d", "5d", "30d", "oo");
        var runSpeedSliderPanel = new RunSpeedSliderPanel(speeds, labels, simulator, 3600.0);
        ClockControlPanel controlPanel = new ClockControlPanel(model, simulator, runSpeedSliderPanel);
        DsolPanel panel = new DsolPanel(controlPanel);
        panel.addTab("logger", new ConsoleLogger(Level.INFO));
        panel.addTab("console", new ConsoleOutput());
        new PortAppSwing("PortModel", panel);
    }

    public static class ClockControlPanel extends RealTimeControlPanel<Duration, DevsRealTimeAnimator<Duration>>
    {
        /**
         * @param model
         * @param simulator
         * @param runSpeedSliderPanel
         * @throws RemoteException
         */
        public ClockControlPanel(final DsolModel<Duration, ? extends DevsSimulatorInterface<Duration>> model,
                final ClockDevsRealTimeAnimator simulator, final RunSpeedSliderPanel runSpeedSliderPanel) throws RemoteException
        {
            super(model, simulator, runSpeedSliderPanel);
            setClockPanel(new ClockPanel.TimeDoubleUnit(getSimulator())
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected String formatSimulationTime(final Duration simulationTime)
                {
                    return simulator.getSimulatorClockTime().ymd() + " " + simulator.getSimulatorClockTime().hm();
                }
            });
            setSpeedPanel(new SpeedPanel.TimeDoubleUnit(getSimulator()));
            setRunUntilPanel(new RunUntilPanel.TimeDoubleUnit(getSimulator()));
        }

    }
}
