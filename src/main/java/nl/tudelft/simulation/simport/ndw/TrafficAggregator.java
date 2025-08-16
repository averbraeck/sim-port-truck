package nl.tudelft.simulation.simport.ndw;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TrafficAggregator.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TrafficAggregator
{
    private final Map<String, AggregatedBucket> buckets = new HashMap<>();

    private final long bucketSizeMillis;

    public TrafficAggregator(final long bucketSizeMillis)
    {
        this.bucketSizeMillis = bucketSizeMillis;
    }

    public void addMeasurement(final String siteId, final long timestamp, final double flow, final double speed)
    {
        long bucketId = (timestamp / this.bucketSizeMillis) * this.bucketSizeMillis;
        String key = siteId + "#" + bucketId;
        AggregatedBucket b = this.buckets.computeIfAbsent(key, k -> new AggregatedBucket());
        b.add(flow, speed);
    }

    public Collection<AggregatedBucket> getResults()
    {
        return this.buckets.values();
    }

    public static class AggregatedBucket
    {
        public double totalFlow = 0;

        public double totalSpeed = 0;

        public int count = 0;

        public void add(final double flow, final double speed)
        {
            this.totalFlow += flow;
            this.totalSpeed += speed;
            this.count++;
        }

        public double avgSpeed()
        {
            return (this.count == 0 ? 0 : this.totalSpeed / this.count);
        }
    }
}
