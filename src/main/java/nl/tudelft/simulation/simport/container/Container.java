package nl.tudelft.simulation.simport;

import org.djutils.base.Identifiable;

/**
 * Information about a container.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface Container extends Identifiable
{
    default String getType()
    {
        int size = getSize();
        if (isReefer())
            return size == 40 ? "42R1" : size + "R1";
        return size + "G1";
    }

    /** @return the container number */
    int getNr();

    @Override
    default String getId()
    {
        return String.valueOf(getNr());
    }

    /** @return size in ft (20/40/45) */
    int getSize();

    /** return true if 20ft, false if not */
    default boolean is20ft()
    {
        return getSize() == 20;
    }

    /** return true if 40ft, false if not */
    default boolean is40ft()
    {
        return getSize() == 40;
    }

    /** return the number of teu of this container */
    default double teu()
    {
        return getSize() / 20.0;
    }

    /** return the integer number of teu of this container, 1 for 20 ft, 2 for 40 ft or more */
    default int teuInt()
    {
        return getSize() <= 20 ? 1 : 2;
    }

    /** @return true if empty; false if full */
    boolean isEmpty();

    /** @return true if full; false if empty */
    default boolean isFull()
    {
        return !isEmpty();
    }

    /** @return true if reefer; false if normal container */
    boolean isReefer();

    /** @return true if normal container; false if reefer */
    default boolean isNormal()
    {
        return !isReefer();
    }

}
