package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDAONeo4JImpl extends BaseNeo4JDAO implements UserDAO {

    //Create a Neo4J User node

    @Override
    public void createUserNode(String id, String username, String picture) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (u:User {id: $id, username: $username, picture: $picture})";
            session.run(query, Map.of("id", id, "title", username, "picture", picture));

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //follow a user OK
    @Override
    public void followUser(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId}), (following:User {id: $followingUserId}) " +
                    "MERGE (follower)-[r:FOLLOWS]->(following) ";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    // unfollow a user OK
    @Override
    public void unfollowUser(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId})-[r:FOLLOWS]->(following:User {id: $followingUserId}) DELETE r";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));

        }
    }

    //show list of following and followers OK
    @Override
    public List<RegisteredUserDTO> getFollowing(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $userId})-[:FOLLOWS]-(f:User) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<RegisteredUserDTO> list = records.stream()
                    .map(this::recordToRegisteredUserDTO)
                    .collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    private RegisteredUserDTO recordToRegisteredUserDTO(Record record) {
        Map<String, Object> map = record.asMap();
        RegisteredUserDTO registeredUserDTO = new RegisteredUserDTO();
        registeredUserDTO.setId(new ObjectId(String.valueOf(map.get("id"))));
        registeredUserDTO.setUsername((String) map.get("username"));
        registeredUserDTO.setProfilePicUrl((String) map.get("picture"));
        return registeredUserDTO;
    }

    //OK
    @Override
    public List<RegisteredUserDTO> getFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (f:User)-[:FOLLOWS]->(following:User {id: $userId}) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<RegisteredUserDTO> list = records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //Suggest users based on common following OK
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
            List<RegisteredUserDTO> list = records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    @Override
    public ObjectId register(User user) throws DAOException {
        return null;
    }

    @Override
    public void remove(ObjectId id) throws DAOException {

    }

    @Override
    public RegisteredUser authenticate(String email, String password) throws DAOException {
        return null;
    }

    @Override
    public RegisteredUser find(ObjectId id) throws DAOException {
        return null;
    }

    @Override
    public List<RegisteredUserDTO> find(String username) throws DAOException {
        return null;
    }

    @Override
    public List<RegisteredUserDTO> findAll() throws DAOException {
        return null;
    }

    @Override
    public void update(RegisteredUser user) throws DAOException {

    }

    @Override
    public List<Document> getGenderDistribution() throws DAOException {
        return null;
    }

    @Override
    public int averageAgeUsers() throws DAOException {
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
    public int averageAppRatingByAge(int yearOfBirth) throws DAOException {
        return 0;
    }

    @Override
    public int averageAppRatingByLocation(String location) throws DAOException {
        return 0;
    }

    @Override
    public List<Document> averageAppRatingByGender() throws DAOException {
        return null;
    }

}
