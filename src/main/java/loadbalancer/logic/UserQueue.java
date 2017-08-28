package loadbalancer.logic;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class UserQueue {

    private static final int DEQUEUE_NUMBER = 10;

    private Queue<String> queuedUsers = new LinkedList<>();

    public void addUser(String userId) {
        queuedUsers.add(userId);
    }

    public int getUsersQueueSize() {
        return queuedUsers.size();
    }

    Queue<String> dequeue() {
        Queue<String> dequeuedUsers = new LinkedList<>();
        int usersToDequeue = queuedUsers.size() < DEQUEUE_NUMBER ? queuedUsers.size() : DEQUEUE_NUMBER;

        //TODO: JAVA 8
        for(int i = 0; i < usersToDequeue; i++) {
            dequeuedUsers.add(queuedUsers.poll());
        }
        return dequeuedUsers;
    }

}
