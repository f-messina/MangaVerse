package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;

public interface UserService {
    User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    RegisteredUser login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;
    void createNode(RegisteredUserDTO registeredUserDTO) throws BusinessException;
    List<RegisteredUserDTO> getFollowing(String userId) throws BusinessException;

    List<RegisteredUserDTO> getFollowers(String userId) throws BusinessException;

    //Service for mongoDB queries
    List<Document> getDistribution(String criteria) throws BusinessException;

    Integer averageAgeUsers() throws BusinessException;

    List<Document> getUsersByAgeRange() throws BusinessException;

    List<Document> getUsersRegisteredByYear() throws BusinessException;

    int averageAppRating(String criteria, String value) throws BusinessException;

    List<Integer> averageAppRatingByAgeRange() throws BusinessException;
    /*
    List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException;
     */
}
