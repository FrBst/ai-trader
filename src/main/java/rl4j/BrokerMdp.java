package rl4j;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import java.util.Random;

public class BrokerMdp implements MDP<SimpleBroker, Integer, DiscreteSpace> {
    private final double inflationRate = 0.06;
    // Assume that the stock falls in price after being delisted.
    private final double delistLoss = 0.1;
    // Used to make loss of money more grave.
    private final double lossWeight = 1.0;
    private final double startingCash;
    private final Random r;
    private int step = 0;
    private double cumulativeReward = 0.0;
    private double buysell = 0;
    private int level;

    private SimpleBroker broker;
    private ArrayObservationSpace<SimpleBroker> observationSpace;
    private DiscreteSpace actionSpace = new DiscreteSpace(25);

    public BrokerMdp(double startingCash, Random r, int level) {
        this.startingCash = startingCash;
        this.r = r;
        this.level = level;

        this.observationSpace = new ArrayObservationSpace<>(new int[]{SimpleBroker.observationSize()});
    }

    @Override
    public ObservationSpace<SimpleBroker> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return actionSpace;
    }

    @Override
    public SimpleBroker reset() {
        broker = new SimpleBroker(startingCash, r, level);
        step = 0;
        cumulativeReward = 0.0;
        buysell = 0;
        return broker;
    }

    @Override
    public void close() {

    }

    @Override
    public StepReply<SimpleBroker> step(Integer action) {
        double oldNetPrice = broker.netValue();
        doAction(action);
        step++;
        double newNetPrice = broker.netValue();

        double reward = -inflationRate * broker.getCash() / 365 / startingCash ;
        if (action == 12) {
            reward += 0.07;
        }
        double balance = newNetPrice - oldNetPrice;
        if (balance > 0) {
            reward += balance / startingCash;
        } else {
            reward += balance / startingCash * lossWeight;
        }

        cumulativeReward += reward;
        // todo: separate events for delisted and end of period.
        if(broker.isEnd()){
            //reward -= (broker.getPortfolioValue() * (1 + delistLoss));
            System.out.println((buysell / step) + " " + String.format("%.2f", broker.netValue()) + " left, grow rate " +
                    String.format("%+.2f", (broker.netValue() - startingCash) / startingCash * 365.0 / step * 100.0) +
                    "%/yr (" + broker.getFeedInfo() + ")");
        }

        return new StepReply<>(broker, reward / 10, isDone(), null);
    }

    public int doAction(int action){
        int deficit = 0;
        int t = action - 12;
        buysell += t;
        if (t > 0) {
            deficit = broker.buy((int)Math.pow(2, t));
        } else if (t < 0) {
            deficit = broker.sell((int)Math.pow(2, -t));
        }
        broker.step();
        return deficit;
    }

    @Override
    public boolean isDone() {
        return broker.isEnd();
    }

    @Override
    public MDP<SimpleBroker, Integer, DiscreteSpace> newInstance() {
        return new BrokerMdp(startingCash, r, level);
    }
}
