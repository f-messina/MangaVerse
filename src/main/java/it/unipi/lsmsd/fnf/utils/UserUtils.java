package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtils {
    /**
     * Updates the user session based on the action performed in the request.
     * @param request The HttpServletRequest containing the current request.
     */
    public static void updateUserSession(HttpServletRequest request) {
        User authUserUpdated = SecurityUtils.getAuthenticatedUser(request);

        switch (request.getParameter("action")) {
            case "delete-list" -> handleRemoveList(authUserUpdated, request.getParameter("listIdToRemove"));
            case "delete-item" -> handleRemoveItemFromList(authUserUpdated, request.getParameter("listId"), request);
            case "update-info" -> handleUserInfo(authUserUpdated, request);
            case "toggleLike" -> handleToggleLike(authUserUpdated, request);
        }

        request.getSession().setAttribute(Constants.AUTHENTICATED_USER_KEY, authUserUpdated);
    }

    /**
     * Handles the removal of a personal list from the user's lists.
     * @param authUserUpdated The user object to be updated.
     * @param listIdToRemove The ID of the list to be removed.
     */
    private static void handleRemoveList(User authUserUpdated, String listIdToRemove) {
        authUserUpdated.removeList(listIdToRemove);
    }

    /**
     * Handles the removal of an item from a personal list.
     * @param authUserUpdated The user object to be updated.
     * @param listId The ID of the list containing the item to be removed.
     * @param request The HttpServletRequest containing the current request.
     */
    private static void handleRemoveItemFromList(User authUserUpdated, String listId, HttpServletRequest request) {
        authUserUpdated.getLists().stream()
                .filter(list -> list.getId().equals(listId))
                .findFirst()
                .ifPresent(list -> {
                    Optional.ofNullable(request.getParameter("mangaIdToRemove")).map(ObjectId::new).ifPresent(list::removeManga);
                    Optional.ofNullable(request.getParameter("animeIdToRemove")).map(ObjectId::new).ifPresent(list::removeAnime);
                });
    }

    /**
     * Handles the update of user information.
     * @param authUserUpdated The user object to be updated.
     * @param request The HttpServletRequest containing the current request.
     */
    private static void handleUserInfo(User authUserUpdated, HttpServletRequest request) {
        Optional.ofNullable(request.getParameter("username")).ifPresent(authUserUpdated::setUsername);
        Optional.ofNullable(request.getParameter("description")).ifPresent(authUserUpdated::setDescription);
        Optional.ofNullable(request.getParameter("country")).ifPresent(authUserUpdated::setLocation);
        Optional.ofNullable(request.getParameter("birthdate"))
                .filter(StringUtils::isNotBlank)
                .map(LocalDate::parse)
                .ifPresent(authUserUpdated::setBirthday);
        Optional.ofNullable(request.getParameter("fullname")).ifPresent(authUserUpdated::setFullname);
        Optional.ofNullable(request.getParameter("gender")).map(Gender::valueOf).ifPresent(authUserUpdated::setGender);
    }

    /**
     * Handles the toggling of a like on a media content item.
     * @param authUserUpdated The user object to be updated.
     * @param request The HttpServletRequest containing the current request.
     */
    private static void handleToggleLike(User authUserUpdated, HttpServletRequest request) {
        if((boolean) request.getAttribute("isLiked")) {
            MediaContent mediaContent = (boolean) request.getAttribute("isManga") ? new Manga() : new Anime();
            mediaContent.setId(request.getParameter("mediaId"));
            mediaContent.setTitle(request.getParameter("mediaTitle"));
            mediaContent.setImageUrl(request.getParameter("mediaImageUrl"));
            authUserUpdated.addLikedMediaContent(mediaContent);
        } else {
            authUserUpdated.removeLikedMediaContent(request.getParameter("mediaId"));
        }
    }

    /**
     * Updates the user information based on the request parameters.
     * @param request The HttpServletRequest containing the current request.
     * @return The updated User object.
     */
    public static User updateUserFromRequest(HttpServletRequest request) {
        User authUser = SecurityUtils.getAuthenticatedUser(request);
        User user = new User();
        user.setId(authUser.getId());

        String[] attributeNames = {"username", "description", "country", "birthdate", "fullname", "gender"};

        for (String attributeName : attributeNames) {
            String requestValue = request.getParameter(attributeName);
            String authUserValue = getAuthUserValue(attributeName, authUser);

            if (!Objects.equals(requestValue, authUserValue)) {
                switch (attributeName) {
                    case "username" -> user.setUsername(requestValue);
                    case "description" -> user.setDescription(StringUtils.isNotBlank(requestValue) ? requestValue : Constants.NULL_STRING);
                    case "country" -> user.setLocation(StringUtils.isNotBlank(requestValue) ? requestValue : Constants.NULL_STRING);
                    case "birthdate" -> user.setBirthday(StringUtils.isNotBlank(requestValue) ? LocalDate.parse(requestValue) : Constants.NULL_DATE);
                    case "fullname" -> user.setFullname(StringUtils.isNotBlank(requestValue) ? requestValue : Constants.NULL_STRING);
                    case "gender" -> user.setGender(Gender.valueOf(requestValue));
                }
            }
        }

        return user;
    }

    /**
     * Retrieves the value of a specific attribute from the authenticated user.
     * @param parameterName The name of the attribute.
     * @param authUser The authenticated user.
     * @return The value of the specified attribute.
     */
    private static String getAuthUserValue(String parameterName, User authUser) {
        return switch (parameterName) {
            case "username" -> authUser.getUsername();
            case "description" -> authUser.getDescription();
            case "country" -> authUser.getLocation();
            case "birthdate" -> authUser.getBirthday() != null ? authUser.getBirthday().toString() : null;
            case "fullname" -> authUser.getFullname();
            case "gender" -> authUser.getGender().name();
            default -> null;
        };
    }

    /**
     * Checks if the current user has liked the media content specified in the request.
     * @param request The HttpServletRequest containing the current request.
     * @return true if the user has liked the media content, false otherwise.
     */
    public static boolean isLiked(HttpServletRequest request) {
        return SecurityUtils.getAuthenticatedUser(request).getLikedMediaContent().stream()
                .anyMatch(mediaContent -> mediaContent.getId().equals(request.getParameter("mediaId")));
    }
}
