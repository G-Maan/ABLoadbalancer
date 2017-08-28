package loadbalancer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, Integer> getGroupsConfigurationAsPercentages() {
        return groupsConfiguration
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue() / 100)));
    }

    public int getGroupsNumber() {
        return groupsConfiguration.keySet().size();
    }
}
