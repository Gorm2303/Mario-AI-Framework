package agents.DQN;

import engine.helper.GameStatus;
import org.bytedeco.libfreenect._freenect_context;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DQNAgent implements MarioAgent {

    private double epsilon;
    private final DQNModel model;
    private final ReplayBuffer replayBuffer;
    private final Random random;
    private MarioEnvironment marioEnvironment;
    private final double gamma; // discount factor
    private final double minEpsilon;
    private final double epsilonDecayRate;
    private final int batchSize;
    private Experience latestExperience;
    private boolean evaluate;
    private int counter = 0;

    //In the constructor or an initialization method, instantiate these components.
    public DQNAgent(DQNModel model, ReplayBuffer replayBuffer, double epsilon, double gamma, double minEpsilon, double epsilonDecayRate, int batchSize, boolean evaluate) {
        this.model = model;
        this.replayBuffer = replayBuffer;
        this.epsilon = epsilon;
        this.gamma = gamma;
        this.minEpsilon = minEpsilon;
        this.epsilonDecayRate = epsilonDecayRate;
        this.batchSize = batchSize;
        this.random = new Random();
        this.evaluate = evaluate;
    }

    public DQNAgent(DQNAgent agent, double epsilon, double gamma, double minEpsilon, double epsilonDecayRate, boolean evaluate) {
        this(agent.model, agent.replayBuffer, epsilon, gamma, minEpsilon, epsilonDecayRate, agent.batchSize, evaluate);
    }

    //Set up any necessary components before the game starts.
    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        //Initialize the neural network and replay memory.
        marioEnvironment = new MarioEnvironment(model);
    }

    //Use this method to select actions at each game step.
    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        // Convert the current state from MarioForwardModel into a format suitable for the DQN model
        marioEnvironment = new MarioEnvironment(model);
        double[] currentState = marioEnvironment.getGameState();

        // Periodically update the agent's experience
        if (!evaluate) {
            experience();
            if (random.nextDouble() > 0.984) {
                learn();
            }
        }

        return selectAction(currentState);
    }

    //Implement Action Selection Method
    public boolean[] selectAction(double[] state) {
        //Implement the epsilon-greedy strategy: With probability ε, select a random action; otherwise, choose the best action according to the neural network.
        //Epsilon should decay over time to shift from exploration to exploitation.
        boolean[] actions = new boolean[MarioActions.numberOfActions()+1];
        if (random.nextDouble() < epsilon) {
            // Exploration: choose a random action
            for (int i = 0; i < numberOfRandomActions(); i++) {
                int randomActionIndex = random.nextInt(MarioActions.numberOfActions());
                actions[randomActionIndex] = true; // Set the action to true
            }
        } else {
            // Exploitation: choose the best action based on the model's prediction
            double[] qValues = model.predict(state);
            int bestActionIndex = getMaxIndex(qValues);
            actions[bestActionIndex] = true;
        }

        return actions;
    }

    private int numberOfRandomActions() {
        // Implement logic to determine the number of actions to choose randomly
        double rand = random.nextDouble();
        if (rand < 0.33) {
            return 1;
        } else if (rand < (0.33+0.33)) {
            return 2;
        } else {
            return 3; // Remaining
        }
    }

    private int getMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private void experience() {
        Experience experience = new Experience(marioEnvironment.getGameState(),
                selectAction(marioEnvironment.getGameState()),
                0,
                new double[]{0.0},
                marioEnvironment.getGameStatus(),
                marioEnvironment.getCurrentModel().getCompletionPercentage(),
                marioEnvironment.getCurrentModel().getRemainingTime());
        if (latestExperience != null) {
            latestExperience.setReward(calculateReward(latestExperience, experience));
            latestExperience.setNextState(experience.getState());
            storeExperience(latestExperience);
        }
        latestExperience = experience;
    }

    //Implement Learning Method
    public void learn() {
        //This method should handle transitions: store experiences in the replay buffer.
        //Sample a batch of experiences from the buffer.
        //Use these experiences to update the Q-values via the neural network.

        if (replayBuffer.size() < batchSize) {
            return; // Don't learn until the buffer has enough samples
        }

        System.out.println("*Learning*");
        // Sample a batch of experiences from the replay buffer
        List<Experience> batch = replayBuffer.sample(batchSize);
        int stateSize = marioEnvironment.getGameState().length;
        int actionSize = MarioActions.numberOfActions();

        // Prepare arrays for components of experiences
        double[][] states = new double[batchSize][stateSize];
        double[][] nextStates = new double[batchSize][stateSize];
        double[] rewards = new double[batchSize];
        double[] getGameStatuses = new double[batchSize];
        double[][] targetQs = new double[batchSize][actionSize];

        // Populate the arrays with sampled experience data
        for (int i = 0; i < batchSize; i++) {
            Experience batchExperience = batch.get(i);
            states[i] = batchExperience.getState();
            nextStates[i] = batchExperience.getNextState();
            rewards[i] = batchExperience.getReward();
            getGameStatuses[i] = batchExperience.getGameStatus();

            double[] nextQs = model.predict(nextStates[i]);
            double maxNextQ = Arrays.stream(nextQs).max().getAsDouble();
            targetQs[i] = model.predict(states[i]); // Get current Q-value predictions
            List<Integer> actionIndices = getActionIndices(batchExperience.getAction());
            for (int actionIndex : actionIndices) {
                // Use rewards and getGameStatuses arrays in target Q-value calculation
                targetQs[i][actionIndex] = getGameStatuses[i]==1 ? rewards[i] : rewards[i] + gamma * maxNextQ;
            }
        }

        // Convert arrays to INDArrays
        INDArray statesINDArray = Nd4j.create(states);
        INDArray targetQsINDArray = Nd4j.create(targetQs);

        // Create a DataSet from the INDArrays
        DataSet dataSet = new DataSet(statesINDArray, targetQsINDArray);

        // Normalize the DataSet
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(dataSet);
        normalizer.transform(dataSet);

        // Create a DataSetIterator from the DataSet
        DataSetIterator dataSetIterator = new ListDataSetIterator<>(dataSet.asList());

        // Update training parameters
        updateEpsilon();

        // Train the model
        model.train(dataSetIterator);
    }

    private double calculateReward(Experience lastExp, Experience exp) {
        double reward;
        reward = ((exp.getCompletionPercentage() - lastExp.getCompletionPercentage()) * 100) / (lastExp.getRemainingTime() - exp.getRemainingTime());
        System.out.println("*Experiencing* Reward for " + marioEnvironment.getCurrentModel().getGameStatus().toString() + ": " + reward);
        return reward;
    }

    private List<Integer> getActionIndices(boolean[] actionArray) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < actionArray.length; i++) {
            if (actionArray[i]) {
                indices.add(i); // Add the index of the action that was taken
            }
        }
        return indices; // Return the list of indices
    }

    public Experience storeExperience(Experience experience) {
        replayBuffer.add(experience);
        return experience;
    }

    public void updateEpsilon() {
        // Reduce epsilon, but not below the minimum value
        epsilon = Math.max(minEpsilon, epsilon * epsilonDecayRate);
    }

    public void saveModel(String filePath) {
        // Code to save the model to the specified file path
        model.saveModel(filePath);
    }

    public void loadModel(String filePath) {
        // Code to load the model from the specified file path
        model.loadModel(filePath);
    }

    public void verifySavedAndLoadable(String filePath, double[] testStateInput) {
        model.verifySavedAndLoadable(filePath, testStateInput);
    }

    @Override
    public String getAgentName() {
        return "GM-DQNAgent2023";
    }

    public MarioEnvironment getMarioEnvironment() {
        return marioEnvironment;
    }

}