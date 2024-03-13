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
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper.userRegistrationDTOToUser;

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

    @Override
    public void createNode(RegisteredUserDTO registeredUserDTO) throws BusinessException {
        try {
            userDAONeo4J.createNode(registeredUserDTO);
        } catch (Exception e) {
            throw new BusinessException("Error while creating the user node.", e);
        }
    }

    @Override
    public void follow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.follow(followerUserId, followingUserId);
        } catch (Exception e) {
            throw new BusinessException("Error while following the user.", e);
        }
    }

    @Override
    public void unfollow(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.unfollow(followerUserId, followingUserId);
        } catch (Exception e) {
            throw new BusinessException("Error while unfollowing the user.", e);
        }
    }

    @Override
    public List<RegisteredUserDTO> getFollowing(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowing(userId);
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the following list.");
        }
    }

    @Override
    public List<RegisteredUserDTO> getFollowers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.getFollowers(userId);
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the follower list.");
        }
    }


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
}
