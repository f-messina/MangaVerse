package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.service.*;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


// handle only async requests about users
@WebServlet("/user")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private static final UserService userService = ServiceLocator.getUserService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");

        switch (action) {
            case "getFollowers" -> handleGetFollowers(request, response);
            case "getFollowings" -> handleGetFollowings(request, response);
            case "getUsers" -> handleGetUsers(request, response);
            case null, default -> {

                // Write the JSON response with an error message
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"Invalid action\"}");
            }
        }
    }

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

            // Convert the list to a JSON array
            if (followers == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                for (UserSummaryDTO user : followers) {
                    user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(),request));
                }
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
        String loggedUserId = authUser == null ? null : authUser.getId();
        String userId = request.getParameter("userId");
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of followings
            List<UserSummaryDTO> followings = userService.searchFollowings(userId, searchValue, loggedUserId);

            // Convert the list to a JSON array
            if (followings == null) {
                jsonResponse.put("notFoundError", true);
            } else {
                for (UserSummaryDTO user : followings) {
                    user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(),request));
                }
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

    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO authUser = SecurityUtils.getAuthenticatedUser(request);
        String loggedUserId = authUser == null ? null : authUser.getId();
        String searchValue = request.getParameter("searchValue");

        try {
            // Get the list of users
            List<UserSummaryDTO> users;
            users = userService.searchFirstNUsers(searchValue, 10, loggedUserId);

            // Convert the list to a JSON array
            if (users == null || users.isEmpty()) {
                jsonResponse.put("notFoundError", true);
            } else {
                for (UserSummaryDTO user : users) {
                    user.setProfilePicUrl(ConverterUtils.getProfilePictureUrlOrDefault(user.getProfilePicUrl(),request));
                }

                ArrayNode usersJsonArray = objectMapper.valueToTree(users);

                // Add the JSON array to the response
                jsonResponse.set("users", usersJsonArray);
                jsonResponse.put("success", true);
            }
        } catch (BusinessException e) {
            if (e.getType() == BusinessExceptionType.NOT_FOUND)
                jsonResponse.put("notFoundError", true);
            else
                jsonResponse.put("error", e.getMessage());
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
