package agents.DQN;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReplayBuffer {

    //Store tuples of (state, action, reward, next state, done). Implement methods for adding experiences and sampling a batch of experiences for training.
    private final int capacity;
    private final List<Experience> buffer;
    private int insertIndex;
    private final Random random;

    public ReplayBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>();
        this.insertIndex = 0;
        this.random = new Random();
    }

    //Initialize Storage Mechanism
    //Decide on the maximum size of the buffer to limit memory usage.
    //Use a Java collection like LinkedList or ArrayList to store experiences. Consider using a custom class or Pair/Tuple for experiences.

    //Implement a method to add experiences to the buffer
    public void add(Experience experience) {
        //Append the experience to the buffer and manage the size to not exceed the maximum capacity (remove the oldest experience if full).
        if (buffer.size() < capacity) {
            buffer.add(experience);
        } else {
            buffer.set(insertIndex, experience);
        }
        insertIndex = (insertIndex + 1) % capacity;
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

    //Handle Edge Cases
    //Add checks to ensure the buffer has a sufficient number of experiences before sampling.
    //Manage scenarios where the buffer is not yet full with proper conditional logic.

    //Memory Management
    //Ensure that old experiences are efficiently removed when the buffer reaches its capacity.
    //Optimize data structures for memory efficiency, especially for large buffer sizes.
}
