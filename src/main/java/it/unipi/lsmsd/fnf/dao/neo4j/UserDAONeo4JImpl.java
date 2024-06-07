package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            String query = "CREATE (u:User {id: $id, username: $username}) RETURN u";

            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("id", user.getId(), "username", user.getUsername())).hasNext();

                if (!created) {
                    throw new Neo4jException("Error while creating user node with username " + user.getUsername());
                }
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
            StringBuilder queryBuilder = new StringBuilder("MATCH (u:User {id: $id}) ");
            Map<String, Object> param = new HashMap<>();
            param.put("id", user.getId());

            if (user.getUsername() == null && user.getProfilePicUrl() == null) {
                throw new IllegalArgumentException("Manga object must have at least one field to update");
            }
            if (user.getUsername() != null && user.getProfilePicUrl() != null && !user.getProfilePicUrl().equals(Constants.NULL_STRING)) {
                queryBuilder.append("SET u.username = $username, u.picture = $picture ");
                param.put("username", user.getUsername());
                param.put("picture", user.getProfilePicUrl());
            } else {
                if (user.getUsername() != null) {
                    queryBuilder.append("SET u.username = $username ");
                    param.put("username", user.getUsername());
                } else if (user.getProfilePicUrl().equals(Constants.NULL_STRING)) {
                    queryBuilder.append("REMOVE u.picture ");
                } else {
                    queryBuilder.append("SET u.picture = $picture ");
                    param.put("picture", user.getProfilePicUrl());
                }
            }
            queryBuilder.append("RETURN u");
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
            String query = "MATCH (u:User {id: $id}) DETACH DELETE u RETURN u";

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
     * @param userId   The ID of the user initiating the follow action.
     * @param followedUserId  The ID of the user being followed.
     * @throws DAOException If an error occurs while establishing the 'follow' relationship.
     */
    @Override
    public void follow(String userId, String followedUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (f:User {id: $followedUserId}) " +
                    "WHERE NOT (u)-[:FOLLOWS]->(f) " +
                    "CREATE (u)-[r:FOLLOWS]->(f) " +
                    "RETURN r";

            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("userId", userId, "followedUserId", followedUserId)).hasNext();

                if(!created)
                    throw new Neo4jException("Error while creating follow relationship between users with IDs " + userId + " and " + followedUserId);

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
            String query = "MATCH (:User {id: $followerUserId})-[r:FOLLOWS]->(:User {id: $followingUserId}) " +
                    "DELETE r " +
                    "RETURN r";

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
    public boolean isFollowing(String userId, String followedUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $userId})-[r:FOLLOWS]->(:User {id: $followedUserId}) RETURN r";
            Boolean followed = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "followedUserId", followedUserId)).hasNext()
            );

            if (followed == null)
                throw new Neo4jException("Error while checking if user with ID " + userId + " is following user with ID " + followedUserId);

            return followed;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Integer getNumOfFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User)-[:FOLLOWS]->(u:User {id: $userId}) RETURN COUNT(follower) AS numOfFollowers";

            Value value = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId)).single().get("numOfFollowers")
            );

            if (value == null)
                throw new Neo4jException("Error while retrieving number of followers for user with ID " + userId);

            return value.asInt();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Integer getNumOfFollowed(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(followed:User) RETURN COUNT(followed) AS numOfFollowed";

            Value value = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId)).single().get("numOfFollowed")
            );

            if (value == null)
                throw new Neo4jException("Error while retrieving number of followed users for user with ID " + userId);

            return value.asInt();

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
     * @param loggedUserId The ID of the user requesting the list of followed users.
     * @return A list of RegisteredUserDTO objects representing the users followed by the specified user.
     * @throws DAOException If an error occurs while retrieving the followed users list.
     */
    @Override
    public List<UserSummaryDTO> getFirstNFollowing(String userId, String loggedUserId) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (:User {id: $userId})-[:FOLLOWS]->(followed:User) ");
            if (loggedUserId != null) {
                queryBuilder.append("WHERE followed.id <> $loggedUserId ");
            }
            queryBuilder.append("RETURN followed AS user ");
            queryBuilder.append("ORDER BY followed.username ");
            queryBuilder.append("LIMIT 10");
            String query = queryBuilder.toString();

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            if (loggedUserId != null) {
                params.put("loggedUserId", loggedUserId);
            }

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, params).list()
            );

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .toList();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public List<UserSummaryDTO> searchFollowing(String userId, String username, String loggedUserId) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (:User {id: $userId})-[:FOLLOWS]->(followed:User) ");
            queryBuilder.append("WHERE toLower(followed.username) CONTAINS toLower($username) ");
            if (loggedUserId != null) {
                queryBuilder.append("AND followed.id <> $loggedUserId ");
            }
            queryBuilder.append("RETURN followed AS user ");
            queryBuilder.append("LIMIT 10");
            String query = queryBuilder.toString();

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("username", username);
            if (loggedUserId != null) {
                params.put("loggedUserId", loggedUserId);
            }

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, params).list()
            );

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
     * @param loggedUserId The ID of the user requesting the list of followers.
     * @return A list of RegisteredUserDTO objects representing the followers of the specified user.
     * @throws DAOException If an error occurs while retrieving the followers list.
     */
    @Override
    public List<UserSummaryDTO> getFirstNFollowers(String userId, String loggedUserId) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (follower:User)-[:FOLLOWS]->(:User {id: $userId}) ");
            if (loggedUserId != null) {
                queryBuilder.append("WHERE follower.id <> $loggedUserId ");
            }
            queryBuilder.append("RETURN follower AS user ");
            queryBuilder.append("ORDER BY follower.username ");
            queryBuilder.append("LIMIT 10");
            String query = queryBuilder.toString();

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            if (loggedUserId != null) {
                params.put("loggedUserId", loggedUserId);
            }

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, params).list()
            );

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .toList();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (follower:User)-[:FOLLOWS]->(:User {id: $userId}) ");
            queryBuilder.append("WHERE toLower(follower.username) CONTAINS toLower($username) ");
            if (loggedUserId != null) {
                queryBuilder.append("AND follower.id <> $loggedUserId ");
            }
            queryBuilder.append("RETURN follower AS user ");
            queryBuilder.append("LIMIT 10");
            String query = queryBuilder.toString();

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("username", username);
            if (loggedUserId != null) {
                params.put("loggedUserId", loggedUserId);
            }

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, params).list()
            );

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
    public List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId, Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;
        List<UserSummaryDTO> suggested;

        try (Session session = getSession()) {

            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User)<-[:FOLLOWS]-(suggested:User) " +
                    "WHERE NOT (u)-[:FOLLOWS]->(suggested) AND u <> suggested " +
                    "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                    "WHERE commonFollowers > 5 " +
                    "RETURN suggested as user " +
                    "ORDER BY commonFollowers DESC " +
                    "LIMIT $n";

            int finalN = n;
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "n", finalN)).list()
            );
            suggested = records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .collect(Collectors.toList());




            n -= records.isEmpty() ? 0 : records.size();
            if (n > 0) {
                //suggest users followed by user I follow
                String query2 = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User)-[:FOLLOWS]->(suggested:User) " +
                        "WHERE NOT (u)-[:FOLLOWS]->(suggested) AND u <> suggested " +
                        "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                        "WHERE commonFollowers > 5 " +
                        "RETURN suggested as user " +
                        "ORDER BY commonFollowers DESC " +
                        "LIMIT $n";
                Value params = parameters("userId", userId, "n", n);
                List<Record> records2 = session.executeRead(
                        tx -> tx.run(query2, params).list()
                );
                records2.stream()
                        .map(this::recordToUserSummaryDTO)
                        .forEach(suggested::add);
            }

            n -= records.isEmpty() ? 0 : records.size();
            if (n > 0) {
                String query3 = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User)<-[:FOLLOWS]-(suggested:User) " +
                        "WHERE NOT (u)-[:FOLLOWS]->(suggested) AND u <> suggested " +
                        "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                        "RETURN suggested as user " +
                        "ORDER BY commonFollowers DESC " +
                        "LIMIT $n";

                Value params = parameters("userId", userId, "n", n);
                List<Record> records3 = session.executeRead(
                        tx -> tx.run(query3, params).list()
                );
                records3.stream()
                        .map(this::recordToUserSummaryDTO)
                        .forEach(suggested::add);

            }

            return suggested.isEmpty() ? null : suggested;


        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public List<UserSummaryDTO> suggestUsersByCommonLikes(String userId, Integer limit, MediaContentType type) throws DAOException {
        int n = limit == null ? 5 : limit;
        List<UserSummaryDTO> suggested;
        try (Session session = getSession()) {
            if (type == null) {
                throw new IllegalArgumentException("Media content type must be specified");
            }
            StringBuilder queryBuilder = new StringBuilder();
            if (type == MediaContentType.ANIME)
                queryBuilder.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Anime)<-[:LIKE]-(suggested:User) ");
            else
                queryBuilder.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Manga)<-[:LIKE]-(suggested:User) ");
            queryBuilder.append(" WHERE u <> suggested AND r.date >= date($date) " +
                    "WITH suggested, COUNT(DISTINCT media) AS commonLikes " +
                    "WHERE commonLikes > $min " +
                    "RETURN suggested AS user, commonLikes " +
                    "ORDER BY commonLikes DESC " +
                    "LIMIT $n");
            String query = queryBuilder.toString();
            System.out.println("query1");

            int finalN = n;
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "n", finalN, "date", LocalDate.now().minusMonths(1), "min", 2)).list()
            );
            System.out.println("records 1 " + records);
            suggested = records.stream()
                    .map(this::recordToUserSummaryDTO)
                    .collect(Collectors.toList());
            System.out.println("suggested 1" + suggested);

            n -= records.isEmpty() ? 0 : records.size();

            if(n>0) {
                StringBuilder queryBuilder2 = new StringBuilder();
                if (type == MediaContentType.ANIME)
                    queryBuilder2.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Anime)<-[:LIKE]-(suggested:User) ");
                else
                    queryBuilder2.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Manga)<-[:LIKE]-(suggested:User) ");
                queryBuilder2.append(" WHERE u <> suggested AND r.date >= date($date) " +
                        "WITH suggested, COUNT(DISTINCT media) AS commonLikes " +
                        "WHERE commonLikes > $min " +
                        "RETURN suggested AS user, commonLikes " +
                        "ORDER BY commonLikes DESC " +
                        "LIMIT $n");
                String query2 = queryBuilder2.toString();
                System.out.println("query2");
                int finalN1 = n;
                List<Record> records2 = session.executeRead(
                        tx -> tx.run(query2, parameters("userId", userId, "n", finalN1, "date", LocalDate.now().minusMonths(6), "min", 2)).list()
                );
                System.out.println("records 2 " + records2);
                suggested = records2.stream()
                        .map(this::recordToUserSummaryDTO)
                        .collect(Collectors.toList());
                System.out.println("suggested 2" + suggested);
            }
            n -= records.isEmpty() ? 0 : records.size();
            if(n>0) {
                StringBuilder queryBuilder3 = new StringBuilder();
                if (type == MediaContentType.ANIME)
                    queryBuilder3.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Anime)<-[:LIKE]-(suggested:User) ");
                else
                    queryBuilder3.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Manga)<-[:LIKE]-(suggested:User) ");
                queryBuilder3.append(" WHERE u <> suggested  " +
                        "WITH suggested, COUNT(DISTINCT media) AS commonLikes " +
                        "RETURN suggested AS user, commonLikes " +
                        "ORDER BY commonLikes DESC " +
                        "LIMIT $n");
                String query3 = queryBuilder3.toString();
                System.out.println("query3");
                int finalN2 = n;
                List<Record> records3 = session.executeRead(
                        tx -> tx.run(query3, parameters("userId", userId, "n", finalN2)).list()
                );
                System.out.println("records 3 " + records3);
                suggested = records3.stream()
                        .map(this::recordToUserSummaryDTO)
                        .collect(Collectors.toList());
                System.out.println("suggested 3" + suggested);
            }


            return suggested.isEmpty() ? null : suggested;

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
        if (userNode.containsKey("picture"))
            userSummaryDTO.setProfilePicUrl(userNode.get("picture").asString());

        return userSummaryDTO;
    }

    // Methods available only in MongoDB

    @Override
    public LoggedUserDTO authenticate(String email, String password) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public RegisteredUser readUser(String userId, boolean onlyStatsInfo, boolean isLoggedUserInfo) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public Map<String, Integer> getDistribution(String criteria) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public Map<String, Double> averageAppRating(String criteria) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public Map<String, Double> averageAppRatingByAgeRange() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public void updateNumOfFollowers(String userId, Integer followers) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public void updateNumOfFollowed(String userId, Integer followed) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public void rateApp(String userId, Integer rating) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

}
