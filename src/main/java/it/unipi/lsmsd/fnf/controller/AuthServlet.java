package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private static final UserService userService = ServiceLocator.getUserService();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AuthServlet.class);

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processRequest(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processRequest(httpServletRequest, httpServletResponse);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String targetJSP = "WEB-INF/jsp/auth.jsp";

        switch (action) {
            case "signup" -> handleSignUp(request, response);
            case "login" -> handleLogin(request, response);
            case "logout" -> handleLogout(request, response);
            case null, default -> {
                LoggedUserDTO loggedUser = SecurityUtils.getAuthenticatedUser(request);
                if (loggedUser != null) {
                    String servlet = loggedUser.getType() == UserType.MANAGER ? "manager" : "profile";
                    response.sendRedirect(servlet);
                } else {
                    request.getRequestDispatcher(targetJSP).forward(request, response);
                }
            }
        }
    }

    // Signup operation for the user and redirect to the target servlet
    // REQUIRED: username, email, password
    private void handleSignUp(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            UserRegistrationDTO user = ConverterUtils.fromRequestToUserRegDTO(request);
            userService.signup(user);
            HttpSession session = request.getSession(true);
            String picture = request.getContextPath() + "/" + Constants.DEFAULT_PROFILE_PICTURE;
            LoggedUserDTO loggedUserDTO = user.toModel().toLoggedUserDTO();
            loggedUserDTO.setProfilePicUrl(picture);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, loggedUserDTO);

            // Set the success flag in the JSON response
            jsonResponse.put("success", true);
            jsonResponse.put("redirect", "mainPage/manga");

        } catch (BusinessException e) {
            // Handle the exception and set the error message in the JSON response
            switch (e.getType()) {
                case DUPLICATED_EMAIL:
                    jsonResponse.put("emailError", "Email already in use");
                    break;
                case DUPLICATED_USERNAME:
                    jsonResponse.put("usernameError", "Password already in use");
                    break;
                case DUPLICATED_KEY:
                    jsonResponse.put("emailError", "Email already in use.");
                    jsonResponse.put("usernameError", "Username already in use.");
                    break;
                default:
                    jsonResponse.put("generalError", "An error occurred. Please try again later.");
                    break;
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Login operation for the user and redirect to the target servlet
    // REQUIRED: email, password
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String targetJSP = "WEB-INF/jsp/auth.jsp";

        if (StringUtils.isEmpty(email))
            request.setAttribute("emailEmptyError", "Email is required");
        if (StringUtils.isEmpty(password))
            request.setAttribute("passwordEmptyError", "Password is required");
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
            request.getRequestDispatcher(targetJSP).forward(request, response);
            return;
        }

        try {
            LoggedUserDTO loggedUserDTO = userService.login(email, password);
            loggedUserDTO.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(loggedUserDTO.getProfilePicUrl(), request));
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, loggedUserDTO);
            response.sendRedirect("mainPage/manga");

        } catch (BusinessException e) {
            if (e.getType() == BusinessExceptionType.AUTHENTICATION_ERROR)
                request.setAttribute("authError", "Invalid email or password");
            else
                request.setAttribute("authError", "Error during login operation.");

            request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    // Logout operation for the user and redirect to the target servlet
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (SecurityUtils.getAuthenticatedUser(request) != null) {
            request.getSession().removeAttribute(Constants.AUTHENTICATED_USER_KEY);
            request.getSession().invalidate();
        }
    }
}
