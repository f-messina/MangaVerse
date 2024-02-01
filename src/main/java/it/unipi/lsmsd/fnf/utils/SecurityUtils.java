package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.model.registeredUser.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SecurityUtils {

    public static User getAuthenticatedUser(HttpServletRequest request){
        HttpSession session = request.getSession();
        return (User)session.getAttribute(Constants.AUTHENTICATED_USER_KEY);
    }

}