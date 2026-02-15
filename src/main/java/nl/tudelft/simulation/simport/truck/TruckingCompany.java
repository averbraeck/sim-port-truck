package nl.tudelft.simulation.simport.truck;

import java.util.ArrayList;
import java.util.List;

import org.djutils.base.Identifiable;

import nl.tudelft.simulation.simport.model.PortModel;

/**
 * TruckingCompany is the company that owns one or more trucks.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TruckingCompany implements Identifiable
{
    /** The id. */
    private final String id;

    /** The model. */
    private final PortModel model;

    /** The truck fleet. */
    private final List<Truck> fleet = new ArrayList<>();

    /**
     * Instantiate a trucking company.
     * @param id the id
     * @param model the model
     * @param nrTruck the number of trucks
     */
    public TruckingCompany(final String id, final PortModel model, final int nrTrucks)
    {
        this.id = id;
        this.model = model;
        for (int i = 0; i < nrTrucks; i++)
        {
            this.fleet.add(new Truck(model.uniqueTruckNr(), this, 100 + i));
        }
    }

    /**
     * @return the truck fleet
     */
    public List<Truck> getFleet()
    {
        return this.fleet;
    }

    /**
     * @return the port model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

}
