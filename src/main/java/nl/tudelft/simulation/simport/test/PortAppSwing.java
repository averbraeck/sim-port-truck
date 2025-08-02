package nl.tudelft.simulation.simport.test;

import java.rmi.RemoteException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.bounds.Bounds2d;
import org.pmw.tinylog.Level;

import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleLogger;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleOutput;
import nl.tudelft.simulation.dsol.swing.gui.DsolPanel;
import nl.tudelft.simulation.dsol.swing.gui.animation.DsolAnimationApplication;
import nl.tudelft.simulation.dsol.swing.gui.animation.DsolAnimationTab;
import nl.tudelft.simulation.language.DsolException;
import nl.tudelft.simulation.simport.clocktime.ClockTime;
import nl.tudelft.simulation.simport.dsol.ClockControlPanel;
import nl.tudelft.simulation.simport.dsol.ClockDevsAnimator;

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
        var simulator = new ClockDevsAnimator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
        var model = new TestCsvModel(simulator);
        var replication = new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));
        // new TabbedParameterDialog(model.getInputParameterMap());
        simulator.initialize(model, replication);
        DsolPanel panel = new DsolPanel(new ClockControlPanel(model, simulator));
        panel.addTab("logger", new ConsoleLogger(Level.INFO));
        panel.addTab("console", new ConsoleOutput());
        new PortAppSwing("PortModel", panel);
    }

}
