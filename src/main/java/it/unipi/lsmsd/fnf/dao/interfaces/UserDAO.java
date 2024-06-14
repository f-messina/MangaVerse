package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.List;
import java.util.Map;

/**
 * Interface for the DAO of the RegisteredUser entity.
 * Provides methods to interact with the databases.
 * The methods are divided into two categories: MongoDB specific methods and Neo4J specific methods.
 * The MongoDB specific methods are used to interact with the MongoDB database and
 * the Neo4J specific methods are used to interact with the Neo4J database.
 * The MongoDB methods provide crud operations, search functionality and operations to get statistics.
 * The Neo4J methods provide CRUD operations to generate relationship between nodes and
 * maintain consistency between nodes and related mongoDB documents,
 * operations to get statistics and operations to get user suggestions.
 */
public interface UserDAO {
    //MongoDB operations
    void saveUser(UserRegistrationDTO user) throws DAOException;
    void updateUser(User user) throws DAOException;
    void deleteUser(String userId) throws DAOException;
    LoggedUserDTO authenticate(String email, String password) throws DAOException;
    RegisteredUser readUser(String userId, boolean onlyStatsInfo, boolean isLoggedUserInfo) throws DAOException;
    List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException;
    Map<String, Integer> getDistribution(String criteria) throws DAOException; // MANAGER (PIE CHART)
    Map<String, Double> averageAppRating(String criteria) throws DAOException; // MANAGER (TABLE OR BAR CHART)
    Map<String, Double> averageAppRatingByAgeRange() throws DAOException; // MANAGER (TABLE OR BAR CHART)
    void updateNumOfFollowers(String userId, Integer followers) throws DAOException;
    void updateNumOfFollowings(String userId, Integer followed) throws DAOException;
    void rateApp(String userId, Integer rating) throws DAOException;

    //Neo4J queries
    void follow(String followerUserId, String followedUserId) throws DAOException;
    void unfollow(String followerUserId, String followedUserId) throws DAOException;
    boolean isFollowing(String followerUserId, String followedUserId) throws DAOException;
    Integer getNumOfFollowers(String userId) throws DAOException;
    Integer getNumOfFollowed(String userId) throws DAOException;
    List<UserSummaryDTO> searchFollowing(String userId, String username, String loggedUserId) throws DAOException;
    List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws DAOException;
    List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId, Integer limit) throws DAOException;
    List<UserSummaryDTO> suggestUsersByCommonLikes(String userId, Integer limit, MediaContentType type) throws DAOException;

}
