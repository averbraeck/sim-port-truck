package nl.tudelft.simulation.simport.output;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.simport.TransportMode;
import nl.tudelft.simulation.simport.container.Booking;
import nl.tudelft.simulation.simport.container.Container;
import nl.tudelft.simulation.simport.model.PortModel;
import nl.tudelft.simulation.simport.terminal.ContainerFacility;
import nl.tudelft.simulation.simport.terminal.TerminalStatistics;
import nl.tudelft.simulation.simport.terminal.TerminalStatistics.TerminalData;
import nl.tudelft.simulation.simport.truck.Truck;
import nl.tudelft.simulation.simport.truck.activity.RealizedDrivingActivity;
import nl.tudelft.simulation.simport.truck.activity.RealizedTerminalActivity;
import nl.tudelft.simulation.simport.truck.activity.RealizedTruckActivity;
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

    /** the vessel writer. */
    private PrintWriter vesselWriter;

    /** the container writer. */
    private BufferedWriter containerWriter;

    /** the final container writer. */
    private PrintWriter finalContainerWriter;

    /** the truck writer. */
    private PrintWriter truckWriter;

    /** the terminal writer. */
    private PrintWriter terminalWriter;

    /** the total terminal writer. */
    private PrintWriter totalTerminalWriter;

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
            this.vesselWriter = new PrintWriter(outputPath + "/vessel.csv");
            writeVesselHeader();
            model.addListener(this, PortModel.VESSEL_EVENT);

            FileOutputStream fos = new FileOutputStream(new File(outputPath + "/container.csv.gz"));
            BufferedOutputStream bos = new BufferedOutputStream(fos, 128 * 1024);
            GZIPOutputStream gos = new GZIPOutputStream(bos);
            OutputStreamWriter osw = new OutputStreamWriter(gos, "UTF-8");
            this.containerWriter = new BufferedWriter(osw, 128 * 1024);
            writeContainerHeader();
            model.addListener(this, PortModel.CONTAINER_EVENT);

            this.truckWriter = new PrintWriter(outputPath + "/truck.csv");
            writeTruckTripHeader();
            model.addListener(this, PortModel.TRUCK_EVENT);

            this.terminalWriter = new PrintWriter(outputPath + "/terminal.csv");
            writeTerminalHeader();
            model.addListener(this, PortModel.DAILY_TERMINAL_EVENT);

            this.totalTerminalWriter = new PrintWriter(outputPath + "/terminal_total.csv");
            writeTotalTerminalHeader();
            model.addListener(this, PortModel.TOTAL_TERMINAL_EVENT);

            this.finalContainerWriter = new PrintWriter(outputPath + "/container_final.csv");
            writeFinalContainerHeader();

            model.getSimulator().addListener(this, Replication.END_REPLICATION_EVENT);
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

    private void writeVesselHeader()
    {
        this.vesselWriter.print("\"date\"");
        this.vesselWriter.print(",\"vessel_id\"");
        this.vesselWriter.print(",\"terminal_id\"");
        this.vesselWriter.print(",\"vessel_type\"");
        this.vesselWriter.print(",\"eta\"");
        this.vesselWriter.print(",\"ata\"");
        this.vesselWriter.print(",\"etd\"");
        this.vesselWriter.print(",\"atd\"");
        this.vesselWriter.print(",\"containers_loaded\"");
        this.vesselWriter.print(",\"containers_loaded_full\"");
        this.vesselWriter.print(",\"containers_loaded_empty\"");
        this.vesselWriter.print(",\"containers_loaded_general\"");
        this.vesselWriter.print(",\"containers_loaded_reefer\"");
        this.vesselWriter.print(",\"containers_loaded_20ft\"");
        this.vesselWriter.print(",\"containers_loaded_40ft\"");
        this.vesselWriter.print(",\"teu_loaded\"");
        this.vesselWriter.print(",\"contains_unloaded\"");
        this.vesselWriter.print(",\"containers_unloaded_full\"");
        this.vesselWriter.print(",\"containers_unloaded_empty\"");
        this.vesselWriter.print(",\"containers_unloaded_general\"");
        this.vesselWriter.print(",\"containers_unloaded_reefer\"");
        this.vesselWriter.print(",\"containers_unloaded_20ft\"");
        this.vesselWriter.print(",\"containers_unloaded_40ft\"");
        this.vesselWriter.print(",\"teu_unloaded\"");
        this.vesselWriter.print(",\"containers_present\"");
        this.vesselWriter.print(",\"teu_present\"");
        this.vesselWriter.print(",\"containers_transshipped_loaded\"");
        this.vesselWriter.print(",\"containers_transshipped_unloaded\"");
        this.vesselWriter.println();
        this.vesselWriter.flush();
    }

    private void writeVesselLine(final Vessel vessel)
    {
        this.vesselWriter.print("\"" + getModel().getSimulator().getSimulatorClockTime().ymdhm() + "\"");
        this.vesselWriter.print(",\"" + vessel.getId() + "\"");
        this.vesselWriter.print(",\"" + vessel.getTerminal().getId() + "\"");
        this.vesselWriter.print(",\"" + vessel.getVesselType().toString() + "\"");
        this.vesselWriter.print(",\"" + vessel.getEta().ymdhm() + "\"");
        this.vesselWriter.print(",\"" + vessel.getAta().ymdhm() + "\"");
        this.vesselWriter.print(",\"" + vessel.getEtd().ymdhm() + "\"");
        this.vesselWriter.print(",\"" + vessel.getAtd().ymdhm() + "\"");
        this.vesselWriter.print("," + vessel.getLoadList().size());
        int[] fegr24Loaded = calcFEGR24(vessel.getLoadList());
        for (int i = 0; i < 6; i++)
            this.vesselWriter.print("," + fegr24Loaded[i]);
        this.vesselWriter.print("," + (fegr24Loaded[4] + 2 * fegr24Loaded[5]));
        this.vesselWriter.print("," + vessel.getUnloadList().size());
        int[] fegr24Unloaded = calcFEGR24(vessel.getUnloadList());
        for (int i = 0; i < 6; i++)
            this.vesselWriter.print("," + fegr24Unloaded[i]);
        this.vesselWriter.print("," + (fegr24Unloaded[4] + 2 * fegr24Unloaded[5]));
        this.vesselWriter.print("," + vessel.getContainerList().size());
        int teu = 0;
        for (Container c : vessel.getContainerList())
            teu += c.is20ft() ? 1 : 2;
        this.vesselWriter.print("," + teu);
        this.vesselWriter.print("," + vessel.getNrContainersTransshippedLoaded());
        this.vesselWriter.print("," + vessel.getNrContainersTransshippedUnloaded());
        this.vesselWriter.println();
        this.vesselWriter.flush();
    }

    int[] calcFEGR24(final List<Booking> bookings)
    {
        int[] fegr24 = new int[6];
        for (Booking booking : bookings)
        {
            if (booking.isFull())
                fegr24[0]++;
            else
                fegr24[1]++;

            if (booking.isGeneral())
                fegr24[2]++;
            else
                fegr24[3]++;

            if (booking.is20ft())
                fegr24[4]++;
            else
                fegr24[5]++;
        }
        return fegr24;
    }

    private void writeContainerHeader()
    {
        try
        {
            this.containerWriter.write("\"container_nr\"");
            this.containerWriter.write(",\"terminal_in\"");
            this.containerWriter.write(",\"vessel_in\"");
            this.containerWriter.write(",\"date_vessel_in\"");
            this.containerWriter.write(",\"terminal_out\"");
            this.containerWriter.write(",\"vessel_out\"");
            this.containerWriter.write(",\"date_vessel_out\"");
            this.containerWriter.write(",\"container_type\"");
            this.containerWriter.write(",\"empty_full\"");
            this.containerWriter.write(",\"location_chain\"");
            this.containerWriter.write(",\"direction\"");
            this.containerWriter.write(",\"transport_mode\"");
            this.containerWriter.write("\n");
            this.containerWriter.flush();
        }
        catch (IOException ioe)
        {
            CategoryLogger.always().error("Error writing to containerWriter. Error: " + ioe.getMessage());
        }
    }

    private void writeContainerLine(final Container container)
    {
        int vesselInNr = container.getVesselInNr();
        Vessel vesselIn = vesselInNr < 0 ? null : getModel().getVesselMap().get(vesselInNr);
        int vesselOutNr = container.getVesselOutNr();
        Vessel vesselOut = vesselOutNr < 0 ? null : getModel().getVesselMap().get(vesselOutNr);
        try
        {
            this.containerWriter.write("\"" + container.getId() + "\"");
            this.containerWriter.write(",\"" + (vesselIn == null ? "" : vesselIn.getTerminal().getId()) + "\"");
            this.containerWriter.write(",\"" + (vesselIn == null ? "" : vesselIn.getId()) + "\"");
            this.containerWriter.write(",\"" + (vesselIn == null ? "" : vesselIn.getAta().ymdhm()) + "\"");
            this.containerWriter.write(",\"" + (vesselOut == null ? "" : vesselOut.getTerminal().getId()) + "\"");
            this.containerWriter.write(",\"" + (vesselOut == null ? "" : vesselOut.getId()) + "\"");
            this.containerWriter.write(",\"" + (vesselOut == null ? "" : vesselOut.getAta().ymdhm()) + "\"");
            this.containerWriter.write("\"" + container.getType() + "\"");
            this.containerWriter.write("\"" + (container.isEmpty() ? "E" : "F") + "\"");
            String locations = container.getLocations().toString();
            this.containerWriter.write(",\"" + locations + "\"");
            String direction = "domestic";
            if (vesselIn == null && vesselOut != null)
                direction = "export";
            else if (vesselIn != null && vesselOut == null)
                direction = "import";
            else if (vesselIn != null && vesselOut != null)
                direction = "transshipment";
            this.containerWriter.write(",\"" + direction + "\"");
            String transportMode = "TRANSSHIP";
            if (locations.contains("TRUCK"))
                transportMode = "TRUCK";
            else if (locations.contains("BARGE"))
                transportMode = "BARGE";
            else if (locations.contains("RAIL"))
                transportMode = "RAIL";
            this.containerWriter.write(",\"" + transportMode + "\"");
            this.containerWriter.write("\n");
            this.containerWriter.flush();
        }
        catch (IOException ioe)
        {
            CategoryLogger.always().error("Error writing to containerWriter. Error: " + ioe.getMessage());
        }
    }

    private void writeTruckTripHeader()
    {
        this.truckWriter.print("\"trucking_company\"");
        this.truckWriter.print(",\"truck_nr\"");
        this.truckWriter.print(",\"activity_nr\"");
        this.truckWriter.print(",\"activity_type\"");

        this.truckWriter.print(",\"driving_leg_type\"");
        this.truckWriter.print(",\"driving_etd\"");
        this.truckWriter.print(",\"driving_atd\"");
        this.truckWriter.print(",\"driving_eta\"");
        this.truckWriter.print(",\"driving_ata\"");
        this.truckWriter.print(",\"driving_container_nr1\"");
        this.truckWriter.print(",\"driving_container_nr2\"");
        this.truckWriter.print(",\"driving_orig_centroid\"");
        this.truckWriter.print(",\"driving_dest_centroid\"");
        this.truckWriter.print(",\"driving_start_link\"");
        this.truckWriter.print(",\"driving_end_link\"");
        this.truckWriter.print(",\"driving_distance_km\"");
        this.truckWriter.print(",\"driving_planned_duration_s\"");
        this.truckWriter.print(",\"driving_actual_duration_s\"");

        this.truckWriter.print(",\"visit_terminal_id\"");
        this.truckWriter.print(",\"visit_activity_type\"");
        this.truckWriter.print(",\"visit_dropoff_container_nr1\"");
        this.truckWriter.print(",\"visit_dropoff_container_nr2\"");
        this.truckWriter.print(",\"visit_pickup_container_nr1\"");
        this.truckWriter.print(",\"visit_pickup_container_nr2\"");
        this.truckWriter.print(",\"visit_slot_nr\"");
        this.truckWriter.print(",\"visit_earliest_grace_time\"");
        this.truckWriter.print(",\"visit_earliest_standard_time\"");
        this.truckWriter.print(",\"visit_latest_standard_time\"");
        this.truckWriter.print(",\"visit_latest_grace_time\"");
        this.truckWriter.print(",\"visit_ata\"");
        this.truckWriter.print(",\"visit_waittime_gate_in_s\"");
        this.truckWriter.print(",\"visit_handling_time_s\"");
        this.truckWriter.print(",\"visit_waittime_gate_out_s\"");

        this.truckWriter.println();
        this.truckWriter.flush();
    }

    private void writeTruckTripLine(final Truck truck)
    {
        this.truckWriter.print("\"" + truck.getTruckingCompany().getId() + "\"");
        this.truckWriter.print(",\"" + truck.getId() + "\"");
        for (RealizedTruckActivity ta : truck.getRealizedActivityList())
        {
            if (ta instanceof RealizedDrivingActivity da)
                writeDrivingLine(da);
            else
                writeVisitLine((RealizedTerminalActivity) ta);
        }
        this.truckWriter.println();
        this.truckWriter.flush();
    }

    private void writeDrivingLine(final RealizedDrivingActivity da)
    {
    }

    private void writeVisitLine(final RealizedTerminalActivity ta)
    {
    }

    private void writeTerminalHeader()
    {
        writeTerminalHeader(this.terminalWriter);
    }

    private void writeTotalTerminalHeader()
    {
        writeTerminalHeader(this.totalTerminalWriter);
    }

    private void writeTerminalHeader(final PrintWriter writer)
    {
        writer.print("\"date\"");
        writer.print(",\"terminal\"");
        writer.print(",\"nr_vessel_arrivals\"");
        writer.print(",\"nr_vessel_departures\"");
        writer.print(",\"nr_deepsea_arrivals\"");
        writer.print(",\"nr_deepsea_departures\"");
        writer.print(",\"nr_feeder_arrivals\"");
        writer.print(",\"nr_feeder_departures\"");
        for (String yad : new String[] {"yard", "arrivals", "departures"})
        {
            for (String ct : new String[] {"containers", "teu"})
            {
                for (String type : new String[] {"total", "full", "empty", "general", "reefer", "20ft", "40ft"})
                {
                    writer.print(",\"nr_" + ct + "_" + type + "_" + yad + "\"");
                }
            }
        }
        writer.print(",\"nr_truck_visits_pickup\"");
        writer.print(",\"nr_truck_visits_delivery\"");
        writer.print(",\"nr_truck_visits_dual\"");
        writer.print(",\"nr_container_arrivals_deepsea\"");
        writer.print(",\"nr_container_departures_deepsea\"");
        writer.print(",\"nr_teu_arrivals_deepsea\"");
        writer.print(",\"nr_teu_departures_deepsea\"");
        writer.print(",\"nr_container_arrivals_feeder\"");
        writer.print(",\"nr_container_departures_feeder\"");
        writer.print(",\"nr_teu_arrivals_feeder\"");
        writer.print(",\"nr_teu_departures_feeder\"");
        writer.print(",\"nr_container_arrivals_truck\"");
        writer.print(",\"nr_container_departures_truck\"");
        writer.print(",\"nr_teu_arrivals_truck\"");
        writer.print(",\"nr_teu_departures_truck\"");
        writer.print(",\"nr_container_arrivals_barge\"");
        writer.print(",\"nr_container_departures_barge\"");
        writer.print(",\"nr_teu_arrivals_barge\"");
        writer.print(",\"nr_teu_departures_barge\"");
        writer.print(",\"nr_container_arrivals_rail\"");
        writer.print(",\"nr_container_departures_rail\"");
        writer.print(",\"nr_teu_arrivals_rail\"");
        writer.print(",\"nr_teu_departures_rail\"");
        writer.println();
        writer.flush();
    }

    private void writeTerminalLine(final TerminalStatistics terminalStatistics)
    {
        var stat = terminalStatistics.getPeriodic();
        writeTerminalLine(this.terminalWriter, stat, terminalStatistics.getFacility());
    }

    private void writeTotalTerminalLine(final TerminalStatistics terminalStatistics)
    {
        var stat = terminalStatistics.getTotal();
        writeTerminalLine(this.totalTerminalWriter, stat, terminalStatistics.getFacility());
    }

    private void writeTerminalLine(final PrintWriter writer, final TerminalData stat, final ContainerFacility facility)
    {
        writer.print("\"" + stat.getStartTime().ymdhm() + "\"");
        writer.print(",\"" + facility.getId() + "\"");
        writer.print("," + stat.getNrVesselArrivals());
        writer.print("," + stat.getNrVesselDepartures());
        writer.print("," + stat.getNrDeepseaArrivals());
        writer.print("," + stat.getNrDeepseaDepartures());
        writer.print("," + stat.getNrFeederArrivals());
        writer.print("," + stat.getNrFeederDepartures());

        writer.print("," + stat.getNrContainersTotal());
        writer.print("," + stat.getNrContainersFull());
        writer.print("," + stat.getNrContainersEmpty());
        writer.print("," + stat.getNrContainersGeneral());
        writer.print("," + stat.getNrContainersReefer());
        writer.print("," + stat.getNrContainers20Ft());
        writer.print("," + stat.getNrContainers40Ft());

        writer.print("," + stat.getNrTeuTotal());
        writer.print("," + stat.getNrTeuFull());
        writer.print("," + stat.getNrTeuEmpty());
        writer.print("," + stat.getNrTeuGeneral());
        writer.print("," + stat.getNrTeuReefer());
        writer.print("," + stat.getNrTeu20Ft());
        writer.print("," + stat.getNrTeu40Ft());

        writer.print("," + stat.getNrContainersArrTotal());
        writer.print("," + stat.getNrContainersArrFull());
        writer.print("," + stat.getNrContainersArrEmpty());
        writer.print("," + stat.getNrContainersArrGeneral());
        writer.print("," + stat.getNrContainersArrReefer());
        writer.print("," + stat.getNrContainersArr20Ft());
        writer.print("," + stat.getNrContainersArr40Ft());

        writer.print("," + stat.getNrTeuArrTotal());
        writer.print("," + stat.getNrTeuArrFull());
        writer.print("," + stat.getNrTeuArrEmpty());
        writer.print("," + stat.getNrTeuArrGeneral());
        writer.print("," + stat.getNrTeuArrReefer());
        writer.print("," + stat.getNrTeuArr20Ft());
        writer.print("," + stat.getNrTeuArr40Ft());

        writer.print("," + stat.getNrContainersDepTotal());
        writer.print("," + stat.getNrContainersDepFull());
        writer.print("," + stat.getNrContainersDepEmpty());
        writer.print("," + stat.getNrContainersDepGeneral());
        writer.print("," + stat.getNrContainersDepReefer());
        writer.print("," + stat.getNrContainersDep20Ft());
        writer.print("," + stat.getNrContainersDep40Ft());

        writer.print("," + stat.getNrTeuDepTotal());
        writer.print("," + stat.getNrTeuDepFull());
        writer.print("," + stat.getNrTeuDepEmpty());
        writer.print("," + stat.getNrTeuDepGeneral());
        writer.print("," + stat.getNrTeuDepReefer());
        writer.print("," + stat.getNrTeuDep20Ft());
        writer.print("," + stat.getNrTeuDep40Ft());

        writer.print("," + stat.getNrTruckVisitsPickup());
        writer.print("," + stat.getNrTruckVisitsDelivery());
        writer.print("," + stat.getNrTruckVisitsDual());

        for (TransportMode tm : new TransportMode[] {TransportMode.DEEPSEA, TransportMode.FEEDER, TransportMode.TRUCK,
                TransportMode.BARGE, TransportMode.RAIL})
        {
            writer.print("," + stat.getNrContainerArrivals(tm));
            writer.print("," + stat.getNrContainerDepartures(tm));
            writer.print("," + stat.getNrTeuArrivals(tm));
            writer.print("," + stat.getNrTeuDepartures(tm));
        }
        writer.println();
        writer.flush();
    }

    private void writeFinalContainerHeader()
    {
        this.finalContainerWriter.print("\"terminal_id\"");
        this.finalContainerWriter.print(",\"container_nr\"");
        this.finalContainerWriter.print(",\"vessel_in_nr\"");
        this.finalContainerWriter.print(",\"vessel_out_nr\"");
        this.finalContainerWriter.print(",\"locations\"");
        this.finalContainerWriter.println();
        this.finalContainerWriter.flush();
    }

    private void writeFinalContainerLines(final ContainerFacility facility)
    {
        String id = facility.getId();
        for (Container container : facility.getYard().getContainerMap().values())
        {
            this.finalContainerWriter.print("\"" + id + "\"");
            this.finalContainerWriter.print(",\"" + container.getId() + "\"");
            this.finalContainerWriter.print("," + container.getVesselInNr());
            this.finalContainerWriter.print("," + container.getVesselOutNr());
            this.finalContainerWriter.print("," + container.getLocations().toString());
            this.finalContainerWriter.println();
        }
        this.finalContainerWriter.flush();
    }

    /** close the files. */
    protected void closeFiles()
    {
        this.vesselWriter.close();
        this.truckWriter.close();
        this.terminalWriter.close();
        this.totalTerminalWriter.close();
        this.finalContainerWriter.close();
        try
        {
            this.containerWriter.close();
        }
        catch (IOException ioe)
        {
            CategoryLogger.always().error("Error closing containerWriter file. Error: " + ioe.getMessage());
        }
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
        if (event.getType().equals(PortModel.VESSEL_EVENT))
        {
            writeVesselLine((Vessel) event.getContent());
        }
        else if (event.getType().equals(PortModel.CONTAINER_EVENT))
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
        else if (event.getType().equals(PortModel.TOTAL_TERMINAL_EVENT))
        {
            writeTotalTerminalLine((TerminalStatistics) event.getContent());
            writeFinalContainerLines((((TerminalStatistics) event.getContent()).getFacility()));
        }
        else if (event.getType().equals(Replication.END_REPLICATION_EVENT))
        {
            closeFiles();
        }
    }
}
