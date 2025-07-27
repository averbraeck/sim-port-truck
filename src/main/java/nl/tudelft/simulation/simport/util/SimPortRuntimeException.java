package nl.tudelft.simulation.simport.util;

/**
 * SimPortException for project-specific runtime exceptions.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SimPortRuntimeException extends RuntimeException
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a SimPortRuntimeException.
     */
    public SimPortRuntimeException()
    {
    }

    /**
     * Create a SimPortRuntimeException.
     * @param message the message of the exception
     */
    public SimPortRuntimeException(final String message)
    {
        super(message);
    }

    /**
     * Create a SimPortRuntimeException.
     * @param cause underlying exception that caused this exception
     */
    public SimPortRuntimeException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a SimPortRuntimeException.
     * @param message the message of the exception
     * @param cause underlying exception that caused this exception
     */
    public SimPortRuntimeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
