package nl.tudelft.simulation.simport.ndw;

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
    private final long bucketSizeMillis;

    private final boolean flowWeightedSpeed;

    private final java.util.concurrent.ConcurrentHashMap<String, AggregatedBucket> active =
            new java.util.concurrent.ConcurrentHashMap<>();

    public TrafficAggregator(final long bucketSizeMillis, final boolean flowWeightedSpeed)
    {
        this.bucketSizeMillis = bucketSizeMillis;
        this.flowWeightedSpeed = flowWeightedSpeed;
    }

    public long bucketOf(final long ts)
    {
        return (ts / this.bucketSizeMillis) * this.bucketSizeMillis;
    }

    private String key(final String siteId, final String classLabel, final long bucketId)
    {
        return siteId + "#" + classLabel + "#" + bucketId;
    }

    public void addFlow(final String siteId, final String classLabel, final long timestamp, final double flow)
    {
        long bucketId = bucketOf(timestamp);
        this.active.compute(key(siteId, classLabel, bucketId), (k, b) ->
        {
            if (b == null)
                b = new AggregatedBucket(siteId, classLabel, bucketId);
            b.totalFlow += flow;
            b.countSamplesFlow++;
            return b;
        });
    }

    public void addSpeed(final String siteId, final String classLabel, final long timestamp, final double speed,
            final Double weightFlow)
    {
        long bucketId = bucketOf(timestamp);
        this.active.compute(key(siteId, classLabel, bucketId), (k, b) ->
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

    /** Drain and remove exactly the given bucketId. */
    public java.util.List<AggregatedBucket> drainBucket(final long bucketId)
    {
        java.util.List<AggregatedBucket> out = new java.util.ArrayList<>();
        java.util.Iterator<java.util.Map.Entry<String, AggregatedBucket>> it = this.active.entrySet().iterator();
        while (it.hasNext())
        {
            var e = it.next();
            if (e.getValue().bucketId == bucketId)
            {
                out.add(e.getValue());
                it.remove();
            }
        }
        return out;
    }

    /** Drain and remove all buckets with bucketId < cutoffExclusive. */
    public java.util.List<AggregatedBucket> drainUpToExclusive(final long cutoffExclusive)
    {
        java.util.List<AggregatedBucket> out = new java.util.ArrayList<>();
        java.util.Iterator<java.util.Map.Entry<String, AggregatedBucket>> it = this.active.entrySet().iterator();
        while (it.hasNext())
        {
            var e = it.next();
            if (e.getValue().bucketId < cutoffExclusive)
            {
                out.add(e.getValue());
                it.remove();
            }
        }
        return out;
    }

    public java.util.Collection<AggregatedBucket> results()
    {
        return this.active.values();
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
