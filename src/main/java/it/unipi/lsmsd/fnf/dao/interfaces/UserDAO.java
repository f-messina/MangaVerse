package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.List;
import java.util.Map;

public interface UserDAO {
    //MongoDB queries
    void saveUser(UserRegistrationDTO user) throws DAOException;
    void updateUser(User user) throws DAOException;
    void deleteUser(String userId) throws DAOException;
    List<UserSummaryDTO> suggestUsersByCommonLikes(String userId, Integer limit, MediaContentType type) throws DAOException;
    LoggedUserDTO authenticate(String email, String password) throws DAOException;
    RegisteredUser readUser(String userId, boolean onlyStatsInfo, boolean isLoggedUserInfo) throws DAOException;
    List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException;
    Map<String, Integer> getDistribution(String criteria) throws DAOException; // MANAGER (PIE CHART)
    Map<String, Double> averageAppRating(String criteria) throws DAOException; // MANAGER (TABLE OR BAR CHART)
    Map<String, Double> averageAppRatingByAgeRange() throws DAOException; // MANAGER (TABLE OR BAR CHART)
    void updateNumOfFollowers(String userId, Integer followers) throws DAOException;
    void updateNumOfFollowed(String userId, Integer followed) throws DAOException;
    void rateApp(String userId, Integer rating) throws DAOException;

    //Neo4J queries
    void follow(String followerUserId, String followedUserId) throws DAOException;
    void unfollow(String followerUserId, String followedUserId) throws DAOException;
    boolean isFollowing(String followerUserId, String followedUserId) throws DAOException;
    Integer getNumOfFollowers(String userId) throws DAOException;
    Integer getNumOfFollowed(String userId) throws DAOException;
    List<UserSummaryDTO> getFirstNFollowing(String userId, String loggedUser) throws DAOException;
    List<UserSummaryDTO> searchFollowing(String userId, String username, String loggedUserId) throws DAOException;
    List<UserSummaryDTO> getFirstNFollowers(String userId, String loggedUserId) throws DAOException;
    List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws DAOException;
    List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId, Integer limit) throws DAOException;
}
