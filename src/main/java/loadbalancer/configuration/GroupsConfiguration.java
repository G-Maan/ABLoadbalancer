package loadbalancer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class GroupsConfiguration {

    @Value("#{${groups}}")
    private Map<String, Integer> groupsConfiguration;

    public Map<String, Integer> getGroupsConfiguration() {
        return groupsConfiguration;
    }
}
