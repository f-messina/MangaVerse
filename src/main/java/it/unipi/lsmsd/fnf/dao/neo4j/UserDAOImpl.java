package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.Document;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Implementation of the UserDAO interface for handling User-related operations in Neo4j.
 */
public class UserDAOImpl extends BaseNeo4JDAO implements UserDAO {

    /**
     * Creates a node for a RegisteredUser in the Neo4j database.
     *
     * @param registeredUserDTO The RegisteredUserDTO object containing information about the user to be created.
     * @throws DAOException If an error occurs while creating the user node.
     */
    @Override
    public void createNode(RegisteredUserDTO registeredUserDTO) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (u:User {id: $id, username: $username, picture: $picture})";
            session.run(query, Map.of("id", registeredUserDTO.getId(), "title", registeredUserDTO.getUsername(), "picture", registeredUserDTO.getProfilePicUrl()));

        } catch (Exception e) {
            throw new DAOException("Error while creating user node", e);
        }
    }

    /**
     * Establishes a 'follow' relationship between two users in the Neo4j database.
     *
     * @param followerUserId   The ID of the user initiating the follow action.
     * @param followingUserId  The ID of the user being followed.
     * @throws DAOException If an error occurs while establishing the 'follow' relationship.
     */
    @Override
    public void follow(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId}), (following:User {id: $followingUserId}) " +
                    "MERGE (follower)-[r:FOLLOWS]->(following) ";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));
        } catch (Exception e) {
            throw new DAOException("Error while following user", e);
        }
    }

    /**
     * Removes a 'follow' relationship between two users in the Neo4j database.
     *
     * @param followerUserId   The ID of the user initiating the unfollow action.
     * @param followingUserId  The ID of the user being unfollowed.
     * @throws DAOException If an error occurs while removing the 'follow' relationship.
     */
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId})-[r:FOLLOWS]->(following:User {id: $followingUserId}) DELETE r";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));
        } catch (Exception e) {
            throw new DAOException("Error while unfollowing user", e);
        }
    }

    /**
     * Retrieves a list of users followed by a specific user from the Neo4j database.
     *
     * @param userId The ID of the user whose followed users are to be retrieved.
     * @return A list of RegisteredUserDTO objects representing the users followed by the specified user.
     * @throws DAOException If an error occurs while retrieving the followed users list.
     */
    @Override
    public List<RegisteredUserDTO> getFollowing(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $userId})-[:FOLLOWS]-(f:User) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream()
                    .map(this::recordToRegisteredUserDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while retrieving the following list", e);
        }
    }

    private RegisteredUserDTO recordToRegisteredUserDTO(Record record) {
        Map<String, Object> map = record.asMap();
        RegisteredUserDTO registeredUserDTO = new RegisteredUserDTO();
        registeredUserDTO.setId(String.valueOf(map.get("id")));
        registeredUserDTO.setUsername((String) map.get("username"));
        registeredUserDTO.setProfilePicUrl((String) map.get("picture"));
        return registeredUserDTO;
    }

    /**
     * Retrieves a list of users following a specific user from the Neo4j database.
     *
     * @param userId The ID of the user whose followers are to be retrieved.
     * @return A list of RegisteredUserDTO objects representing the followers of the specified user.
     * @throws DAOException If an error occurs while retrieving the followers list.
     */
    @Override
    public List<RegisteredUserDTO> getFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (f:User)-[:FOLLOWS]->(following:User {id: $userId}) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while retrieving the follower list", e);
        }
    }

    /**
     * Retrieves a list of suggested users for a specific user from the Neo4j database.
     *
     * @param userId The ID of the user for whom suggested users are to be retrieved.
     * @return A list of RegisteredUserDTO objects representing suggested users for the specified user.
     * @throws DAOException If an error occurs while retrieving suggested users.
     */
    @Override
    public List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $userId})-[:FOLLOWS]->(following:User)-[:FOLLOWS]->(suggested:User) " +
                    "WHERE NOT (:User{id: $userId})-[:FOLLOWS]->(suggested) " +
                    "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                    "WHERE commonFollowers > 5 " +
                    "RETURN suggested.id as id, suggested.username as username, suggested.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();

            return records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while suggesting users", e);
        }
    }

    // Methods available only in MongoDB
    @Override
    public String register(User user) throws DAOException {
        return null;
    }
    @Override
    public void remove(String id) throws DAOException {
    }
    @Override
    public RegisteredUser authenticate(String email, String password) throws DAOException {
        return null;
    }
    @Override
    public RegisteredUser find(String id) throws DAOException {
        return null;
    }
    @Override
    public List<RegisteredUserDTO> search(String username) throws DAOException {
        return null;
    }
    @Override
    public List<RegisteredUserDTO> findAll() throws DAOException {
        return null;
    }
    @Override
    public void update(User user) throws DAOException {
    }
    @Override
    public void update(RegisteredUser user) throws DAOException {
    }
    @Override
    public List<Document> getGenderDistribution() throws DAOException {
        return null;
    }
    @Override
    public Integer averageAgeUsers() throws DAOException {
        return 0;
    }
    @Override
    public List<Document> getLocationDistribution() throws DAOException {
        return null;
    }
    @Override
    public List<Document> getUsersByAgeRange() throws DAOException {
        return null;
    }
    @Override
    public List<Document> getUsersRegisteredByYear() throws DAOException {
        return null;
    }
    @Override
    public Integer averageAppRatingByAge(Integer yearOfBirth) throws DAOException {
        return 0;
    }
    @Override
    public Integer averageAppRatingByLocation(String location) throws DAOException {
        return 0;
    }
    @Override
    public List<Document> averageAppRatingByGender() throws DAOException {
        return null;
    }
}
