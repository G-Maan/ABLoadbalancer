package loadbalancer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by Pawel on 2017-08-27.
 */
@Configuration
@PropertySource(value={"groups-configuration.properties"})
public class ApplicationConfiguration {

   /* public static PropertiesFactoryBean getGroupsConfiguration() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource(
                "groups-configuration.properties"));
        return bean;
    }*/

}
