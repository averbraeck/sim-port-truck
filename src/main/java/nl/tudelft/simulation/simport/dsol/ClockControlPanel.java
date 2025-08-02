package nl.tudelft.simulation.simport.dsol;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.Event;

import nl.tudelft.simulation.dsol.model.DsolModel;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.RunState;
import nl.tudelft.simulation.dsol.swing.gui.control.ClockPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.DevsControlPanel;
import nl.tudelft.simulation.dsol.swing.gui.control.RunUntilPanel;

/**
 * ControlPanel container for the a DEVS simulator, with clocks for a djunits double timeunit.
 * <p>
 * Copyright (c) 2020-2025 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://simulation.tudelft.nl/dsol/manual/" target="_blank">DSOL Manual</a>. The DSOL
 * project is distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://simulation.tudelft.nl/dsol/docs/latest/license.html" target="_blank">DSOL License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class ClockControlPanel extends DevsControlPanel<Duration, ClockDevsAnimator> implements PropertyChangeListener
{
    /** */
    private static final long serialVersionUID = 20201227L;

    /** The timeWarpPanel to control the speed. */
    private final ClockRunSpeedSliderPanel runSpeedSliderPanel;

    /** The default animation delay (stored during fast forward). */
    private long savedAnimationDelay = 100L;

    /**
     * Construct a real time control panel for a djunits double time unit, with a different set of control buttons. The control
     * panel assumes a DevsSimulator and animation. The model specification is not necessarily specified as "real time"; its
     * execution is.
     * @param model DsolModel&lt;Duration&gt;; the model for the control panel, to allow a reset of the model
     * @param simulator DevsRealTimeAnimator&lt;Duration&gt;; the simulator. Specified separately, because the model can have
     *            been specified with a superclass of the simulator that the ControlPanel actually needs (e.g., model has been
     *            specified with a DevsAnimator, whereas the panel needs a RealTimeControlAnimator)
     * @throws RemoteException when simulator cannot be accessed for listener attachment
     */
    public ClockControlPanel(final DsolModel<Duration, ClockSimulatorInterface> model, final ClockDevsAnimator simulator)
            throws RemoteException
    {
        super(model, simulator);

        getControlButtonsPanel().add(makeButton("fastForwardButton", "/resources/FastForward.png", "FastForward",
                "Run the simulation as fast as possible", true));

        this.runSpeedSliderPanel = new ClockRunSpeedSliderPanel(3600.0, getSimulator());
        add(this.runSpeedSliderPanel);

        getSimulator().addListener(this, DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT);

        var cp = new ClockPanel.TimeDoubleUnit(getSimulator())
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected String formatSimulationTime(final Duration simulationTime)
            {
                var dt = ((ClockDevsAnimator) getSimulator()).getSimulatorClockTime().localDateTime();
                return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            }
        };
        setClockPanel(cp);
        // setRunUntilPanel(new RunUntilPanel.TimeDoubleUnit(getSimulator()));
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        try
        {
            if (actionCommand.equals("FastForward"))
            {
                if (getSimulator().isStoppingOrStopped())
                {
                    this.savedAnimationDelay = getSimulator().getAnimationDelay();
                    getSimulator().setAnimationDelay(0L);
                    getSimulator().setUpdateMsec(1000);
                    getSimulator().setAnimationDelay(500); // 2 Hz
                    getSimulator().start();
                }
            }
            if (actionCommand.equals("RunPause") || actionCommand.equals("Reset"))
            {
                getSimulator().setAnimationDelay(this.savedAnimationDelay);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        super.actionPerformed(actionEvent); // includes fixButtons()
    }

    @Override
    protected void fixButtons()
    {
        final boolean moreWorkToDo = getSimulator().getRunState() != RunState.ENDED;
        for (JButton button : getControlButtons())
        {
            final String actionCommand = button.getActionCommand();
            if (actionCommand.equals("FastForward"))
            {
                button.setEnabled(moreWorkToDo && isControlButtonsEnabled() && getSimulator().isStoppingOrStopped());
            }
        }
        super.fixButtons(); // handles the start/stop button
    }

    @Override
    protected void invalidateButtons()
    {
        for (JButton button : getControlButtons())
        {
            final String actionCommand = button.getActionCommand();
            if (actionCommand.equals("FastForward"))
            {
                button.setEnabled(false);
            }
        }
        super.invalidateButtons(); // handles the start/stop button
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt)
    {
        // TODO: when external change on speed -- update the slider panel
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT))
        {
            this.runSpeedSliderPanel.setSpeedFactor((Double) event.getContent());
            fixButtons();
        }
        super.notify(event);
    }

}
