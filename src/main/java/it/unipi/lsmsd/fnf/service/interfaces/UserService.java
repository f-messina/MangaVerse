package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * Interface for the User service.
 * Provides methods to interact with the User entity.
 * The methods access to User entity in the database and provide
 * operations to maintain consistency between collections, search functionality,
 * operations to get statistics and operations to get user suggestions.
 * The methods, in general, execute a single DAO method. When needed to maintain
 * eventual consistency between collections, the methods execute multiple DAO methods,
 * executing the consistency operations in an asynchronous way.
 * @see User
 * @see UserRegistrationDTO
 * @see LoggedUserDTO
 */
public interface UserService {
    void signup(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    LoggedUserDTO login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    void deleteUser(String userId, List<String> reviewsIds) throws BusinessException;
    User getUserById(String userId, boolean isLoggedUserInfo) throws BusinessException;
    List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId) throws BusinessException;
    List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws BusinessException;
    List<UserSummaryDTO> suggestUsersByCommonLikes(String userId) throws BusinessException;
    void rateApp(String userId, Integer rating) throws BusinessException;
    Map<String, Integer> getDistribution(String criteria) throws BusinessException;
    Map<String, Double> averageAppRating(String criteria) throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;
    boolean isFollowing(String followerUserId, String followingUserId) throws BusinessException;
    List<UserSummaryDTO> searchFollowings(String userId, String username, String loggedUserId) throws BusinessException;
    List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws BusinessException;
}
