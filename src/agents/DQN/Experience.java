package agents.DQN;

import engine.helper.GameStatus;

public class Experience {
    private double[] state;
    private boolean[] action;
    private double reward;
    private double[] nextState;
    private double gameStatus;

    public Experience(double[] state, boolean[] action, double reward, double[] nextState, double gameStatus) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.gameStatus = gameStatus;
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

    public void setReward(double reward) {
        this.reward = reward;
    }

    public void setNextState(double[] nextState) {
        this.nextState = nextState;
    }
}
