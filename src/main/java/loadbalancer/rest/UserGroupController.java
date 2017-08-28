package loadbalancer.rest;

import loadbalancer.logic.Loadbalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Pawel on 2017-08-27.
 */
@RestController
public class UserGroupController {

    @Autowired
    private Loadbalancer loadbalancer;

    @GetMapping(value = "/route")
    public String getUserGroup(@RequestParam(value = "id") String userId) {
        System.out.println("abc");
        return userId;
    }

}
