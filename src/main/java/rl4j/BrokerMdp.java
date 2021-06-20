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
    private final double lossWeight = 2;
    private final int startingCash;
    private final Random r;

    private SimpleBroker broker;
    private ArrayObservationSpace<SimpleBroker> observationSpace;
    private DiscreteSpace actionSpace = new DiscreteSpace(15);

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
        double newNetPrice = broker.netValue();

        double reward = -inflationRate * broker.netValue() / 365 ;
        double balance = 1 - (newNetPrice / oldNetPrice);
        if (balance > 0) {
            reward += balance;
        } else {
            reward += balance * lossWeight;
        }

        // todo: separate events for delisted and end of period.
        if(broker.isEnd()){
            reward -= (broker.stocksValue() * delistLoss);
        }

        return new StepReply<>(broker, reward, isDone(), null);
    }

    public void doAction(int action){
        int t = action - 7;
        if (t > 0) {
            broker.buy((int)Math.pow(2, t));
        } else if (action < 0) {
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
