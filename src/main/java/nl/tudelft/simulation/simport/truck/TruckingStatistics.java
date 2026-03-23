package nl.tudelft.simulation.simport.truck;

/**
 * TruckingStatistics keeps the statistics for trucking.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TruckingStatistics
{
    // @formatter:off

    /** The total number of trips. */
    private int totalTrips = 0;
    private void incTotalTrips() {this.totalTrips++; }
    public int getTotalTrips() {return this.totalTrips; }

    /** The number of single import trips. */
    private int importTrips = 0;
    public void incImportTrips() {this.importTrips++; incTotalTrips(); }
    public int getImportTrips() {return this.importTrips; }

    /** The number of single export trips. */
    private int exportTrips = 0;
    public void incExportTrips() {this.exportTrips++; incTotalTrips(); }
    public int getExportTrips() {return this.exportTrips; }

    /** The number of combined trips with 1 terminal. */
    private int combinedTrips1Terminal = 0;
    public void incCombinedTrips1Terminal() {this.combinedTrips1Terminal++; incTotalTrips(); }
    public int getCombinedTrips1Terminal() {return this.combinedTrips1Terminal; }

    /** The number of combined trips with 2 terminals. */
    private int combinedTrips2Terminals = 0;
    public void incCombinedTrips2Terminals() {this.combinedTrips2Terminals++; incTotalTrips(); }
    public int getCombinedTrips2Terminals() {return this.combinedTrips2Terminals; }

    // @formatter:on

}
