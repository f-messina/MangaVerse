package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface UserService {
    void signup(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    LoggedUserDTO login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    void deleteUser(String userId) throws BusinessException;
    User getUserById(String userId) throws BusinessException;
    List<UserSummaryDTO> suggestUsers(String userId) throws BusinessException;
    List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws BusinessException;
    Map<String, Integer> getDistribution(String criteria) throws BusinessException;
    Map<String, Double> averageAppRating(String criteria) throws BusinessException;
    Map<String, Double> averageAppRatingByAgeRange() throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;
    boolean isFollowing(String followerUserId, String followingUserId) throws BusinessException;
    List<UserSummaryDTO> getFollowings(String userId, String loggedUserId) throws BusinessException;
    List<UserSummaryDTO> searchFollowings(String userId, String username, String loggedUserId) throws BusinessException;
    List<UserSummaryDTO> getFollowers(String userId, String loggedUserId) throws BusinessException;
    List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws BusinessException;
}
