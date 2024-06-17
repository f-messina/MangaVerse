package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.UpdateMediaRedundancyTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks.RemoveDeletedUserReviewsTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks.UpdateReviewRedundancyTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks.*;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.getUserDAO;
import static it.unipi.lsmsd.fnf.service.ServiceLocator.getExecutorTaskService;
import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

/**
 * Implementation of the UserService interface.
 * It uses DAOs to interact with the data repository and ExecutorTaskService to execute tasks in the background.
 * The methods access to User entity in the database, provide
 * operations to maintain consistency between collections, search functionality,
 * operations to get statistics and operations to get user suggestions.
 * The methods, in general, execute a single DAO method. When needed to maintain
 * eventual consistency between collections, the methods execute multiple DAO methods,
 * executing the consistency operations in an asynchronous way.
 * @see UserService
 * @see UserDAO
 * @see User
 * @see UserRegistrationDTO
 * @see LoggedUserDTO
 * @see UserSummaryDTO
 */
public class UserServiceImpl implements UserService {

    private static final UserDAO userDAO;
    private static final UserDAO userDAONeo4J;
    private static final ExecutorTaskService aperiodicExecutorTaskService;

    static {
        userDAO = getUserDAO(DataRepositoryEnum.MONGODB);
        userDAONeo4J = getUserDAO(DataRepositoryEnum.NEO4J);
        aperiodicExecutorTaskService = getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
    }

