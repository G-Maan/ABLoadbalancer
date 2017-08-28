package loadbalancer;

import loadbalancer.interceptors.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Pawel on 2017-08-27.
 */
@Configuration
@PropertySource(value={"groups-configuration.properties"})
@EnableScheduling
public class ApplicationConfiguration extends WebMvcConfigurerAdapter{

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor()).addPathPatterns("/route");
    }

    /* public static PropertiesFactoryBean getGroupsConfiguration() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource(
                "groups-configuration.properties"));
        return bean;
    }*/

}
