package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

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
        String userId = request.getParameter("userId");

        if (authUser == null) {
            response.sendRedirect("auth");
        } else switch (action) {
            case "edit-profile" -> handleUpdate(request, response);
            case "getFollowers" -> handleGetFollowers(request, response);
            case "getFollowings" -> handleGetFollowings(request, response);
            case "getAnimeLikes" -> handleGetAnimeLikes(request, response);
            case "getMangaLikes" -> handleGetMangaLikes(request, response);
            case "getReviews" -> handleGetReviews(request, response);
            case null, default -> {
                try {
                    request.setAttribute("userInfo", userService.getUserById(authUser.getId()));
                    request.setAttribute("mangaLikes", mediaContentService.getLikedMediaContent(userId, 0, MediaContentType.MANGA));
                } catch (BusinessException e) {
                    targetJSP = "error-page.jsp";
                }
                request.getRequestDispatcher(targetJSP).forward(request, response);
            }
        }
    }


    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

    private void handleGetFollowers(HttpServletRequest request, HttpServletResponse response) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        String userId = request.getParameter("userId");
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of followers
            List<UserSummaryDTO> followers;
            if (StringUtils.isNotBlank(searchValue)) {
                followers = userService.searchFollowers(userId, searchValue, authUser.getId());
            } else {
                followers = userService.getFollowers(userId, authUser.getId());
            }

            // Convert the list to a JSON array
            if (followers == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                ArrayNode followersJsonArray = objectMapper.valueToTree(followers);

                // Add the JSON array to the response
                jsonResponse.set("followers", followersJsonArray);
                jsonResponse.put("success", true);
            }
        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleGetFollowings(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        String userId = request.getParameter("userId");
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of followings
            List<UserSummaryDTO> followings;
            if (StringUtils.isNotBlank(searchValue)) {
                followings = userService.searchFollowings(userId, searchValue, authUser.getId());
            } else {
                followings = userService.getFollowings(userId, authUser.getId());
            }

            // Convert the list to a JSON array
            if (followings == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                ArrayNode followingsJsonArray = objectMapper.valueToTree(followings);

                // Add the JSON array to the response
                jsonResponse.set("followings", followingsJsonArray);
                jsonResponse.put("success", true);
            }
        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleGetAnimeLikes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String userId = request.getParameter("userId");
        String pageString = request.getParameter("page");
        int page = 0;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }

        try {
            // Get the list of anime likes
            List<AnimeDTO> animeLikes = (List<AnimeDTO>) mediaContentService.getLikedMediaContent(userId, page, MediaContentType.ANIME);

            // Convert the list to a JSON array
            if (animeLikes == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                ArrayNode animeLikesJsonArray = objectMapper.valueToTree(animeLikes);

                // Add the JSON array to the response
                jsonResponse.set("mediaLikes", animeLikesJsonArray);
                jsonResponse.put("success", true);
            }
        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleGetMangaLikes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String userId = request.getParameter("userId");
        String pageString = request.getParameter("page");
        int page = 0;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }
        try {
            // Get the list of manga likes
            List<MangaDTO> mangaLikes = (List<MangaDTO>) mediaContentService.getLikedMediaContent(userId, page, MediaContentType.MANGA);

            // Convert the list to a JSON array
            if (mangaLikes == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                ArrayNode mangaLikesJsonArray = objectMapper.valueToTree(mangaLikes);

                // Add the JSON array to the response
                jsonResponse.set("mediaLikes", mangaLikesJsonArray);
                jsonResponse.put("success", true);
            }
        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleGetReviews(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        objectMapper.registerModule(new JavaTimeModule());

        String userId = request.getParameter("userId");
        String pageString = request.getParameter("page");
        int page = 0;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }
        try {
            // Get the list of reviews
            PageDTO<ReviewDTO> reviews = reviewService.findByUser(userId, page);
            logger.info("Reviews: " + reviews);

            // Convert the list to a JSON array
            if (reviews == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                ArrayNode reviewsJsonArray = objectMapper.valueToTree(reviews);

                // Add the JSON array to the response
                jsonResponse.set("reviews", reviewsJsonArray);
                jsonResponse.put("success", true);
            }
        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
