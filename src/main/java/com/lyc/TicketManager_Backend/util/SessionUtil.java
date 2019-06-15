package com.lyc.TicketManager_Backend.util;

import com.lyc.TicketManager_Backend.bean.RequestException;
import com.lyc.TicketManager_Backend.config.StatusCode;
import com.lyc.TicketManager_Backend.db.bean.User;
import com.lyc.TicketManager_Backend.db.repo.UserRepository;

import javax.servlet.http.HttpSession;
import java.util.Optional;

public class SessionUtil {

    public static User checkLogin(HttpSession httpSession, boolean nullable) throws RequestException {
        User result = null;
        Object o;
        try {
            if (httpSession != null && (o = httpSession.getAttribute("user")) != null) {
                if (o instanceof User) {
                    result = (User) o;
                } else {
                    httpSession.setAttribute(httpSession.getId(), null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestException(StatusCode.REQUEST_ERROR, "登录信息已失效，请重新登录");
        }
        if (!nullable && result == null) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "用户未登录");
        }
        return result;
    }

    public static void checkPassword(UserRepository userRepository, User user) throws RequestException {
        Optional<User> userOptional = userRepository.findByUsernameAndPasswordHash(user.getUsername(), user.getPasswordHash());
        if (!userOptional.isPresent()) {
            throw new RequestException(StatusCode.REQUEST_ERROR, "登录信息已失效，请重新登录");
        }
    }

    public static void logout(HttpSession session) {
        session.invalidate();
    }
}
