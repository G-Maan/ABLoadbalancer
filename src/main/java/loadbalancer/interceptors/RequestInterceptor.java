package loadbalancer.interceptors;

import loadbalancer.ApplicationContextProvider;
import loadbalancer.logic.Loadbalancer;
import loadbalancer.logic.UserQueue;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Pawel on 2017-08-27.
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    private Loadbalancer loadbalancer;

    private UserQueue userQueue;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        //TODO: logger
        String userId =  httpServletRequest.getParameter("id");

        //TODO: delegate to method
        loadbalancer = ApplicationContextProvider.getContext().getBean("loadbalancer", Loadbalancer.class);
        userQueue = ApplicationContextProvider.getContext().getBean("userQueue", UserQueue.class);

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
}
