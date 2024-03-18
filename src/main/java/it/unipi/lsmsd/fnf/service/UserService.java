package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface UserService {
    void registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    UserSummaryDTO login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;

<<<<<<< HEAD
    User getUserById(String userId) throws BusinessException;

    void createNode(UserSummaryDTO userSummaryDTO) throws BusinessException;
    List<UserSummaryDTO> getFollowing(String userId) throws BusinessException;

    List<UserSummaryDTO> getFollowers(String userId) throws BusinessException;
    /*
    List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException;
     */
=======
    List<RegisteredUserDTO> getFollowers(String userId) throws BusinessException;

    //Service for mongoDB queries
    Map<String, Integer> getDistribution(String criteria) throws BusinessException;

    Double averageAgeUsers() throws BusinessException;


    Map<String, Double> averageAppRating(String criteria) throws BusinessException;

    Map<String, Double> averageAppRatingByAgeRange() throws BusinessException;

>>>>>>> noemi
}
