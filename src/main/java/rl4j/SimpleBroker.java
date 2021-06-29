package rl4j;

import model.DataFeed;
import model.StockTimepoint;
import org.apache.commons.lang3.NotImplementedException;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Random;


// todo: cloning tests for everything.
public class SimpleBroker implements Encodable {
    //double taxPercent = 13;
    private double commissionPercent = 0.3;
    private double cash;
    private int stocksCount;
    private double avgBuyPrice;
    private double currentPrice;
    private boolean seriesEnd;

    private final DataFeed feed;


    public SimpleBroker(double startingCash, Random r, int level) {
        cash = startingCash;
        feed = new DataFeed(r, level);
        step();
    }

    public SimpleBroker(double commissionPercent, double cash, int stocksCount, double avgBuyPrice, double currentPrice, boolean seriesEnd, DataFeed feed) {
        this.commissionPercent = commissionPercent;
        this.cash = cash;
        this.stocksCount = stocksCount;
        this.avgBuyPrice = avgBuyPrice;
        this.currentPrice = currentPrice;
        this.seriesEnd = seriesEnd;
        this.feed = feed;
    }

    @Override
    public Encodable dup() {
        return new SimpleBroker(commissionPercent, cash, stocksCount, avgBuyPrice, currentPrice, seriesEnd, new DataFeed(feed));
    }

    @Override
    public boolean isSkipped() {
        return false;
    }

    @Override
    @Deprecated
    public double[] toArray() {
        throw new NotImplementedException("Method SimpleBroker.toArray()");
    }

    @Override
    public String toString() {
        return "SimpleBroker{" +
                "commissionPercent=" + commissionPercent +
                "cash=" + cash +
                ", stocksCount=" + stocksCount +
                ", avgBuyPrice=" + avgBuyPrice +
                ", currentPrice=" + currentPrice +
                ", seriesEnd=" + seriesEnd +
                ", feed=" + feed +
                '}';
    }

    public static int observationSize() { return 4; }

    @Override
    public INDArray getData() {
        // Observation size must be changed if the number of variables here changes.
        return Nd4j.create(new double[][][]{{{cash / 4000}, {getPortfolioValue() / 4000}, {avgBuyPrice / 100}, {currentPrice / 100}}});
    }

    public void step() {
        if (seriesEnd) {
            throw new IllegalStateException("No more datapoints");
        }
        StockTimepoint[] res = feed.getNext();
        if (res == null) {
            seriesEnd = true;
        }
        else {
            currentPrice = res[0].getOpen();
            if (res[0].getSplit_coefficient() != 1.0 && stocksCount != 0) {
                int oldCount = stocksCount;
                stocksCount = (int) Math.round(stocksCount * res[0].getSplit_coefficient());
                avgBuyPrice = avgBuyPrice * oldCount / stocksCount;
            }
            if (res[0].getDividend_amount() != 0.0 && stocksCount != 0) {
                cash += stocksCount * res[0].getDividend_amount();
            }
        }
        if (cash < 0 || stocksCount < 0 || netValue() > 5000 || avgBuyPrice < 0) {
            String wtf = "dsf";
        }
    }

    /**
     * Place a buy order. Buys up to {@code count}, depending on {@code cash} available.
     * Do not use with {@code count == 0}.
     * @param count
     * @throws IllegalArgumentException When called with {@code count == 0}.
     */
    public int buy(int count) {
        if (count == 0) { throw new IllegalArgumentException("Attempt to buy 0 stocks, which is not nice"); }
        int canBuy;
        if (feed.pollPrice() == 0) {
            return 0;
        } else {
            canBuy = (int) Math.floor(cash / (feed.pollPrice() * (1 + 0.01 * commissionPercent)));
            if (canBuy == 0) { return -count; };
        }
        int numberToBuy = Math.min(count, canBuy);
        double totalPrice = numberToBuy * feed.pollPrice();
//        System.out.println(totalPrice + " " + cash + " " + getPortfolioValue());
        cash -= totalPrice * (1 + 0.01 * commissionPercent);
        if (cash < 0 || stocksCount < 0 || netValue() > 5000 || avgBuyPrice < 0) {
            String wtf = "wtf";
        }
        avgBuyPrice = avgBuyPrice * stocksCount + totalPrice;
        stocksCount += numberToBuy;
        avgBuyPrice /= stocksCount;
        return (numberToBuy < count) ? numberToBuy - count : 0;
    }

    /**
     * Place a sell order. Sells up to {@code count}, depending on stocks number in portfolio.
     * Do not use with {@code count == 0}.
     * @param count
     * @throws IllegalArgumentException When called with {@code count == 0}.
     */
    public int sell(int count) {
        if (count == 0) { throw new IllegalArgumentException("Attempt to sell 0 stocks, which is not nice"); }
        if (stocksCount == 0) {
            return -count;
        }
        if (feed.pollPrice() == 0) {
            return 0;
        }
        int numberToSell = Math.min(count, stocksCount);
        double total = feed.pollPrice() * numberToSell;
//        System.out.println("-" + total + " " + cash + " " + getPortfolioValue());
        cash += total * (1 - 0.01 * commissionPercent);
        stocksCount -= numberToSell;
        if (stocksCount < 0 || cash < 0 || netValue() > 5000 || avgBuyPrice < 0) {
            String wtf = "wtf";
        }
        return numberToSell < count ? numberToSell - count : 0;
    }

    public double netValue() {
        return cash + getPortfolioValue();
    }

    public boolean isEnd() { return seriesEnd; }

    public double getPortfolioValue() { return currentPrice * stocksCount; }

    public String getFeedInfo() { return feed.getInfo(); }

    public double getCash() { return cash; }

    public int getStocksCount() { return stocksCount; }

    public double getCurrentPrice() { return currentPrice; }

    public int getDataLength() { return feed.size(); }
}
