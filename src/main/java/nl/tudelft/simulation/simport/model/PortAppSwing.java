package nl.tudelft.simulation.simport.model;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.bounds.Bounds2d;

import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.reader.ReadInputParameters;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulator;
import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleOutput;
import nl.tudelft.simulation.dsol.swing.gui.DsolPanel;
import nl.tudelft.simulation.dsol.swing.gui.LoggerConsole;
import nl.tudelft.simulation.dsol.swing.gui.animation.DsolAnimationApplication;
import nl.tudelft.simulation.dsol.swing.gui.animation.DsolAnimationGisTab;
import nl.tudelft.simulation.dsol.swing.gui.control.ClockPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.RealTimeControlPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.RunSpeedSliderPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.SpeedPanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
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
     * @param title the title
     * @param panel the panel
     * @param animationTab the (custom) animation tab
     * @throws DsolException when simulator is not an animator
     * @throws IllegalArgumentException for illegal bounds
     * @throws RemoteException on network error
     */
    public PortAppSwing(final String title, final DsolPanel panel, final DsolAnimationGisTab animationTab)
            throws DsolException, RemoteException, IllegalArgumentException
    {
        super(panel, title, animationTab);
        panel.enableSimulationControlButtons();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        String propertyFilename = (args.length > 0) ? args[0] : "/scenario-2022.properties";
        boolean interactive = (args.length < 2) || !args[1].toLowerCase().equals("batch");
        long seed = (args.length < 3) ? -1 : Long.valueOf(args[2]);
        if (interactive)
        {
            var simulator = new ClockDevsRealTimeAnimator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
            var model = new PortModelTruck(simulator, true);
            ReadInputParameters.loadfromProperties(propertyFilename, model.getInputParameterMap());
            ReadInputParameters.loadFromArgs(args, true, model.getInputParameterMap());
            setInputParametersDefaults(model.getInputParameterMap());
            var replication =
                    new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));
            if (TabbedParameterDialog.process(model.getInputParameterMap()))
            {
                simulator.initialize(model, replication);
                List<Double> speeds = List.of(1.0, 10.0, 60.0, 600.0, 3600.0, 6 * 3600.0, 24 * 3600.0, 5 * 24 * 3600.0,
                        30 * 24 * 3600.0, 1.0E9);
                List<String> labels = List.of("1s", "10s", "1m", "10m", "1h", "6h", "1d", "5d", "30d", "oo");
                var runSpeedSliderPanel = new RunSpeedSliderPanel(speeds, labels, simulator, 3600.0);
                var controlPanel = new RealTimeControlPanel<Duration, DevsRealTimeAnimator<Duration>>(model, simulator,
                        runSpeedSliderPanel);
                controlPanel.setClockPanel(new ClockPanel.ClockTime(simulator, () -> simulator.getSimulatorClockTime()
                        .localDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
                controlPanel.getClockPanel().setPanelSize(new Dimension(160, 35));
                controlPanel.setSpeedPanel(new SpeedPanel.ClockTime(simulator));
                DsolPanel panel = new DsolPanel(controlPanel);

                Bounds2d mapBounds = new Bounds2d(3.98, 4.21, 51.92, 51.99);
                DsolAnimationGisTab animationTab = new DsolAnimationGisTab(mapBounds, simulator);
                animationTab.getAnimationPanel().setRenderableScale(
                        new RenderableScale(Math.cos(Math.toRadians(mapBounds.midPoint().getY())), 1.0 / 111319.24));
                animationTab.getAnimationPanel().setShowGrid(false);
                // animationTab.addAllToggleGISButtonText("MAP LAYERS", model.getGisMap(), "hide or show map layers");

                for (var gisRenderable : model.getGisMap().getInternalRenderableList())
                {
                    animationTab.addToggleText(" ");
                    animationTab.addToggleText(gisRenderable.getMap().getName());
                    for (String layerName : gisRenderable.getMap().getLayerMap().keySet())
                    {
                        animationTab.addToggleGISButtonText(layerName, layerName, gisRenderable, "hide or show map layers");
                    }
                }

                panel.addTab("logger", new LoggerConsole());
                panel.addTab("console", new ConsoleOutput());
                new PortAppSwing("PortModel", panel, animationTab);
            }
            else
            {
                System.exit(0);
            }
        }

        else

        {
            var simulator = new ClockDevsSimulator("sim", ClockTime.ofIso("2024-07-01T00:00:00"));
            var model = new PortModelTruck(simulator, false);
            ReadInputParameters.loadfromProperties(propertyFilename, model.getInputParameterMap());
            ReadInputParameters.loadFromArgs(args, true, model.getInputParameterMap());
            setInputParametersDefaults(model.getInputParameterMap());
            var replication =
                    new SingleReplication<>("rep1", Duration.ZERO, Duration.ZERO, new Duration(26.0, DurationUnit.WEEK));

        }
    }

    /**
     * Update the defaults for the parameters to be displayed to the loaded values from the properties file and the command
     * line. This ensures that the editing takes place on the basis of the provided information in the properties file and the
     * command line.
     * @param map InputParameterMap; the input parameters
     */
    @SuppressWarnings("unchecked")
    private static void setInputParametersDefaults(final InputParameterMap map)
    {
        for (Map.Entry<String, InputParameter<?, ?>> entry : map.getValue().entrySet())
        {
            @SuppressWarnings("rawtypes")
            InputParameter parameter = entry.getValue();
            if (parameter instanceof InputParameterMap)
            {
                setInputParametersDefaults((InputParameterMap) parameter);
            }
            try
            {
                parameter.setDefaultValue(parameter.getCalculatedValue());
            }
            catch (InputParameterException e)
            {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
