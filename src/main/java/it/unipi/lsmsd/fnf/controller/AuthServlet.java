package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unipi.lsmsd.fnf.service.UserService;

import java.io.IOException;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private static final UserService userService = ServiceLocator.getUserService();

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

        String targetJSP = "tests/auth_test.jsp";
        switch (action) {
            case "signup" -> handleSignUp(request, response);
            case "login" -> handleLogin(request, response);
            case "logout" -> handleLogout(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    private void handleSignUp(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetJSP;
        try {
            User user = userService.registerUserAndLogin(ConverterUtils.fromRequestToUserRegDTO(request));
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, user);
            response.sendRedirect("profile");
            return;
        } catch (BusinessException e) {
            logger.error("BusinessException during signup operation.", e);
            targetJSP = "tests/auth_test.jsp";

            String errorMessage = e.getMessage();
            switch (errorMessage) {
                case "Email already in use" -> request.setAttribute("emailError", e.getMessage());
                case "Username already in use" -> request.setAttribute("usernameError", e.getMessage());
                case "Email and username already in use" -> {
                    request.setAttribute("emailError", "Email already in use.");
                    request.setAttribute("usernameError", "Username already in use.");
                }
                case "Username, password and email cannot be empty" ->
                        request.setAttribute("errorMessage", e.getMessage());
                case null, default -> {
                    request.setAttribute("errorMessage", "Error during signup operation.");
                    targetJSP = "error.jsp";
                }
            }
        } catch (Exception e) {
            logger.error("Error during signup operation.", e);
            targetJSP = "error.jsp";
        }
        logger.info(targetJSP);
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String targetJSP;

        try {
            RegisteredUser registeredUser = userService.login(email, password);
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, registeredUser);
            // Redirect to avoid resubmission on page reload
            response.sendRedirect("profile");
            return;
        } catch (BusinessException e) {
            logger.error("BusinessException during login operation.", e);
            targetJSP = "tests/auth_test.jsp";

            String errorMessage = e.getMessage();
            switch (errorMessage) {
                case "Invalid email" -> request.setAttribute("emailLoginError", e.getMessage());
                case "Wrong password" -> request.setAttribute("passwordLoginError", e.getMessage());
                case null, default -> {
                    request.setAttribute("errorMessage", "Error during login operation.");
                    targetJSP = "error.jsp";
                }
            }
        } catch (Exception e) {
            logger.error("Error during login operation.", e);
            targetJSP = "error.jsp";
        }

        // Forward in case of error
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User authenticatedUserDTO = SecurityUtils.getAuthenticatedUser(request);
        String targetJSP = request.getParameter("targetJSP") != null ? request.getParameter("targetJSP") : "tests/auth_test.jsp";
        if (authenticatedUserDTO != null) {
            request.getSession().removeAttribute(Constants.AUTHENTICATED_USER_KEY);
            request.getSession().invalidate();
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}