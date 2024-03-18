package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import org.bson.Document;
import java.util.List;
import java.util.Map;

public interface UserDAO {
    //MongoDB queries
<<<<<<< HEAD
    public void createUser(UserRegistrationDTO user) throws DAOException;
    public void updateUser(User user) throws DAOException;
    public void deleteUser(String userId) throws DAOException;
    public UserSummaryDTO authenticate(String email, String password) throws DAOException;
    public RegisteredUser readUser(String userId, boolean onlyStatsInfo) throws DAOException;
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException;
    List<Document> getGenderDistribution() throws DAOException;
    Integer averageAgeUsers() throws DAOException;
    List<Document> getLocationDistribution() throws DAOException;
    List<Document> getUsersByAgeRange() throws DAOException;
    List<Document> getUsersRegisteredByYear() throws DAOException;
    Integer averageAppRatingByAge(Integer yearOfBirth) throws DAOException;
    Integer averageAppRatingByLocation(String location) throws DAOException;
    List<Document> averageAppRatingByGender() throws DAOException;
=======
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
>>>>>>> noemi

    //Neo4J queries
    void createNode(UserSummaryDTO userSummaryDTO) throws DAOException;
    void follow(String followerUserId, String followingUserId) throws DAOException;
    void unfollow(String followerUserId, String followingUserId) throws DAOException;
    List<UserSummaryDTO> getFollowing(String userId) throws DAOException;
    List<UserSummaryDTO> getFollowers(String userId) throws DAOException;
    List<UserSummaryDTO> suggestUsers(String userId) throws DAOException;
}