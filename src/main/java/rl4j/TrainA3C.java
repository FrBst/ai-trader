package rl4j;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscrete;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscreteDense;
import org.deeplearning4j.rl4j.learning.configuration.A3CLearningConfiguration;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactoryCompGraphStdDense;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactorySeparateStdDense;
import org.deeplearning4j.rl4j.network.ac.IActorCritic;
import org.deeplearning4j.rl4j.network.configuration.ActorCriticDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.Random;

public class TrainA3C implements Runnable {
    public static void main(String[] args) {
        int maxTries = 100;

        A3CDiscrete.A3CConfiguration A3C =
                A3CDiscrete.A3CConfiguration.builder()
                        .seed(5)
                        .maxEpochStep(365 * 11)
                        .maxStep(100000)
                        .numThread(1)
                        .nstep(10)
                        .updateStart(0)
                        .rewardFactor(0.1)
                        .gamma(0.99)
                        .errorClamp(1.0)
                        .build();

        ActorCriticFactorySeparateStdDense.Configuration configuration = ActorCriticFactorySeparateStdDense.Configuration.builder()
                .updater(new Adam(0.003))
                .useLSTM(true)
                .l2(0)
                .numHiddenNodes(16)
                .numLayer(50)
                .build();

        MDP<SimpleBroker, Integer, DiscreteSpace> mdp = new BrokerMdp(1000, new Random(121));

        A3CDiscreteDense<SimpleBroker> a3c = new A3CDiscreteDense<>(mdp, configuration, A3C);

        //start the training
        a3c.train();

        //useless on toy but good practice!
        mdp.close();
        try {
            a3c.getPolicy().save("snake-player-a3c-value-10.bin", "snake-player-a3c-policy-10.bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        main(new String[] { } );
    }

//    public static MultiLayerNetwork getNet() {
//        int hiddenNodesNum = 10;
//
//        MultiLayerNetwork net = new MultiLayerNetwork(
//                new NeuralNetConfiguration().Builder()
//                        .seed(123)
//                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//                        .learningRate(0.01)
//                        .weightInit(WeightInit.XAVIER)
//                        .updater(Updater.ADAM)
//                        .list()
//                        .layer(0, new DenseLayer.Builder().nIn(SimpleBroker.observationSize()).nOut(hiddenNodesNum)
//                                .activation(Activation.TANH)
//                                .build())
//                        .layer(1, new LSTM.Builder().nOut(hiddenNodesNum).activation(Activation.TANH).build());
//
//                        .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
//                                .activation(Activation.IDENTITY)
//                                .nOut(out).build())
//                        .pretrain(false).backprop(true).build()
//        );
//    }
}
