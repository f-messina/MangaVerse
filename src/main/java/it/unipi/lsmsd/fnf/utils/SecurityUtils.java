package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class to manage security operations.
 */
public class SecurityUtils {

    /**
     * Retrieves the authenticated user from the current session.
     *
     * @param request The HttpServletRequest containing the current request.
     * @return The User object representing the authenticated user, or null if no user is authenticated.
     */
    public static LoggedUserDTO getAuthenticatedUser(HttpServletRequest request){
        HttpSession session = request.getSession();
        return (LoggedUserDTO) session.getAttribute(Constants.AUTHENTICATED_USER_KEY);
    }
}
