package nl.tudelft.simulation.simport.ndw;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.djutils.exceptions.Throw;

import de.siegmar.fastcsv.writer.CsvWriter;

/**
 * DailyDataProcessor.java.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DailyDataProcessor
{
    public static class BoundingBox
    {
        private final double minLat;

        private final double minLon;

        private final double maxLat;

        private final double maxLon;

        public BoundingBox(final double minLat, final double minLon, final double maxLat, final double maxLon)
        {
            Throw.when(minLat > maxLat, IllegalArgumentException.class, "minLat > maxLat");
            Throw.when(minLon > maxLon, IllegalArgumentException.class, "minLon > maxLon");
            this.minLat = minLat;
            this.minLon = minLon;
            this.maxLat = maxLat;
            this.maxLon = maxLon;
        }

        public boolean contains(final double lat, final double lon)
        {
            return lat >= this.minLat && lat <= this.maxLat && lon >= this.minLon && lon <= this.maxLon;
        }
    }

    private final Map<String, SiteMetadata> metadataMap;

    private final TrafficAggregator aggregator;

    private final BoundingBox bbox;

    private final long bucketSizeMillis;

    public DailyDataProcessor(final Map<String, SiteMetadata> metadataMap, final long bucketSizeMillis)
    {
        this(metadataMap, bucketSizeMillis, null, true);
    }

    public DailyDataProcessor(final Map<String, SiteMetadata> metadataMap, final long bucketSizeMillis, final BoundingBox bbox,
            final boolean flowWeightedSpeed)
    {
        this.metadataMap = metadataMap;
        this.bbox = bbox;
        this.bucketSizeMillis = bucketSizeMillis;
        this.aggregator = new TrafficAggregator(bucketSizeMillis, flowWeightedSpeed);
    }

    private static XMLInputFactory staxFactory()
    {
        // XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLInputFactory factory = new com.fasterxml.aalto.stax.InputFactoryImpl();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        try
        {
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        }
        catch (Exception ignore)
        {
        }
        try
        {
            factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        }
        catch (Exception ignore)
        {
        }
        return factory;
    }

    // ---------------------------
    // Simple sequential streaming
    // ---------------------------
    public void processZipSequential(final File zipFile, final Writer out) throws Exception
    {
        System.out.println("\nReading data (sequential)");
        XMLInputFactory factory = staxFactory();
        CsvWriter csv = CsvWriter.builder().build(out);
        writeHeader(csv);

        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile))
        {
            var en = zip.entries();
            while (en.hasMoreElements())
            {
                java.util.zip.ZipEntry ze = en.nextElement();
                if (ze.isDirectory())
                    continue;
                try (InputStream gz = new java.util.zip.GZIPInputStream(new BufferedInputStream(zip.getInputStream(ze))))
                {
                    System.out.println("Reading entry: " + ze.getName());
                    parseOne(factory, gz);
                }
            }
        }
        // flush all remaining buckets
        flushBuckets(csv, null, Long.MAX_VALUE);
        out.flush();
    }

    // ---------------------------------------------------
    // Parallel (unordered) streaming
    // Workers only parse; we flush after all tasks finish
    // ---------------------------------------------------
    public void processZipParallel(final File zipFile, final int threads, final Writer out) throws Exception
    {
        System.out.println("\nReading data (parallel)");
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threads));
        XMLInputFactory factory = staxFactory();
        CsvWriter csv = CsvWriter.builder().build(out);
        writeHeader(csv);

        try (ZipFile zf = new ZipFile(zipFile))
        {
            List<Future<?>> futures = new ArrayList<>();
            Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements())
            {
                ZipEntry ze = en.nextElement();
                if (ze.isDirectory())
                    continue;
                futures.add(pool.submit(() ->
                {
                    try (InputStream in = new BufferedInputStream(zf.getInputStream(ze), 1 << 16);
                            GZIPInputStream gzip = new GZIPInputStream(in, 1 << 15))
                    {
                        System.out.println("Starting  pooled entry: " + ze.getName());
                        parseOne(factory, gzip);
                        System.out.println("Finishing pooled entry: " + ze.getName());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }));
            }
            for (Future<?> f : futures)
            {
                try
                {
                    f.get();
                }
                catch (Exception ignore)
                {
                }
            }
        }
        finally
        {
            pool.shutdown();
        }
        // After all workers finished, flush everything in order
        flushBuckets(csv, null, Long.MAX_VALUE);
        out.flush();
    }

    // ---------------------------------------------------
    // Parallel ordered streaming with per-bucket barriers
    // ---------------------------------------------------
    public void processZipParallelOrderedAndStreamCsv(final File zipFile, final int threads, final Writer out,
            final boolean useFastCsv) throws Exception
    {
        System.out.println("\nReading data (parallel, streamed)");
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threads));
        XMLInputFactory factory = staxFactory();
        CsvWriter csv = useFastCsv ? CsvWriter.builder().build(out) : null;
        PrintWriter pw = useFastCsv ? null
                : new PrintWriter(new BufferedWriter(out instanceof Writer ? (Writer) out : new StringWriter(), 1 << 20));
        if (csv != null)
            writeHeader(csv);
        else
            writeHeader(pw);

        try (ZipFile zf = new ZipFile(zipFile))
        {
            // 1) Build index of entries with their bucket based on filename timestamp
            record EntryMeta(ZipEntry ze, long bucket)
            {
            }
            List<EntryMeta> list = new ArrayList<>();
            Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements())
            {
                ZipEntry ze = en.nextElement();
                if (ze.isDirectory())
                    continue;
                long ts = parseEntryEpochMs(ze.getName());
                if (ts <= 0)
                    continue; // skip if filename unexpected
                long bucket = (ts / this.bucketSizeMillis) * this.bucketSizeMillis;
                list.add(new EntryMeta(ze, bucket));
            }
            // 2) Sort by bucket
            list.sort(Comparator.comparingLong(EntryMeta::bucket));

            // 3) Process per-bucket groups
            int i = 0;
            while (i < list.size())
            {
                long bkt = list.get(i).bucket();
                int j = i;
                while (j < list.size() && list.get(j).bucket() == bkt)
                    j++;

                // submit tasks for [i, j)
                List<Callable<Void>> tasks = new ArrayList<>(j - i);
                for (int k = i; k < j; k++)
                {
                    ZipEntry ze = list.get(k).ze();
                    tasks.add(() ->
                    {
                        try (InputStream in = new BufferedInputStream(zf.getInputStream(ze), 1 << 16);
                                GZIPInputStream gzip = new GZIPInputStream(in, 1 << 15))
                        {
                            System.out.println("Starting  task for entry: " + ze.getName());
                            parseOne(factory, gzip);
                            System.out.println("Finishing task for entry: " + ze.getName());
                        }
                        return null;
                    });
                }
                pool.invokeAll(tasks); // barrier for this bucket group

                // Now it is safe to drain exactly this bucket
                List<TrafficAggregator.AggregatedBucket> done = new ArrayList<>(this.aggregator.drainBucket(bkt));
                Comparator<TrafficAggregator.AggregatedBucket> cmp =
                        Comparator.comparingLong((final TrafficAggregator.AggregatedBucket b) -> b.bucketId)
                                .thenComparing(b -> b.siteId).thenComparing(b -> b.classLabel);
                done.sort(cmp);
                if (csv != null)
                {
                    for (var b : done)
                        writeRow(csv, b);
                }
                else
                {
                    for (var b : done)
                        writeRow(pw, b);
                }

                i = j;
            }

            // Any residuals (should be none, but safe):
            List<TrafficAggregator.AggregatedBucket> leftovers =
                    new ArrayList<>(this.aggregator.drainUpToExclusive(Long.MAX_VALUE));
            leftovers.sort(Comparator.comparingLong((final TrafficAggregator.AggregatedBucket b) -> b.bucketId));
            if (csv != null)
                for (var b : leftovers)
                    writeRow(csv, b);
            else
                for (var b : leftovers)
                    writeRow(pw, b);
        }
        finally
        {
            pool.shutdown();
        }
        out.flush();
    }

    // --------------------------------------------------
    // Core XML parse for one .gz entry (no writing here)
    // --------------------------------------------------
    private void parseOne(final XMLInputFactory factory, final InputStream in) throws Exception
    {
        XMLStreamReader reader = factory.createXMLStreamReader(in);
        String currentSiteId = null;
        long currentTs = Long.MIN_VALUE;
        Integer currentIndex = null;
        boolean dataErrorFlag = false;
        Double pendingFlowForWeight = null;

        while (reader.hasNext())
        {
            int evt = reader.next();
            if (evt == XMLStreamConstants.START_ELEMENT)
            {
                String name = reader.getLocalName();
                switch (name)
                {
                    case "measurementSiteReference":
                        currentSiteId = reader.getAttributeValue(null, "id");
                        break;
                    case "measurementTimeDefault":
                        String ts = reader.getElementText();
                        currentTs = java.time.Instant.parse(ts).toEpochMilli();
                        break;
                    case "measuredValue":
                        String idxAttr = reader.getAttributeValue(null, "index");
                        currentIndex = idxAttr != null ? Integer.parseInt(idxAttr) : null;
                        dataErrorFlag = false;
                        pendingFlowForWeight = null;
                        break;
                    case "dataError":
                        String v = reader.getElementText();
                        if ("true".equalsIgnoreCase(v))
                            dataErrorFlag = true;
                        break;
                    case "vehicleFlowRate":
                        String flowTxt = reader.getElementText();
                        if (!dataErrorFlag)
                        {
                            double flow = safeParseDouble(flowTxt);
                            String classLabel = vehicleClassLabel(currentSiteId, currentIndex);
                            if (classLabel != null && !Double.isNaN(flow))
                            {
                                this.aggregator.addFlow(currentSiteId, classLabel, currentTs, flow);
                                pendingFlowForWeight = flow;
                            }
                        }
                        break;
                    case "speed":
                        String spTxt = reader.getElementText();
                        if (!dataErrorFlag)
                        {
                            double sp = safeParseDouble(spTxt);
                            if (sp >= 0)
                            {
                                String classLabel2 = vehicleClassLabel(currentSiteId, currentIndex);
                                if (classLabel2 != null && !Double.isNaN(sp))
                                {
                                    this.aggregator.addSpeed(currentSiteId, classLabel2, currentTs, sp, pendingFlowForWeight);
                                }
                            }
                        }
                        break;
                }
            }
        }
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static final Pattern FILENAME_TS = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})");

    private long parseEntryEpochMs(final String name)
    {
        Matcher m = FILENAME_TS.matcher(new File(name).getName());
        if (!m.find())
            return -1L;
        int y = Integer.parseInt(m.group(1));
        int mo = Integer.parseInt(m.group(2));
        int d = Integer.parseInt(m.group(3));
        int h = Integer.parseInt(m.group(4));
        int mi = Integer.parseInt(m.group(5));
        int s = Integer.parseInt(m.group(6));
        return java.time.ZonedDateTime.of(y, mo, d, h, mi, s, 0, java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private void writeHeader(final CsvWriter csv)
    {
        csv.writeRow("timestamp", "siteId", "roadName", "direction", "lanes", "lat", "lon", "hm", "vehicleClass",
                "vehicleCount", "avgSpeed");
    }

    private void writeHeader(final PrintWriter pw)
    {
        pw.println("timestamp,siteId,roadName,direction,lanes,lat,lon,hm,vehicleClass,vehicleCount,avgSpeed");
    }

    private void flushBuckets(final CsvWriter csv, final PrintWriter pw, final long cutoffExclusive)
    {
        List<TrafficAggregator.AggregatedBucket> drained = this.aggregator.drainUpToExclusive(cutoffExclusive);
        drained.sort(Comparator.comparingLong((final TrafficAggregator.AggregatedBucket b) -> b.bucketId)
                .thenComparing(b -> b.siteId).thenComparing(b -> b.classLabel));
        if (csv != null)
            drained.forEach(b -> writeRow(csv, b));
        if (pw != null)
            drained.forEach(b -> writeRow(pw, b));
    }

    private void writeRow(final CsvWriter csv, final TrafficAggregator.AggregatedBucket b)
    {
        SiteMetadata md = this.metadataMap.get(b.siteId);
        String road = md != null ? md.roadName() : "";
        String dir = md != null ? md.direction() : "";
        String lanes = String.valueOf(md != null ? md.numberOfLanes() : 0);
        String lat = md != null && !Double.isNaN(md.lat()) ? String.format(Locale.ROOT, "%.7f", md.lat()) : "";
        String lon = md != null && !Double.isNaN(md.lon()) ? String.format(Locale.ROOT, "%.7f", md.lon()) : "";
        String hm = (md != null && md.hm() != null) ? String.format(Locale.ROOT, "%.2f", md.hm()) : "";
        String flow = String.format(Locale.ROOT, "%.0f", b.totalFlow);
        String spd = String.format(Locale.ROOT, "%.3f", b.avgSpeed());
        csv.writeRow(String.valueOf(b.bucketId), b.siteId, road, dir, lanes, lat, lon, hm, b.classLabel, flow, spd);
    }

    private void writeRow(final PrintWriter pw, final TrafficAggregator.AggregatedBucket b)
    {
        SiteMetadata md = this.metadataMap.get(b.siteId);
        String road = md != null ? safeCsv(md.roadName()) : "";
        String dir = md != null ? safeCsv(md.direction()) : "";
        int lanes = md != null ? md.numberOfLanes() : 0;
        double lat = md != null ? md.lat() : Double.NaN;
        double lon = md != null ? md.lon() : Double.NaN;
        String hm = (md != null && md.hm() != null) ? String.format(Locale.ROOT, "%.2f", md.hm()) : "";
        pw.printf(Locale.ROOT, "%d,%s,%s,%s,%d,%.7f,%.7f,%s,%s,%.0f,%.3f%n", b.bucketId, b.siteId, road, dir, lanes, lat, lon,
                hm, b.classLabel, b.totalFlow, b.avgSpeed());
    }

    private static double safeParseDouble(final String s)
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch (Exception e)
        {
            return Double.NaN;
        }
    }

    private String vehicleClassLabel(final String siteId, final Integer index)
    {
        if (siteId == null || index == null)
            return "unknown";
        SiteMetadata md = this.metadataMap.get(siteId);
        if (md == null)
            return null; // unknown site → skip
        if (this.bbox != null)
        {
            if (Double.isNaN(md.lat()) || Double.isNaN(md.lon()))
                return null;
            if (!this.bbox.contains(md.lat(), md.lon()))
                return null;
        }
        SiteMetadata.VehicleLengthBand band = md.indexToBand().get(index);
        if (band != null)
            return band.label();
        return "unknown";
    }

    private static String safeCsv(final String s)
    {
        return s == null ? "" : s.replace(',', ' ');
    }

}
