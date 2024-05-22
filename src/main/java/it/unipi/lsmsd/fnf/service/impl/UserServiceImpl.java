package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
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
 * The UserServiceImpl class provides implementation for the UserService interface.
 * It handles user registration, authentication, updating user information, and other user-related functionalities.
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
     * Registers a new user and logs them in.
     * @param userRegistrationDTO The user registration data.
     * @throws BusinessException If an error occurs during the registration process.
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
                throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Username, password and email cannot be empty");
            }

            userDAO.saveUser(userRegistrationDTO);

            // Create a task which adds a new node User in Neo4j
            aperiodicExecutorTaskService.executeTask(new CreateUserTask(userRegistrationDTO));

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
     * @param email    The email of the user.
     * @param password The password of the user.
     * @return The authenticated user.
     * @throws BusinessException If an error occurs during the authentication process.
     */
    @Override
    public LoggedUserDTO login(String email, String password) throws BusinessException {
        try {
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
     * Updates user information.
     * @param user The updated user information.
     * @throws BusinessException If an error occurs during the update process.
     */
    @Override
    public void updateUserInfo(User user) throws BusinessException {
        try {
            userDAO.updateUser(user);

            // Create a task which update the node User in Neo4j
            if (user.getUsername() != null || user.getProfilePicUrl() != null) {
                aperiodicExecutorTaskService.executeTask(new UpdateUserTask(user));
                aperiodicExecutorTaskService.executeTask(new UpdateMediaRedundancyTask(user.toSummaryDTO()));
            }

            // Create a task which updates the user redundancy inside reviews
            if (user.getUsername() != null || user.getProfilePicUrl() != null || user.getBirthday() != null || user.getLocation() != null) {
                aperiodicExecutorTaskService.executeTask(new UpdateReviewRedundancyTask(null, user.toSummaryDTO()));
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
    @Override
    public void deleteUser(String userId) throws BusinessException {
        try {
            userDAO.deleteUser(userId);

            // Create a task which deletes the node User in Neo4j
            aperiodicExecutorTaskService.executeTask(new DeleteUserTask(userId));

            // Create a task which removes the user redundancy inside reviews
            aperiodicExecutorTaskService.executeTask(new RemoveDeletedUserReviewsTask(userId));

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public User getUserById(String userId) throws BusinessException {
        try {
            return (User) userDAO.readUser(userId, false);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Follows a user.
     * @param followerUserId The ID of the user who wants to follow.
     * @param followingUserId The ID of the user to be followed.
     * @throws BusinessException If an error occurs while following the user.
     */
    @Override
    public void follow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.follow(followerUserId, followingUserId);

            // Create a task which updates the number of followed and followers
            UpdateNumberOfFollowedTask task = new UpdateNumberOfFollowedTask(followerUserId);
            aperiodicExecutorTaskService.executeTask(task);

            UpdateNumberOfFollowersTask task1 = new UpdateNumberOfFollowersTask(followingUserId);
            aperiodicExecutorTaskService.executeTask(task1);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }

    /**
     * Unfollows a user.
     * @param followerUserId The ID of the user who wants to unfollow.
     * @param followingUserId The ID of the user to be unfollowed.
     * @throws BusinessException If an error occurs while unfollowing the user.
     */
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.unfollow(followerUserId, followingUserId);

            // Create a task which updates the number of followed and followers
            UpdateNumberOfFollowedTask task = new UpdateNumberOfFollowedTask(followerUserId);
            aperiodicExecutorTaskService.executeTask(task);

            UpdateNumberOfFollowersTask task1 = new UpdateNumberOfFollowersTask(followingUserId);
            aperiodicExecutorTaskService.executeTask(task1);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }

    @Override
    public boolean isFollowing(String followerUserId, String followingUserId) throws BusinessException {
        try {
            return userDAONeo4J.isFollowing(followerUserId, followingUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return false;
        }
    }

    /**
     * Retrieves the list of users being followed by a particular user.
     * @param userId The ID of the user.
     * @return The list of users being followed by the specified user.
     * @throws BusinessException If an error occurs while retrieving the list.
     */
    @Override
    public List<UserSummaryDTO> getFollowings(String userId, String loggedUserId) throws BusinessException {
        try {
            return userDAONeo4J.getFirstNFollowing(userId, loggedUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    @Override
    public List<UserSummaryDTO> searchFollowings(String userId, String username, String loggedUserId) throws BusinessException {
        try {
            return userDAONeo4J.searchFollowing(userId, username, loggedUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    /**
     * Retrieves the list of users following a particular user.
     * @param userId The ID of the user.
     * @return The list of users following the specified user.
     * @throws BusinessException If an error occurs while retrieving the list.
     */
    @Override
    public List<UserSummaryDTO> getFollowers(String userId, String loggedUserId) throws BusinessException {
        try {
            return userDAONeo4J.getFirstNFollowers(userId, loggedUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    @Override
    public List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws BusinessException {
        try {
            return userDAONeo4J.searchFollowers(userId, username, loggedUserId);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws BusinessException {
        try {
            return userDAO.searchFirstNUsers(username, n, loggedUser);

        } catch (DAOException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public List<UserSummaryDTO> suggestUsers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.suggestUsersByCommonFollows(userId, null);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> getDistribution(String criteria) throws BusinessException {
        try {
            if(!(criteria.equals("location") || (criteria.equals("gender")) || (criteria.equals("birthday") || (criteria.equals("joined_on"))))) {
                throw new BusinessException("Invalid criteria");
            }
            return userDAO.getDistribution(criteria);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Double> averageAppRating(String criteria) throws BusinessException {
        try {
            if(!(criteria.equals("location") || (criteria.equals("gender")))) {
                throw new BusinessException("Invalid criteria");
            }
            return userDAO.averageAppRating(criteria);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Double> averageAppRatingByAgeRange() throws BusinessException {
        try {
            return userDAO.averageAppRatingByAgeRange();

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
