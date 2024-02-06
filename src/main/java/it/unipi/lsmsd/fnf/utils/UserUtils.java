package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.enums.Gender;
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

    public static void updateUserSession(HttpServletRequest request) {
        User authUserUpdated = SecurityUtils.getAuthenticatedUser(request);

        if (request.getParameter("listIdToRemove") != null) {
            handleRemoveList(authUserUpdated, request.getParameter("listIdToRemove"));
        } else if (request.getParameter("listId") != null) {
            handleRemoveItemFromList(authUserUpdated, request.getParameter("listId"), request);
        } else {
            handleUserInfo(authUserUpdated, request);
        }

        request.getSession().setAttribute(Constants.AUTHENTICATED_USER_KEY, authUserUpdated);
    }

    private static void handleRemoveList(User authUserUpdated, String listIdToRemove) {
        ObjectId listId = new ObjectId(listIdToRemove);
        authUserUpdated.removeList(listId);
    }

    private static void handleRemoveItemFromList(User authUserUpdated, String listId, HttpServletRequest request) {
        ObjectId objectId = new ObjectId(listId);
        authUserUpdated.getLists().stream()
                .filter(list -> list.getId().equals(objectId))
                .findFirst()
                .ifPresent(list -> {
                    Optional.ofNullable(request.getParameter("mangaIdToRemove")).map(ObjectId::new).ifPresent(list::removeManga);
                    Optional.ofNullable(request.getParameter("animeIdToRemove")).map(ObjectId::new).ifPresent(list::removeAnime);
                });
    }

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

        Logger logger = LoggerFactory.getLogger(UserUtils.class);
        logger.info("User updated: " + user);
        return user;
    }

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
}
