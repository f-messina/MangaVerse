package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;

import java.util.List;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper.userRegistrationDTOToUser;

public class UserServiceImpl implements UserService {

    private static final UserDAO userDAO;
    private static final PersonalListDAO personalListDAO;
    private static final ReviewDAO reviewDAO;
    private static final UserDAO userDAONeo4J;

    static {
        userDAO = getUserDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        userDAONeo4J = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
    }

    @Override
    public User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException {
        try {
            if (userRegistrationDTO.getUsername() == null || userRegistrationDTO.getUsername().isEmpty())
                throw new BusinessException("Username cannot be empty");
            if (userRegistrationDTO.getPassword() == null || userRegistrationDTO.getPassword().isEmpty())
                throw new BusinessException("Password cannot be empty");
            if (userRegistrationDTO.getEmail() == null || userRegistrationDTO.getEmail().isEmpty())
                throw new BusinessException("Email cannot be empty");
            User user = userRegistrationDTOToUser(userRegistrationDTO);
            user.setId(userDAO.register(user));
            return user;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public RegisteredUser login(String username, String password) throws BusinessException {
        try {
            RegisteredUser registeredUser = userDAO.authenticate(username, password);
            if (registeredUser instanceof User user) {
                List<PersonalListDTO> personalLists = personalListDAO.findByUser(user.getId());
                user.setLists(personalLists.stream().map(DtoToModelMapper::personalListDTOtoPersonalList).toList());
                List<ReviewDTO> reviews = reviewDAO.findByUser(user.getId());
                user.setReviews(reviews.stream().map(DtoToModelMapper::reviewDTOtoReview).toList());
            }

            return registeredUser;
        } catch (Exception e) {
            throw new BusinessException(e);
        }


    }

    @Override
    public void createUserNode(String id, String username, String picture) throws BusinessException {
        try {
            userDAONeo4J.createUserNode(id, username, picture);
        } catch (Exception e) {
            throw new BusinessException("Error while creating the user node.", e);
        }
    }

    @Override
    public void followUser(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.followUser(followerUserId, followingUserId);
        } catch (Exception e) {
            throw new BusinessException("Error while following the user.", e);
        }
    }
    @Override
    public void unfollowUser(String followerUserId, String followingUserId) throws BusinessException {
        try {
            userDAONeo4J.unfollowUser(followerUserId, followingUserId);
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

    @Override
    public List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException {
        try {
            return userDAONeo4J.suggestUsers(userId);
        } catch (Exception e) {
            throw new BusinessException("Error while suggesting users.", e);
        }
    }
}
