package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private static final UserService userService = ServiceLocator.getUserService();
    private static final PersonalListService personalListService = ServiceLocator.getPersonalListService();
    private static RegisteredUser authUser;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String targetJSP = "profile.jsp";
        authUser = SecurityUtils.getAuthenticatedUser(request);
        if (authUser == null) {
            targetJSP = "main-page.jsp";
            request.getRequestDispatcher(targetJSP).forward(request, response);
        } else switch (action) {
            case "update-info" -> handleUpdate(request, response);
            case "delete" -> handleDelete(request, response);
            case "logout" -> handleLogout(request, response);
            case "removeItem" -> handleRemoveItem(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
        String targetJSP = "profile.jsp";
        User user = null;
        try {
            user = new User();
            user.setId(authUser.getId());
            String username = request.getParameter("username");
            String description = request.getParameter("description");
            String birthdayString = request.getParameter("birthday").replaceAll("\\s", "");
            String country = request.getParameter("country");
            String gender = request.getParameter("gender");
            if (username.length() >= 3 && username.length() <= 16 && !username.equals(((User) authUser).getUsername()))
                user.setUsername(username);
            if (!description.equals(((User) authUser).getDescription()))
                user.setDescription(description);
            if (birthdayString.matches( "^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$" )) {
                LocalDate date = LocalDate.parse(birthdayString);
                if (!date.equals(((User) authUser).getBirthday())) {
                    user.setBirthday(date);
                }
            }
            if (!country.equals(((User) authUser).getLocation()))
                user.setLocation(country);
            if (!gender.equals(((User) authUser).getGender()))
                user.setGender(gender);
            userService.updateUserInfo(user);
        } catch (BusinessException e) {
            logger.error("BusinessException during update operation.", e);
            request.setAttribute("errorMessage", "Invalid input. Please check your data.");
            targetJSP = "profile.jsp";
        } catch (Exception e) {
            logger.error("Error during update operation.", e);
            targetJSP = "error.jsp";
        }
        if (user != null) {
            User authUserUpdated = updateAuthUser(user);
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, authUserUpdated);
            logger.info(session.getAttribute(Constants.AUTHENTICATED_USER_KEY).toString());
            targetJSP = "profile.jsp";
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
         */
    }

    private static User updateAuthUser(User user) {
        User authUserUpdated = (User) authUser;
        if (user.getUsername() != null)
            authUserUpdated.setUsername(user.getUsername());
        if (user.getDescription() != null)
            authUserUpdated.setDescription(user.getDescription());
        if (user.getBirthday() != null) {
            authUserUpdated.setBirthday(user.getBirthday());
        }
        if (user.getLocation() != null) {
            authUserUpdated.setLocation(user.getLocation());
        }
        if (user.getGender() != null) {
            authUserUpdated.setGender(user.getGender());
        }
        return authUserUpdated;
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("home-page.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
String targetJSP = "main-page.jsp";
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleRemoveItem(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetJSP = "profile.jsp";
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}
