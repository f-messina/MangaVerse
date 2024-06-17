package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.controller.exception.NotAuthorizedException;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class to manage security operations.
 */
public class SecurityUtils {

    /**
     * Retrieves the authenticated user from the current session.
     *
     * @param request       The HttpServletRequest containing the current request.
     * @return              The User object representing the authenticated user,
     *                      or null if no user is authenticated.
     */
    public static LoggedUserDTO getAuthenticatedUser(HttpServletRequest request){
        HttpSession session = request.getSession();
        return (LoggedUserDTO) session.getAttribute(Constants.AUTHENTICATED_USER_KEY);
    }

    /**
     * Checks if the user is authorized to perform an operation.
     *
     * @param request       The HttpServletRequest containing the current request.
     * @param role          The role required to perform the operation.
     * @return              True if the user is authorized, false otherwise.
     */
    public static void isUserAuthorized(HttpServletRequest request, UserType role) throws NotAuthorizedException {
        LoggedUserDTO loggedUser = getAuthenticatedUser(request);
        if (loggedUser == null || loggedUser.getType() != role) {
            throw new NotAuthorizedException("User is not authorized to perform this operation.");
        }
    }
}
