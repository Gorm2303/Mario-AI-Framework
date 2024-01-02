package agents.DQN;

public class Experience {
    private double[] state;
    private boolean[] action;
    private double reward;
    private double[] nextState;
    private boolean done;

    public Experience(double[] state, boolean[] action, double reward, double[] nextState, boolean done) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.done = done;
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

    public boolean isDone() {
        return done;
    }
}
