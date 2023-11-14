package agents.DQN;

import engine.core.MarioForwardModel;

public class MarioEnvironment {

    //Should convert game states to a format suitable for the neural network and translate the network's outputs to in-game actions.
    public void convertGameState(MarioForwardModel model) {

    }
    //This method should take the game state as input, typically provided by MarioForwardModel.
    //Convert the game state into a numerical format (e.g., an array or tensor) that can be processed by your DQN model

    public void translateActions() {

    }
    //This method should accept the output of your neural network (e.g., an array indicating action probabilities or choices).
    //Translate this output into a format recognized by the game, such as specific key presses or action commands.
}
