package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            // Validation checks for empty fields
            if (StringUtils.isAnyEmpty(
                    userRegistrationDTO.getUsername(),
                    userRegistrationDTO.getPassword(),
                    userRegistrationDTO.getEmail()
            )) {
                throw new BusinessException("Username, password and email cannot be empty");
            }

            User user = userRegistrationDTOToUser(userRegistrationDTO);
            user.setId(userDAO.register(user));
            return user;

        } catch (DAOException e) {
            String errorMessage = e.getMessage();

            if (errorMessage.contains("Email") || errorMessage.contains("Username")) {
                throw new BusinessException(errorMessage, e);
            } else {
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
            throw new BusinessException("Email cannot be empty");
        if (StringUtils.isEmpty(password))
            throw new BusinessException("Password cannot be empty");

        try {
            RegisteredUser registeredUser = userDAO.authenticate(email, password);
            if (registeredUser instanceof User user) {
                user.setLists(personalListDAO.findByUser(user.getId())
                        .stream()
                        .map(DtoToModelMapper::personalListDTOtoPersonalList)
                        .collect(Collectors.toCollection(ArrayList::new)));
                user.setReviews(reviewDAO.findByUser(user.getId())
                        .stream()
                        .map(DtoToModelMapper::reviewDTOtoReview)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
            return registeredUser;

        } catch (DAOException e) {
            switch (e.getMessage()) {
                case "User not found":
                    throw new BusinessException("Invalid email", e);
                case "Wrong password":
                    throw new BusinessException("Wrong password", e);
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
            if (e.getMessage().contains("already exists")) {
                throw new BusinessException("Username already in use", e);
            } else {
                throw new BusinessException(e);
            }
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
