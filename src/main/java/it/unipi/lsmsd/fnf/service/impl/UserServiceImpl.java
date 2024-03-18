package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.List;
<<<<<<< HEAD
import java.util.Objects;
=======
import java.util.Map;
import java.util.stream.Collectors;
>>>>>>> noemi

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

/**
 * The UserServiceImpl class provides implementation for the UserService interface.
 * It handles user registration, authentication, updating user information, and other user-related functionalities.
 */
public class UserServiceImpl implements UserService {

    private static final UserDAO userDAO;
    private static final UserDAO userDAONeo4J;
    private static final PersonalListDAO personalListDAO;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;

    static {
        userDAO = getUserDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        userDAONeo4J = getUserDAO(DataRepositoryEnum.NEO4J);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
    }

    /**
     * Registers a new user and logs them in.
     * @param userRegistrationDTO The user registration data.
     * @throws BusinessException If an error occurs during the registration process.
     */
    @Override
    public void registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException {
        try {
            // Validation checks for empty fields
            if (StringUtils.isAnyEmpty(
                    userRegistrationDTO.getUsername(),
                    userRegistrationDTO.getPassword(),
                    userRegistrationDTO.getEmail()
            )) {
                throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Username, password and email cannot be empty");
            }

            userDAO.createUser(userRegistrationDTO);
        } catch (DAOException e) {
            DAOExceptionType type = e.getType();

            switch (type) {
                case DUPLICATED_EMAIL -> throw new BusinessException(BusinessExceptionType.DUPLICATED_EMAIL,"Email is already taken");
                case DUPLICATED_USERNAME -> throw new BusinessException(BusinessExceptionType.DUPLICATED_USERNAME,"Username is taken");
                case DUPLICATED_KEY -> throw new BusinessException(BusinessExceptionType.DUPLICATED_KEY,"Email and username are already taken");
                case null, default -> throw new BusinessException("DAOException during registration operation");
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
    public UserSummaryDTO login(String email, String password) throws BusinessException {
        // Validation checks for empty fields
        if (StringUtils.isEmpty(email))
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Email cannot be empty");
        if (StringUtils.isEmpty(password))
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Password cannot be empty");

        try {

            return userDAO.authenticate(email, password);

        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            if (Objects.requireNonNull(type) == DAOExceptionType.AUTHENTICATION_ERROR)
                throw new BusinessException(BusinessExceptionType.AUTHENTICATION_ERROR, "Email or password is incorrect");
            else
                throw new BusinessException("DAOException during authenticating operation");
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

        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DUPLICATED_USERNAME.equals(type)) {
                throw new BusinessException(BusinessExceptionType.DUPLICATED_USERNAME, "Username already in use");
            } else {
                throw new BusinessException("Error updating user info");
            }
        }
    }

    @Override
    public User getUserById(String userId) throws BusinessException {
        try {
            return (User) userDAO.readUser(userId, false);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving user by id.", e);
        }
    }

    /**
     * Creates a node for a registered user in the Neo4j graph database.
     * @param userSummaryDTO The user summary data.
     * @throws BusinessException If an error occurs during the node creation process.
     */
    @Override
    public void createNode(UserSummaryDTO userSummaryDTO) throws BusinessException {
        try {
            userDAONeo4J.createNode(userSummaryDTO);
        } catch (DAOException e) {
            throw new BusinessException("Error while creating the user node.", e);
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
        } catch (DAOException e) {
            throw new BusinessException("Error while following the user.", e);
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
        } catch (DAOException e) {
            throw new BusinessException("Error while unfollowing the user.", e);
        }
    }

    /**
     * Retrieves the list of users being followed by a particular user.
     * @param userId The ID of the user.
     * @return The list of users being followed by the specified user.
     * @throws BusinessException If an error occurs while retrieving the list.
     */
    @Override
    public List<UserSummaryDTO> getFollowing(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowing(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the following list.");
        }
    }


    /**
     * Retrieves the list of users following a particular user.
     * @param userId The ID of the user.
     * @return The list of users following the specified user.
     * @throws BusinessException If an error occurs while retrieving the list.
     */
    @Override
    public List<UserSummaryDTO> getFollowers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowers(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the follower list.");
        }
    }
<<<<<<< HEAD
=======


    //Service for mongoDB queries
    @Override
    public Map<String, Integer> getDistribution (String criteria) throws BusinessException {
        try {
            if(!(criteria.equals("location") || (criteria.equals("gender")) || (criteria.equals("birthday") || (criteria.equals("joined_on"))))) {
                throw new BusinessException("Invalid criteria");
            }
            return userDAO.getDistribution(criteria);
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the distribution.", e);
        }
    }

    @Override
    public Double averageAgeUsers() throws BusinessException {
        try {
            return userDAO.averageAgeUsers();
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the average age of users.", e);
        }
    }

    @Override
    public Map<String, Double> averageAppRating (String criteria) throws BusinessException {
        try {
            if(!(criteria.equals("location") || (criteria.equals("gender")))) {
                throw new BusinessException("Invalid criteria");
            }
            return userDAO.averageAppRating(criteria);
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the average app rating.", e);
        }
    }

    @Override
    public Map<String, Double> averageAppRatingByAgeRange () throws BusinessException {
        try {
            return userDAO.averageAppRatingByAgeRange();
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the average app rating by age range.", e);
        }
    }
>>>>>>> noemi
}
