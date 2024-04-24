package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.*;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private static final UserService userService = ServiceLocator.getUserService();
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final ReviewService reviewService = ServiceLocator.getReviewService();

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
        String targetJSP = "WEB-INF/jsp/profile.jsp";
        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);

        if (authUser == null) {
            response.sendRedirect("auth");
        } else switch (action) {
            case "update-info" -> handleUpdate(request, response);
            case "delete" -> handleDelete(request, response);
            case "logout" -> handleLogout(request, response);
            case null, default -> {
                try {
                    request.setAttribute("userInfo", userService.getUserById(authUser.getId()));
                } catch (BusinessException e) {
                    logger.error("Error during find lists by user operation.", e);
                    request.setAttribute("errorMessage", "Error during find lists by user operation.");
                    targetJSP = "error-page.jsp";
                }
                request.getRequestDispatcher(targetJSP).forward(request, response);
            }
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetJSP;
        try {
            User user = new User();
            userService.updateUserInfo(user);

            // Redirect to a different URL after successful update
            targetJSP = request.getContextPath() + "/profile?updateSuccess=true";
            response.sendRedirect(targetJSP);
            return;
        } catch (BusinessException e) {
            BusinessExceptionType type = e.getType();
            if (BusinessExceptionType.DUPLICATED_USERNAME.equals(type)) {
                request.setAttribute("usernameError", "Username is already in use");
            } else {
                handleUpdateError(request, "Invalid input. Please check your data.", e);
            }
            targetJSP = "WEB-INF/jsp/profile.jsp";
        } catch (Exception e) {
            handleUpdateError(request, "Error during update operation.", e);
            targetJSP = "error-page.jsp";
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleUpdateError(HttpServletRequest request, String errorMessage, Exception e) {
        logger.error(errorMessage, e);
        request.setAttribute("errorMessage", errorMessage);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("home-page.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String targetJSP = "homepage.jsp";
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}
