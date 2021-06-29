package model;

import application.Utils;
import java.io.*;
import java.util.*;

public class DataFeed {
    private final ArrayList<StockTimepoint> data;
    private int idx;
    private double randomIntradayPrice;
    private String info;

    public DataFeed(Random r, int level) {

        ArrayList<StockTimepoint> fromFile;
        // Random file from the training set or the test set.
        // todo: really? KYS, but first fix this mess.
        List<File> filesList;
        if (level >= 0) {
            filesList = Utils.getTrainFiles(level);
        } else {
            filesList = Utils.getTestFiles();
        }

        do {
            // Starting date (within 1999-01-01 to 2019-12-12).
            int year = r.nextInt(21) + 1999;
            int month = r.nextInt(12) + 1;
            int day = r.nextInt(31) + 1;
            String startDate = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

            // End day is 3 to 10 years after start day.
            month = r.nextInt(12) + 1;
            day = r.nextInt(31) + 1;
            String endDate = (year + r.nextInt(8) + 3) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

            File file = filesList.get(r.nextInt(filesList.size()));

            fromFile = getData(file, startDate, endDate);
            info = file.getName() + " (" + startDate + "-" + endDate + ")";
        } while (fromFile == null || fromFile.size() < 200);
        data = fromFile;
    }

//    /**
//     * Loads sorted time series from the specified file from startDate, inclusive to endDate, exclusive.
//     * Guaranteed to start with startDate. Can end before endDate, if it is the last day available in dataset.
//     * @param file
//     * @param startDate
//     * @param endDate
//     */
    private static ArrayList<StockTimepoint> getData(File file, String startDate, String endDate) {
        ArrayList<StockTimepoint> data = new ArrayList<>();

        // Load time series from the file. IT'S REVERSED!!!!!!
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            line = br.readLine();
            String[] tokens = line.split(",");
            if (tokens[0].compareTo(startDate) < 0) {
                return null;
            }
            while (true) {
                if (tokens[0].compareTo(startDate) < 0) {
                    break;
                }
                if (tokens[0].compareTo(endDate) <= 0) {
                    data.add(new StockTimepoint(tokens));
                }
                line = br.readLine();
                if (line == null) {
                    break;
                }
                tokens = line.split(",");
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // They are reversed in files from AlphaVantage, so we need this.
        Collections.reverse(data);
        if (data.size() == 0) {
            return null;
        }
        return data;
    }

    public DataFeed(DataFeed other) {
        this.data = new ArrayList<>(other.data);
        this.idx = other.idx;
        this.randomIntradayPrice = other.randomIntradayPrice;
    }

    // todo: try something instead of an array?
    public StockTimepoint[] getNext() {
        if (idx >= data.size()-1) {
            return null;
        }
        StockTimepoint st = data.get(++idx);
        randomIntradayPrice = st.getLow() + Math.random() * (st.getHigh() - st.getLow());
        return new StockTimepoint[] { st };
    }

    public double pollPrice() {
        return randomIntradayPrice;
    }

    public String getInfo() { return info; }

    public int size() { return data.size(); }
}
