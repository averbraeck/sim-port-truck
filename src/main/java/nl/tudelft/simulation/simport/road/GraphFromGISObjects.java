package nl.tudelft.simulation.simport.road;

import java.awt.geom.Point2D;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.djutils.io.ResourceResolver;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import nl.tudelft.simulation.dsol.animation.gis.GisMapInterface;
import nl.tudelft.simulation.simport.gis.DbfReader;
import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Make a graph from the GIS objects.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class GraphFromGISObjects
{
    public GraphFromGISObjects(final GisMapInterface map, final PortModel model, final URL csvCentroidUrl)
    {
        Map<String, String> shapeFileMap = new HashMap<>();
        try
        {
            NamedCsvReader csvCentroid = NamedCsvReader.builder().build(Paths.get(csvCentroidUrl.toURI()));
            csvCentroid.forEach((row) -> shapeFileMap.put(row.getField("layer"), row.getField("shapeFile")));
            URL nodesUrl = ResourceResolver.resolve(shapeFileMap.get("nodes").replace(".shp", ".dbf")).asUrl();
            DbfReader dbfReader = new DbfReader(nodesUrl);
            String[] colNames = dbfReader.getColumnNames();
            String[][] data = dbfReader.getRows();

            var layerMap = map.getLayerMap();
            var centroidLayer = layerMap.get("centroids");
            for (var feature : centroidLayer.getFeatures())
            {
                for (int i = 0; i < feature.getNumPoints(); i++)
                {
                    Point2D point = feature.getPoint(i);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
