package com.lyc.TicketManager_Backend.config;

import com.lyc.TicketManager_Backend.bean.RequestException;
import com.lyc.TicketManager_Backend.db.bean.User;
import com.lyc.TicketManager_Backend.db.repo.UserRepository;
import com.lyc.TicketManager_Backend.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws RequestException {
        if (skipSession(handler)) return true;
        User user = SessionUtil.checkLogin(request.getSession(), false);
        SessionUtil.checkPassword(userRepository, user);
        return true;
    }

    private boolean skipSession(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            SkipSession skipSession = handlerMethod.getMethod().getAnnotation(SkipSession.class);
            if (skipSession == null) {
                skipSession = handlerMethod.getMethod().getDeclaringClass().getAnnotation(SkipSession.class);
            }

            return skipSession != null;
        }
        return false;
    }
}
