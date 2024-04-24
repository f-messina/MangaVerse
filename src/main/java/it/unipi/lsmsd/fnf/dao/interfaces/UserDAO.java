package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.List;
import java.util.Map;

public interface UserDAO {
    //MongoDB queries
    void saveUser(UserRegistrationDTO user) throws DAOException;
    void updateUser(User user) throws DAOException;
    void deleteUser(String userId) throws DAOException;
    LoggedUserDTO authenticate(String email, String password) throws DAOException;
    RegisteredUser readUser(String userId, boolean onlyStatsInfo) throws DAOException;
    List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException;

    //MongoDB complex queries
    Map<String, Integer> getDistribution(String criteria) throws DAOException;
    Double averageAgeUsers() throws DAOException;
    Map<String, Double> averageAppRating(String criteria) throws DAOException;
    Map<String, Double> averageAppRatingByAgeRange() throws DAOException;

    //Neo4J queries
    void createNode(UserSummaryDTO userSummaryDTO) throws DAOException;
    void follow(String followerUserId, String followingUserId) throws DAOException;
    void unfollow(String followerUserId, String followingUserId) throws DAOException;
    List<UserSummaryDTO> getFollowing(String userId) throws DAOException;
    List<UserSummaryDTO> getFollowers(String userId) throws DAOException;
    List<UserSummaryDTO> suggestUsers(String userId) throws DAOException;
}