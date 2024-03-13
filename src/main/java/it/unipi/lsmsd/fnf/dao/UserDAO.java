package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import org.bson.Document;
import java.util.List;
import java.util.Map;

public interface UserDAO {
    //MongoDB queries
    String register(User user) throws DAOException;
    void remove(String id) throws DAOException;
    RegisteredUser authenticate(String email, String password) throws DAOException;
    RegisteredUser find(String id) throws DAOException;
    List<RegisteredUserDTO> search(String username) throws DAOException;
    List<RegisteredUserDTO> findAll() throws DAOException;
    void update(User user) throws DAOException;
    void update(RegisteredUser user) throws DAOException;

    //MongoDB complex queries

    Map<String, Integer> getDistribution(String criteria) throws DAOException;
    Double averageAgeUsers() throws DAOException;
    Map<String, Double> averageAppRating(String criteria) throws DAOException;
    Map<String, Double> averageAppRatingByAgeRange() throws DAOException;

    //Neo4J queries
    void createNode(RegisteredUserDTO registeredUserDTO) throws DAOException;
    void follow(String followerUserId, String followingUserId) throws DAOException;
    void unfollow(String followerUserId, String followingUserId) throws DAOException;
    List<RegisteredUserDTO> getFollowing(String userId) throws DAOException;
    List<RegisteredUserDTO> getFollowers(String userId) throws DAOException;
    List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException;

}