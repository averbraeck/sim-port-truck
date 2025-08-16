package nl.tudelft.simulation.simport.ndw;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;
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

    public DailyDataProcessor(final Map<String, SiteMetadata> metadataMap, final long bucketSizeMillis)
    {
        this.metadataMap = metadataMap;
        this.aggregator = new TrafficAggregator(bucketSizeMillis);
    }

    public void processZip(final File xmlZipFile) throws Exception
    {
        System.out.println("\nReading data");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (ZipFile zipFile = new ZipFile(xmlZipFile))
        {
            var entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory())
                {
                    try (var inputStream = new GZIPInputStream(zipFile.getInputStream(entry)))
                    {
                        System.out.println("Reading entry... " + entry.getName());
                        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
                        long currentTimestamp = 0;
                        String currentSiteId = null;

                        while (reader.hasNext())
                        {
                            int evt = reader.next();
                            if (evt == XMLStreamConstants.START_ELEMENT)
                            {
                                String name = reader.getLocalName();
                                if ("measurementSiteReference".equals(name))
                                {
                                    currentSiteId = reader.getAttributeValue(null, "id");
                                }
                                else if ("measurementTimeDefault".equals(name))
                                {
                                    currentTimestamp =
                                            DatatypeConverter.parseDateTime(reader.getElementText()).getTimeInMillis();
                                }
                                else if ("vehicleFlowRate".equals(name))
                                {
                                    double flow = Double.parseDouble(reader.getElementText());
                                    this.aggregator.addMeasurement(currentSiteId, currentTimestamp, flow, 0);
                                }
                                else if ("speed".equals(name))
                                {
                                    double sp = Double.parseDouble(reader.getElementText());
                                    this.aggregator.addMeasurement(currentSiteId, currentTimestamp, 0, sp);
                                }
                            }
                            // then aggregator.addMeasurement(siteId, currentTimestamp, flow, speed);
                        }
                    }
                }
            }
        }
    }

    public void writeCsv(final String outputPath) throws Exception
    {
        // write aggregator.getResults() to CSV using FastCSV or similar
    }
}
