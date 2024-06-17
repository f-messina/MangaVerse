package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unipi.lsmsd.fnf.controller.exception.NotAuthorizedException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * Servlet for handling user operations such as getting followers, followings, users,
 * following and unfollowing users and getting liked anime and manga.
 */
@WebServlet("/user")
public class UserServlet extends HttpServlet {
    private static final UserService userService = ServiceLocator.getUserService();
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (request.getParameter("action")) {
            case "getFollowers" -> handleGetFollowers(request, response);
            case "getFollowings" -> handleGetFollowings(request, response);
            case "getUsers" -> handleGetUsers(request, response);
            case "follow" -> handleFollow(request, response);
            case "unfollow" -> handleUnfollow(request, response);
            case "getAnimeLikes" -> handleGetAnimeLikes(request, response);
            case "getMangaLikes" -> handleGetMangaLikes(request, response);
            case null, default -> {
                // Write the JSON response with an error message
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"Invalid action\"}");
            }
        }
    }

    // Get the list of followers of a user
    // REQUEST PARAMETERS:      userId, searchValue
    // RESPONSE:                JSON object with a list of followers and a success flag
    //                          or an error message
    private void handleGetFollowers(HttpServletRequest request, HttpServletResponse response) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        String loggedUserId = authUser == null ? null : authUser.getId();
        String userId = request.getParameter("userId");
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of followers
            List<UserSummaryDTO> followers = userService.searchFollowers(userId, searchValue, loggedUserId);

            // Add the list of followers and a success flag to the JSON response if the list is not empty
            if (followers == null || followers.isEmpty()) {
                jsonResponse.put("notFoundError", true);

            } else {
                for (UserSummaryDTO user : followers) {
                    user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(),request));
                }
                ArrayNode followersJsonArray = objectMapper.valueToTree(followers);
                jsonResponse.set("followers", followersJsonArray);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error getting followers");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the list of followings of a user
    // REQUEST PARAMETERS:      userId, searchValue
    // RESPONSE:                JSON object with a list of followings and a success flag
    //                          or an error message
    private void handleGetFollowings(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        String loggedUserId = authUser == null ? null : authUser.getId();
        String userId = request.getParameter("userId");
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of followings
            List<UserSummaryDTO> followings = userService.searchFollowings(userId, searchValue, loggedUserId);

            // Add the list of followings and a success flag to the JSON response if the list is not empty
            if (followings == null) {
                jsonResponse.put("notFoundError", true);

            } else {
                for (UserSummaryDTO user : followings) {
                    user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(),request));
                }
                ArrayNode followingsJsonArray = objectMapper.valueToTree(followings);
                jsonResponse.set("followings", followingsJsonArray);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error getting followings");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the list of users that match the search value
    // REQUEST PARAMETERS:      searchValue
    // RESPONSE:                JSON object with a list of users and a success flag
    //                          or an error message
    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        String loggedUserId = authUser == null ? null : authUser.getId();
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of users
            List<UserSummaryDTO> users = userService.searchFirstNUsers(searchValue, 10, loggedUserId);

            // Add the list of users and a success flag to the JSON response if the list is not empty
            if (users == null || users.isEmpty()) {
                jsonResponse.put("notFoundError", true);

            } else {
                for (UserSummaryDTO user : users) {
                    user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(),request));
                }
                ArrayNode usersJsonArray = objectMapper.valueToTree(users);
                jsonResponse.set("users", usersJsonArray);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            if (e.getType() == BusinessExceptionType.NOT_FOUND)
                jsonResponse.put("notFoundError", true);
            else
                jsonResponse.put("error", "Error getting users");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Follow a user
    // REQUEST PARAMETERS:      userId
    // RESPONSE:                JSON object with a success flag
    //                          or an error message
    private void handleFollow(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        try {
            // Check if the user is authorized to follow another user
            SecurityUtils.isUserAuthorized(request, UserType.USER);
            String followerId = SecurityUtils.getAuthenticatedUser(request).getId();
            String followedId = request.getParameter("userId");
            if (StringUtils.equals(followerId, followedId)) {
                throw new NotAuthorizedException("User cannot follow themselves");
            }

            // Follow the user and add a success flag to the JSON response
            userService.follow(followerId, followedId);
            jsonResponse.put("success", true);

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error following user");

        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User not authorized to perform this action");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Unfollow a user
    // REQUEST PARAMETERS:      userId
    // RESPONSE:                JSON object with a success flag
    //                          or an error message
    private void handleUnfollow(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        try {
            // Check if the user is authorized to unfollow another user
            SecurityUtils.isUserAuthorized(request, UserType.USER);
            String followerId = SecurityUtils.getAuthenticatedUser(request).getId();
            String followedId = request.getParameter("userId");

            // Unfollow the user and add a success flag to the JSON response
            userService.unfollow(followerId, followedId);
            jsonResponse.put("success", true);

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error unfollowing user");

        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User not authorized to perform this action");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the list of anime likes of a user
    // REQUEST PARAMETERS:      userId, page
    // RESPONSE:                JSON object with a list of anime likes and a success flag
    //                          or an error message
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

            // Add the list of anime likes and a success flag to the JSON response if the list is not empty
            if (animeLikes == null) {
                jsonResponse.put("notFoundError", true);

            } else {
                JsonNode animeLikesJsonObject = objectMapper.valueToTree(animeLikes);
                jsonResponse.set("mediaLikes", animeLikesJsonObject);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error getting anime likes");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the list of manga likes of a user
    // REQUEST PARAMETERS:      userId, page
    // RESPONSE:                JSON object with a list of manga likes and a success flag
    //                          or an error message
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

            // Add the list of manga likes and a success flag to the JSON response if the list is not empty
            if (mangaLikes == null) {
                jsonResponse.put("notFoundError", true);

            } else {
                JsonNode mangaLikesJsonObject = objectMapper.valueToTree(mangaLikes);
                jsonResponse.set("mediaLikes", mangaLikesJsonObject);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error getting manga likes");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
