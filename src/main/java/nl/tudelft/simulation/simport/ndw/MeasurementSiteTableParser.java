package nl.tudelft.simulation.simport.ndw;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
     * @param xmlOrZipGz an xml file or a zipped file with gzip-files to parse
     * @return a Map with entries mapping the site id to a metadata-record
     * @throws Exception in case parsing fails or there is a problem with the file
     */
    public static Map<String, SiteMetadata> parse(final File xmlOrZipGz) throws Exception
    {
        System.out.println("\nReading config");
        Map<String, SiteMetadata> map = new HashMap<>();
        // XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLInputFactory factory = new com.fasterxml.aalto.stax.InputFactoryImpl();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);

        if (xmlOrZipGz.getName().toLowerCase().endsWith(".xml"))
        {
            System.out.println("Reading file: " + xmlOrZipGz.getName());
            try (InputStream in = new BufferedInputStream(new FileInputStream(xmlOrZipGz)))
            {
                parseOne(factory.createXMLStreamReader(in), map);
            }
        }
        else
        {
            try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(xmlOrZipGz))
            {
                var en = zipFile.entries();
                while (en.hasMoreElements())
                {
                    java.util.zip.ZipEntry ze = en.nextElement();
                    if (ze.isDirectory())
                        continue;
                    try (InputStream gz =
                            new java.util.zip.GZIPInputStream(new BufferedInputStream(zipFile.getInputStream(ze))))
                    {
                        System.out.println("Reading entry: " + ze.getName());
                        parseOne(factory.createXMLStreamReader(gz), map);
                    }
                }
            }
        }
        return map;
    }

    private static void parseOne(final XMLStreamReader reader, final Map<String, SiteMetadata> out) throws Exception
    {
        String siteId = null;
        String roadName = null;
        String direction = null;
        String locationName = null;
        double lat = Double.NaN, lon = Double.NaN;
        int lanes = 0;
        Map<Integer, SiteMetadata.VehicleLengthBand> idxMap = new HashMap<>();

        Integer currentIndex = null; // measurementSpecificCharacteristics @index
        String currentValueType = null; // trafficFlow or trafficSpeed
        Double tmpMin = null, tmpMax = null;
        String cmpOp = null;

        while (reader.hasNext())
        {
            int ev = reader.next();
            if (ev == XMLStreamConstants.START_ELEMENT)
            {
                String local = reader.getLocalName();
                if ("measurementSiteRecord".equals(local))
                {
                    if (siteId != null)
                    {
                        out.put(siteId, new SiteMetadata(siteId, roadName, direction, lat, lon, lanes, locationName,
                                new HashMap<>(idxMap)));
                        idxMap.clear();
                        roadName = direction = locationName = null;
                        lat = lon = Double.NaN;
                        lanes = 0;
                    }
                    siteId = reader.getAttributeValue(null, "id");
                }
                else if ("value".equals(local) && roadName == null)
                {
                    String val = reader.getElementText();
                    roadName = val;
                    locationName = val;
                }
                else if ("measurementSide".equals(local))
                {
                    direction = reader.getElementText();
                }
                else if ("measurementSiteNumberOfLanes".equals(local))
                {
                    try
                    {
                        lanes = Integer.parseInt(reader.getElementText());
                    }
                    catch (Exception ignore)
                    {
                    }
                }
                else if ("latitude".equals(local))
                {
                    try
                    {
                        lat = Double.parseDouble(reader.getElementText());
                    }
                    catch (Exception ignore)
                    {
                    }
                }
                else if ("longitude".equals(local))
                {
                    try
                    {
                        lon = Double.parseDouble(reader.getElementText());
                    }
                    catch (Exception ignore)
                    {
                    }
                }
                else if ("measurementSpecificCharacteristics".equals(local))
                {
                    String idxAttr = reader.getAttributeValue(null, "index");
                    currentIndex = idxAttr != null ? Integer.parseInt(idxAttr) : null;
                }
                else if ("specificMeasurementValueType".equals(local))
                {
                    currentValueType = reader.getElementText();
                }
                else if ("lengthCharacteristic".equals(local))
                {
                    tmpMin = null;
                    tmpMax = null;
                    cmpOp = null;
                }
                else if ("comparisonOperator".equals(local))
                {
                    cmpOp = reader.getElementText();
                }
                else if ("vehicleLength".equals(local) || "length".equals(local))
                {
                    String txt = reader.getElementText();
                    Double v = null;
                    try
                    {
                        v = Double.parseDouble(txt);
                    }
                    catch (Exception ignore)
                    {
                    }
                    if (v != null && cmpOp != null)
                    {
                        if (cmpOp.toLowerCase().contains("greater"))
                            tmpMin = v;
                        else if (cmpOp.toLowerCase().contains("less"))
                            tmpMax = v;
                    }
                }
            }
            else if (ev == XMLStreamConstants.END_ELEMENT)
            {
                String local = reader.getLocalName();
                if ("lengthCharacteristic".equals(local))
                {
                    if (currentIndex != null)
                    {
                        SiteMetadata.VehicleLengthBand prev = idxMap.get(currentIndex);
                        Double min = tmpMin, max = tmpMax;
                        if (prev != null)
                        {
                            if (min == null)
                                min = prev.min();
                            if (max == null)
                                max = prev.max();
                        }
                        idxMap.put(currentIndex, new SiteMetadata.VehicleLengthBand(min, max, currentValueType));
                    }
                    tmpMin = tmpMax = null;
                    cmpOp = null;
                }
                else if ("measurementSiteRecord".equals(local))
                {
                    if (siteId != null)
                    {
                        out.put(siteId, new SiteMetadata(siteId, roadName, direction, lat, lon, lanes, locationName,
                                new HashMap<>(idxMap)));
                        idxMap.clear();
                        siteId = null;
                        roadName = direction = locationName = null;
                        lat = lon = Double.NaN;
                        lanes = 0;
                    }
                }
            }
        }
        if (siteId != null)
        {
            out.put(siteId,
                    new SiteMetadata(siteId, roadName, direction, lat, lon, lanes, locationName, new HashMap<>(idxMap)));
        }
    }
}
