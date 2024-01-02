package agents.DQN;

import engine.core.MarioForwardModel;
import engine.helper.GameStatus;
import engine.helper.MarioActions;

import java.util.Arrays;

public class MarioEnvironment {
    private final MarioForwardModel currentModel;
    private double[] currentState;

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

        // Get game status information
        GameStatus status = currentModel.getGameStatus();
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
        int[][] marioSceneObservation = currentModel.getMarioSceneObservation(1); // Detail level 1
        int[][] marioEnemiesObservation = currentModel.getMarioEnemiesObservation(0); // Detail level 0


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
        };

        int numEnemiesToInclude = enemiesPos.length/3;
        int valuesPerEnemy = 3; // type, x, y
        for (int i = 0; i < numEnemiesToInclude * valuesPerEnemy; i++) {
            if (i < enemiesPos.length) {
                gameState = Arrays.copyOf(gameState, gameState.length + 1);
                gameState[gameState.length - 1] = enemiesPos[i];
            } else {
                // Pad with zeros if there are fewer enemies
                gameState = Arrays.copyOf(gameState, gameState.length + 1);
                gameState[gameState.length - 1] = 0.0;
            }
        }

        // Flatten the 2D arrays (marioSceneObservation and marioEnemiesObservation) and add them to gameState
        gameState = flatten2DArray(marioSceneObservation, gameState);
        gameState = flatten2DArray(marioEnemiesObservation, gameState);

        return gameState;
    }

    private double[] flatten2DArray(int[][] marioObservation, double[] gameState) {
        for (int i = 0; i < marioObservation.length; i++) {
            for (int j = 0; j < marioObservation[i].length; j++) {
                gameState = Arrays.copyOf(gameState, gameState.length + 1);
                gameState[gameState.length - 1] = marioObservation[i][j];
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
}
