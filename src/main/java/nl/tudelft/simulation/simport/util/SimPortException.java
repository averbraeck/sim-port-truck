package nl.tudelft.simulation.simport.util;

/**
 * SimPortException for project-specific exceptions.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SimPortException extends Exception
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a SimPortException.
     */
    public SimPortException()
    {
    }

    /**
     * Create a SimPortException.
     * @param message the message of the exception
     */
    public SimPortException(final String message)
    {
        super(message);
    }

    /**
     * Create a SimPortException.
     * @param cause underlying exception that caused this exception
     */
    public SimPortException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a SimPortException.
     * @param message the message of the exception
     * @param cause underlying exception that caused this exception
     */
    public SimPortException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
