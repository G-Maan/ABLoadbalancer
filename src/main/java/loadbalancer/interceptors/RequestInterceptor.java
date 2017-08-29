package loadbalancer.interceptors;

import loadbalancer.ApplicationContextProvider;
import loadbalancer.logic.Loadbalancer;
import loadbalancer.logic.UserQueue;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    private static final Logger logger = getLogger(RequestInterceptor.class);

    private Loadbalancer loadbalancer;

    private UserQueue userQueue;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        logger.info("Entered preHandle method");
        String userId =  httpServletRequest.getParameter("id");

        initializeBeans();

        if(!loadbalancer.userHasGroup(userId)) {
            userQueue.addUser(userId);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

    private void initializeBeans() {
        loadbalancer = ApplicationContextProvider.getContext().getBean("loadbalancer", Loadbalancer.class);
        userQueue = ApplicationContextProvider.getContext().getBean("userQueue", UserQueue.class);
        logger.info("Beans initialized successfully");
    }

}
