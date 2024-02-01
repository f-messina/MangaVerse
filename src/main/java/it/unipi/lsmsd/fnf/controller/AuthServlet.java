package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
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
import java.time.LocalDate;

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
        String targetJSP = "index.jsp";
        switch (action) {
            case "signup" -> handleSignUp(request, response);
            case "login" -> handleLogin(request, response);
            case "logout" -> handleLogout(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    private void handleSignUp(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User user = null;
        String targetJSP = "index.jsp";

        try {
            UserRegistrationDTO userRegistrationDTO = ConverterUtils.fromRequestToUserRegDTO(request);
            user = userService.registerUserAndLogin(userRegistrationDTO);
        } catch (Exception e) {
            logger.error("Error during signup operation.",e);
            response.sendRedirect(targetJSP);
        }
        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, user);
            targetJSP = "main--registered-user.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String targetJSP = "index.jsp";

        try {
            if (email != null && password != null && !email.isEmpty() && !password.isEmpty()){
                RegisteredUser registeredUser = userService.login(email, password);
                if (registeredUser != null) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute(Constants.AUTHENTICATED_USER_KEY, registeredUser);
                    if (registeredUser instanceof User)
                        targetJSP = "main--registered-user.jsp";
                    else if (registeredUser instanceof Manager)
                        targetJSP = "main--manager.jsp";
                }
            } else {
                request.setAttribute("errorMessage", "Invalid email or password.");
            }
        } catch (Exception e) {
            logger.error("Error during login operation.",e);
            request.setAttribute("errorMessage", "Invalid email or password.");
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User authenticatedUserDTO = SecurityUtils.getAuthenticatedUser(request);
        String targetJSP = "index.jsp";
        if (authenticatedUserDTO != null) {
            request.getSession().removeAttribute(Constants.AUTHENTICATED_USER_KEY);
            request.getSession().invalidate();
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}