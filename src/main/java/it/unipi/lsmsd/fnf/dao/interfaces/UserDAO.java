package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import org.bson.Document;
import java.util.List;
import java.util.Map;

public interface UserDAO {
    //MongoDB queries
    public void createUser(UserRegistrationDTO user) throws DAOException;
    public void updateUser(User user) throws DAOException;
    public void deleteUser(String userId) throws DAOException;
    public LoggedUserDTO authenticate(String email, String password) throws DAOException;
    public RegisteredUser readUser(String userId, boolean onlyStatsInfo) throws DAOException;
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException;

    // CRUD operations for personal lists
    void insertList(PersonalListSummaryDTO listSummaryDTO) throws DAOException;
    void updateList(PersonalListSummaryDTO listSummaryDTO) throws DAOException;
    void deleteList(String userId, String listId) throws DAOException;
    void addToList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException;
    void removeFromList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException;
    void removeElementInListWithoutMedia() throws DAOException;

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