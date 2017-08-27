package loadbalancer.rest;

import loadbalancer.configuration.GroupsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Pawel on 2017-08-27.
 */
@RestController
public class UserGroupController {

    @Autowired
    private GroupsConfiguration groupsConfiguration;

    @GetMapping(value = "/route")
    public String getUserGroup(@RequestParam(value = "id") String userId) {
        groupsConfiguration.getGroupsConfiguration().entrySet().stream().forEach(entry -> {
            System.out.println("KEY: " + entry.getKey());
            System.out.println("VALUE: " + entry.getValue());
        });
        return userId;
    }

}
