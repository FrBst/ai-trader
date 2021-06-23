package rl4j;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscrete;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactorySeparateStdDense;
import org.deeplearning4j.rl4j.network.ac.ActorCriticSeparate;
import org.deeplearning4j.rl4j.network.ac.IActorCritic;
import org.deeplearning4j.rl4j.policy.ACPolicy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.Random;

public class TrainA3C implements Runnable {
    public static void main(String[] args) {

        ScoreIterationListener[] listeners = new ScoreIterationListener[] {new ScoreIterationListener(50)};

        A3CDiscrete.A3CConfiguration A3C =
                A3CDiscrete.A3CConfiguration.builder()
                        .seed(5)
                        .maxEpochStep(365 * 11)
                        .maxStep(50000)
                        .numThread(16)
                        .nstep(10)
                        .updateStart(0)
                        .rewardFactor(0.1)
                        .gamma(0.995)
                        .errorClamp(1.0)
                        .build();

        ActorCriticFactorySeparateStdDense.Configuration configuration = ActorCriticFactorySeparateStdDense.Configuration.builder()
                .updater(new Adam(0.01))
                .useLSTM(true)
                .l2(0.001)
                .numHiddenNodes(32)
                .numLayer(32)
                .build();

        MDP<SimpleBroker, Integer, DiscreteSpace> mdp = new BrokerMdp(1000, new Random(121), 0);

        A3CDiscreteDense<SimpleBroker> a3c = new A3CDiscreteDense<>(mdp, configuration, A3C);

//        try {
//            a3c = new A3CDiscreteDense<SimpleBroker>(mdp,
//                    new ActorCriticSeparate(ModelSerializer.restoreMultiLayerNetwork("value-balanced-v1.bin"),
//                            ModelSerializer.restoreMultiLayerNetwork("policy-balanced-v1.bin"))
//                    , A3C);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        //start the training
        a3c.train();
        System.out.println("End of training");

        //useless on toy but good practice!
        mdp.close();
        try {
            a3c.getPolicy().save("value-balanced-v1.bin", "policy-balanced-v1.bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        main(new String[] { } );
    }
}