    /**
     * Signs up a new user.
     * The method creates a new user in the database and executes
     * a task to add a new node User in Neo4j.
     *
     * @param userRegistrationDTO   The user registration information.
     * @throws BusinessException    If an error occurs during the signup process.
     */
    @Override
    public void signup(UserRegistrationDTO userRegistrationDTO) throws BusinessException {
        try {
            // Validation checks for empty fields
            if (StringUtils.isAnyEmpty(
                    userRegistrationDTO.getUsername(),
                    userRegistrationDTO.getPassword(),
                    userRegistrationDTO.getEmail()
            )) {
                throw new IllegalArgumentException("Username, password and email cannot be empty");
            }

            // Save the user in the database
            userDAO.saveUser(userRegistrationDTO);

            // Create a task which adds a new node User in Neo4j
            aperiodicExecutorTaskService.executeTask(new CreateUserTask(userRegistrationDTO));

        } catch (IllegalArgumentException e) {
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS, e.getMessage());

        } catch (DAOException e) {
            switch (e.getType()) {
                case DUPLICATED_EMAIL -> throw new BusinessException(BusinessExceptionType.DUPLICATED_EMAIL,e.getMessage());
                case DUPLICATED_USERNAME -> throw new BusinessException(BusinessExceptionType.DUPLICATED_USERNAME, e.getMessage());
                case DUPLICATED_KEY -> throw new BusinessException(BusinessExceptionType.DUPLICATED_KEY, e.getMessage());
                case DATABASE_ERROR -> throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
                default -> throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Authenticates a user based on email and password.
     *
     * @param email                 The email of the user.
     * @param password              The password of the user.
     * @return                      The authenticated user.
     * @throws BusinessException    If an error occurs during the authentication process.
     */
    @Override
    public LoggedUserDTO login(String email, String password) throws BusinessException {
        try {
            // Check if the email and password are valid and return the user
            return userDAO.authenticate(email, password);

        } catch (DAOException e) {
            switch (e.getType()) {
                case DATABASE_ERROR -> throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
                case AUTHENTICATION_ERROR -> throw new BusinessException(BusinessExceptionType.AUTHENTICATION_ERROR, e.getMessage());
                default -> throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Updates the user information.
     * If the user's username or profile picture URL is updated, the method executes
     * a task to update the user node in Neo4j and the user redundancy inside anime and manga.
     * If the user's username, profile picture URL, birthday or location is updated,
     * the method executes a task to update the user redundancy inside reviews.
     *
     * @param user                  The user object containing the updated information.
     * @throws BusinessException    If an error occurs during the update process.
     */
    @Override
    public void updateUserInfo(User user) throws BusinessException {
        try {
            // Update the user in the database
            userDAO.updateUser(user);

            // Create a task which update the node User in Neo4j and the user redundancy inside anime and manga
            if (user.getUsername() != null || user.getProfilePicUrl() != null) {
                aperiodicExecutorTaskService.executeTask(new UpdateUserTask(user));
                aperiodicExecutorTaskService.executeTask(new UpdateMediaRedundancyTask(user.toSummaryDTO()));
            }

            // Create a task which updates the user redundancy inside reviews
            List<String> reviewIds = user.getReviewIds();
            if (reviewIds != null && !reviewIds.isEmpty() &&
                    (user.getUsername() != null || user.getProfilePicUrl() != null || user.getBirthday() != null || user.getLocation() != null)) {
                aperiodicExecutorTaskService.executeTask(new UpdateReviewRedundancyTask(null, user.toSummaryDTO(), reviewIds));
            }

        } catch (DAOException e) {
            switch (e.getType()) {
                case DATABASE_ERROR -> throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
                case NO_CHANGES -> throw new BusinessException(BusinessExceptionType.NO_CHANGE, e.getMessage());
                case DUPLICATED_USERNAME -> throw new BusinessException(BusinessExceptionType.DUPLICATED_USERNAME, e.getMessage());
                default -> throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Deletes a user from the database and executes tasks to delete the user node in Neo4j.
     * If the user has reviews, the method executes a task to remove the user redundancy inside reviews
     * and, if the reviews are present in the latest reviews, the method executes a task to update the latest reviews.
     *
     * @param userId                The ID of the user to delete.
     * @throws BusinessException    If an error occurs during the deletion process.
     */
    @Override
    public void deleteUser(String userId, List<String> reviewIds) throws BusinessException {
        try {
            // Delete the user from the database
            userDAO.deleteUser(userId);

            // Create a task which deletes the node User in Neo4j
            aperiodicExecutorTaskService.executeTask(new DeleteUserTask(userId));

            // Create a task which removes the user redundancy inside reviews
            if (reviewIds != null && !reviewIds.isEmpty())
                aperiodicExecutorTaskService.executeTask(new RemoveDeletedUserReviewsTask(reviewIds));

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the user information based on the provided ID.
     *
     * @param userId                The ID of the user to retrieve.
     * @param isUserLoggedInfo      Indicates whether to include user's logged information.
     * @return                      The user object corresponding to the provided ID.
     * @throws BusinessException    If an error occurs during the retrieval process.
     */
    @Override
    public User getUserById(String userId, boolean isUserLoggedInfo) throws BusinessException {
        try {
            // Retrieve the user from the database
            return (User) userDAO.readUser(userId, false, isUserLoggedInfo);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Follows a user.
     * The method executes a task to update the number of followed and followers.
     *
     * @param followerUserId        The ID of the user who wants to follow.
     * @param followingUserId       The ID of the user to be followed.
     * @throws BusinessException    If an error occurs while following the user.
     */
    @Override
    public void follow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            // Follow the user
            userDAONeo4J.follow(followerUserId, followingUserId);

            // Create a task which updates the number of followings
            UpdateNumberOfFollowingsTask task = new UpdateNumberOfFollowingsTask(followerUserId, 1);
            aperiodicExecutorTaskService.executeTask(task);

            // Create a task which updates the number of followers
            UpdateNumberOfFollowersTask task1 = new UpdateNumberOfFollowersTask(followingUserId, 1);
            aperiodicExecutorTaskService.executeTask(task1);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }

    /**
     * Unfollows a user.
     * The method executes a task to update the number of followed and followers.
     *
     * @param followerUserId        The ID of the user who wants to unfollow.
     * @param followingUserId       The ID of the user to be unfollowed.
     * @throws BusinessException    If an error occurs while unfollowing the user.
     */
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            // Unfollow the user
            userDAONeo4J.unfollow(followerUserId, followingUserId);

            // Create a task which updates the number of followings
            UpdateNumberOfFollowingsTask task = new UpdateNumberOfFollowingsTask(followerUserId, -1);
            aperiodicExecutorTaskService.executeTask(task);

            // Create a task which updates the number of followers
            UpdateNumberOfFollowersTask task1 = new UpdateNumberOfFollowersTask(followingUserId, -1);
            aperiodicExecutorTaskService.executeTask(task1);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }

    /**
     * Checks if a user is following another user.
     *
     * @param followerUserId        The ID of the user who is following.
     * @param followingUserId       The ID of the user being followed.
     * @return                      True if the follower user is following the specified user, false otherwise.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public boolean isFollowing(String followerUserId, String followingUserId) throws BusinessException {
        try {
            // Check if the user is following the other user
            return userDAONeo4J.isFollowing(followerUserId, followingUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return false;
        }
    }

    /**
     * Searches followings of a user (with userID) based on the provided username.
     * Hide the logged user from the search results.
     *
     * @param userId                The ID of the user whose followings are being searched.
     * @param username              The username of the user being searched for.
     * @param loggedUserId          The ID of the logged-in user performing the search.
     * @return                      A list of UserSummaryDTO objects representing the followings found based on the search criteria.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<UserSummaryDTO> searchFollowings(String userId, String username, String loggedUserId) throws BusinessException {
        try {
            // Search for the followings
            return userDAONeo4J.searchFollowing(userId, username, loggedUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    /**
     * Searches followers of a user (with userID) based on the provided username.
     * Hide the logged user from the search results.
     *
     * @param userId                The ID of the user whose followers are being searched.
     * @param username              The username of the user being searched for.
     * @param loggedUserId          The ID of the logged-in user performing the search.
     * @return                      A list of UserSummaryDTO objects representing the followers found based on the search criteria.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws BusinessException {
        try {
            // Search for the followers
            return userDAONeo4J.searchFollowers(userId, username, loggedUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    /**
     * Searches for the first N users based on the provided username.
     *
     * @param username              The username to search for.
     * @param n                     The maximum number of users to retrieve.
     * @param loggedUser            The ID of the logged-in user performing the search.
     * @return                      A list of UserSummaryDTO objects representing the first N users found based on the search criteria.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws BusinessException {
        try {
            // Search for the first N users
            return userDAO.searchFirstNUsers(username, n, loggedUser);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Suggests users based on common followings with a specified user.
     *
     * @param userId                The ID of the user for whom suggestions are made.
     * @return                      A list of UserSummaryDTO objects representing suggested users.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId) throws BusinessException {
        try {
            // Suggest users based on common followings
            return userDAONeo4J.suggestUsersByCommonFollowings(userId, 10);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Suggests users based on common likes with a specified user.
     *
     * @param userId                The ID of the user for whom suggestions are made.
     * @return                      A list of UserSummaryDTO objects representing suggested users.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<UserSummaryDTO> suggestUsersByCommonLikes(String userId) throws BusinessException {
        try {
            // Suggest users based on common anime likes
            List<UserSummaryDTO> users = userDAONeo4J.suggestUsersByCommonLikes(userId, 5, MediaContentType.ANIME);

            // Suggest users based on common manga likes
            users.addAll(userDAONeo4J.suggestUsersByCommonLikes(userId, 5 + users.size(), MediaContentType.MANGA));

            return users;
        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Add the user's rating for the application.
     *
     * @param userId                The ID of the user who is rating the application.
     * @param rating                The rating provided by the user.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void rateApp(String userId, Integer rating) throws BusinessException {
        try {
            // Rate the application
            userDAO.rateApp(userId, rating);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the distribution of users based on the specified criteria.
     *
     * @param criteria              The criteria for which the distribution is requested.
     *                              Criteria Types: "location", "gender", "birthday", "joined_on".
     * @return                      A map containing the distribution data.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public Map<String, Integer> getDistribution(String criteria) throws BusinessException {
        try {
            // Check if the criteria is valid
            if(!(criteria.equals("location") || (criteria.equals("gender")) || (criteria.equals("birthday") || (criteria.equals("joined_on"))))) {
                throw new BusinessException(BusinessExceptionType.INVALID_INPUT, "Invalid criteria");
            }

            // Get the distribution of users
            return userDAO.getDistribution(criteria);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the average app rating based on the specified criteria.
     *
     * @param criteria              The criteria for which the average app rating is requested.
     *                              Criteria Types: "location", "gender", "age".
     * @return                      A map containing the average app rating data.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public Map<String, Double> averageAppRating(String criteria) throws BusinessException {
        try {
            // Check if the criteria is valid and get the average app rating
            if(criteria.equals("location") || (criteria.equals("gender")))
                return userDAO.averageAppRating(criteria);
            else if(criteria.equals("age"))
                return userDAO.averageAppRatingByAgeRange();
            else
                throw new BusinessException(BusinessExceptionType.INVALID_INPUT, "Invalid criteria");

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
