package nl.tudelft.simulation.simport.ndw;

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
 * @param minLength minimum length of the vehicle
 * @param maxLength maximum length of the vehicle
 */
public record SiteMetadata(String siteId, String roadName, String direction, double lat, double lon, double minLength,
        double maxLength)
{
}
