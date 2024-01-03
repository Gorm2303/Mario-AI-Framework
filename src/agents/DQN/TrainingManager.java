package agents.DQN;

import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

public class TrainingManager {
    //Include necessary fields to hold references to the DQN agent (DQNAgent), the game environment, and any evaluation metrics.
    private static DQNAgent agent;
    private MarioEnvironment environment;
    private static final MarioGame marioGame = new MarioGame();

    // Manages the training loop, allowing the agent to interact with the environment and learn.
    // Manage the training loop (episodes, steps per episode).
    //Set up a loop to run for a specified number of episodes.
    //Inside each episode, run a loop for a defined number of steps or until the end of an episode (game over).
    //At each step, let the agent choose an action, execute it in the environment, and provide feedback (reward, next state) to the agent.
    public static double trainAgent(int timer, int episodes) {
        double totalReward = 0.0;
        Random random = new Random();

        // Run the full game loop
        for (int i = 0; i < episodes; i++) {
            // Make a loop, which loops through each level until MarioResults isn't a WIN.
            System.out.println("Episode: " + i);
            for (int level = 1; level <= 15; level++) {
                String levelName = getLevel("./././levels/original/lvl-" + level + ".txt");
                MarioResult marioResult = marioGame.runGame(agent, levelName, timer, 0, true, 200);
                double reward = analyzeResults(marioResult, level);
                totalReward += reward;
                if (!marioResult.getGameStatus().equals(GameStatus.WIN)) {
                    break;
                }
            }
        }
        return totalReward/episodes;
    }

    //Agent-Environment Interaction
    //Handle interactions between the agent and the environment during training.
    //Within the training loop, fetch the current state from the environment.
    //Pass this state to the agent to get the action.
    //Apply the action in the environment and get the next state and reward.


    //Evaluates the agent's performance without the learning steps.
    //Similar to the training loop, but without the learning steps (no updates to the network).
    //Track the agent's performance (e.g., total reward per episode) to evaluate its learning progress.
    public static double evaluateAgent(int episodes, int timer) {
        double totalReward = 0.0;

        for (int i = 0; i < episodes; i++) {
            // Make a loop, which loops through each level until MarioResults isn't a WIN.
            for (int level = 1; level <= 15; level++) {
                String levelName = getLevel("./././levels/original/lvl-" + level + ".txt");
                DQNAgent dqnAgent = new DQNAgent(agent,0.0,0.99,0.0,0.0, true);
                MarioResult marioResult = marioGame.runGame(dqnAgent, levelName, timer, 0, true, 200);
                double reward = analyzeResults(marioResult, level);
                totalReward += reward;
                if (!marioResult.getGameStatus().equals(GameStatus.WIN)) {
                    break;
                }
            }
        }
        System.out.println("Reward mean is: " + totalReward);

        return totalReward/episodes;
    }

    private static double analyzeResults(MarioResult marioResult, int level) {
        float completionPercentage = marioResult.getCompletionPercentage();
        int getRemainingTime = marioResult.getRemainingTime() / 1000;
        double reward = 0.0;
        if (marioResult.getGameStatus().equals(GameStatus.WIN)) {
            reward = completionPercentage * 100 + getRemainingTime;
        } else {
            reward = completionPercentage * 100;
        }
        printResults(marioResult);
        System.out.println("*Training* Reward for " + marioResult.getGameStatus().toString() + " level " + level + ": " + reward);
        return reward;
    }


    //Implement logging and performance metrics.
    //During training and testing, record relevant metrics such as total rewards, number of steps per episode, and any other custom metrics.
    //Optionally, log these metrics to a file or console for later analysis.


    //Integrate evaluation during the training process.
    //Periodically, within the training loop, call the evaluateAgent method to test the agent's performance.
    //This helps in monitoring the agent's learning progress and adjusting strategies if needed.


    //Implement hyperparameter tuning capabilities.
    //Allow for easy adjustment of hyperparameters like learning rate, discount factor, epsilon values, etc.
    //Monitor the impact of these changes on the agent's performance.


    public static void main(String[] args) {
        agent = new DQNAgent(new DQNModel(267, 5, 0.01),
                new ReplayBuffer(5000),
                1,
                0.99,
                0.1,
                0.9998,
                64,
                false);

        //agent.loadModel("savedModels/model_9.502851486206055pt_03012024");

        trainAgent(60, 10);
        double score = evaluateAgent(2, 10);

        // Save the model every saveInterval number of times learn is called
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
        String dateTime = LocalDateTime.now().format(dtf);
        String modelPath = "savedModels/model_" + score + "pt_" + dateTime;
        agent.saveModel(modelPath);
        agent.verifySavedAndLoadable(modelPath, agent.getMarioEnvironment().getGameState());
    }

    public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString() +
                " Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() +
                " Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode() +
                " (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
        System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() +
                " Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() +
                " Falls: " + result.getKillsByFall() + ")");
        System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() +
                " Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
    }

    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }
}
