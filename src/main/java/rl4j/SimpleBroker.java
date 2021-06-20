package rl4j;

import model.DataFeeder;
import org.apache.commons.lang3.NotImplementedException;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.Random;


// todo: cloning tests for everything.
public class SimpleBroker implements Encodable {
    //double taxPercent = 13;
    private double commissionPercent = 0.3;
    private double cash;
    private int stocksCount;
    private double avgPrice;

    DataFeeder feeder;

    boolean seriesEnd;

    public SimpleBroker(double startingCash, Random r) {
        cash = startingCash;
        stocksCount = 0;
        avgPrice = 0.0;

        feeder = new DataFeeder(r);
    }

    private SimpleBroker(double commissionPercent, double cash, int stocksCount, double avgPrice, DataFeeder feeder) {
        this.commissionPercent = commissionPercent;
        this.cash = cash;
        this.stocksCount = stocksCount;
        this.avgPrice = avgPrice;
        this.feeder = feeder;
    }

    @Override
    public Encodable dup() {
        return new SimpleBroker(commissionPercent, cash, stocksCount, avgPrice, new DataFeeder(feeder));
    }

    @Override
    public boolean isSkipped() {
        return false;
    }

    @Override
    @Deprecated
    public double[] toArray() {
        return new double[] {0, 0, 0, 0, 0};
    }

    @Override
    public String toString() {
        return "SimpleBroker{" +
                "commissionPercent=" + commissionPercent +
                "cash=" + cash +
                ", stocksCount=" + stocksCount +
                ", avgPrice=" + avgPrice +
                ", feeder=" + feeder +
                ", seriesEnd=" + seriesEnd +
                '}';
    }

    public static int observationSize() { return 5; }

    // Not very nice, but should be correct.
//    private DataFeeder getRandomFeed(Random r) throws IOException {
//        Random r1 = new Random(r.nextLong());
//        int year = r.nextInt(23) + 1999;
//        int month = r.nextInt(12) + 1;
//        int day = r.nextInt(31) + 1;
//
//        String startDate = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
//        String endDate = String.valueOf(year + r.nextInt(7) + 3) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
//        List<String> filesList = Utils.getTestFileNames();
//        File file = new File(filesList.get(r.nextInt(filesList.size())));
//
//        return new DataFeeder(file, startDate, endDate);
//    }

    @Override
    public INDArray getData() {
        return Nd4j.zeros(1,observationSize(),1);
        //return Nd4j.create(new double[][][]{{{cash}, {stocksCount}, {avgPrice}, {currentPrice}, {currentGlobalMetric}}});
        // Observation size must be changed if the number of variables here changes.
        //return Nd4j.createFromArray(new double[][][] {{{cash, stocksCount, avgPrice, currentPrice, currentGlobalMetric}}});
    }

    // todo: split/dividend
    public void step() {
        if (seriesEnd) {
            throw new IllegalStateException("End of simulation");
        }
        Double[] res = feeder.getNext();
        if (res == null) {
            seriesEnd = true;
            return;
        }
    }

    public void buy(int count) {
        double total = feeder.pollPrice() * (1 + 0.01 * commissionPercent) * count;
        if (cash >= total) {
            cash -= total;
            avgPrice = avgPrice * stocksCount + total;
            stocksCount += count;
            avgPrice /= stocksCount;
        }
    }

    public void sell(int count) {
        if (stocksCount >= count) {
            double total = feeder.pollPrice() * (1 - 0.01 * commissionPercent) * count;
            cash += total;
            stocksCount -= count;
        }
    }

    public double netValue() {
        return cash + stocksCount * avgPrice;
    }

    public boolean isEnd() { return seriesEnd; }

    public double stocksValue() { return avgPrice * stocksCount; }
}
