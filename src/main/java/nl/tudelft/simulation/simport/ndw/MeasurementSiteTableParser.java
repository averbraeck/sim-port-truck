package nl.tudelft.simulation.simport.ndw;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * MeasurementSiteTableParser parses a file of metadata for interpreting the DATEX-II data.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class MeasurementSiteTableParser
{
    /**
     * StAX parser for a DATEX-II metadata-file.
     * @param xmlZipFile the zipped file to parse
     * @return a Map with entries mapping the site id to a metadata-record
     * @throws Exception in case parsing fails or there is a problem with the file
     */
    public static Map<String, SiteMetadata> parse(final File xmlZipFile) throws Exception
    {
        System.out.println("\nReading config");
        Map<String, SiteMetadata> map = new HashMap<>();
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
                        String siteId = null;
                        String roadName = null;
                        String direction = null;
                        double lat = 0, lon = 0;
                        double minLength = 0, maxLength = 0;

                        while (reader.hasNext())
                        {
                            int event = reader.next();
                            if (event == XMLStreamConstants.START_ELEMENT)
                            {
                                String local = reader.getLocalName();
                                if ("measurementSiteRecord".equals(local))
                                {
                                    siteId = reader.getAttributeValue(null, "id");
                                    // reset fields for new record
                                    roadName = direction = null;
                                    lat = lon = minLength = maxLength = 0;
                                }
                                if ("value".equals(local) && roadName == null)
                                {
                                    roadName = reader.getElementText();
                                }
                                else if ("measurementSide".equals(local))
                                {
                                    direction = reader.getElementText();
                                }
                            }
                            else if (event == XMLStreamConstants.END_ELEMENT)
                            {
                                if ("measurementSiteRecord".equals(reader.getLocalName()))
                                {
                                    SiteMetadata md =
                                            new SiteMetadata(siteId, roadName, direction, lat, lon, minLength, maxLength);
                                    map.put(siteId, md);
                                }
                            }
                        }
                    }
                }
            }
        }
        return map;
    }
}
