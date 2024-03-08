package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.model.registeredUser.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SecurityUtils {
    /**
     * Retrieves the authenticated user from the current session.
     * @param request The HttpServletRequest containing the current request.
     * @return The User object representing the authenticated user, or null if no user is authenticated.
     */
    public static User getAuthenticatedUser(HttpServletRequest request){
        HttpSession session = request.getSession();
        return (User)session.getAttribute(Constants.AUTHENTICATED_USER_KEY);
    }
}
