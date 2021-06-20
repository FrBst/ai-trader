package rl4j;

import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscrete;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactorySeparateStdDense;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;

public class MyA3C extends A3CDiscreteDense<SimpleBroker> {
    @Deprecated
    public MyA3C(MDP<SimpleBroker, Integer, DiscreteSpace> mdp,
                            ActorCriticFactorySeparateStdDense.Configuration netConf, A3CConfiguration conf) {
        super(mdp, new ActorCriticFactorySeparateStdDense(netConf.toNetworkConfiguration()), conf);
    }
}
