package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface UserService {
    void registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    UserSummaryDTO login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    User getUserById(String userId) throws BusinessException;
    void insertList(String userId, String name) throws BusinessException;
    void updateList(String userId, String listId, String name) throws BusinessException;
    void addToList(String listId, MediaContentDTO content) throws BusinessException;
    void removeFromList(String listId, String mediaContentId, MediaContentType type) throws BusinessException;
    void deleteList(String id) throws BusinessException;

    //Service for mongoDB queries
    Map<String, Integer> getDistribution(String criteria) throws BusinessException;
    Double averageAgeUsers() throws BusinessException;
    Map<String, Double> averageAppRating(String criteria) throws BusinessException;
    Map<String, Double> averageAppRatingByAgeRange() throws BusinessException;

    //Neo4J queries
    void createNode(UserSummaryDTO userSummaryDTO) throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;
    List<UserSummaryDTO> getFollowing(String userId) throws BusinessException;
    List<UserSummaryDTO> getFollowers(String userId) throws BusinessException;
}
