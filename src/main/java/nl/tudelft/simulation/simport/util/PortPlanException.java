package nl.tudelft.simulation.simport.util;

/**
 * PortPlanException.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PortPlanException extends Exception
{

    /**
     * 
     */
    public PortPlanException()
    {
    }

    /**
     * @param message
     */
    public PortPlanException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public PortPlanException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public PortPlanException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public PortPlanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
