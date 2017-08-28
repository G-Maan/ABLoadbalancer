package loadbalancer.logic;


import loadbalancer.configuration.GroupsConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Pawel on 2017-08-28.
 */
public class LoadbalancerTest {

    private Loadbalancer loadbalancer;
    private GroupsConfiguration groupsConfiguration = mock(GroupsConfiguration.class);
    private UserQueue userQueue = mock(UserQueue.class);


    @Before
    public void setUp() throws Exception {
        loadbalancer = new Loadbalancer(groupsConfiguration, userQueue);
    }

    @Test
    public void shouldAssignUserToGroups() throws Exception {
        Queue<String> usersList = new LinkedList<>();
        String userId = "abc:1";
        String userId2 = "abc:2";
        String userId3 = "abc:3";
        String userId4 = "abc:4";
        String userId5 = "abc:5";
        String userId6 = "abc:6";
        String userId7 = "abc:7";
        String userId8 = "abc:8";
        String userId9 = "abc:9";
        String userId10 = "abc:10";

        usersList.add(userId);
        usersList.add(userId2);
        usersList.add(userId3);
        usersList.add(userId4);
        usersList.add(userId5);
        usersList.add(userId6);
        usersList.add(userId7);
        usersList.add(userId8);
        usersList.add(userId9);
        usersList.add(userId10);


        Map<String, Integer> groupsConfigurationMap = new HashMap<>();
        groupsConfigurationMap.put("groupA", 20);
        groupsConfigurationMap.put("groupB", 30);
        groupsConfigurationMap.put("groupC", 50);

        when(userQueue.dequeue()).thenReturn(usersList);
        when(groupsConfiguration.getGroupsConfiguration()).thenReturn(groupsConfigurationMap);

        loadbalancer.assignUserToGroups();

        int groupACount = Collections.frequency(loadbalancer.getUserGroups().values(), "groupA");
        int groupBCount = Collections.frequency(loadbalancer.getUserGroups().values(), "groupB");
        int groupCCount = Collections.frequency(loadbalancer.getUserGroups().values(), "groupC");

        Assert.assertEquals(2, groupACount);
        Assert.assertEquals(3, groupBCount);
        Assert.assertEquals(5, groupCCount);

    }

    @Test
    public void shouldNotAssignAnyUsers() throws Exception {
        Queue<String> usersList = new LinkedList<>();

        Map<String, Integer> groupsConfigurationMap = new HashMap<>();
        groupsConfigurationMap.put("groupA", 20);
        groupsConfigurationMap.put("groupB", 30);
        groupsConfigurationMap.put("groupC", 50);

        when(userQueue.dequeue()).thenReturn(usersList);
        when(groupsConfiguration.getGroupsConfiguration()).thenReturn(groupsConfigurationMap);

        loadbalancer.assignUserToGroups();

        int groupACount = Collections.frequency(loadbalancer.getUserGroups().values(), "groupA");
        int groupBCount = Collections.frequency(loadbalancer.getUserGroups().values(), "groupB");
        int groupCCount = Collections.frequency(loadbalancer.getUserGroups().values(), "groupC");

        Assert.assertEquals(0, groupACount);
        Assert.assertEquals(0, groupBCount);
        Assert.assertEquals(0, groupCCount);
    }

}