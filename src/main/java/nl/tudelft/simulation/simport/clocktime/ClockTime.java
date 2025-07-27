package nl.tudelft.simulation.simport.clocktime;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;

/**
 * ClockTime is an extension of DJUNITS time aware of the calendar and clock time.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ClockTime extends Time
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param value
     */
    public ClockTime(final Time value)
    {
        super(value);
    }

    /**
     * @param value
     * @param unit
     */
    public ClockTime(final double value, final TimeUnit unit)
    {
        super(value, unit);
    }

    public LocalDateTime localDateTime()
    {
        return LocalDateTime.ofEpochSecond(Math.round(getSI()), 0, ZoneOffset.UTC);
    }

    public String ymd()
    {
        return localDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String hms()
    {
        return localDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public String hm()
    {
        return hms().substring(0, 5);
    }

    public String ymdhm()
    {
        return ymd() + " " + hm();
    }

    public DayOfWeek dayOfWeek()
    {
        return localDateTime().getDayOfWeek();
    }

    /**
     * Return the int for the day of the week with 1=MONDAY, 7=SUNDAY.
     * @return the int for the day of the week with 1=MONDAY, 7=SUNDAY
     */
    public int dayOfWeekInt()
    {
        return dayOfWeek().getValue();
    }

    public static ClockTime ofIso(final String isoDateTime)
    {
        double seconds = LocalDateTime.parse(isoDateTime).toEpochSecond(ZoneOffset.UTC);
        return new ClockTime(seconds, TimeUnit.EPOCH_SECOND);
    }

    @Override
    public String toString()
    {
        return ymdhm();
    }

}
