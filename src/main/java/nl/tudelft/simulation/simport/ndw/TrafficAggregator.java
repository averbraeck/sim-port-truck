package nl.tudelft.simulation.simport.ndw;

import java.util.Collection;

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
    private final java.util.concurrent.ConcurrentHashMap<String, AggregatedBucket> buckets =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final long bucketSizeMillis;

    private final boolean flowWeightedSpeed;

    public TrafficAggregator(final long bucketSizeMillis, final boolean flowWeightedSpeed)
    {
        this.bucketSizeMillis = bucketSizeMillis;
        this.flowWeightedSpeed = flowWeightedSpeed;
    }

    private String key(final String siteId, final String classLabel, final long bucketId)
    {
        return siteId + "#" + classLabel + "#" + bucketId;
    }

    public void addFlow(final String siteId, final String classLabel, final long timestamp, final double flow)
    {
        long bucketId = (timestamp / this.bucketSizeMillis) * this.bucketSizeMillis;
        this.buckets.compute(key(siteId, classLabel, bucketId), (
                k, b
        ) ->
        {
            if (b == null)
                b = new AggregatedBucket(siteId, classLabel, bucketId);
            b.totalFlow += flow;
            b.countSamplesFlow++;
            return b;
        });
    }

    public void addSpeed(final String siteId, final String classLabel, final long timestamp, final double speed, final Double weightFlow)
    {
        long bucketId = (timestamp / this.bucketSizeMillis) * this.bucketSizeMillis;
        this.buckets.compute(key(siteId, classLabel, bucketId), (
                k, b
        ) ->
        {
            if (b == null)
                b = new AggregatedBucket(siteId, classLabel, bucketId);
            if (this.flowWeightedSpeed && weightFlow != null && !Double.isNaN(speed))
            {
                b.speedWeightedSum += speed * weightFlow;
                b.speedWeight += weightFlow;
            }
            else if (!Double.isNaN(speed))
            {
                b.speedSimpleSum += speed;
                b.countSamplesSpeed++;
            }
            return b;
        });
    }

    public Collection<AggregatedBucket> results()
    {
        return this.buckets.values();
    }

    public static class AggregatedBucket
    {
        public final String siteId;

        public final String classLabel;

        public final long bucketId;

        public double totalFlow = 0;

        public long countSamplesFlow = 0;

        public double speedWeightedSum = 0;

        public double speedWeight = 0;

        public double speedSimpleSum = 0;

        public long countSamplesSpeed = 0;

        public AggregatedBucket(final String siteId, final String classLabel, final long bucketId)
        {
            this.siteId = siteId;
            this.classLabel = classLabel;
            this.bucketId = bucketId;
        }

        public double avgSpeed()
        {
            if (this.speedWeight > 0)
                return this.speedWeightedSum / this.speedWeight;
            if (this.countSamplesSpeed > 0)
                return this.speedSimpleSum / this.countSamplesSpeed;
            return Double.NaN;
        }
    }
}
