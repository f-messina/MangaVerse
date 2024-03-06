package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
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
        String targetJSP = "WEB-INF/jsp/auth.jsp";

        User authUser = SecurityUtils.getAuthenticatedUser(request);
        if (authUser != null) {
            response.sendRedirect("profile");
        } else switch (action) {
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
            BusinessExceptionType type = e.getType();

            logger.error("BusinessException during signup operation.", e);
            targetJSP = "WEB-INF/jsp/auth.jsp";


            switch (type) {
                case BusinessExceptionType.TAKEN_EMAIL -> request.setAttribute("emailError", "Email already in use");
                case BusinessExceptionType.TAKEN_USERNAME-> request.setAttribute("usernameError", "Password already in use");
                case BusinessExceptionType.TAKEN_EMAIL_PSW-> {
                    request.setAttribute("emailError", "Email already in use.");
                    request.setAttribute("usernameError", "Username already in use.");
                }
                case BusinessExceptionType.EMPTY_USERNAME_PSW_EMAIL ->
                        request.setAttribute("errorMessage", "Username, password and email cannot be empty");
                case null, default -> {
                    request.setAttribute("errorMessage", "Error during signup operation.");
                    targetJSP = "error.jsp";
                }
            }
        } catch (Exception e) {
            logger.error("Error during signup operation.", e);
            targetJSP = "error.jsp";
        }
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
            response.sendRedirect("profile");
            return;
        } catch (BusinessException e) {
            logger.error("BusinessException during login operation.", e);
            targetJSP = "WEB-INF/jsp/auth.jsp";

            BusinessExceptionType type = e.getType();
            switch (type) {
                case BusinessExceptionType.INVALID_EMAIL -> request.setAttribute("emailLoginError", "Invalid Email");
                case BusinessExceptionType.WRONG_PSW -> request.setAttribute("passwordLoginError", "Wrong Pasword");
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
        String targetJSP = request.getParameter("targetJSP");
        if (authenticatedUserDTO != null) {
            request.getSession().removeAttribute(Constants.AUTHENTICATED_USER_KEY);
            request.getSession().invalidate();
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}
