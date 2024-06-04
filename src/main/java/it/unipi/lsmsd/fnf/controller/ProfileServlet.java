package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import it.unipi.lsmsd.fnf.controller.exception.NotAuthorizedException;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
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

        switch (action) {
            case "editProfile" -> handleUpdate(request, response);
            case "follow" -> handleFollow(request, response);
            case "unfollow" -> handleUnfollow(request, response);
            case "getAnimeLikes" -> handleGetAnimeLikes(request, response);
            case "getMangaLikes" -> handleGetMangaLikes(request, response);
            case "getReviews" -> handleGetReviews(request, response);
            case null, default -> {
                String targetJSP = "WEB-INF/jsp/profile.jsp";
                String userId = request.getParameter("userId");
                try {
                    LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
                    if (authUser == null && userId == null) {
                        targetJSP = "WEB-INF/jsp/auth.jsp";
                    } else {
                        if (authUser != null && userId == null) {
                            userId = authUser.getId();
                        } else if (authUser != null && !userId.equals(authUser.getId())) {
                            request.setAttribute("isFollowed", userService.isFollowing(authUser.getId(), userId));
                        }
                        User user = userService.getUserById(userId);
                        user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(), request));
                        request.setAttribute("userInfo", user);
                    }
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
            LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);

            if (authUser == null || !user.getId().equals(authUser.getId())) {
                throw new NotAuthorizedException("Trying to update profile without being logged in.");
            }
            userService.updateUserInfo(user);

            // Update the user in the session
            if (user.getUsername() != null) {
                authUser.setUsername(user.getUsername());
            }
            if (user.getProfilePicUrl() != null) {
                authUser.setProfilePicUrl(Objects.equals(user.getProfilePicUrl(), Constants.NULL_STRING) ?
                        ConverterUtils.getProfilePictureUrlOrDefault(null, request) : user.getProfilePicUrl());
            }

            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.AUTHENTICATED_USER_KEY, authUser);

            // Set the success flag in the JSON response
            jsonResponse.put("success", true);

        } catch (NotAuthorizedException e) {
            jsonResponse.put("generalError", "You are not authorized to update this profile.");
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

    private void handleFollow(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String followerId = SecurityUtils.getAuthenticatedUser(request).getId();
        String followedId = request.getParameter("userId");

        try {
            userService.follow(followerId, followedId);
            jsonResponse.put("success", true);
        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleUnfollow(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String followerId = SecurityUtils.getAuthenticatedUser(request).getId();
        String followedId = request.getParameter("userId");

        try {
            userService.unfollow(followerId, followedId);
            jsonResponse.put("success", true);
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
            // Get the page of anime likes
            PageDTO<MediaContentDTO> animeLikes = mediaContentService.getLikedMediaContent(userId, page, MediaContentType.ANIME);

            // Convert the page to a JSON Object
            if (animeLikes == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                JsonNode animeLikesJsonObject = objectMapper.valueToTree(animeLikes);
                // Add the JSON array to the response
                jsonResponse.set("mediaLikes", animeLikesJsonObject);
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
            // Get the page of manga likes
            PageDTO<MediaContentDTO> mangaLikes = mediaContentService.getLikedMediaContent(userId, page, MediaContentType.MANGA);

            // Convert the page to a JSON Object
            if (mangaLikes == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                JsonNode mangaLikesJsonObject = objectMapper.valueToTree(mangaLikes);

                // Add the JSON array to the response
                jsonResponse.set("mediaLikes", mangaLikesJsonObject);
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

        // Create a module to handle the serialization and deserialization of LocalDate and LocalDateTime objects
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        // Register the formatters for serialization
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        // Register the formatters for deserialization
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        // Register the module with the ObjectMapper
        objectMapper.registerModule(javaTimeModule);

        List<String> reviewIds = Collections.singletonList(request.getParameter("review_ids"));
        String pageString = request.getParameter("page");
        int page = 0;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }

        try {
            LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);

            if (authUser == null || !reviewIds.equals(authUser.getId())) {
                throw new NotAuthorizedException("Trying to update profile without being logged in.");
            }
            // Get the page of reviews
            PageDTO<ReviewDTO> reviews = reviewService.findByUser(reviewIds, page);

            // Convert the page to a JSON Object
            if (reviews == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                JsonNode reviewsJsonObject  = objectMapper.valueToTree(reviews);

                // Add the JSON array to the response
                jsonResponse.set("reviews", reviewsJsonObject);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", e.getMessage());
        } catch (NotAuthorizedException e) {
            jsonResponse.put("generalError", "You are not authorized to view these reviews.");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
