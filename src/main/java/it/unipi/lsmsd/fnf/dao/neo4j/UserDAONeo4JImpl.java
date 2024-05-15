package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.driver.types.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.Values.parameters;
/**
 * Implementation of the UserDAO interface for handling User-related operations in Neo4j.
 */
public class UserDAONeo4JImpl extends BaseNeo4JDAO implements UserDAO {

    /**
     * Creates a node for a RegisteredUser in the Neo4j database.
     *
     * @param user The UserRegistrationDTO object containing the user's information.
     * @throws DAOException If an error occurs while creating the user node.
     */
    @Override
    public void saveUser(UserRegistrationDTO user) throws DAOException {
        try (Session session = getSession()) {
            String query = "CREATE (u:User {id: $id, username: $username, picture: $picture})";
            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("id", user.getId(), "title", user.getUsername(), "picture", Constants.DEFAULT_PROFILE_PICTURE)).hasNext();

                if(!created)
                    throw new Neo4jException("Error while creating user node with username " + user.getUsername());

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public void updateUser(User user) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (u:User {id: $id}) SET");

            if (user.getUsername() == null && user.getProfilePicUrl() == null) {
                throw new IllegalArgumentException("Manga object must have at least one field to update");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("id", user.getId());
            if (user.getUsername() != null) {
                queryBuilder.append(" u.username = $username");
                param.put("username", user.getUsername());
            }
            if (user.getProfilePicUrl() != null) {
                if (user.getUsername() != null)
                    queryBuilder.append(",");
                queryBuilder.append(" u.picture = $picture");
                param.put("picture", user.getProfilePicUrl());
            }
            String query = queryBuilder.toString();

            session.executeWrite(tx -> {
                boolean updated = tx.run(query, param).hasNext();

                if(!updated)
                    throw new Neo4jException("Error while updating user node with ID " + user.getId());

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public void deleteUser(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $id}) DETACH DELETE u";
            session.executeWrite(tx -> {
                boolean deleted = tx.run(query, parameters("id", userId)).hasNext();

                if(!deleted)
                    throw new Neo4jException("Error while deleting user node with ID " + userId);

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
                    "MERGE (follower)-[r:FOLLOWS]->(following)";

            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("followerUserId", followerUserId, "followingUserId", followingUserId)).hasNext();

                if(!created)
                    throw new Neo4jException("Error while creating follow relationship between users with IDs " + followerUserId + " and " + followingUserId);

                return null;
            });
        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
            session.executeWrite(tx -> {
                    boolean deleted = tx.run(query, parameters("followerUserId", followerUserId, "followingUserId", followingUserId)).hasNext();

                    if(!deleted)
                        throw new Neo4jException("Error while deleting follow relationship between users with IDs " + followerUserId + " and " + followingUserId);

                    return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public boolean isFollowing(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId})-[r:FOLLOWS]->(following:User {id: $followingUserId}) RETURN r";
            Boolean followed = session.executeRead(
                    tx -> tx.run(query, parameters("followerUserId", followerUserId, "followingUserId", followingUserId)).hasNext()
            );

            if (followed == null)
                throw new Neo4jException("Error while checking if user with ID " + followerUserId + " is following user with ID " + followingUserId);

            return followed;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
    public List<UserSummaryDTO> getFollowedUsers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]-(followed:User) " +
                    "RETURN followed AS user";
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId))
            ).list();

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .toList();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of users following a specific user from the Neo4j database.
     *
     * @param userId The ID of the user whose followers are to be retrieved.
     * @return A list of RegisteredUserDTO objects representing the followers of the specified user.
     * @throws DAOException If an error occurs while retrieving the followers list.
     */
    @Override
    public List<UserSummaryDTO> getFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User)-[:FOLLOWS]->(u:User {id: $userId}) RETURN follower AS user";
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId))
            ).list();

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .toList();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
    public List<UserSummaryDTO> suggestUsers(String userId, Integer limit) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $userId})-[:FOLLOWS]->(following:User)-[:FOLLOWS]->(suggested:User) " +
                    "WHERE NOT (:User{id: $userId})-[:FOLLOWS]->(suggested) " +
                    "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                    "WHERE commonFollowers > 5 " +
                    "RETURN suggested as user" +
                    "LIMIT $n";
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "n", limit == null ? 5 : limit))
            ).list();

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .toList();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    private UserSummaryDTO recordToUserSummaryDTO(Record record) {
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO();
        Node userNode = record.get("user").asNode();
        userSummaryDTO.setId(userNode.get("id").asString());
        userSummaryDTO.setUsername(userNode.get("username").asString());
        userSummaryDTO.setProfilePicUrl(userNode.get("picture").asString());

        return userSummaryDTO;
    }

    // Methods available only in MongoDB

    @Override
    public LoggedUserDTO authenticate(String email, String password) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public RegisteredUser readUser(String userId, boolean onlyStatsInfo) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
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
