package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper.userRegistrationDTOToUser;

/**
 * The UserServiceImpl class provides implementation for the UserService interface.
 * It handles user registration, authentication, updating user information, and other user-related functionalities.
 */
public class UserServiceImpl implements UserService {

    private static final UserDAO userDAO;
    private static final PersonalListDAO personalListDAO;
    private static final ReviewDAO reviewDAO;
    private static final UserDAO userDAONeo4J;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;

    static {
        userDAO = getUserDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        userDAONeo4J = getUserDAO(DataRepositoryEnum.NEO4J);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
    }

    /**
     * Registers a new user and logs them in.
     * @param userRegistrationDTO The user registration data.
     * @return The registered user.
     * @throws BusinessException If an error occurs during the registration process.
     */
    @Override
    public User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException {
        try {
            // Validation checks for empty fields
            if (StringUtils.isAnyEmpty(
                    userRegistrationDTO.getUsername(),
                    userRegistrationDTO.getPassword(),
                    userRegistrationDTO.getEmail()
            )) {
                throw new BusinessException(BusinessExceptionType.EMPTY_USERNAME_PSW_EMAIL,"Username, password and email cannot be empty");
            }

            User user = userRegistrationDTOToUser(userRegistrationDTO);
            user.setId(userDAO.register(user));
            return user;

        } catch (DAOException e) {
            DAOExceptionType type = e.getType();

            if (DAOExceptionType.TAKEN_EMAIL.equals(type)) {
                throw new BusinessException(BusinessExceptionType.TAKEN_EMAIL,"Email is already taken");
            }
            else if(DAOExceptionType.TAKEN_USERNAME.equals(type)) {
                throw new BusinessException(BusinessExceptionType.TAKEN_USERNAME,"Username is taken");
            }else {
                throw new BusinessException("DAOException during registration operation", e);
            }

        } catch (Exception e) {
            throw new BusinessException("Error registering user", e);
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
            throw new BusinessException(BusinessExceptionType.EMPTY_EMAIL,"Email cannot be empty");
        if (StringUtils.isEmpty(password))
            throw new BusinessException(BusinessExceptionType.EMPTY_PSW,"Password cannot be empty");

        try {
            RegisteredUser registeredUser = userDAO.authenticate(email, password);
            if (registeredUser instanceof User user) {
                user.setLists(personalListDAO.findByUser(user.getId(), true)
                        .stream()
                        .map(DtoToModelMapper::personalListDTOtoPersonalList)
                        .collect(Collectors.toCollection(ArrayList::new)));
                user.setReviews(reviewDAO.findByUser(user.getId())
                        .stream()
                        .map(DtoToModelMapper::reviewDTOtoReview)
                        .collect(Collectors.toCollection(ArrayList::new)));
                List<MediaContent> likedManga = mangaDAONeo4J.getLiked(user.getId()).stream()
                        .map(mangaDTO -> DtoToModelMapper.mangaDTOtoManga((MangaDTO) mangaDTO))
                        .collect(Collectors.toList());
                List<MediaContent> likedAnime = animeDAONeo4J.getLiked(user.getId()).stream()
                        .map(animeDTO -> DtoToModelMapper.animeDTOtoAnime((AnimeDTO) animeDTO))
                        .collect(Collectors.toList());

                user.setLikedMediaContent(likedManga);
                user.getLikedMediaContent().addAll(likedAnime);
            }
            return registeredUser;

        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            switch (type) {
                case DAOExceptionType.WRONG_EMAIL:
                    throw new BusinessException(BusinessExceptionType.INVALID_EMAIL,"Invalid email");
                case DAOExceptionType.WRONG_PSW:
                    throw new BusinessException(BusinessExceptionType.WRONG_PSW,"Wrong password");
                default:
                    throw new BusinessException("DAOException during authenticating operation", e);
            }
        } catch (Exception e) {
            throw new BusinessException("Error authenticating user", e);
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
            userDAO.update(user);
        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.TAKEN_USERNAME.equals(type)) {
                throw new BusinessException(BusinessExceptionType.TAKEN_USERNAME,"Username already in use");
            } else {
                throw new BusinessException(e);
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    /**
     * Creates a node for a registered user in the Neo4j graph database.
     * @param registeredUserDTO The registered user data.
     * @throws BusinessException If an error occurs during the node creation process.
     */
    @Override
    public void createNode(RegisteredUserDTO registeredUserDTO) throws BusinessException {
        try {
            userDAONeo4J.createNode(registeredUserDTO);
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
    public List<RegisteredUserDTO> getFollowing(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowing(userId);
        } catch (Exception e) {
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
    public List<RegisteredUserDTO> getFollowers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowers(userId);
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the follower list.");
        }
    }

    /*
    @Override
    public List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.suggestUsers(userId);
        } catch (Exception e) {
            throw new BusinessException("Error while suggesting users.", e);
        }
    }
     */
}
