package nl.tudelft.simulation.simport.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.TerminalStatistics;
import nl.tudelft.simulation.simport.truck.Truck;
import nl.tudelft.simulation.simport.util.SimPortRuntimeException;
import nl.tudelft.simulation.simport.vessel.Vessel;

/**
 * OutputWriter writes output of the simulation that it obtains through pub/sub to a number of output files.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OutputWriter implements EventListener
{
    /** the model. */
    private final PortModel model;

    /** the container writer. */
    private PrintWriter containerWriter;

    /** the truck writer. */
    private PrintWriter truckWriter;

    /** the terminal writer. */
    private PrintWriter terminalWriter;

    /** the loop detector writer. */
    private PrintWriter loopDetectorWriter;

    /** the tds writer. */
    private PrintWriter tdsWriter;

    /** the terminal queue writer. */
    private PrintWriter terminalQueueWriter;

    /** the terminal visit writer. */
    private PrintWriter terminalVisitWriter;

    /**
     * OutputWriter writes output of the simulation that it obtains through pub/sub to a number of output files.
     * @param model the model for pub/sub
     * @param outputPath the output path to which the filenames will be appended
     */
    public OutputWriter(final PortModel model, final String outputPath)
    {
        this.model = model;
        makeOutputDirectory(outputPath);

        try
        {
            this.containerWriter = new PrintWriter(outputPath + "/container.csv");
            writeContainerHeader();
            model.addListener(this, PortModel.CONTAINER_EVENT);

            this.truckWriter = new PrintWriter(outputPath + "/truck.csv");
            writeTruckTripHeader();
            model.addListener(this, PortModel.TRUCK_EVENT);

            this.terminalWriter = new PrintWriter(outputPath + "/terminal.csv");
            writeTerminalHeader();
            model.addListener(this, PortModel.DAILY_TERMINAL_EVENT);
        }
        catch (IOException ioe)
        {
            throw new SimPortRuntimeException(ioe);
        }
    }

    /**
     * Make the output path + directory.
     * @param directory string; the full path to the output directory to create
     */
    private void makeOutputDirectory(final String directory)
    {
        // try to create directory
        File f = new File(directory);
        if (!f.exists())
        {
            try
            {
                if (!f.mkdirs())
                {
                    throw new Exception("Could not create directory for output: " + directory);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }
    }

    private void writeContainerHeader()
    {
        this.containerWriter.print("\"container_nr\"");
        this.containerWriter.print(",\"terminal_in\"");
        this.containerWriter.print(",\"vessel_in\"");
        this.containerWriter.print(",\"date_vessel_in\"");
        this.containerWriter.print(",\"terminal_out\"");
        this.containerWriter.print(",\"vessel_out\"");
        this.containerWriter.print(",\"date_vessel_out\"");
        this.containerWriter.print(",\"container_type\"");
        this.containerWriter.print(",\"empty_full\"");
        this.containerWriter.print(",\"location_chain\"");
        this.containerWriter.print(",\"direction\"");
        this.containerWriter.print(",\"transport_mode\"");
        this.containerWriter.println();
        this.containerWriter.flush();
    }

    private void writeContainerLine(final Container container)
    {
        int vesselInNr = container.getVesselInNr();
        Vessel vesselIn = vesselInNr < 0 ? null : getModel().getVesselMap().get(vesselInNr);
        int vesselOutNr = container.getVesselOutNr();
        Vessel vesselOut = vesselOutNr < 0 ? null : getModel().getVesselMap().get(vesselOutNr);
        this.containerWriter.print("\"" + container.getId() + "\"");
        this.containerWriter.print(",\"" + (vesselIn == null ? "" : vesselIn.getTerminal().getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselIn == null ? "" : vesselIn.getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselIn == null ? "" : vesselIn.getAta().ymdhm()) + "\"");
        this.containerWriter.print(",\"" + (vesselOut == null ? "" : vesselOut.getTerminal().getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselOut == null ? "" : vesselOut.getId()) + "\"");
        this.containerWriter.print(",\"" + (vesselOut == null ? "" : vesselOut.getAta().ymdhm()) + "\"");
        this.containerWriter.print("\"" + container.getType() + "\"");
        this.containerWriter.print("\"" + (container.isEmpty() ? "E" : "F") + "\"");
        String locations = container.getLocations().toString();
        this.containerWriter.print(",\"" + locations + "\"");
        String direction = "domestic";
        if (vesselIn == null && vesselOut != null)
            direction = "export";
        else if (vesselIn != null && vesselOut == null)
            direction = "import";
        else if (vesselIn != null && vesselOut != null)
            direction = "transshipment";
        this.containerWriter.print(",\"" + direction + "\"");
        String transportMode = "TRANSSHIP";
        if (locations.contains("TRUCK"))
            transportMode = "TRUCK";
        else if (locations.contains("BARGE"))
            transportMode = "BARGE";
        else if (locations.contains("RAIL"))
            transportMode = "RAIL";
        this.containerWriter.print(",\"" + transportMode + "\"");
        this.containerWriter.println();
        this.containerWriter.flush();
    }

    private void writeTruckTripHeader()
    {
        this.truckWriter.print("\"trucking_company\"");
        this.truckWriter.print(",\"truck_nr\"");
        this.truckWriter.print(",\"container_nr1\"");
        this.truckWriter.print(",\"container_nr2\"");
        this.truckWriter.print(",\"date_pickup\"");
        this.truckWriter.print(",\"centroid_pickup\"");
        this.truckWriter.print(",\"terminal_pickup\"");
        this.truckWriter.print(",\"wait_time_gate_pickup\"");
        this.truckWriter.print(",\"yard_time_pickup\"");
        this.truckWriter.print(",\"date_delivery\"");
        this.truckWriter.print(",\"centroid_delivery\"");
        this.truckWriter.print(",\"terminal_delivery\"");
        this.truckWriter.print(",\"wait_time_gate_delivery\"");
        this.truckWriter.print(",\"yard_time_delivery\"");
        this.truckWriter.println();
        this.truckWriter.flush();
    }

    private void writeTruckTripLine(final Truck truck)
    {
        this.truckWriter.print("\"" + truck.getTruckingCompany().getId() + "\"");
        this.truckWriter.print("\"" + truck.getId() + "\"");
        this.truckWriter.print("\"" + (truck.getContainer() == null ? "" : truck.getContainer().getId()) + "\"");
        this.truckWriter.print(",\"" + "" + "\"");
        this.truckWriter.print(",\"" + (truck.getPickupTime() == null ? "" : truck.getPickupTime().ymdhm()) + "\"");
        this.truckWriter.print(",\"" + (truck.getLoadCentroid() == null ? "" : truck.getLoadCentroid()) + "\"");
        this.truckWriter.print(",\"\""); // TODO: terminal_pickup
        this.truckWriter.print(",\"\""); // TODO: wait_time_gate_pickup
        this.truckWriter.print(",\"\""); // TODO: yard_time_pickup
        this.truckWriter.print(",\"" + (truck.getDeliveryTime() == null ? "" : truck.getDeliveryTime().ymdhm()) + "\"");
        this.truckWriter.print(",\"" + (truck.getUnloadCentroid() == null ? "" : truck.getUnloadCentroid()) + "\"");
        this.truckWriter.print(",\"\""); // TODO: terminal_delivery
        this.truckWriter.print(",\"\""); // TODO: wait_time_gate_delivery
        this.truckWriter.print(",\"\""); // TODO: yard_time_delivery
        this.truckWriter.println();
        this.truckWriter.flush();
    }

    private void writeTerminalHeader()
    {
        this.terminalWriter.print("\"date\"");
        this.terminalWriter.print(",\"terminal\"");
        this.terminalWriter.print(",\"nr_vessel_arrivals\"");
        this.terminalWriter.print(",\"nr_vessel_departures\"");
        this.terminalWriter.print(",\"nr_containers_yard\"");
        this.terminalWriter.print(",\"nr_containers_full_yard\"");
        this.terminalWriter.print(",\"nr_containers_empty_yard\"");
        this.terminalWriter.print(",\"nr_containers_general_yard\"");
        this.terminalWriter.print(",\"nr_containers_reefer_yard\"");
        this.terminalWriter.print(",\"nr_containers_20ft_yard\"");
        this.terminalWriter.print(",\"nr_containers_40ft_yard\"");
        this.terminalWriter.print(",\"nr_teu_yard\"");
        this.terminalWriter.print(",\"nr_teu_full_yard\"");
        this.terminalWriter.print(",\"nr_teu_empty_yard\"");
        this.terminalWriter.print(",\"nr_teu_general_yard\"");
        this.terminalWriter.print(",\"nr_teu_reefer_yard\"");
        this.terminalWriter.print(",\"nr_teu_20ft_yard\"");
        this.terminalWriter.print(",\"nr_teu_40ft_yard\"");
        this.terminalWriter.print(",\"nr_truck_visits_pickup\"");
        this.terminalWriter.print(",\"nr_truck_visits_delivery\"");
        this.terminalWriter.print(",\"nr_truck_visits_dual\"");
        this.terminalWriter.print(",\"nr_container_arrivals_deepsea\"");
        this.terminalWriter.print(",\"nr_container_departures_deepsea\"");
        this.terminalWriter.print(",\"nr_teu_arrivals_deepsea\"");
        this.terminalWriter.print(",\"nr_teu_departures_deepsea\"");
        this.terminalWriter.print(",\"nr_container_arrivals_feeder\"");
        this.terminalWriter.print(",\"nr_container_departures_feeder\"");
        this.terminalWriter.print(",\"nr_teu_arrivals_feeder\"");
        this.terminalWriter.print(",\"nr_teu_departures_feeder\"");
        this.terminalWriter.print(",\"nr_container_arrivals_truck\"");
        this.terminalWriter.print(",\"nr_container_departures_truck\"");
        this.terminalWriter.print(",\"nr_teu_arrivals_truck\"");
        this.terminalWriter.print(",\"nr_teu_departures_truck\"");
        this.terminalWriter.print(",\"nr_container_arrivals_barge\"");
        this.terminalWriter.print(",\"nr_container_departures_barge\"");
        this.terminalWriter.print(",\"nr_teu_arrivals_barge\"");
        this.terminalWriter.print(",\"nr_teu_departures_barge\"");
        this.terminalWriter.print(",\"nr_container_arrivals_rail\"");
        this.terminalWriter.print(",\"nr_container_departures_rail\"");
        this.terminalWriter.print(",\"nr_teu_arrivals_rail\"");
        this.terminalWriter.print(",\"nr_teu_departures_rail\"");
        this.terminalWriter.println();
        this.terminalWriter.flush();
    }

    private void writeTerminalLine(final TerminalStatistics terminalStatistics)
    {
        var stat = terminalStatistics.getPeriodic();
        this.terminalWriter.print("\"" + stat.getStartTime().ymdhm() + "\"");
        this.terminalWriter.print("\"" + terminalStatistics.getFacility().getId() + "\"");
        this.terminalWriter.print("," + stat.getNrVesselArrivals());
        this.terminalWriter.print("," + stat.getNrVesselDepartures());
        this.terminalWriter.print("," + stat.getNrContainersTotal());
        this.terminalWriter.print("," + stat.getNrContainersFull());
        this.terminalWriter.print("," + stat.getNrContainersEmpty());
        this.terminalWriter.print("," + stat.getNrContainersGeneral());
        this.terminalWriter.print("," + stat.getNrContainersReefer());
        this.terminalWriter.print("," + stat.getNrContainers20Ft());
        this.terminalWriter.print("," + stat.getNrContainers40Ft());
        this.terminalWriter.print("," + stat.getNrTeuTotal());
        this.terminalWriter.print("," + stat.getNrTeuFull());
        this.terminalWriter.print("," + stat.getNrTeuEmpty());
        this.terminalWriter.print("," + stat.getNrTeuGeneral());
        this.terminalWriter.print("," + stat.getNrTeuReefer());
        this.terminalWriter.print("," + stat.getNrTeu20Ft());
        this.terminalWriter.print("," + stat.getNrTeu40Ft());
        this.terminalWriter.print("," + stat.getNrTruckVisitsPickup());
        this.terminalWriter.print("," + stat.getNrTruckVisitsDelivery());
        this.terminalWriter.print("," + stat.getNrTruckVisitsDual());
        for (TransportMode tm : new TransportMode[] {TransportMode.DEEPSEA, TransportMode.FEEDER, TransportMode.TRUCK,
                TransportMode.BARGE, TransportMode.RAIL})
        {
            this.terminalWriter.print("," + stat.getNrContainerArrivals(tm));
            this.terminalWriter.print("," + stat.getNrContainerDepartures(tm));
            this.terminalWriter.print("," + stat.getNrTeuArrivals(tm));
            this.terminalWriter.print("," + stat.getNrTeuDepartures(tm));
        }
        this.terminalWriter.println();
        this.terminalWriter.flush();
    }

    /**
     * Return the model.
     * @return the model
     */
    public PortModel getModel()
    {
        return this.model;
    }

    /* ****************************************** NOTIFY ******************************************** */

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(PortModel.CONTAINER_EVENT))
        {
            writeContainerLine((Container) event.getContent());
        }
        else if (event.getType().equals(PortModel.TRUCK_EVENT))
        {
            writeTruckTripLine((Truck) event.getContent());
        }
        else if (event.getType().equals(PortModel.DAILY_TERMINAL_EVENT))
        {
            writeTerminalLine((TerminalStatistics) event.getContent());
        }
    }
}
