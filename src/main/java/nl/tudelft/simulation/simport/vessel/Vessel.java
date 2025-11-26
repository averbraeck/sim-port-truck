package nl.tudelft.simulation.simport.vessel;

import org.djutils.base.Identifiable;
import org.djutils.event.EventProducer;

import nl.tudelft.simulation.dsol.simulators.clock.ClockTime;

/**
 * The Vessel interface is the contract for a Vessel class.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface Vessel extends Identifiable, EventProducer
{
    /**
     * Return the ETA for the vessel.
     * @return the ETA for the vessel
     */
    ClockTime getEta();

    /**
     * Return the ETD for the vessel.
     * @return the ETD for the vessel
     */
    ClockTime getEtd();

    /**
     * Return the ATA for the vessel.
     * @return the ATA for the vessel
     */
    ClockTime getAta();

    /**
     * Set a new ATA and (re)schedule the vessel arrival event.
     * @param ata new ATA
     */
    void setAta(final ClockTime ata);

    /**
     * Return the ATD for the vessel
     * @return the ATD for the vessel
     */
    ClockTime getAtd();

    /**
     * Set a new ATD and (re)schedule the vessel departure event.
     * @param atd new ATD
     */
    void setAtd(final ClockTime atd);

}
