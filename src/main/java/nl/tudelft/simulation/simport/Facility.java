package nl.tudelft.simulation.simport;

import org.djutils.base.Identifiable;
import org.djutils.draw.point.Point3d;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Facility is any facility within or outside the port, such as a parking, terminal or depot.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface Facility extends Identifiable, Locatable
{
    /** @return the model. */
    PortModel getModel();

    /** @return the simulator. */
    default ClockDevsSimulatorInterface getSimulator()
    {
        return getModel().getSimulator();
    }

    /** @return the latitude. */
    double getLat();

    /** @return the longitude. */
    double getLon();

    @Override
    default Point3d getLocation()
    {
        return new Point3d(getLon(), getLat(), 1.0);
    }

}
