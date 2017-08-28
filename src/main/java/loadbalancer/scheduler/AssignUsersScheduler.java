package loadbalancer.scheduler;

import loadbalancer.logic.Loadbalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Pawel on 2017-08-28.
 */
@Component
public class AssignUsersScheduler {

    @Autowired
    private Loadbalancer loadbalancer;

    @Scheduled(fixedDelay = 1000)
    public void assignUsersToGroups() {
        loadbalancer.assignUserToGroups();
    }

}
