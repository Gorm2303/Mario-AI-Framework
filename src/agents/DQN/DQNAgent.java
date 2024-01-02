package agents.DQN;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DQNAgent implements MarioAgent {

    //Declare a field for the neural network model (DQNModel).
    //Declare a field for the replay memory (ReplayBuffer).
    private double epsilon;
    private final DQNModel model;
    private final ReplayBuffer replayBuffer;
    private final Random random;
    private MarioEnvironment marioEnvironment;
    private final double gamma; // discount factor
    private final double minEpsilon;
    private final double epsilonDecayRate;
    private final int batchSize;

    //In the constructor or an initialization method, instantiate these components.
    public DQNAgent(DQNModel model, ReplayBuffer replayBuffer, double epsilon, double gamma, double minEpsilon, double epsilonDecayRate, int batchSize) {
        this.model = model;
        this.replayBuffer = replayBuffer;
        this.epsilon = epsilon;
        this.gamma = gamma;
        this.minEpsilon = minEpsilon;
        this.epsilonDecayRate = epsilonDecayRate;
        this.batchSize = batchSize;
        this.random = new Random();
    }

    public DQNAgent(DQNAgent agent, double epsilon, double gamma, double minEpsilon, double epsilonDecayRate) {
        this.model = agent.model;
        this.replayBuffer = agent.replayBuffer;
        this.batchSize = agent.batchSize;
        this.random = agent.random;
        this.epsilon = epsilon;
        this.gamma = gamma;
        this.minEpsilon = minEpsilon;
        this.epsilonDecayRate = epsilonDecayRate;
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

        // Select actions based on the current state using the epsilon-greedy strategy
        return selectAction(currentState);
    }

    //Implement Action Selection Method
    public boolean[] selectAction(double[] state) {
        //Implement the epsilon-greedy strategy: With probability ε, select a random action; otherwise, choose the best action according to the neural network.
        //Epsilon should decay over time to shift from exploration to exploitation.
        boolean[] actions = new boolean[MarioActions.numberOfActions()];
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
        // This could be a fixed number or could vary
        double rand = random.nextDouble();
        if (rand < 0.17) {
            return 1; // 17% chance to choose 1 action
        } else if (rand < 0.17 + 0.33) {
            return 2; // 33% chance to choose 2 actions
        } else {
            return 3; // Remaining 50% chance to choose 3 actions
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

    //Implement Learning Method
    public void learn() {
        //This method should handle transitions: store experiences in the replay buffer.
        //Sample a batch of experiences from the buffer.
        //Use these experiences to update the Q-values via the neural network.
        if (replayBuffer.size() < batchSize) {
            return; // Don't learn until the buffer has enough samples
        }

        // Sample a batch of experiences from the replay buffer
        List<Experience> batch = replayBuffer.sample(batchSize);
        int stateSize = marioEnvironment.getGameState().length;
        int actionSize = MarioActions.numberOfActions();

        // Prepare arrays for components of experiences
        double[][] states = new double[batchSize][stateSize];
        double[][] nextStates = new double[batchSize][stateSize];
        double[] rewards = new double[batchSize];
        boolean[] dones = new boolean[batchSize];
        double[][] targetQs = new double[batchSize][actionSize];

        // Populate the arrays with sampled experience data
        for (int i = 0; i < batchSize; i++) {
            Experience experience = batch.get(i);
            states[i] = experience.getState();
            nextStates[i] = experience.getNextState();
            rewards[i] = experience.getReward();
            dones[i] = experience.isDone();

            double[] nextQs = model.predict(nextStates[i]);
            double maxNextQ = Arrays.stream(nextQs).max().getAsDouble();
            targetQs[i] = model.predict(states[i]); // Get current Q-value predictions
            List<Integer> actionIndices = getActionIndices(experience.getAction());
            for (int actionIndex : actionIndices) {
                // Use rewards and dones arrays in target Q-value calculation
                targetQs[i][actionIndex] = dones[i] ? rewards[i] : rewards[i] + gamma * maxNextQ;
            }
        }

        // Update the DQN model with the states and target Q-values
        model.train(states, targetQs);
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

    public void storeExperience(double[] state, boolean[] action, double reward, double[] nextState, boolean done) {
        Experience experience = new Experience(state, action, reward, nextState, done);
        replayBuffer.add(experience);
    }

    public void updateEpsilon() {
        // Reduce epsilon, but not below the minimum value
        epsilon = Math.max(minEpsilon, epsilon * epsilonDecayRate);
    }

    public void saveModel(String filePath) {
        // Code to save the model to the specified file path
        // This will depend on the specific deep learning framework you are using
    }

    public void loadModel(String filePath) {
        // Code to load the model from the specified file path
        // This will depend on the specific deep learning framework you are using
    }

    @Override
    public String getAgentName() {
        return "GM-DQNAgent2023";
    }

    public MarioEnvironment getMarioEnvironment() {
        return marioEnvironment;
    }

    //The Q-value update mechanism
    //Implement the learning algorithm (updating Q-values, handling transitions).
    //Calculate the target Q-value using the reward and the discounted highest Q-value of the next state.
    //Use the calculated target to train the neural network model.


    //Integrate with the Environment Interface
    //In essence, DQNAgent acts as the mediator that integrates the DQNModel and MarioEnvironmentInterface, handling both the input to and output from the neural network.

    //Ensure that MarioEnvironmentInterface works seamlessly with your DQNModel
    //Your DQN agent should use MarioEnvironmentInterface to process game states before feeding them into the neural network.
    //It should also use MarioEnvironmentInterface to interpret the network's output into game actions.

    //Integrate the DQNModel with MarioEnvironmentInterface
    //Ensure that the model receives input in the correct format from MarioEnvironmentInterface.
    //The output of the model should be appropriately processed by MarioEnvironmentInterface to generate game actions.



    //Manage state transitions
    //After each action, store the transition (state, action, reward, next state, done) in the replay buffer.
    //Call the learn method periodically (not necessarily at every step) to update the network.

    //Integrate the ReplayBuffer with your DQN agent
    //Create an instance of ReplayBuffer.
    //Use addExperience to store experiences after each action.
    //Use sampleBatch during the learning process to train your neural network.
}