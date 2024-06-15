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
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Servlet for handling user profile operations (edit, delete, follow, unfollow, get user reviews, rate app)
 * and loading the profile page.
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final UserService userService = ServiceLocator.getUserService();
    private static final ReviewService reviewService = ServiceLocator.getReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        switch (request.getParameter("action")) {
            case "editProfile" -> handleUpdate(request, response);
            case "deleteProfile" -> handleDelete(request, response);
            case "getReviews" -> handleGetReviewsByUserId(request, response);
            case "rateApp" -> handleRateApp(request, response);
            case "suggestedMediaContent" -> handleSuggestedMediaContent(request, response);
            case "suggestedUsers" -> handleSuggestedUsers(request, response);
            case null, default -> handleLoadPage(request, response);
        }
    }

    private void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String targetJSP = "WEB-INF/jsp/profile.jsp";
        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);

        // Redirect to the auth page if the user is not authenticated
        if (authUser == null && request.getParameter("userId") == null) {
            response.sendRedirect("auth");
            return;
        }
        try {
            User user;
            if (authUser != null) {
                String userId = request.getParameter("userId") == null ? authUser.getId() : request.getParameter("userId");
                boolean isCurrentUser = userId.equals(authUser.getId());
                boolean isFollowed = false;

                // Redirect to the manager page if the user is a manager and try to access is own profile (doesn't exist)
                if (isCurrentUser && authUser.getType().equals(UserType.MANAGER)) {
                    response.sendRedirect("manager");
                    return;
                }

                // Get the user by id showing private information if it's the current user
                user = userService.getUserById(userId, isCurrentUser);


                // Check if the logged user is following the user if it's not the same user and set the attribute
                if (!isCurrentUser) {
                    isFollowed = userService.isFollowing(authUser.getId(), userId);
                }
                request.setAttribute("isFollowed", isFollowed);

            } else {
                // Get the user by id hiding private information
                user = userService.getUserById(request.getParameter("userId"), false);
            }

            user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(), request));
            request.setAttribute("userInfo", user);

        } catch (BusinessException e) {
            targetJSP = "WEB-INF/jsp/error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    // Update the user information
    // REQUIRED PARAMETERS:     username, email, password, profilePicUrl
    // UPDATE:                  user in the database
    // RESPONSE:                JSON object with success flag
    //                          or error messages
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        User user = ConverterUtils.fromRequestToUser(request);
        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);

        try {
            // Check if the user is authorized to update the profile
            if (authUser == null || !user.getId().equals(authUser.getId())) {
                throw new NotAuthorizedException("Trying to update profile of another user.");
            }

            // Update the user in the database and set the flag to true if the user was updated
            userService.updateUserInfo(user);
            jsonResponse.put("success", true);

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

    // Delete the user
    // REQUIRED PARAMETERS:     reviewsIds
    // DELETE:                  user in the database
    // RESPONSE:                JSON object with success flag
    //                          or error messages
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        List<String> reviewsIds = Arrays.stream(objectMapper.readValue(request.getParameter("reviewsIds"), String[].class)).toList();

        try {
            // Check if the user is authorized to delete the profile
            SecurityUtils.isUserAuthorized(request, UserType.USER);

            // Delete the user in the database and set the flag to true if the user was deleted
            userService.deleteUser(authUser.getId(), reviewsIds);
            jsonResponse.put("success", true);

        } catch (BusinessException e) {
            jsonResponse.put("error", "An error occurred while deleting the profile. Please try again later.");

        } catch (NotAuthorizedException e) {
            jsonResponse.put("generalError", "You are not authorized to delete this profile.");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the reviews of a user by id
    // REQUIRED PARAMETERS:     userId, reviewsIds, page
    // RESPONSE:                JSON object with reviews page and success flag
    //                          or error messages
    private void handleGetReviewsByUserId(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        String userId = request.getParameter("userId");
        List<String> reviewIds = Arrays.stream(objectMapper.readValue(request.getParameter("reviewsIds"), String[].class)).toList();
        String pageString = request.getParameter("page");
        int page = 0;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }
        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);

        try {
            // Check if the user is authorized to view the reviews
            if (authUser == null || !userId.equals(authUser.getId())) {
                throw new NotAuthorizedException("Trying to view reviews of another user.");
            }

            // Get the page of reviews
            PageDTO<ReviewDTO> reviews = reviewService.getReviewsByIdsList(reviewIds, page, "user");

            // Convert the page to a JSON Object and set the success flag to true
            if (reviews == null || reviews.getTotalCount() == 0) {
                jsonResponse.put("notFoundError", true);

            } else {
                JsonNode reviewsJsonObject  = objectMapper.valueToTree(reviews);
                jsonResponse.set("reviews", reviewsJsonObject);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "An error occurred while getting the reviews. Please try again later.");
        }
         catch (NotAuthorizedException e) {
            jsonResponse.put("generalError", "You are not authorized to view these reviews.");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Rate the app
    // REQUIRED PARAMETERS:     rating
    // UPDATE:                  user in the database
    // RESPONSE:                JSON object with success flag
    //                          or error messages
    private void handleRateApp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        String ratingString = request.getParameter("rating");
        int rating = Integer.parseInt(ratingString);

        try {
            // Check if the user is authorized to rate the app
            SecurityUtils.isUserAuthorized(request, UserType.USER);

            // Rate the app and set the flag to true if the app was rated
            userService.rateApp(userId, rating);
            jsonResponse.put("success", true);

        } catch (BusinessException e) {
            jsonResponse.put("error", "An error occurred while rating the app. Please try again later.");

        } catch (NotAuthorizedException e) {
            jsonResponse.put("generalError", "User is not authorized to rate the app.");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the suggested media content
    // REQUIRED PARAMETERS:     criteria, type, value
    // RESPONSE:                JSON object with suggested media content and success flag
    //                          or error messages
    private void handleSuggestedMediaContent(HttpServletRequest request, HttpServletResponse response) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");
        String type = request.getParameter("type");
        String value = request.getParameter("value");

        if (type == null){
            jsonResponse.put("error", "Media content type not specified");
        } else {
            try {
                // Check if the user is authorized to get the suggested media content
                SecurityUtils.isUserAuthorized(request, UserType.USER);

                // Get the suggested media content and set the flag to true if the media content was found
                List<MediaContentDTO> suggestedMediaContent = reviewService.suggestMediaContent(type.equals("manga")?MediaContentType.MANGA:MediaContentType.ANIME, criteria, value);
                if (suggestedMediaContent == null) {
                    jsonResponse.put("notFoundError", true);

                } else {
                    JsonNode suggestedMediaContentJsonObject = objectMapper.valueToTree(suggestedMediaContent);
                    jsonResponse.set("suggestedMediaContent", suggestedMediaContentJsonObject);
                    jsonResponse.put("success", true);
                }

            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.DATABASE_ERROR))
                    jsonResponse.put("notFoundError", true);
                else
                    jsonResponse.put("error", "An error occurred while getting the suggested media content. Please try again later.");
            } catch (NotAuthorizedException e) {
                jsonResponse.put("generalError", "User is not authorized to get the suggested media content.");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the suggested users
    // REQUIRED PARAMETERS:     userId, suggestionType
    // RESPONSE:                JSON object with suggested users and success flag
    //                          or error messages
    private void handleSuggestedUsers(HttpServletRequest request, HttpServletResponse response) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String userId = request.getParameter("userId");
        String suggestionType = request.getParameter("suggestionType");
        if (userId == null){
            jsonResponse.put("error", "User Id not specified");

        } else if (suggestionType == null) {
            jsonResponse.put("error", "Suggestion type not specified");
        } else {
            try {
                // Check if the user is authorized to get the suggested users
                SecurityUtils.isUserAuthorized(request, UserType.USER);

                List<UserSummaryDTO> suggestedUsers = Collections.emptyList();
                // Get the suggested users by the specified type
                if (suggestionType.equals("following")) {
                    suggestedUsers = userService.suggestUsersByCommonFollowings(userId);

                // Get the suggested users by the specified type
                } else if (suggestionType.equals("likes")){
                    suggestedUsers = userService.suggestUsersByCommonLikes(userId);
                }

                // Set the flag to true if the suggested users were found
                if (suggestedUsers == null || suggestedUsers.isEmpty()) {
                    jsonResponse.put("notFoundError", true);

                } else {
                    JsonNode suggestedUsersJsonObject = objectMapper.valueToTree(suggestedUsers);
                    jsonResponse.set("suggestedUsers", suggestedUsersJsonObject);
                    jsonResponse.put("success", true);
                }

            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.DATABASE_ERROR))
                    jsonResponse.put("notFoundError", true);
                else
                    jsonResponse.put("error", "An error occurred while getting the suggested users. Please try again later.");

            } catch (NotAuthorizedException e) {
                jsonResponse.put("generalError", "User is not authorized to get the suggested users.");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
