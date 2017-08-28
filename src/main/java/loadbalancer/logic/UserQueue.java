package loadbalancer.logic;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class UserQueue {

    private Queue<String> usersQueue = new LinkedList<>();

    private static final int DEQUEUE_NUMBER = 10;

    public void addUser(String userId) {
        usersQueue.add(userId);
    }

    public List<String> dequeue() {
        List<String> dequeueUsersList = new LinkedList<>();
        int usersToDequeue = usersQueue.size() < DEQUEUE_NUMBER ? usersQueue.size() : DEQUEUE_NUMBER;

        for(int i = 0; i < usersToDequeue; i++) {
            dequeueUsersList.add(usersQueue.poll());
        }

        return dequeueUsersList;
    }

}
