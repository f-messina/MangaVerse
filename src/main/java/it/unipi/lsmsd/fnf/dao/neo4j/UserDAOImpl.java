package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.Document;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDAOImpl extends BaseNeo4JDAO implements UserDAO {

    @Override
    public void createNode(UserSummaryDTO userSummaryDTO) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (u:User {id: $id, username: $username, picture: $picture})";
            session.run(query, Map.of("id", userSummaryDTO.getId(), "title", userSummaryDTO.getUsername(), "picture", userSummaryDTO.getProfilePicUrl()));

        } catch (Exception e) {
            throw new DAOException("Error while creating user node", e);
        }
    }

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

    @Override
    public void unfollow(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId})-[r:FOLLOWS]->(following:User {id: $followingUserId}) DELETE r";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));
        } catch (Exception e) {
            throw new DAOException("Error while unfollowing user", e);
        }
    }

    @Override
    public List<UserSummaryDTO> getFollowing(String userId) throws DAOException {
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

    private UserSummaryDTO recordToRegisteredUserDTO(Record record) {
        Map<String, Object> map = record.asMap();
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO();
        userSummaryDTO.setId(String.valueOf(map.get("id")));
        userSummaryDTO.setUsername((String) map.get("username"));
        userSummaryDTO.setProfilePicUrl((String) map.get("picture"));
        return userSummaryDTO;
    }

    @Override
    public List<UserSummaryDTO> getFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (f:User)-[:FOLLOWS]->(following:User {id: $userId}) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while retrieving the follower list", e);
        }
    }

    @Override
    public List<UserSummaryDTO> suggestUsers(String userId) throws DAOException {
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
    public void createUser(UserRegistrationDTO user) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }

    @Override
    public void updateUser(User user) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }

    @Override
    public void deleteUser(String userId) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }

    @Override
    public RegisteredUser authenticate(String email, String password) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }

    @Override
    public RegisteredUser getById(String userId) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }

    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }

    @Override
    public List<Document> getGenderDistribution() throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public Integer averageAgeUsers() throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public List<Document> getLocationDistribution() throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public List<Document> getUsersByAgeRange() throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public List<Document> getUsersRegisteredByYear() throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public Integer averageAppRatingByAge(Integer yearOfBirth) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public Integer averageAppRatingByLocation(String location) throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
    @Override
    public List<Document> averageAppRatingByGender() throws DAOException {
        throw new DAOException("Method not available in Neo4j");
    }
}
