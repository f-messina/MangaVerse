package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * @param email The email of the user.
     * @param password The password of the user.
     * @return The authenticated user.
     * @throws BusinessException If an error occurs during the authentication process.
     */
    @Override
    public RegisteredUser login(String email, String password) throws BusinessException {
        // Validation checks for empty fields
        if (StringUtils.isEmpty(email))
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Email cannot be empty");
        if (StringUtils.isEmpty(password))
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Password cannot be empty");

        try {
            Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
            logger.info("User with email " + email + " is trying to log in with password " + password + " at " + LocalDate.now() + " " + LocalDate.now().atTime(0, 0) + " UTC.");
            RegisteredUser registeredUser = userDAO.authenticate(email, password);
            logger.info("User with email " + email + " has logged in successfully at " + LocalDate.now() + " " + LocalDate.now().atTime(0, 0) + " UTC.");
            if (registeredUser instanceof User user) {
                logger.info("User with email " + email + " has " + user.getLists().size() + " lists.");
                List<MediaContent> likedManga = mangaDAONeo4J.getLiked(user.getId()).stream()
                        .map(mangaDTO -> DtoToModelMapper.mangaDTOtoManga((MangaDTO) mangaDTO))
                        .collect(Collectors.toList());
                logger.info("User with email " + email + " has " + likedManga.size() + " liked manga.");
                List<MediaContent> likedAnime = animeDAONeo4J.getLiked(user.getId()).stream()
                        .map(animeDTO -> DtoToModelMapper.animeDTOtoAnime((AnimeDTO) animeDTO))
                        .collect(Collectors.toList());
                logger.info("User with email " + email + " has " + likedAnime.size() + " liked anime.");
                user.setLikedMediaContent(likedManga);
                user.getLikedMediaContent().addAll(likedAnime);
            }

            return registeredUser;

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
}
