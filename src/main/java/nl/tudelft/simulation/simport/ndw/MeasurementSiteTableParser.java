package nl.tudelft.simulation.simport.ndw;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
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
        Map<String, SiteMetadata> map = new LinkedHashMap<>();
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
        String siteId = null, roadName = null, direction = null, locationName = null;
        double lat = Double.NaN, lon = Double.NaN;
        int lanes = 0;
        Double hm = null;
        Map<Integer, SiteMetadata.VehicleLengthBand> idxMap = new LinkedHashMap<>();
        Integer currentIndex = null;
        String currentValueType = null;
        Double tmpMin = null, tmpMax = null;
        String cmpOp = null;
        boolean inName = false;

        while (reader.hasNext())
        {
            int ev = reader.next();
            if (ev == XMLStreamConstants.START_ELEMENT)
            {
                String local = reader.getLocalName();
                switch (local)
                {
                    case "measurementSiteRecord":
                        if (siteId != null)
                        {
                            out.put(siteId, new SiteMetadata(siteId, roadName, direction, lat, lon, lanes, locationName, hm,
                                    new LinkedHashMap<>(idxMap)));
                            idxMap.clear();
                            roadName = direction = locationName = null;
                            lat = lon = Double.NaN;
                            lanes = 0;
                            hm = null;
                        }
                        siteId = reader.getAttributeValue(null, "id");
                        break;
                    case "measurementSiteName":
                        inName = true;
                        break;
                    case "value":
                        if (inName && roadName == null)
                        {
                            String val = reader.getElementText();
                            roadName = val;
                            locationName = val;
                            hm = tryParseHmFromLocationName(val);
                        }
                        break;
                    case "measurementSide":
                        direction = reader.getElementText();
                        break;
                    case "measurementSiteNumberOfLanes":
                        try
                        {
                            lanes = Integer.parseInt(reader.getElementText());
                        }
                        catch (Exception ignore)
                        {
                        }
                        break;
                    case "latitude":
                        try
                        {
                            lat = Double.parseDouble(reader.getElementText());
                        }
                        catch (Exception ignore)
                        {
                        }
                        break;
                    case "longitude":
                        try
                        {
                            lon = Double.parseDouble(reader.getElementText());
                        }
                        catch (Exception ignore)
                        {
                        }
                        break;
                    case "distanceAlong":
                    case "offsetDistance":
                        try
                        {
                            double meters = Double.parseDouble(reader.getElementText());
                            hm = meters / 100.0;
                        }
                        catch (Exception ignore)
                        {
                        }
                        break;
                    case "measurementSpecificCharacteristics":
                        String idxAttr = reader.getAttributeValue(null, "index");
                        currentIndex = idxAttr != null ? Integer.parseInt(idxAttr) : null;
                        currentValueType = null;
                        tmpMin = tmpMax = null;
                        cmpOp = null;
                        break;
                    case "specificMeasurementValueType":
                        currentValueType = reader.getElementText();
                        break;
                    case "lengthCharacteristic":
                        tmpMin = tmpMax = null;
                        cmpOp = null;
                        break;
                    case "comparisonOperator":
                        cmpOp = reader.getElementText();
                        break;
                    case "vehicleLength":
                    case "length":
                        try
                        {
                            double v = Double.parseDouble(reader.getElementText());
                            if (cmpOp != null)
                            {
                                String c = cmpOp.toLowerCase(Locale.ROOT);
                                if (c.contains("greater"))
                                    tmpMin = v;
                                else if (c.contains("less"))
                                    tmpMax = v;
                            }
                        }
                        catch (Exception ignore)
                        {
                        }
                        break;
                }
            }
            else if (ev == XMLStreamConstants.END_ELEMENT)
            {
                String local = reader.getLocalName();
                switch (local)
                {
                    case "measurementSiteName":
                        inName = false;
                        break;
                    case "lengthCharacteristic":
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
                        break;
                    case "measurementSiteRecord":
                        if (siteId != null)
                        {
                            out.put(siteId, new SiteMetadata(siteId, roadName, direction, lat, lon, lanes, locationName, hm,
                                    new LinkedHashMap<>(idxMap)));
                            idxMap.clear();
                            siteId = null;
                            roadName = direction = locationName = null;
                            lat = lon = Double.NaN;
                            lanes = 0;
                            hm = null;
                        }
                        break;
                }
            }
        }
        if (siteId != null)
        {
            out.put(siteId,
                    new SiteMetadata(siteId, roadName, direction, lat, lon, lanes, locationName, hm, new LinkedHashMap<>(idxMap)));
        }
    }

    private static Double tryParseHmFromLocationName(final String s)
    {
        if (s == null)
            return null;
        try
        {
            int i = s.indexOf("km");
            if (i >= 0)
            {
                String rest = s.substring(i + 2).trim();
                StringBuilder num = new StringBuilder();
                boolean seen = false;
                for (int k = 0; k < rest.length(); k++)
                {
                    char ch = rest.charAt(k);
                    if ((ch >= '0' && ch <= '9') || ch == '.' || ch == ',')
                    {
                        num.append(ch == ',' ? '.' : ch);
                        seen = true;
                    }
                    else if (seen)
                        break;
                }
                if (num.length() > 0)
                {
                    double km = Double.parseDouble(num.toString());
                    return km * 10.0; // hectometer
                }
            }
        }
        catch (Exception ignore)
        {
        }
        return null;
    }
}
