package nl.tudelft.simulation.simport.freightforwarder;

import org.djutils.base.Identifiable;
import org.djutils.event.LocalEventProducer;

/**
 * The FreightForwarder plans the mode of transport for a container, as well as the pickup and dropoff time of the cargo. The
 * FreightForwarder books trucks at one of the trucking firms, and (in case it is modelled in detail) the trains and barges. The
 * model can work with one 'central' freight forwarder, a freight forwarder per terminal, or a large number of competing freight
 * forwarders.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FreightForwarder extends LocalEventProducer implements Identifiable
{
    /** the id. */
    private final String id;

    /**
     * Instantiate a new Freight Forwarder.
     * @param id the id of the FF
     */
    public FreightForwarder(final String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

}
