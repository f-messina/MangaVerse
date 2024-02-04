package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private static final UserService userService = ServiceLocator.getUserService();
    private static final PersonalListService personalListService = ServiceLocator.getPersonalListService();
    private static User authUser;

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
        } else switch (action) {
            case "update-info" -> handleUpdate(request, response);
            case "delete" -> handleDelete(request, response);
            case "logout" -> handleLogout(request, response);
            case "removeItem" -> handleRemoveItem(request, response);
            case null, default -> {}
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetJSP = "profile.jsp";
        try {
            User user = userFromInfo(request);
            userService.updateUserInfo(user);

            User authUserUpdated = updateAuthUser(request);
            request.getSession().setAttribute(Constants.AUTHENTICATED_USER_KEY, authUserUpdated);
        } catch (BusinessException e) {
            handleUpdateError(request, "Invalid input. Please check your data.", e);
            targetJSP = "profile.jsp";
        } catch (Exception e) {
            handleUpdateError(request, "Error during update operation.", e);
            targetJSP = "error.jsp";
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleUpdateError(HttpServletRequest request, String errorMessage, Exception e) {
        logger.error(errorMessage, e);
        request.setAttribute("errorMessage", errorMessage);
    }

    private static User userFromInfo(HttpServletRequest request) {
        User user = new User();
        user.setId(authUser.getId());

        String username = request.getParameter("username");
        String authUserUsername = (String) getAuthUserValue("username");
        if (!Objects.equals(username, authUserUsername))
            user.setUsername(username);

        String description = request.getParameter("description");
        String authUserDescription = (String) getAuthUserValue("description");
        if (!Objects.equals(description, authUserDescription)) {
            user.setDescription(StringUtils.isEmpty(description) ? description : Constants.NULL_STRING);
        }

        String country = request.getParameter("country");
        String authUserCountry = (String) getAuthUserValue("country");
        if (!Objects.equals(country, authUserCountry)) {
            user.setLocation(StringUtils.isEmpty(country) ? country : Constants.NULL_STRING);
        }

        String birthdate = request.getParameter("birthdate");
        LocalDate authUserBirthdate = (LocalDate) getAuthUserValue("birthdate");
        if (!Objects.equals(LocalDate.parse(birthdate), authUserBirthdate)) {
            user.setBirthday(StringUtils.isEmpty(birthdate) ? LocalDate.parse(birthdate) : Constants.NULL_DATE);
        }

        String fullname = request.getParameter("fullname");
        String authUserFullname = (String) getAuthUserValue("fullname");
        if (!Objects.equals(fullname, authUserFullname)) {
            user.setFullname(StringUtils.isEmpty(fullname) ? fullname : Constants.NULL_STRING);
        }

        String gender = request.getParameter("gender");
        Gender authUserGender = (Gender) getAuthUserValue("gender");
        if (!Objects.equals(Gender.fromString(gender), authUserGender)) {
            user.setGender(Gender.fromString(gender));
        }

        return user;
    }

    private static Object getAuthUserValue(String parameterName) {
        return switch (parameterName) {
            case "username" -> authUser.getUsername();
            case "description" -> authUser.getDescription();
            case "country" -> authUser.getLocation();
            case "birthdate" -> authUser.getBirthday();
            case "fullname" -> authUser.getFullname();
            case "gender" -> authUser.getGender();
            default -> null;
        };
    }

    private static User updateAuthUser(HttpServletRequest request) {
        User authUserUpdated = authUser;
        updateFieldIfNotNull(request, "username", username -> authUserUpdated.setUsername((String) username));
        updateFieldIfNotNull(request, "description", description -> authUserUpdated.setDescription((String) description));
        updateFieldIfNotNull(request, "birthdate", date -> authUserUpdated.setBirthday(ConverterUtils.convertDateToLocalDate((Date) date)));
        updateFieldIfNotNull(request, "country", country -> authUserUpdated.setLocation((String) country));
        updateFieldIfNotNull(request, "gender", gender -> authUserUpdated.setGender(Gender.fromString((String) gender)));
        return authUserUpdated;
    }

    private static void updateFieldIfNotNull(HttpServletRequest request, String parameterName, Consumer<Object> updateFunction) {
        Object parameterValue = request.getAttribute(parameterName);
        if (parameterValue != null) {
            updateFunction.accept(parameterValue);
        }
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
