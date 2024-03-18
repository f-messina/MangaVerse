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

public class UserDAOImpl extends BaseNeo4JDAO implements UserDAO {

    @Override
    public void createNode(RegisteredUserDTO registeredUserDTO) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (u:User {id: $id, username: $username, picture: $picture})";
            session.run(query, Map.of("id", registeredUserDTO.getId(), "title", registeredUserDTO.getUsername(), "picture", registeredUserDTO.getProfilePicUrl()));

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
    public User getInfoForSuggestions(String userId) throws DAOException {
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
    public Map<String, Integer> getDistribution(String criteria) throws DAOException {
        return null;
    }


    @Override
    public Double averageAgeUsers() throws DAOException {
        return null;
    }


    @Override
    public Map<String, Double> averageAppRating(String criteria) throws DAOException {
        return null;
    }

    @Override
    public Map<String, Double> averageAppRatingByAgeRange() throws DAOException {
        return null;
    }


}
