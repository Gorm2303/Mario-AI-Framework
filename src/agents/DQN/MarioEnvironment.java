package agents.DQN;

import engine.core.MarioForwardModel;
import engine.helper.GameStatus;
import engine.helper.MarioActions;

import java.util.Arrays;
import java.util.Comparator;

public class MarioEnvironment {
    private final MarioForwardModel currentModel;
    private double[] currentState;
    private double gameStatus;

    public MarioEnvironment(MarioForwardModel currentModel) {
        this.currentModel = currentModel;
    }

    //Should convert game states to a format suitable for the neural network and translate the network's outputs to in-game actions.
    public double[] getGameState() {
        if (currentState != null) {
            return currentState;
        }

        // Get Mario's position and velocity
        float[] marioPos = currentModel.getMarioFloatPos();
        float[] marioVel = currentModel.getMarioFloatVelocity();

        int remainingTime = currentModel.getRemainingTime();
        float completionPercentage = currentModel.getCompletionPercentage();

        // Get Mario's state information
        boolean isMarioOnGround = currentModel.isMarioOnGround();
        boolean mayMarioJump = currentModel.mayMarioJump();
        boolean canJumpHigher = currentModel.getMarioCanJumpHigher();
        int marioMode = currentModel.getMarioMode(); // 0-small, 1-large, 2-fire

        // Get enemy positions
        float[] enemiesPos = currentModel.getEnemiesFloatPos(); // This is an array containing type, x, and y for each enemy

        // Get the 2D grid observations around Mario (you can choose the level of detail)
        int[][] screenEnemiesObservation = currentModel.getScreenCompleteObservation(0,0); // Detail level

        // Convert this information into a numerical format (array or tensor)
        // Mario's position
        // Mario's velocity
        double[] gameState = new double[]{
                marioPos[0], marioPos[1], // Mario's position
                marioVel[0], marioVel[1], // Mario's velocity
                isMarioOnGround ? 1.0 : 0.0,
                mayMarioJump ? 1.0 : 0.0,
                canJumpHigher ? 1.0 : 0.0,
                marioMode,
                remainingTime,
                completionPercentage,
                getGameStatus()
        };

        // Flatten the 2D arrays (screenSceneObservation and marioEnemiesObservation) and add them to gameState
        gameState = flatten2DArray(screenEnemiesObservation, gameState);

        currentState = gameState;
        return gameState;
    }

    private double[] flatten2DArray(int[][] marioObservation, double[] gameState) {
        for (int[] ints : marioObservation) {
            for (int anInt : ints) {
                gameState = Arrays.copyOf(gameState, gameState.length + 1);
                gameState[gameState.length - 1] = anInt;
            }
        }
        return gameState;
    }

    //This method should accept the output of the neural network (e.g., an array indicating action probabilities or choices).
    //Translate this output into a format recognized by the game, such as specific key presses or action commands.
    public boolean[] translateActions(double[] qValues) {
        // Array to hold which actions to take
        boolean[] actions = new boolean[MarioActions.numberOfActions()];

        double maxQValue = Double.NEGATIVE_INFINITY;
        for (double qValue : qValues) {
            if (qValue > maxQValue) {
                maxQValue = qValue;
            }
        }

        // Select the action(s) with the highest Q-value
        for (int i = 0; i < qValues.length; i++) {
            if (qValues[i] == maxQValue) {
                actions[i] = true;
            }
        }

        return actions;
    }

    public MarioForwardModel getCurrentModel() {
        return currentModel;
    }

    public double getGameStatus() {
        GameStatus status = currentModel.getGameStatus();
        if (status.equals(GameStatus.WIN)) {
            gameStatus = 1;
        } else if (status.equals(GameStatus.LOSE) || status.equals(GameStatus.TIME_OUT)) {
            gameStatus = 0;
        } else {
            gameStatus = currentModel.getCompletionPercentage();
        }
        return gameStatus;
    }
}
