package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;
import java.util.Map;

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
    Map<String, Integer> getDistribution(String criteria) throws BusinessException;

    Double averageAgeUsers() throws BusinessException;


    Map<String, Double> averageAppRating(String criteria) throws BusinessException;

    List<Integer> averageAppRatingByAgeRange() throws BusinessException;

}
