package nl.tudelft.simulation.simport.ndw;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * DailyDataProcessor.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DailyDataProcessor
{
    private final Map<String, SiteMetadata> metadataMap;

    private final TrafficAggregator aggregator;

    private final boolean flowWeightedSpeed;

    private final double minLat, maxLat, minLon, maxLon; // bounding box

    public DailyDataProcessor(final Map<String, SiteMetadata> metadataMap, final long bucketSizeMillis,
            final boolean flowWeightedSpeed, final double minLat, final double maxLat, final double minLon, final double maxLon)
    {
        this.metadataMap = metadataMap;
        this.aggregator = new TrafficAggregator(bucketSizeMillis, flowWeightedSpeed);
        this.flowWeightedSpeed = flowWeightedSpeed;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    private static XMLInputFactory staxFactory()
    {
        // XMLInputFactory f = XMLInputFactory.newFactory();
        XMLInputFactory factory = new com.fasterxml.aalto.stax.InputFactoryImpl();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        try
        {
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        }
        catch (Exception ignore)
        {
        }
        try
        {
            factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        }
        catch (Exception ignore)
        {
        }
        return factory;
    }

    public void processZipSequential(final File zipFile) throws Exception
    {
        XMLInputFactory factory = staxFactory();
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), 1 << 20)))
        {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null)
            {
                try (GZIPInputStream gzip = new GZIPInputStream(new BufferedInputStream(zip, 1 << 16)))
                {
                    parseOne(factory, gzip);
                }
            }
        }
    }

    private void parseOne(final XMLInputFactory factory, final InputStream in) throws Exception
    {
        XMLStreamReader reader = factory.createXMLStreamReader(in);
        String currentSiteId = null;
        long currentTs = Long.MIN_VALUE;
        Integer currentIndex = null;
        boolean dataErrorFlag = false;
        Double pendingFlowForWeight = null;

        while (reader.hasNext())
        {
            int evt = reader.next();
            if (evt == XMLStreamConstants.START_ELEMENT)
            {
                String name = reader.getLocalName();
                switch (name)
                {
                    case "measurementSiteReference":
                        currentSiteId = reader.getAttributeValue(null, "id");
                        break;
                    case "measurementTimeDefault":
                        String ts = reader.getElementText();
                        currentTs = java.time.Instant.parse(ts).toEpochMilli();
                        break;
                    case "measuredValue":
                        String idxAttr = reader.getAttributeValue(null, "index");
                        currentIndex = idxAttr != null ? Integer.parseInt(idxAttr) : null;
                        dataErrorFlag = false;
                        pendingFlowForWeight = null;
                        break;
                    case "dataError":
                        String v = reader.getElementText();
                        if ("true".equalsIgnoreCase(v))
                            dataErrorFlag = true;
                        break;
                    case "vehicleFlowRate":
                        String flowTxt = reader.getElementText();
                        if (!dataErrorFlag)
                        {
                            double flow = safeDouble(flowTxt);
                            String classLabel = classLabel(currentSiteId, currentIndex);
                            if (classLabel != null && !Double.isNaN(flow))
                            {
                                this.aggregator.addFlow(currentSiteId, classLabel, currentTs, flow);
                                pendingFlowForWeight = flow;
                            }
                        }
                        break;
                    case "speed":
                        String spTxt = reader.getElementText();
                        if (!dataErrorFlag)
                        {
                            double sp = safeDouble(spTxt);
                            if (sp >= 0)
                            {
                                String classLabel = classLabel(currentSiteId, currentIndex);
                                if (classLabel != null && !Double.isNaN(sp))
                                {
                                    this.aggregator.addSpeed(currentSiteId, classLabel, currentTs, sp, pendingFlowForWeight);
                                }
                            }
                        }
                        break;
                }
            }
        }
    }

    private static double safeDouble(final String s)
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch (Exception e)
        {
            return Double.NaN;
        }
    }

    private String classLabel(final String siteId, final Integer index)
    {
        if (siteId == null || index == null)
            return "unknown";
        SiteMetadata md = this.metadataMap.get(siteId);
        if (md == null)
            return null;
        if (!insideBBox(md))
            return null;
        SiteMetadata.VehicleLengthBand band = md.indexToBand().get(index);
        if (band != null)
            return band.label();
        return "unknown";
    }

    private boolean insideBBox(final SiteMetadata md)
    {
        if (Double.isNaN(md.lat()) || Double.isNaN(md.lon()))
            return false;
        return md.lat() >= this.minLat && md.lat() <= this.maxLat && md.lon() >= this.minLon && md.lon() <= this.maxLon;
    }

    public void writeCsv(final String outputPath) throws Exception
    {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, false), 1 << 20)))
        {
            pw.println("timestamp,siteId,roadName,direction,lanes,lat,lon,hm,vehicleClass,vehicleCount,avgSpeed");
            for (TrafficAggregator.AggregatedBucket b : this.aggregator.results())
            {
                SiteMetadata md = this.metadataMap.get(b.siteId);
                String road = md != null ? safeCsv(md.roadName()) : "";
                String dir = md != null ? safeCsv(md.direction()) : "";
                int lanes = md != null ? md.numberOfLanes() : 0;
                double lat = md != null ? md.lat() : Double.NaN;
                double lon = md != null ? md.lon() : Double.NaN;
                Double hm = null; // md != null ? md.hm : null;
                pw.printf(Locale.ROOT, "%d,%s,%s,%s,%d,%.7f,%.7f,%s,%s,%.0f,%.3f%n", b.bucketId, b.siteId, road, dir, lanes,
                        lat, lon, hm == null ? "" : String.format(Locale.ROOT, "%.2f", hm), b.classLabel, b.totalFlow,
                        b.avgSpeed());
            }
        }
    }

    private static String safeCsv(final String s)
    {
        return s == null ? "" : s.replace(',', ' ');
    }

    // Optional FastCSV writer variant — pass your own Writer
    public void writeCsvFastCsv(final java.io.Writer out) throws Exception
    {
        // Requires dependency: de.siegmar:fastcsv:4.x
        de.siegmar.fastcsv.writer.CsvWriter csv = de.siegmar.fastcsv.writer.CsvWriter.builder().build(out);
        csv.writeRow("timestamp", "siteId", "roadName", "direction", "lanes", "lat", "lon", "hm", "vehicleClass",
                "vehicleCount", "avgSpeed");
        for (TrafficAggregator.AggregatedBucket b : this.aggregator.results())
        {
            SiteMetadata md = this.metadataMap.get(b.siteId);
            String road = md != null ? md.roadName() : "";
            String dir = md != null ? md.direction() : "";
            String lanes = String.valueOf(md != null ? md.numberOfLanes() : 0);
            String lat = md != null ? String.format(Locale.ROOT, "%.7f", md.lat()) : "";
            String lon = md != null ? String.format(Locale.ROOT, "%.7f", md.lon()) : "";
            String hm = ""; // (md != null && md.hm() != null) ? String.format(Locale.ROOT, "%.2f", md.hm()) : "";
            String flow = String.format(Locale.ROOT, "%.0f", b.totalFlow);
            String spd = String.format(Locale.ROOT, "%.3f", b.avgSpeed());
            csv.writeRow(String.valueOf(b.bucketId), b.siteId, road, dir, lanes, lat, lon, hm, b.classLabel, flow, spd);
        }
        if (out != null)
            out.flush();
    }
}
