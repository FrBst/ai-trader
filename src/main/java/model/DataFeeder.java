package model;

import application.Configuration;
import application.Utils;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;

public class DataFeeder {
    private final List<StockTimepoint> data;
    private final List<Double> globalMetric;
    private ListIterator<StockTimepoint> dataIter;
    private ListIterator<Double> globalMetricIter;

    double randomPrice;

    // Not very nice, but should be correct.
    public DataFeeder(Random r) {
        data = new LinkedList<>();
        globalMetric = new LinkedList<>();

        while (data.isEmpty() || globalMetric.isEmpty()) {
            int year = r.nextInt(23) + 1999;
            int month = r.nextInt(12) + 1;
            int day = r.nextInt(31) + 1;
            String startDate = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

            // About 3 to 10 years to trade.
            month = r.nextInt(12) + 1;
            day = r.nextInt(31) + 1;
            String endDate = (year + r.nextInt(7) + 3) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

            List<File> filesList = Utils.getTestFiles();
            File file = filesList.get(r.nextInt(filesList.size()));

            try {
                load(file, startDate, endDate);
            } catch (IOException e) {
                throw new RuntimeException("Error in load method, DataFeeder class. Data length " + data.size() + ", Global metric length " + globalMetric.size());
            }
        }
    }

    private void load(File dataFile, String start, String end) throws IOException {
        // Load time series from the file.
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens[0].compareTo(start) >= 0) {
                    data.add(new StockTimepoint(tokens));
                }
                if (tokens[0].compareTo(end) >= 0) {
                    break;
                }
            }
            // They are reversed in files from AlphaVantage, so we need this.
            Collections.reverse(data);
            dataIter = data.listIterator();
        }

        // todo: Move this to Utils for performance.
        // Load global metric.
        try (BufferedReader br = new BufferedReader(new FileReader(Configuration.getConfig("data-folder") + "global-metric.csv"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens[0].compareTo(start) >= 0) {
                    globalMetric.add(Double.parseDouble(tokens[1]));
                }
                if (tokens[0].compareTo(end) >= 0) {
                    break;
                }
            }
            globalMetricIter = globalMetric.listIterator();
        }
    }

    public DataFeeder(DataFeeder other) {
        this.data = new LinkedList<>(other.data);
        this.globalMetric = new LinkedList<>(other.globalMetric);
        this.dataIter = this.data.listIterator(other.dataIter.nextIndex());
        this.globalMetricIter = this.globalMetric.listIterator(other.globalMetricIter.nextIndex());
        this.randomPrice = other.randomPrice;
    }

    // todo: try something instead of an array?
    public Double[] getNext() {
        if (dataIter.hasNext()) {
            StockTimepoint st = dataIter.next();
            randomPrice = st.getLow() + Math.random() * st.getHigh();
            return new Double[] {st.getOpen(), globalMetricIter.next()};
        }
        return null;
    }

    public double pollPrice() {
        return randomPrice;
    }
}
