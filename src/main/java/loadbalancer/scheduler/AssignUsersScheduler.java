package loadbalancer.scheduler;

import loadbalancer.logic.Loadbalancer;
import loadbalancer.logic.UserQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Pawel on 2017-08-28.
 */
@Component
public class AssignUsersScheduler {

    private static final long DELAY = 1000l; //TODO: comment

    private Loadbalancer loadbalancer;
    private UserQueue userQueue;

    @Autowired
    public AssignUsersScheduler(Loadbalancer loadbalancer, UserQueue userQueue) {
        this.loadbalancer = loadbalancer;
        this.userQueue = userQueue;
    }

    @Scheduled(fixedDelay = DELAY)
    public void assignUsersToGroups() {
        //TODO: start worker
        if (userQueue.getUsersQueueSize() > 0){
            loadbalancer.assignUserToGroups();
            //TODO: loadbalancer working
        }
        //TODO: finished worker
    }

}
