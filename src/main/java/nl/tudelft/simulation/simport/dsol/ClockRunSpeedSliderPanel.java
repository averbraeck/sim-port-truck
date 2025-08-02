package nl.tudelft.simulation.simport.dsol;

import java.awt.Dimension;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.swing.multislider.CategorialMultiSlider;

import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;

public class ClockRunSpeedSliderPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 20150408L;

    /** The JSlider that the user sees. */
    private final CategorialMultiSlider<Double> slider;

    /** The values at each tick. */
    private Map<Integer, Double> tickValues = new LinkedHashMap<>();

    /**
     * Construct a new TimeWarpPanel.
     * @param initialValue double; the initially selected value on the scale
     * @param simulator DevsSimulatorInterface&lt;?, ?, ?&gt;; the simulator to change the speed of
     */
    public ClockRunSpeedSliderPanel(final double initialValue, final DevsRealTimeAnimator<Duration> simulator)
    {
        List<Double> scale =
                List.of(1.0, 10.0, 60.0, 600.0, 3600.0, 6 * 3600.0, 24 * 3600.0, 5 * 24 * 3600.0, 30 * 24 * 3600.0, 1.0E9);
        List<String> label = List.of("1s", "10s", "1m", "10m", "1u", "6u", "1d", "5d", "30d", "oo");
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        for (int i = 0; i < scale.size(); i++)
        {
            labels.put(i, new JLabel(label.get(i)));
            this.tickValues.put(i, scale.get(i));
        }
        this.slider = new CategorialMultiSlider<>(scale, initialValue);
        this.slider.setLabelTable(labels);
        this.slider.setMajorTickSpacing(1);
        this.slider.setMinorTickSpacing(1);
        this.slider.setPaintTicks(true);
        this.slider.setPaintLabels(true);
        this.slider.setPreferredSize(new Dimension(400, 40));
        this.slider.setMinimumSize(new Dimension(400, 40));
        this.slider.setSize(new Dimension(400, 40));
        this.add(this.slider);

        // initial value of simulation speed
        if (simulator instanceof DevsRealTimeAnimator)
        {
            DevsRealTimeAnimator<?> clock = (DevsRealTimeAnimator<?>) simulator;
            clock.setSpeedFactor(this.slider.getValue(0));
        }

        // adjust the simulation speed
        this.slider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent ce)
            {
                @SuppressWarnings("unchecked")
                CategorialMultiSlider<Double> source = (CategorialMultiSlider<Double>) ce.getSource();
                if (simulator instanceof DevsRealTimeAnimator)
                {
                    DevsRealTimeAnimator<?> clock = (DevsRealTimeAnimator<?>) simulator;
                    clock.setSpeedFactor(source.getValue(0));
                }
            }
        });
    }

    /**
     * Access to tickValues map from within the event handler.
     * @return Map&lt;Integer, Double&gt; the tickValues map of this TimeWarpPanel
     */
    protected Map<Integer, Double> getTickValues()
    {
        return this.tickValues;
    }

    /**
     * Retrieve the current TimeWarp factor.
     * @return double; the current TimeWarp factor
     */
    public double getFactor()
    {
        return this.slider.getValue(0);
    }

    @Override
    public String toString()
    {
        return "TimeWarpPanel [timeWarp=" + this.getFactor() + "]";
    }

    /**
     * Set the time warp factor to the best possible approximation of a given value.
     * @param factor double; the requested speed factor
     */
    public void setSpeedFactor(final double factor)
    {
        //
    }
}
