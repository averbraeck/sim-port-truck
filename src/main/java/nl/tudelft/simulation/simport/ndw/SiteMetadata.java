package nl.tudelft.simulation.simport.ndw;

import java.util.Map;

/**
 * SiteMetadata contains the metadata for a measurement site, including classification of the vehicle length.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param siteId NDW id of the site
 * @param roadName name of the road in human readable text
 * @param direction direction (N, S, W, E, etc.)
 * @param lat latitude
 * @param lon longitude
 * @param numberOfLanes number of lanes at the measurement point
 * @param locationName name of the location
 * @param hm hectometers on the road
 * @param indexToBand index number used in the metadata, mapped to vehicle length band
 */
public record SiteMetadata(String siteId, String roadName, String direction, double lat, double lon, int numberOfLanes,
        String locationName, Double hm, Map<Integer, VehicleLengthBand> indexToBand)
{
    /**
     * SiteMetadata.VehicleLengthBand for vehicle lengths.
     * @param min minimum length in meters
     * @param max maximum length in meters
     * @param valueType trafficFlow or trafficSpeed
     */
    public static record VehicleLengthBand(Double min, Double max, String valueType)
    {
        public String label()
        {
            String lo = (this.min == null ? "-inf" : String.valueOf(this.min));
            String hi = (this.max == null ? "+inf" : String.valueOf(this.max));
            return "len[" + lo + "," + hi + "]";
        }
    }
}
