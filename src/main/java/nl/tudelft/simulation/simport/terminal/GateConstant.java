package nl.tudelft.simulation.simport.terminal;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.simulators.clock.ClockDevsSimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousDuration;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.truck.Truck;

/**
 * GateConstant is a gate with a constant number of lanes and a constant processing time.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class GateConstant implements Gate
{
    /** The terminal or depot to which the gate belongs. */
    private final ContainerFacility portFacility;

    /** the (sub) id for the gate. */
    private final String id;

    /** the (fixed) number of lanes-in. */
    private int lanesIn;

    /** the (fixed) number of lanes-out. */
    private int lanesOut;

    /** the fixed handling time distribution for entering the terminal. */
    private DistContinuousDuration timeInDist;

    /** the fixed handling time distribution for leaving the terminal. */
    private DistContinuousDuration timeOutDist;

    /** the average gate-in time. */
    private Duration avgGateTimeIn;

    /** the average gate-out time. */
    private Duration avgGateTimeOut;

    /** the truck queue for entering the terminal. */
    private List<Truck> truckQueueIn = new ArrayList<>();

    /** the shortest interval between successive gate-in events because of capacity. */
    private Duration intervalGateIn;

    private int capTotal = 0;

    private int capFull = 0;

    private int capEmpty = 0;

    private int capImport = 0;

    private int capExport = 0;

    private int capImportFull = 0;

    private int capImportEmpty = 0;

    private int capExportFull = 0;

    private int capExportEmpty = 0;

    /**
     * Create a gate for a terminal with a constant number of lanes and constant handling times.
     * @param portFacility the terminal or depot
     * @param id the unique id within the portFacility; will be appended to the terminal id for display
     */
    public GateConstant(final ContainerFacility portFacility, final String id)
    {
        this.portFacility = portFacility;
        this.id = id;

        // sample gate queue in every 6 minutes
        getSimulator().scheduleEventNow(() -> sampleGateIn());
    }

    public void setCapacities(final int capTotal, final int capFull, final int capEmpty, final int capImport,
            final int capExport, final int capImportFull, final int capImportEmpty, final int capExportFull,
            final int capExportEmpty)
    {
        this.capTotal = capTotal + capFull + capEmpty + capImport + capExport + capImportFull + capImportEmpty + capExportFull
                + capExportEmpty;
        this.capFull = capFull;
        this.capEmpty = capEmpty;
        this.capImport = capImport;
        this.capExport = capExport;
        this.capImportFull = capImportFull;
        this.capImportEmpty = capImportEmpty;
        this.capExportFull = capExportFull;
        this.capExportEmpty = capExportEmpty;

        // check the in-queue periodically
        this.intervalGateIn = new Duration(60.0 / this.capTotal, DurationUnit.MINUTE);
        getSimulator().scheduleEventNow(() -> checkGateIn());
    }

    protected void sampleGateIn()
    {
        getContainerFacility().getModel().fireEvent(PortModel.TERMINAL_QUEUE_EVENT,
                new Object[] {getContainerFacility().getId(), this.truckQueueIn.size()});
        getSimulator().scheduleEventRel(new Duration(6.0, DurationUnit.MINUTE), () -> sampleGateIn());
    }

    protected void checkGateIn()
    {
        if (this.truckQueueIn.size() > 0)
            this.truckQueueIn.remove(0).enterGateFromQueue();
        getSimulator().scheduleEventRel(this.intervalGateIn, () -> checkGateIn());
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public ContainerFacility getContainerFacility()
    {
        return this.portFacility;
    }

    @Override
    public void setLanesIn(final int lanesIn)
    {
        this.lanesIn = lanesIn;
    }

    @Override
    public int getCurrentLanesIn()
    {
        return this.lanesIn;
    }

    @Override
    public void setLanesOut(final int lanesOut)
    {
        this.lanesOut = lanesOut;
    }

    @Override
    public int getCurrentLanesOut()
    {
        return this.lanesOut;
    }

    /**
     * Set the fixed handling time distribution for entering the terminal.
     * @param timeInDist the fixed handling time distribution for entering the terminal
     */
    public void setTimeInDist(final DistContinuousDuration timeInDist)
    {
        this.timeInDist = timeInDist;
    }

    @Override
    public Duration drawCurrentGateTimeIn()
    {
        return this.timeInDist.draw();
    }

    /**
     * Set the fixed handling time distribution for leaving the terminal.
     * @param timeInDist the fixed handling time distribution for leaving the terminal
     */
    public void setTimeOutDist(final DistContinuousDuration timeOutDist)
    {
        this.timeOutDist = timeOutDist;
    }

    @Override
    public Duration drawCurrentGateTimeOut()
    {
        return this.timeOutDist.draw();
    }

    @Override
    public Duration getAvgGateTimeIn()
    {
        if (this.avgGateTimeIn == null)
        {
            double durationSi = 0.0;
            for (int i = 0; i < 1000; i++)
            {
                durationSi += drawCurrentGateTimeIn().si;
            }
            this.avgGateTimeIn = Duration.ofSI(durationSi / 1000.0);
        }
        return this.avgGateTimeIn;
    }

    @Override
    public Duration getAvgGateTimeOut()
    {
        if (this.avgGateTimeOut == null)
        {
            double durationSi = 0.0;
            for (int i = 0; i < 1000; i++)
            {
                durationSi += drawCurrentGateTimeOut().si;
            }
            this.avgGateTimeOut = Duration.ofSI(durationSi / 1000.0);
        }
        return this.avgGateTimeOut;
    }

    @Override
    public List<Truck> getTruckQueueIn()
    {
        return this.truckQueueIn;
    }

    @Override
    public void addTruckToQueueIn(final Truck truck)
    {
        this.truckQueueIn.add(truck);

    }

    @Override
    public ClockDevsSimulatorInterface getSimulator()
    {
        return this.portFacility.getSimulator();
    }

    /**
     * @return capTotal
     */
    public int getCapTotal()
    {
        return this.capTotal;
    }

    /**
     * @return capFull
     */
    public int getCapFull()
    {
        return this.capFull;
    }

    /**
     * @return capEmpty
     */
    public int getCapEmpty()
    {
        return this.capEmpty;
    }

    /**
     * @return capImport
     */
    public int getCapImport()
    {
        return this.capImport;
    }

    /**
     * @return capExport
     */
    public int getCapExport()
    {
        return this.capExport;
    }

    /**
     * @return capImportFull
     */
    public int getCapImportFull()
    {
        return this.capImportFull;
    }

    /**
     * @return capImportEmpty
     */
    public int getCapImportEmpty()
    {
        return this.capImportEmpty;
    }

    /**
     * @return capExportFull
     */
    public int getCapExportFull()
    {
        return this.capExportFull;
    }

    /**
     * @return capExportEmpty
     */
    public int getCapExportEmpty()
    {
        return this.capExportEmpty;
    }

    @Override
    public String toString()
    {
        return "Gate [id=" + getContainerFacility().getId() + "." + this.id + "]";
    }

}
