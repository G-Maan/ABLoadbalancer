package loadbalancer.logic;

import loadbalancer.configuration.GroupsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thavam.util.concurrent.BlockingHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class Loadbalancer {

    private GroupsConfiguration groupsConfiguration;

    private UserQueue userQueue;

    private Map<String, String> userGroups = new BlockingHashMap<>();

    @Autowired
    public Loadbalancer(GroupsConfiguration groupsConfiguration, UserQueue userQueue) {
        this.groupsConfiguration = groupsConfiguration;
        this.userQueue = userQueue;
    }

    public boolean userHasGroup(String userId) {
        return userGroups.containsKey(userId);
    }

    private String getUserGroup(String userId) {
        return userGroups.get(userId);
    }

    public void assignUserToGroups() {
        List<String> unassignedUsersList = userQueue.dequeue();
        /*
        CONFIGURACJA
        A: 20%
        B: 30%
        C: 50%


        AKTUALNY STAN
        A:1 20%
        B:1 20%
        C:2 40%

        OFFSET
        A: 0%
        B: 10%
        C: 10%

        AFTER RETAIN
        B: 10%
        C: 10%
            petla{
                dla kazdej grupy ile uzytkownikow
                   jak puste to do pierwszej
                   jak nie
                   procentowo do kazdej
                   sprawdzenie ile do kazdej przydzielono i ile uzytkownikow zostalo bez grupy
                   policzenie najwiekszego offseta i przydzielenie pozostalych uzytkownikow
            }
         */

        Map<String, Integer> percentageMap = new HashMap<>();

        int usersToBeAssigned = unassignedUsersList.size();
        System.out.println("Users to be assigned: " + usersToBeAssigned);
        groupsConfiguration.getGroupsConfiguration().entrySet().forEach(entry -> percentageMap.put(entry.getKey(), entry.getValue() / 100 * usersToBeAssigned));

        percentageMap.entrySet().forEach(System.out::println);

        int numberOfUsersAssigned = percentageMap.values().stream().mapToInt(Integer::intValue).sum();
        int numberOfUsersUnassigned = usersToBeAssigned - numberOfUsersAssigned;
        if(numberOfUsersUnassigned > 0) {
            Map<String, Integer> offsetMap = calculateOffset(percentageMap);
            while (numberOfUsersUnassigned > 0) {
                offsetMap = retainOnlyPositiveOffsetEntries(offsetMap);
                offsetMap = addToBiggestOffset(offsetMap);
                numberOfUsersUnassigned--;
            }
            mergeGroupsMaps(offsetMap, unassignedUsersList);
        }
    }

    private void mergeGroupsMaps(Map<String, Integer> offsetMap, List<String> unassignedUsersList) {
        offsetMap.entrySet().forEach(entry ->
            IntStream
                    .range(0, entry.getValue())
                    .forEach(index -> userGroups.put(unassignedUsersList.get(0), entry.getKey()))
        );
    }

    private Map<String, Integer> addToBiggestOffset(Map<String, Integer> offsetMap) {
        String biggestOffsetGroup = offsetMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        Map<String, Integer> tempMap = new HashMap<>(offsetMap);
        tempMap.compute(biggestOffsetGroup,  (k, v) -> v + 1);
        return tempMap;
    }

    private Map<String, Integer> calculateOffset(Map<String, Integer> newMap) {
        Map<String, Integer> offsetMap = Stream
                .of(newMap, groupsConfiguration.getGroupsConfigurationAsPercentages())
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1 - v2
                ));
        return offsetMap;
    }

    private Map<String, Integer> retainOnlyPositiveOffsetEntries(Map<String, Integer> offsetMap) {
        Map<String, Integer> tempMap = new HashMap<>(offsetMap);
        tempMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
        return tempMap;
    }

    private Map<String, Integer> getCurrentUserGroupAssociation() {
        Map<String, Integer> userGroupsAssociation = new HashMap<>(groupsConfiguration.getGroupsNumber());
        for (String group: userGroups.values()) {
            int value = userGroups.get(group) == null ? 0 : userGroupsAssociation.get(group);
            userGroupsAssociation.put(group, value + 1);
        }
        return userGroupsAssociation;
    }

}