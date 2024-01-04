package agents.DQN;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ReplayBuffer {

    //Store tuples of (state, action, reward, next state, done). Implement methods for adding experiences and sampling a batch of experiences for training.
    private final int capacity;
    private final LinkedList<Experience> buffer;
    private final Random random;

    public ReplayBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
        this.random = new Random();
    }

    //Implement a method to add experiences to the buffer
    public void add(Experience experience) {
        //Append the experience to the buffer and manage the size to not exceed the maximum capacity (remove the oldest experience if full).
        if (buffer.size() < capacity) {
            buffer.add(experience);
        } else {
            buffer.removeFirst();
            buffer.addLast(experience);
        }
    }

    //Develop a method to sample a batch of experiences.
    public List<Experience> sample(int batchSize) {
        List<Experience> batch = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            int index = random.nextInt(buffer.size());
            batch.add(buffer.get(index));
        }
        return batch;
    }

    public int size() {
        return buffer.size();
    }

}
