package nl.tudelft.simulation.simport.network;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * Reads an O/D CSV with: <br>
 * - First row: column headers (destinations), cell [0,0] often empty or a title. <br>
 * - First column of each row: row header (origin). <br>
 * - Remaining cells: numeric volumes (supports decimal comma and thousand separators).
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OdMatrix
{
    private final double[][] values;

    private final List<String> rowLabels;

    private final List<String> colLabels;

    private final Map<String, Integer> rowIndexByLabel;

    private final Map<String, Integer> colIndexByLabel;

    private OdMatrix(final double[][] values, final List<String> rowLabels, final List<String> colLabels,
            final Map<String, Integer> rowIndexByLabel, final Map<String, Integer> colIndexByLabel)
    {
        this.values = values;
        this.rowLabels = rowLabels;
        this.colLabels = colLabels;
        this.rowIndexByLabel = rowIndexByLabel;
        this.colIndexByLabel = colIndexByLabel;
    }

    public static OdMatrix fromCsv(final Path path, final char delimiter, final Charset charset, final boolean decimalComma,
            final boolean trimHeaders, final boolean normalizeNbsp) throws IOException
    {

        CsvReader reader = CsvReader.builder().fieldSeparator(delimiter).quoteCharacter('"')
                // .skipEmptyRows(true) // <- not available in all 2.x builds; we handle empties manually
                .build(path, charset);

        // ---- Stream rows and take the first as header ----
        Iterator<CsvRow> it = reader.stream().iterator();
        if (!it.hasNext())
        {
            throw new IllegalArgumentException("Empty CSV: " + path);
        }

        CsvRow headerRow = it.next();
        List<String> headerFields = new ArrayList<>(headerRow.getFields());
        if (headerFields.isEmpty())
        {
            throw new IllegalArgumentException("Header row has no fields: " + path);
        }

        // Column labels are header fields from index 1..end
        int nCols = headerFields.size() - 1;
        if (nCols <= 0)
        {
            throw new IllegalArgumentException("Expected at least one destination column in header");
        }

        List<String> colLabels = new ArrayList<>(nCols);
        for (int c = 1; c < headerFields.size(); c++)
        {
            String lbl = headerFields.get(c);
            if (lbl == null)
                lbl = "";
            if (normalizeNbsp)
                lbl = lbl.replace('\u00A0', ' ');
            if (trimHeaders)
                lbl = lbl.trim();
            colLabels.add(lbl);
        }

        // Build destination index map
        Map<String, Integer> colIndex = new LinkedHashMap<>(nCols * 2);
        for (int c = 0; c < nCols; c++)
        {
            String label = colLabels.get(c);
            if (colIndex.put(label, c) != null)
            {
                System.err.println("Duplicate destination label in header: '" + label + "'");
            }
        }

        // ---- Read remaining rows into dynamic buffers (no pre-count needed) ----
        List<String> rowLabels = new ArrayList<>(1024);
        Map<String, Integer> rowIndex = new LinkedHashMap<>(2048);
        List<double[]> rowsValues = new ArrayList<>(1024);

        while (it.hasNext())
        {
            CsvRow row = it.next();

            // Handle "empty" rows manually
            if (isEffectivelyEmpty(row))
            {
                continue;
            }

            int fieldCount = row.getFieldCount();
            if (fieldCount < 1)
            {
                continue;
            }

            // Row label at column 0
            String rowLabel = safeCell(row, 0, trimHeaders, normalizeNbsp);
            if (rowLabel.isEmpty())
            {
                // You can choose to skip or fail here; we'll fail for data integrity
                throw new IllegalArgumentException("Missing origin label in a data row (first column empty).");
            }

            // Parse numeric cells
            double[] vals = new double[nCols];
            int maxDataCells = Math.min(nCols, Math.max(0, fieldCount - 1));
            for (int c = 0; c < nCols; c++)
            {
                double v;
                if (c < maxDataCells)
                {
                    String raw = row.getField(c + 1);
                    v = parseNumber(raw, decimalComma);
                }
                else
                {
                    v = 0.0; // or Double.NaN if you want to distinguish missing
                }
                vals[c] = v;
            }

            // Store
            int rIndex = rowLabels.size();
            rowLabels.add(rowLabel);
            if (rowIndex.put(rowLabel, rIndex) != null)
            {
                System.err.println("Duplicate origin label: '" + rowLabel + "'");
            }
            rowsValues.add(vals);
        }

        if (rowLabels.isEmpty())
        {
            throw new IllegalArgumentException("No data rows found after header in " + path);
        }

        // Convert List<double[]> → double[][]
        double[][] values = rowsValues.toArray(new double[0][]);

        return new OdMatrix(values, rowLabels, colLabels, rowIndex, colIndex);
    }

    private static boolean isEffectivelyEmpty(final CsvRow row)
    {
        if (row == null || row.getFieldCount() == 0)
            return true;
        for (int i = 0; i < row.getFieldCount(); i++)
        {
            String f = row.getField(i);
            if (f != null && !f.trim().isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    private static String safeCell(final CsvRow row, final int idx, final boolean trim, final boolean normalizeNbsp)
    {
        String s = (idx < row.getFieldCount() ? row.getField(idx) : "");
        if (s == null)
            s = "";
        if (normalizeNbsp)
            s = s.replace('\u00A0', ' ');
        return trim ? s.trim() : s;
    }

    /**
     * Parses common EU/US numeric formats: "1234.56", "1,234.56", "1234,56", "1.234,56", "1 234,56"
     */
    private static double parseNumber(final String raw, final boolean decimalComma)
    {
        if (raw == null)
            return 0.0;
        String s = raw.trim().replace("\u00A0", ""); // remove nbsp
        if (s.isEmpty())
            return 0.0;

        if (decimalComma)
        {
            // remove thousands separators '.' or spaces, then replace decimal comma
            s = s.replace(" ", "").replace(".", "").replace(",", ".");
        }
        else
        {
            // remove thousands separators ',' or spaces (keep '.')
            s = s.replace(" ", "").replace(",", "");
        }

        try
        {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException nfe)
        {
            // tolerant fallback if mixed formats sneak in
            return Double.parseDouble(s.replace(',', '.'));
        }
    }

    // ----- Public API -----
    public double get(final String origin, final String destination)
    {
        Integer ri = this.rowIndexByLabel.get(origin);
        if (ri == null)
            throw new NoSuchElementException("Unknown origin: " + origin);
        Integer ci = this.colIndexByLabel.get(destination);
        if (ci == null)
            throw new NoSuchElementException("Unknown destination: " + destination);
        return this.values[ri][ci];
    }

    /**
     * Returns a map of all destination volumes for the given origin. Map keys are destination labels; values are volumes. The
     * iteration order matches the original column order.
     * @throws NoSuchElementException if the origin label is unknown.
     */
    public Map<String, Double> getAllDestinationsForOrigin(final String origin)
    {
        Integer ri = this.rowIndexByLabel.get(origin);
        if (ri == null)
        {
            throw new NoSuchElementException("Unknown origin: " + origin);
        }
        double[] row = this.values[ri];
        // Preserve label order with LinkedHashMap and pre-size for performance
        Map<String, Double> result = new LinkedHashMap<>(this.colLabels.size() * 2);
        for (int c = 0; c < this.colLabels.size(); c++)
        {
            result.put(this.colLabels.get(c), row[c]);
        }
        return result;
    }

    /**
     * Returns a map of all origin volumes for the given destination. Map keys are origin labels; values are volumes. The
     * iteration order matches the original row order.
     * @throws NoSuchElementException if the destination label is unknown.
     */
    public Map<String, Double> getAllOriginsForDestination(final String destination)
    {
        Integer ci = this.colIndexByLabel.get(destination);
        if (ci == null)
        {
            throw new NoSuchElementException("Unknown destination: " + destination);
        }
        Map<String, Double> result = new LinkedHashMap<>(this.rowLabels.size() * 2);
        for (int r = 0; r < this.rowLabels.size(); r++)
        {
            result.put(this.rowLabels.get(r), this.values[r][ci]);
        }
        return result;
    }

    public boolean containsOrigin(final String origin)
    {
        return this.rowIndexByLabel.containsKey(origin);
    }

    public boolean containsDestination(final String dest)
    {
        return this.colIndexByLabel.containsKey(dest);
    }

    public List<String> getRowLabels()
    {
        return this.rowLabels;
    }

    public List<String> getColLabels()
    {
        return this.colLabels;
    }

    public double[][] getValues()
    {
        return this.values;
    }

}
