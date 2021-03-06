/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */

package rl4j;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;

import org.deeplearning4j.rl4j.network.ac.ActorCriticFactorySeparate;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactorySeparateStdDense;
import org.deeplearning4j.rl4j.network.ac.ActorCriticLoss;
import org.deeplearning4j.rl4j.network.ac.ActorCriticSeparate;
import org.deeplearning4j.rl4j.network.configuration.ActorCriticDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.network.configuration.ActorCriticNetworkConfiguration;
import org.deeplearning4j.rl4j.util.Constants;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Arrays;

public class FactoryTest implements ActorCriticFactorySeparate {

    ActorCriticDenseNetworkConfiguration conf;

    public ActorCriticSeparate buildActorCritic(int[] numInputs, int numOutputs) {
        int nIn = 1;
        for (int i : numInputs) {
            nIn *= i;
        }
        NeuralNetConfiguration.ListBuilder confB = new NeuralNetConfiguration.Builder().seed(Constants.NEURAL_NET_SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(conf.getUpdater() != null ? conf.getUpdater() : new Adam())
                .weightInit(WeightInit.XAVIER)
                .l2(conf.getL2())
                .list().layer(0, new DenseLayer.Builder().nIn(nIn).nOut(conf.getNumHiddenNodes())
                        .activation(Activation.RELU).build());


        for (int i = 1; i < conf.getNumLayers(); i++) {
            confB.layer(i, new DenseLayer.Builder().nIn(conf.getNumHiddenNodes()).nOut(conf.getNumHiddenNodes())
                    .activation(Activation.RELU).build());
        }

        if (conf.isUseLSTM()) {
            confB.layer(conf.getNumLayers(), new LSTM.Builder().nIn(conf.getNumHiddenNodes())
                    .nOut(conf.getNumHiddenNodes()).activation(Activation.TANH).build());

            confB.layer(conf.getNumLayers() + 1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
                    .nIn(conf.getNumHiddenNodes()).nOut(1).build());
        } else {
            confB.layer(conf.getNumLayers(), new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
                    .nIn(conf.getNumHiddenNodes()).nOut(1).build());
        }

        confB.setInputType(conf.isUseLSTM() ? InputType.recurrent(nIn) : InputType.feedForward(nIn));
        MultiLayerConfiguration mlnconf2 = confB.build();
        MultiLayerNetwork model = new MultiLayerNetwork(mlnconf2);
        model.init();
        if (conf.getListeners() != null) {
            model.setListeners(conf.getListeners());
        } else {
            model.setListeners(new ScoreIterationListener(Constants.NEURAL_NET_ITERATION_LISTENER));
        }

        NeuralNetConfiguration.ListBuilder confB2 = new NeuralNetConfiguration.Builder().seed(Constants.NEURAL_NET_SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(conf.getUpdater() != null ? conf.getUpdater() : new Adam())
                .weightInit(WeightInit.XAVIER)
                //.regularization(true)
                //.l2(conf.getL2())
                .list().layer(0, new DenseLayer.Builder().nIn(nIn).nOut(conf.getNumHiddenNodes())
                        .activation(Activation.RELU).build());


        for (int i = 1; i < conf.getNumLayers(); i++) {
            confB2.layer(i, new DenseLayer.Builder().nIn(conf.getNumHiddenNodes()).nOut(conf.getNumHiddenNodes())
                    .activation(Activation.RELU).build());
        }

        if (conf.isUseLSTM()) {
            confB2.layer(conf.getNumLayers(), new LSTM.Builder().nIn(conf.getNumHiddenNodes())
                    .nOut(conf.getNumHiddenNodes()).activation(Activation.TANH).build());

            confB2.layer(conf.getNumLayers() + 1, new RnnOutputLayer.Builder(new ActorCriticLoss())
                    .activation(Activation.SOFTMAX).nIn(conf.getNumHiddenNodes()).nOut(numOutputs).build());
        } else {
            confB2.layer(conf.getNumLayers(), new OutputLayer.Builder(new ActorCriticLoss())
                    .activation(Activation.SOFTMAX).nIn(conf.getNumHiddenNodes()).nOut(numOutputs).build());
        }

        confB2.setInputType(conf.isUseLSTM() ? InputType.recurrent(nIn) : InputType.feedForward(nIn));
        MultiLayerConfiguration mlnconf = confB2.build();
        MultiLayerNetwork model2 = new MultiLayerNetwork(mlnconf);
        model2.init();
        if (conf.getListeners() != null) {
            model2.setListeners(conf.getListeners());
        } else {
            model2.setListeners(new ScoreIterationListener(Constants.NEURAL_NET_ITERATION_LISTENER));
        }


        return new ActorCriticSeparate(model, model2);
    }

    @Deprecated
    public static class Configuration {

        int numLayer;
        int numHiddenNodes;
        double l2;
        IUpdater updater;
        TrainingListener[] listeners;
        boolean useLSTM;

        public ActorCriticDenseNetworkConfiguration toNetworkConfiguration() {
            ActorCriticDenseNetworkConfiguration.ActorCriticDenseNetworkConfigurationBuilder builder = ActorCriticDenseNetworkConfiguration.builder()
                    .numHiddenNodes(numHiddenNodes)
                    .numLayers(numLayer)
                    .l2(l2)
                    .updater(updater)
                    .useLSTM(useLSTM);

            if (listeners != null) {
                builder.listeners(Arrays.asList(listeners));
            }

            return builder.build();

        }

        public Configuration(int numLayer, int numHiddenNodes, double l2, IUpdater updater, TrainingListener[] listeners, boolean useLSTM) {
            this.numLayer = numLayer;
            this.numHiddenNodes = numHiddenNodes;
            this.l2 = l2;
            this.updater = updater;
            this.listeners = listeners;
            this.useLSTM = useLSTM;
        }

        public static FactoryTest.Configuration.ConfigurationBuilder builder() {
            return new FactoryTest.Configuration.ConfigurationBuilder();
        }

        public static class ConfigurationBuilder {
            private int numLayer;
            private int numHiddenNodes;
            private double l2;
            private IUpdater updater;
            private TrainingListener[] listeners;
            private boolean useLSTM;

            ConfigurationBuilder() {
            }

            public FactoryTest.Configuration.ConfigurationBuilder numLayer(int numLayer) {
                this.numLayer = numLayer;
                return this;
            }

            public FactoryTest.Configuration.ConfigurationBuilder numHiddenNodes(int numHiddenNodes) {
                this.numHiddenNodes = numHiddenNodes;
                return this;
            }

            public FactoryTest.Configuration.ConfigurationBuilder l2(double l2) {
                this.l2 = l2;
                return this;
            }

            public FactoryTest.Configuration.ConfigurationBuilder updater(IUpdater updater) {
                this.updater = updater;
                return this;
            }

            public FactoryTest.Configuration.ConfigurationBuilder listeners(TrainingListener[] listeners) {
                this.listeners = listeners;
                return this;
            }

            public FactoryTest.Configuration.ConfigurationBuilder useLSTM(boolean useLSTM) {
                this.useLSTM = useLSTM;
                return this;
            }

            public FactoryTest.Configuration build() {
                return new FactoryTest.Configuration(this.numLayer, this.numHiddenNodes, this.l2, this.updater, this.listeners, this.useLSTM);
            }

            public String toString() {
                return "FactoryTest.Configuration.ConfigurationBuilder(numLayer=" + this.numLayer + ", numHiddenNodes=" + this.numHiddenNodes + ", l2=" + this.l2 + ", updater=" + this.updater + ", listeners=" + Arrays.deepToString(this.listeners) + ", useLSTM=" + this.useLSTM + ")";
            }
        }
    }


}
