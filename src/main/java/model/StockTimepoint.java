package model;

public class StockTimepoint {
    private String timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double adjusted_close;
    private int volume;
    private double dividend_amount;
    private double split_coefficient;

    public StockTimepoint(String[] tokens) {
        timestamp = tokens[0];
        open = Double.parseDouble(tokens[1]);
        high = Double.parseDouble(tokens[2]);
        low = Double.parseDouble(tokens[3]);
        close = Double.parseDouble(tokens[4]);
        adjusted_close = Double.parseDouble(tokens[5]);
        volume = Integer.parseInt(tokens[6]);
        dividend_amount = Double.parseDouble(tokens[7]);
        split_coefficient = Double.parseDouble(tokens[8]);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getAdjusted_close() {
        return adjusted_close;
    }

    public int getVolume() {
        return volume;
    }

    public double getDividend_amount() {
        return dividend_amount;
    }

    public double getSplit_coefficient() {
        return split_coefficient;
    }
}
