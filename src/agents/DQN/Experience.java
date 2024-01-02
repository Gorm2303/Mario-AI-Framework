package agents.DQN;

import engine.helper.GameStatus;

import java.util.Arrays;

public class Experience {
    private double[] state;
    private boolean[] action;
    private double reward;
    private double[] nextState;
    private double gameStatus;
    private float completionPercentage;
    private int remainingTime;

    public Experience(double[] state, boolean[] action, double reward, double[] nextState, double gameStatus, float completionPercentage, int remainingTime) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.gameStatus = gameStatus;
        this.completionPercentage = completionPercentage;
        this.remainingTime = remainingTime;
    }

    public double[] getState() {
        return state;
    }

    public boolean[] getAction() {
        return action;
    }

    public double getReward() {
        return reward;
    }

    public double[] getNextState() {
        return nextState;
    }

    public double getGameStatus() {
        return gameStatus;
    }

    public float getCompletionPercentage() {
        return completionPercentage;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public void setNextState(double[] nextState) {
        this.nextState = nextState;
    }

    public void print() {
        System.out.println("Action: " + Arrays.toString(action));
        System.out.println("Reward: " + reward);
    }
}
