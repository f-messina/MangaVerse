package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
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
import it.unipi.lsmsd.fnf.service.interfaces.UserService;

import java.io.IOException;
import java.util.Objects;

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

        UserSummaryDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        if (authUser != null) {
            if (Objects.equals(action, "logout"))
                handleLogout(request, response);
            else
                response.sendRedirect("profile");
        } else switch (action) {
            case "signup" -> handleSignUp(request, response);
            case "login" -> handleLogin(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    private void handleSignUp(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetJSP;
        try {
            UserRegistrationDTO user = ConverterUtils.fromRequestToUserRegDTO(request);
            userService.registerUserAndLogin(ConverterUtils.fromRequestToUserRegDTO(request));

            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, new UserSummaryDTO(user.getId(), user.getUsername(), Constants.DEFAULT_PROFILE_PICTURE));
            response.sendRedirect("profile");
            return;
        } catch (BusinessException e) {
            BusinessExceptionType type = e.getType();

            logger.error("BusinessException during signup operation.", e);
            targetJSP = "WEB-INF/jsp/auth.jsp";


            switch (type) {
                case BusinessExceptionType.DUPLICATED_EMAIL -> request.setAttribute("emailError", "Email already in use");
                case BusinessExceptionType.DUPLICATED_USERNAME-> request.setAttribute("usernameError", "Password already in use");
                case BusinessExceptionType.DUPLICATED_KEY-> {
                    request.setAttribute("emailError", "Email already in use.");
                    request.setAttribute("usernameError", "Username already in use.");
                }
                case BusinessExceptionType.EMPTY_FIELDS ->
                        request.setAttribute("errorMessage", "Username, password and email cannot be empty");
                case null, default -> {
                    request.setAttribute("errorMessage", "Error during signup operation.");
                    targetJSP = "error.jsp";
                }
            }
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String targetJSP;

        try {
            UserSummaryDTO userSummaryDTO = userService.login(email, password);
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, userSummaryDTO);
            response.sendRedirect("mainPage/manga");
            return;
        } catch (BusinessException e) {
            logger.error("BusinessException during login operation.", e);

            if (e.getType() == BusinessExceptionType.AUTHENTICATION_ERROR) {
                request.setAttribute("Authentication Error", "Invalid email or password");
                targetJSP = "WEB-INF/jsp/auth.jsp";
            } else {
                request.setAttribute("errorMessage", "Error during login operation.");
                targetJSP = "error.jsp";
            }
        }

        // Forward in case of error
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetServlet = request.getParameter("targetServlet");
        if (SecurityUtils.getAuthenticatedUser(request) != null) {
            request.getSession().removeAttribute(Constants.AUTHENTICATED_USER_KEY);
            request.getSession().invalidate();
        }
        request.getRequestDispatcher(targetServlet).forward(request, response);
    }
}
