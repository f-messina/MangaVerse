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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

public class UserServiceImpl implements UserService {

    private static final UserDAO userDAO;
    private static final PersonalListDAO personalListDAO;
    private static final UserDAO userDAONeo4J;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;

    static {
        userDAO = getUserDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        userDAONeo4J = getUserDAO(DataRepositoryEnum.NEO4J);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
    }

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

    @Override
    public RegisteredUser login(String email, String password) throws BusinessException {
        // Validation checks for empty fields
        if (StringUtils.isEmpty(email))
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Email cannot be empty");
        if (StringUtils.isEmpty(password))
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Password cannot be empty");

        try {
            RegisteredUser registeredUser = userDAO.authenticate(email, password);
            if (registeredUser instanceof User user) {
                user.setLists(personalListDAO.findByUser(user.getId(), true)
                        .stream()
                        .map(DtoToModelMapper::personalListDTOtoPersonalList)
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
            if (Objects.requireNonNull(type) == DAOExceptionType.AUTHENTICATION_ERROR)
                throw new BusinessException(BusinessExceptionType.AUTHENTICATION_ERROR, "Email or password is incorrect");
            else
                throw new BusinessException("DAOException during authenticating operation");
        }
    }


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
    public void createNode(UserSummaryDTO userSummaryDTO) throws BusinessException {
        try {
            userDAONeo4J.createNode(userSummaryDTO);
        } catch (DAOException e) {
            throw new BusinessException("Error while creating the user node.", e);
        }
    }

    @Override
    public void follow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.follow(followerUserId, followingUserId);
        } catch (DAOException e) {
            throw new BusinessException("Error while following the user.", e);
        }
    }

    @Override
    public void unfollow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.unfollow(followerUserId, followingUserId);
        } catch (DAOException e) {
            throw new BusinessException("Error while unfollowing the user.", e);
        }
    }

    @Override
    public List<UserSummaryDTO> getFollowing(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowing(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the following list.");
        }
    }

    @Override
    public List<UserSummaryDTO> getFollowers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowers(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the follower list.");
        }
    }
}
