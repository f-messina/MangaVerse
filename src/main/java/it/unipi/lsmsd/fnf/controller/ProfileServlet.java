package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.*;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import it.unipi.lsmsd.fnf.utils.UserUtils;

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
    private static final PersonalListService personalListService = ServiceLocator.getPersonalListService();
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
        User authUser = SecurityUtils.getAuthenticatedUser(request);

        if (authUser == null) {
            response.sendRedirect("auth");
        } else switch (action) {
            case "update-info" -> handleUpdate(request, response);
            case "delete" -> handleDelete(request, response);
            case "add-list" -> handleAddList(request, response);
            case "delete-list" -> handleDeleteList(request, response);
            case "logout" -> handleLogout(request, response);
            case "delete-item" -> handleDeleteItem(request, response);
            case null, default -> {
                try {
                    request.setAttribute("lists", personalListService.findListsByUser(authUser.getId(), false));
                    request.setAttribute("reviews", reviewService.findByUser(authUser.getId(), 1));
                    logger.info("User reviews: " + reviewService.findByUser(authUser.getId(), 1));
                    request.setAttribute("likedAnime", mediaContentService.getLikedMediaContent(authUser.getId(), MediaContentType.ANIME));
                    request.setAttribute("likedManga", mediaContentService.getLikedMediaContent(authUser.getId(), MediaContentType.MANGA));
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
            User user = UserUtils.updateUserFromRequest(request);
            userService.updateUserInfo(user);
            UserUtils.updateUserSession(request);

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

    private void handleDeleteList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String listId = request.getParameter("listIdToRemove");
        try {
            personalListService.deleteList(listId);
            UserUtils.updateUserSession(request);
            response.sendRedirect("profile");
        } catch (BusinessException e) {
            logger.error("Error during delete list operation.", e);
            request.setAttribute("errorMessage", "Error during delete list operation.");
            request.getRequestDispatcher("error-page.jsp").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("home-page.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
String targetJSP = "homepage.jsp";
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleAddList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            User authUser = SecurityUtils.getAuthenticatedUser(request);
            String listName = request.getParameter("listName");

            personalListService.insertList(authUser.getId(), listName);
            PersonalList list = new PersonalList();
            list.setName(listName);
            authUser.addList(list);

            request.getSession().setAttribute(Constants.AUTHENTICATED_USER_KEY, authUser);
            response.sendRedirect("profile");

        } catch (BusinessException e) {
            logger.error("Error during add list operation.", e);
            request.setAttribute("errorMessage", "Error during add list operation.");
            request.getRequestDispatcher("error-page.jsp").forward(request, response);
        }
    }

    private void handleDeleteItem(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String listId = request.getParameter("listId");
        if (request.getParameter("mangaIdToRemove") != null) {
            String mangaId = request.getParameter("mangaIdToRemove");
            try {
                personalListService.removeFromList(listId, mangaId, MediaContentType.MANGA);
                UserUtils.updateUserSession(request);
                response.sendRedirect("profile");
            } catch (BusinessException e) {
                logger.error("Error during delete manga operation.", e);
                request.setAttribute("errorMessage", "Error during delete manga operation.");
                request.getRequestDispatcher("error-page.jsp").forward(request, response);
            }
        } else {
            String animeId = request.getParameter("animeIdToRemove");
            try {
                personalListService.removeFromList(listId, animeId, MediaContentType.ANIME);
                UserUtils.updateUserSession(request);
                response.sendRedirect("profile");
            } catch (BusinessException e) {
                logger.error("Error during delete anime operation.", e);
                request.setAttribute("errorMessage", "Error during delete anime operation.");
                request.getRequestDispatcher("error-page.jsp").forward(request, response);
            }
        }
    }
}
