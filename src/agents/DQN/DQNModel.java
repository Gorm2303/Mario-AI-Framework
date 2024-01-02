package agents.DQN;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class DQNModel {
    private MultiLayerNetwork model;

    public DQNModel(int inputSize, int outputSize) {
        // Define the neural network configuration
        NeuralNetConfiguration.ListBuilder config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(64).weightInit(WeightInit.XAVIER).activation(Activation.RELU).build())
                .layer(1, new DenseLayer.Builder().nIn(64).nOut(64).weightInit(WeightInit.XAVIER).activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY).nIn(64).nOut(outputSize).build());

        // Create the model
        this.model = new MultiLayerNetwork(config.build());
        this.model.init();
        this.model.setListeners(new ScoreIterationListener(100));
    }

    public double[] predict(double[] state) {
        INDArray input = Nd4j.create(state, new int[]{1, state.length});
        INDArray output = model.output(input);
        return output.toDoubleVector();
    }

    public void train(DataSetIterator iterator) {
        model.fit(iterator);
    }

    public void saveModel(String filePath) {
        File file = new File(filePath);
        try {
            ModelSerializer.writeModel(model, file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String filePath) {
        File file = new File(filePath);
        try {
            this.model = ModelSerializer.restoreMultiLayerNetwork(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}