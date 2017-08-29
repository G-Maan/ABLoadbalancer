package loadbalancer.logic;

import loadbalancer.configuration.GroupsConfiguration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class Loadbalancer {

    private static final Logger logger = getLogger(Loadbalancer.class);

    private GroupsConfiguration groupsConfiguration;

    private UserQueue userQueue;

    private Map<String, String> userGroups = new HashMap<>();

    private CountDownLatch latch;

    private static final int LATCH_COUNT = 1;

    @Autowired
    public Loadbalancer(GroupsConfiguration groupsConfiguration, UserQueue userQueue) {
        this.groupsConfiguration = groupsConfiguration;
        this.userQueue = userQueue;
    }

    Map<String, String> getUserGroups() {
        return this.userGroups;
    }

    public boolean userHasGroup(String userId) {
        return userGroups.containsKey(userId);
    }

    public String getUserGroup(String userId) {

        try {
            if (latch != null) {
                latch.await();
            }
            return userGroups.get(userId);
        } catch (InterruptedException e) {
            logger.error("Couldn't wait on latch", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method which delegates assigning users to userGroup based on number of users currently in a waiting queue.
     */
    public void assignUserToGroups() {
        latch = new CountDownLatch(LATCH_COUNT);
        Queue<String> unassignedUsersQueue = userQueue.dequeue();

        Map<String, Integer> initialUsersGroups = new HashMap<>();

        int unassignedUsers = unassignedUsersQueue.size();
        logger.info("Users to be assigned: " + unassignedUsers);

        if (unassignedUsers <= groupsConfiguration.getGroupsSize()){
            assignBasedOnCurrentCapacity(unassignedUsers, unassignedUsersQueue);
            return;
        }

        groupsConfiguration.getGroupsConfiguration().entrySet().forEach(entry -> initialUsersGroups.put(entry.getKey(), (int)Math.floor(entry.getValue() / 100.00 * unassignedUsers)));

        int numberOfUsersAssigned = initialUsersGroups.values().stream().mapToInt(Integer::intValue).sum();
        int numberOfUsersUnassigned = unassignedUsers - numberOfUsersAssigned;
        logger.info("Users assigned: " + numberOfUsersAssigned);
        logger.info("Users still unassigned: " + numberOfUsersUnassigned);

        mergeWithUserGroupsAndReleaseLatch(assignBasedOnPreviousAssignments(numberOfUsersUnassigned, initialUsersGroups), unassignedUsersQueue);
    }

    private Map<String, Integer> assignBasedOnPreviousAssignments(int numberOfUsersUnassigned, Map<String, Integer> initialUsersGroups) {
        Map<String, Integer> assignedUsersGroups = initialUsersGroups;
        if(numberOfUsersUnassigned > 0) {
            assignedUsersGroups = assignRestOfUsers(assignedUsersGroups, numberOfUsersUnassigned);
        }
        return assignedUsersGroups;
    }

    private void assignBasedOnCurrentCapacity(int usersUnassigned, Queue<String> unassignedUsersqueue) {
        Map<String, Integer> usersGroupLoad = calculateCurrentLoad();
        int numberOfUsersUnassigned = usersUnassigned;
        usersGroupLoad = assignRestOfUsers(usersGroupLoad, numberOfUsersUnassigned);
        usersGroupLoad = substractOriginalLoad(usersGroupLoad);
        mergeWithUserGroupsAndReleaseLatch(usersGroupLoad, unassignedUsersqueue);
    }

    private Map<String, Integer> assignRestOfUsers(Map<String, Integer> usersGroupLoad, int numberOfUsersUnassigned) {
        Map<String, Integer> offsetMap;
        while (numberOfUsersUnassigned > 0) {
            offsetMap = calculateOffset(usersGroupLoad);
            usersGroupLoad = addToBiggestOffset(offsetMap, usersGroupLoad);
            numberOfUsersUnassigned--;
        }
        return usersGroupLoad;
    }

    private Map<String, Integer> substractOriginalLoad(Map<String, Integer> usersGroupLoad) {
        Map<String, Integer> currentLoad = calculateCurrentLoad();
        return  Stream
                .of(currentLoad, usersGroupLoad)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v2 - v1
                ));
    }

    private Map<String, Integer> calculateCurrentLoad() {
        Map<String, Integer> currentLoad = new HashMap<>();
        groupsConfiguration.getGroupsConfiguration().entrySet().forEach(entry -> currentLoad.put(entry.getKey(), 0));
        userGroups.entrySet().forEach(entry -> currentLoad.compute(entry.getValue(), (k, v) -> v == null ? 1 : v + 1));
        return currentLoad;
    }

    private void releaseLatch() {
        if (latch != null) {
            latch.countDown();
        }
    }

    private void mergeWithUserGroupsAndReleaseLatch(Map<String, Integer> offsetMap, Queue<String> unassignedUsersQueue) {
        offsetMap.entrySet().forEach(entry ->
            IntStream.range(0, entry.getValue()).forEach($ -> userGroups.put(unassignedUsersQueue.poll(), entry.getKey()))
        );
        releaseLatch();
    }

    private Map<String, Integer> addToBiggestOffset(Map<String, Integer> offsetMap, Map<String, Integer> mockUsersGroups) {
        String biggestOffsetGroup = offsetMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        Map<String, Integer> tempMap = new HashMap<>(mockUsersGroups);
        tempMap.compute(biggestOffsetGroup,  (k, v) -> v + 1);
        return tempMap;
    }

    private Map<String, Integer> calculateOffset(Map<String, Integer> newMap) {
        double assignedUsers = userGroups.keySet().size();
        if (assignedUsers == 0) {
            assignedUsers = 1;
        }
        double finalAssignedUsers = assignedUsers;
        Map<String, Integer> offsetMap = Stream
                .of(newMap, groupsConfiguration.getGroupsConfiguration())
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> (int)Math.floor(Double.valueOf(v2) - (Double.valueOf(v1) * 100.00 / finalAssignedUsers))
                ));
        return offsetMap;
    }
}