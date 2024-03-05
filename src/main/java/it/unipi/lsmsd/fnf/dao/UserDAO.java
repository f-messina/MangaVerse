package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import org.bson.Document;
import java.util.List;

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
    //Find the distribution of genders, of ages, of locations
    List<Document> getDistribution(String criteria) throws DAOException;

    //List<Document> getGenderDistribution() throws DAOException;
    Integer averageAgeUsers() throws DAOException;
    //List<Document> getLocationDistribution() throws DAOException;
    List<Document> getUsersByAgeRange() throws DAOException;
    List<Document> getUsersRegisteredByYear() throws DAOException;
    //Integer averageAppRatingByAge(Integer yearOfBirth) throws DAOException;
    //Integer averageAppRatingByLocation(String location) throws DAOException;
    //List<Document> averageAppRatingByGender() throws DAOException;

    //Find average app_rating based on the age, location and gender.
    int averageAppRating(String criteria, String value) throws DAOException;

    //Find the average app_rating of users based on group af ages
    List<Integer> averageAppRatingByAgeRange() throws DAOException;

    //Neo4J queries
    void createNode(RegisteredUserDTO registeredUserDTO) throws DAOException;
    void follow(String followerUserId, String followingUserId) throws DAOException;
    void unfollow(String followerUserId, String followingUserId) throws DAOException;
    List<RegisteredUserDTO> getFollowing(String userId) throws DAOException;
    List<RegisteredUserDTO> getFollowers(String userId) throws DAOException;
    List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException;

}