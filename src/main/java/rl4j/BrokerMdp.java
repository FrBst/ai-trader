package rl4j;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import java.util.Random;

public class BrokerMdp implements MDP<SimpleBroker, Integer, DiscreteSpace> {
    private final double inflationRate = 0.04;
    // Assume that the stock falls in price after being delisted.
    private final double delistLoss = 0.1;
    // Used to make loss of money more grave.
    private final double lossWeight = 1.1;
    private final int startingCash;
    private final Random r;
    private int step = 0;

    private SimpleBroker broker;
    private ArrayObservationSpace<SimpleBroker> observationSpace;
    private DiscreteSpace actionSpace = new DiscreteSpace(25);

    public BrokerMdp(int startingCash, Random r) {
        this.startingCash = startingCash;
        this.r = r;

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
        broker = new SimpleBroker(startingCash, r);
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

        double reward = -inflationRate * newNetPrice / 365 / startingCash ;
        double balance = newNetPrice - oldNetPrice;
        if (balance > 0) {
            reward += balance / startingCash;
        } else {
            reward += balance / startingCash * lossWeight;
        }

        // todo: separate events for delisted and end of period.
        if(broker.isEnd()){
            //reward -= (broker.getPortfolioValue() * (1 + delistLoss));
            System.out.println((broker.netValue() / startingCash - 1) * 365 / step * 100 + "%/yr (" + broker.getFeedInfo() + ")");
        }

//        reward *= 100;
        return new StepReply<>(broker, reward, isDone(), null);
    }

    public void doAction(int action){
        int t = action - 12;
        if (t > 0) {
            broker.buy((int)Math.pow(2, t));
        } else if (t < 0) {
            broker.sell((int)Math.pow(2, -t));
        }
        broker.step();
    }

    @Override
    public boolean isDone() {
        return broker.isEnd();
    }

    @Override
    public MDP<SimpleBroker, Integer, DiscreteSpace> newInstance() {
        return new BrokerMdp(startingCash, r);
    }
}
