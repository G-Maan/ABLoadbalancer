package loadbalancer.logic;

import loadbalancer.configuration.GroupsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
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

    private Map<String, String> userGroups = new HashMap<>();

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
        return userGroups.get(userId);
    }

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

   //TODO: change method's name
    public void assignUserToGroups() {
        Queue<String> unassignedUsersQueue = userQueue.dequeue();

        Map<String, Integer> percentageMap = new HashMap<>();

        int unassignedUsers = unassignedUsersQueue.size();
        System.out.println("Users to be assigned: " + unassignedUsers);
        groupsConfiguration.getGroupsConfiguration().entrySet().forEach(entry -> percentageMap.put(entry.getKey(), (int)Math.floor(entry.getValue() / 100.00 * unassignedUsers)));

        percentageMap.entrySet().forEach(System.out::println);

        int numberOfUsersAssigned = percentageMap.values().stream().mapToInt(Integer::intValue).sum();
        int numberOfUsersUnassigned = unassignedUsers - numberOfUsersAssigned;
        //TODO:LOggers

        //TODO: wywalic ifa
        if(numberOfUsersUnassigned > 0) {
            Map<String, Integer> offsetMap = calculateOffset(percentageMap, unassignedUsers);
            while (numberOfUsersUnassigned > 0) {
                offsetMap = retainOnlyPositiveOffsetEntries(offsetMap);
                offsetMap = addToBiggestOffset(offsetMap);
                numberOfUsersUnassigned--;
            }
            mergeWithUserGroups(offsetMap, unassignedUsersQueue);
        }
        mergeWithUserGroups(percentageMap, unassignedUsersQueue);
        userGroups.entrySet().forEach(entry -> System.out.println("Key: " + entry.getKey() + " ,Value: " + entry.getValue()));
    }

    void mergeWithUserGroups(Map<String, Integer> offsetMap, Queue<String> unassignedUsersQueue) {
        offsetMap.entrySet().forEach(entry ->
            IntStream.range(0, entry.getValue()).forEach($ -> userGroups.put(unassignedUsersQueue.poll(), entry.getKey()))
        );
    }

    Map<String, Integer> addToBiggestOffset(Map<String, Integer> offsetMap) {
        String biggestOffsetGroup = offsetMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        Map<String, Integer> tempMap = new HashMap<>(offsetMap);
        tempMap.compute(biggestOffsetGroup,  (k, v) -> v + 1);
        return tempMap;
    }

    Map<String, Integer> calculateOffset(Map<String, Integer> newMap, int numberOfUsers) {
        Map<String, Integer> offsetMap = Stream
                .of(newMap, groupsConfiguration.getGroupsConfigurationAsPercentages(numberOfUsers))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1 - v2
                ));
        return offsetMap;
    }

    Map<String, Integer> retainOnlyPositiveOffsetEntries(Map<String, Integer> offsetMap) {
        Map<String, Integer> tempMap = new HashMap<>(offsetMap);
        tempMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
        return tempMap;
    }

    Map<String, Integer> getCurrentUserGroupAssociation() {
        Map<String, Integer> userGroupsAssociation = new HashMap<>(groupsConfiguration.getGroupsNumber());
        for (String group: userGroups.values()) {
            int value = userGroups.get(group) == null ? 0 : userGroupsAssociation.get(group);
            userGroupsAssociation.put(group, value + 1);
        }
        return userGroupsAssociation;
    }

}