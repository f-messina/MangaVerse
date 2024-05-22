package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.*;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
        logger.info("Action profile: " + action);
        if (authUser == null) {
            response.sendRedirect("auth");
        } else switch (action) {
            case "edit-profile" -> handleUpdate(request, response);
            case "getAnimeLikes" -> handleGetAnimeLikes(request, response, authUser);
            case "getMangaLikes" -> handleGetMangaLikes(request, response, authUser);
            case "getReviews" -> handleGetReviews(request, response, authUser);
            case null, default -> {
                try {
                    request.setAttribute("userInfo", userService.getUserById(authUser.getId()));
                } catch (BusinessException e) {
                    targetJSP = "error-page.jsp";
                }
                request.getRequestDispatcher(targetJSP).forward(request, response);
            }
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            User user = ConverterUtils.fromRequestToUser(request);
            userService.updateUserInfo(user);

            HttpSession session = request.getSession(true);
            LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
            authUser.setUsername(user.getUsername());
            authUser.setProfilePicUrl(user.getProfilePicUrl() == null ? Constants.DEFAULT_PROFILE_PICTURE : user.getProfilePicUrl());
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, authUser);

            // Set the success flag in the JSON response
            jsonResponse.put("success", true);

        } catch (BusinessException e) {
            switch (e.getType()) {
                case DUPLICATED_USERNAME -> jsonResponse.put("usernameError", "Username already in use. Please choose another one.");
                case NO_CHANGE -> jsonResponse.put("generalError", "No changes were made to the profile.");
                default -> jsonResponse.put("generalError", "An error occurred while updating the profile. Please try again later.");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
    private void handleGetAnimeLikes(HttpServletRequest request, HttpServletResponse response, LoggedUserDTO authUser) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            List<? extends MediaContentDTO> animeLikes = mediaContentService.getLikedMediaContent(authUser.getId(), MediaContentType.ANIME);
            jsonResponse.put("animeLikes", objectMapper.valueToTree(animeLikes));

            HttpSession session = request.getSession(true);
            jsonResponse.put("success", true);
            session.setAttribute("animeLikes", animeLikes);

        } catch (BusinessException e) {
            if (Objects.requireNonNull(e.getType()) == BusinessExceptionType.NO_LIKES) {
                jsonResponse.put("noLikes", "You have not liked any anime yet.");
            } else {
                jsonResponse.put("generalError", "An error occurred while retrieving the liked anime. Please try again later.");
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());



    }

    private void handleGetMangaLikes(HttpServletRequest request, HttpServletResponse response, LoggedUserDTO authUser) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            List<? extends MediaContentDTO> mangaLikes = mediaContentService.getLikedMediaContent(authUser.getId(), MediaContentType.MANGA);
            jsonResponse.put("animeLikes", objectMapper.valueToTree(mangaLikes));

            HttpSession session = request.getSession(true);
            jsonResponse.put("success", true);
            session.setAttribute("mangaLikes", mangaLikes);

        } catch (BusinessException e) {
            if (Objects.requireNonNull(e.getType()) == BusinessExceptionType.NO_LIKES) {
                jsonResponse.put("noLikes", "You have not liked any manga yet.");
            } else {
                jsonResponse.put("generalError", "An error occurred while retrieving the liked manga. Please try again later.");
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

    }
    private void handleGetReviews(HttpServletRequest request, HttpServletResponse response, LoggedUserDTO authUser) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            PageDTO<ReviewDTO> reviews = reviewService.findByUser(authUser.getId(), 1);
            jsonResponse.put("reviews", objectMapper.valueToTree(reviews));

            HttpSession session = request.getSession(true);
            jsonResponse.put("success", true);
            session.setAttribute("reviews", reviews);

        } catch (BusinessException e) {
            if (Objects.requireNonNull(e.getType()) == BusinessExceptionType.NO_REVIEWS) {
                jsonResponse.put("noReviews", "You have not reviewed any media yet.");
            } else {
                jsonResponse.put("generalError", "An error occurred while retrieving the reviews. Please try again later.");
            }
        }
    }
}
