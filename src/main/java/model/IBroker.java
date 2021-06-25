package model;

import org.deeplearning4j.rl4j.space.Encodable;

public interface IBroker extends Encodable {
    public static int observationSize() { return 4; }

    public void step();

    public int buy(int count);

    public int sell(int count);

    public double netValue();

    public boolean isEnd();

    public double getPortfolioValue();

    public String getFeedInfo();

    public double getCash();

    public int getStocksCount();

    public double getCurrentPrice();
}
