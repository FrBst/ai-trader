package rl4j;

import application.Configuration;
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
        A3CDiscrete.A3CConfiguration A3C;
        ActorCriticFactorySeparateStdDense.Configuration configuration;
        MDP<SimpleBroker, Integer, DiscreteSpace> mdp;
        A3CDiscreteDense<SimpleBroker> a3c;

        A3C = A3CDiscrete.A3CConfiguration.builder()
                .seed(1)
                .maxEpochStep(365 * 11)
                .maxStep(100000)
                .numThread(16)
                .nstep(10)
                .updateStart(0)
                .rewardFactor(0.1)
                .gamma(0.995)
                .errorClamp(1.0)
                .build();
        configuration = ActorCriticFactorySeparateStdDense.Configuration.builder()
                .updater(new Adam(0.007))
                .useLSTM(true)
                .l2(0)
                .numHiddenNodes(32)
                .numLayer(32)
                .build();
        mdp = new BrokerMdp(1000, new Random(66), 1);

       // a3c = new A3CDiscreteDense<>(mdp, configuration, A3C);

                    try {
                a3c = new A3CDiscreteDense<SimpleBroker>(mdp,
                        new ActorCriticSeparate(ModelSerializer.restoreMultiLayerNetwork(Configuration.getConfig("network-folder") + "value-zero-bias-lv1.bin"),
                                ModelSerializer.restoreMultiLayerNetwork(Configuration.getConfig("network-folder") + "policy-zero-bias-lv1.bin"))
                        , A3C);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        a3c.train();

        mdp.close();
        try {
            a3c.getPolicy().save(Configuration.getConfig("network-folder") + "value-zero-bias-lv1.bin",
                    Configuration.getConfig("network-folder") + "policy-zero-bias-lv1.bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        for (int i = 4; i < 10; i++) {
//            A3C = A3CDiscrete.A3CConfiguration.builder()
//                            .seed(5)
//                            .maxEpochStep(365 * 11)
//                            .maxStep(50000)
//                            .numThread(16)
//                            .nstep(10)
//                            .updateStart(0)
//                            .rewardFactor(0.1)
//                            .gamma(0.995)
//                            .errorClamp(1.0)
//                            .build();
//            mdp = new BrokerMdp(1000, new Random(121), i);
//
//            try {
//                a3c = new A3CDiscreteDense<SimpleBroker>(mdp,
//                        new ActorCriticSeparate(ModelSerializer.restoreMultiLayerNetwork(Configuration.getConfig("network-folder") + "value-test-lv" + (i-1) + ".bin"),
//                                ModelSerializer.restoreMultiLayerNetwork(Configuration.getConfig("network-folder") + "policy-test-lv" + (i-1) + ".bin"))
//                        , A3C);
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            a3c.train();
//
//            mdp.close();
//            try {
//                a3c.getPolicy().save(Configuration.getConfig("network-folder") + "value-test-lv" + i + ".bin",
//                        Configuration.getConfig("network-folder") + "policy-test-lv" + i + ".bin");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    public void run() {
        main(new String[] { } );
    }
}
