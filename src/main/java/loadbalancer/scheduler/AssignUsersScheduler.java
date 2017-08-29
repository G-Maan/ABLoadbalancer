package loadbalancer.scheduler;

import loadbalancer.logic.Loadbalancer;
import loadbalancer.logic.UserQueue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Pawel on 2017-08-28.
 */
@Component
public class AssignUsersScheduler {

    private static final long FREQUENCY = 100L; //Scheduler's frequency
    private static final Logger logger = getLogger(AssignUsersScheduler.class);

    private Loadbalancer loadbalancer;
    private UserQueue userQueue;

    @Autowired
    public AssignUsersScheduler(Loadbalancer loadbalancer, UserQueue userQueue) {
        this.loadbalancer = loadbalancer;
        this.userQueue = userQueue;
    }

    @Scheduled(fixedDelay = FREQUENCY)
    public void assignUsersToGroups() {
        if (userQueue.getUsersQueueSize() > 0){
            logger.info("Scheduler invoked scheduled method");
            loadbalancer.assignUserToGroups();
        }
    }
}
