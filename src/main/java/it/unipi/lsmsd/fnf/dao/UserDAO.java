package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserDAO {
    ObjectId register(User user) throws DAOException;
    void remove(ObjectId id) throws DAOException;
    RegisteredUser authenticate(String email, String password) throws DAOException;
    RegisteredUser find(ObjectId id) throws DAOException;
    List<RegisteredUserDTO> find(String username) throws DAOException;
    List<RegisteredUserDTO> findAll() throws DAOException;


    //MongoDB queries
    //Find the distribution of genders between users
    List<Document> getGenderDistribution() throws DAOException;

    //Find the average age of users
    int averageAgeUsers() throws DAOException;

    //Find the distribution of users by location
    List<Document> getLocationDistribution() throws DAOException;

    //Find how many users there are grouped by age range
    List<Document> getUsersByAgeRange() throws DAOException;

    //Find how many users registered for each year
    List<Document> getUsersRegisteredByYear() throws DAOException;

    //Find average app_rating based on the age of users
    int averageAppRatingByAge(int yearOfBirth) throws DAOException;

    //Find average app_rating based on the location of users
    int averageAppRatingByLocation(String location) throws DAOException;

    //Find average app_rating based on the gender of users
    List<Document> averageAppRatingByGender() throws DAOException;

    //Neo4J queries

    void createUserNode(String id, String username, String picture) throws DAOException;

    void followUser(String followerUserId, String followingUserId) throws DAOException;


    void unfollowUser(String followerUserId, String followingUserId) throws DAOException;

    List<RegisteredUserDTO> getFollowing(String userId) throws DAOException;

    List<RegisteredUserDTO> getFollowers(String userId) throws DAOException;


    List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException;


    void update(User user) throws DAOException;

}