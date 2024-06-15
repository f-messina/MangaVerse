package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.driver.types.Node;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

/**
 * Implementation of the MediaContentDAO interface for User objects, providing crud operations
 * and operations to get suggestions and analytics from the Neo4j database.
 * @see BaseNeo4JDAO
 * @see UserDAO
 * @see User
 */
public class UserDAONeo4JImpl extends BaseNeo4JDAO implements UserDAO {

    /**
     * Creates a node for a RegisteredUser in the Neo4j database.
     *
     * @param user              The UserRegistrationDTO object containing the user's information.
     * @throws DAOException     If an error occurs while creating the user node.
     */
    @Override
    public void saveUser(UserRegistrationDTO user) throws DAOException {
        try (Session session = getSession()) {
            String query = "CREATE (u:User {id: $id, username: $username}) RETURN u";
            Value params = parameters("id", user.getId(), "username", user.getUsername());

            session.executeWrite(tx -> {
                boolean created = tx.run(query, params).hasNext();

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

    /**
     * Updates the information of a user in the Neo4j database.
     *
     * @param user              The User object containing the updated user information.
     *                          The object must have at least one field to update (username or profile picture URL).
     * @throws DAOException     If an error occurs while updating the user node.
     */
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

    /**
     * Deletes a user node from the Neo4j database.
     *
     * @param userId            The ID of the user to be deleted.
     * @throws DAOException     If an error occurs while deleting the user node.
     */
    @Override
    public void deleteUser(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $id}) DETACH DELETE u RETURN u";
            Value params = parameters("id", userId);

            session.executeWrite(tx -> {
                boolean deleted = tx.run(query, params).hasNext();

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
     * @param userId                The ID of the user initiating the follow action.
     * @param followedUserId        The ID of the user being followed.
     * @throws DAOException         If an error occurs while establishing the 'follow' relationship.
     */
    @Override
    public void follow(String userId, String followedUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = """
                    MATCH (u:User {id: $userId}), (f:User {id: $followedUserId})
                    WHERE NOT (u)-[:FOLLOWS]->(f)
                    CREATE (u)-[r:FOLLOWS]->(f)
                    RETURN r
                    """;
            Value params = parameters("userId", userId, "followedUserId", followedUserId);

            session.executeWrite(tx -> {
                boolean created = tx.run(query, params).hasNext();

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
     * @param followerUserId        The ID of the user initiating the unfollow action.
     * @param followingUserId       The ID of the user being unfollowed.
     * @throws DAOException         If an error occurs while removing the 'follow' relationship.
     */
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = """
                    MATCH (:User {id: $followerUserId})-[r:FOLLOWS]->(:User {id: $followingUserId})
                    DELETE r
                    RETURN r
                    """;
            Value params = parameters("followerUserId", followerUserId, "followingUserId", followingUserId);

            session.executeWrite(tx -> {
                    boolean deleted = tx.run(query, params).hasNext();

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

    /**
     * Checks if a user is following another user in the Neo4j database.
     *
     * @param userId            The ID of the user to check if it is following another user.
     * @param followedUserId    The ID of the user to check if it is being followed.
     * @return                  True if the user is following the other user, false otherwise.
     * @throws DAOException     If an error occurs while checking if the user is following another user.
     */
    @Override
    public boolean isFollowing(String userId, String followedUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $userId})-[r:FOLLOWS]->(:User {id: $followedUserId}) RETURN r";
            Value params = parameters("userId", userId, "followedUserId", followedUserId);

            Boolean followed = session.executeRead(
                    tx -> tx.run(query, params).hasNext()
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

    /**
     * Retrieves the number of followers for a specific user from the Neo4j database.
     *
     * @param userId            The ID of the user whose followers are to be counted.
     * @return                  The number of followers for the specified user.
     * @throws DAOException     If an error occurs while retrieving the number of followers.
     */
    @Override
    public Integer getNumOfFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User)-[:FOLLOWS]->(u:User {id: $userId}) RETURN COUNT(follower) AS numOfFollowers";
            Value params = parameters("userId", userId);

            Value value = session.executeRead(
                    tx -> tx.run(query, params).single().get("numOfFollowers")
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

    /**
     * Retrieves the number of users followed by a specific user from the Neo4j database.
     *
     * @param userId            The ID of the user whose followed users are to be counted.
     * @return                  The number of users followed by the specified user.
     * @throws DAOException     If an error occurs while retrieving the number of followed users.
     */
    @Override
    public Integer getNumOfFollowed(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(followed:User) RETURN COUNT(followed) AS numOfFollowed";
            Value params = parameters("userId", userId);

            Value value = session.executeRead(
                    tx -> tx.run(query, params).single().get("numOfFollowed")
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
     * Retrieves a list of users following a specific user from the Neo4j database.
     *
     * @param userId            The ID of the user whose followers are to be retrieved.
     * @param loggedUserId      The ID of the user requesting the list of followers.
     * @return                  A list of RegisteredUserDTO objects representing the followers of the specified user.
     * @throws DAOException     If an error occurs while retrieving the followers list.
     */
    @Override
    public List<UserSummaryDTO> searchFollowing(String userId, String username, String loggedUserId) throws DAOException {
        try (Session session = getSession()) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            StringBuilder queryBuilder = new StringBuilder("MATCH (:User {id: $userId})-[:FOLLOWS]->(followed:User) ");
            boolean hasUsernameCondition = StringUtils.isNotBlank(username);
            boolean hasLoggedUserIdCondition = StringUtils.isNotBlank(loggedUserId);

            if (hasUsernameCondition || hasLoggedUserIdCondition) {
                queryBuilder.append("WHERE ");
                if (hasUsernameCondition) {
                    queryBuilder.append("toLower(followed.username) CONTAINS toLower($username) ");
                    params.put("username", username);
                }
                if (hasLoggedUserIdCondition) {
                    if (hasUsernameCondition)
                        queryBuilder.append("AND ");
                    queryBuilder.append("followed.id <> $loggedUserId ");
                    params.put("loggedUserId", loggedUserId);
                }
            }
            queryBuilder.append("RETURN followed AS user ");
            queryBuilder.append("LIMIT 10");
            String query = queryBuilder.toString();

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
     * Retrieves a list of users followed by a specific user from the Neo4j database.
     *
     * @param userId            The ID of the user whose followed users are to be retrieved.
     * @param loggedUserId      The ID of the user requesting the list of followed users.
     * @return                  A list of RegisteredUserDTO objects representing the users followed by the specified user.
     * @throws DAOException     If an error occurs while retrieving the followed users list.
     */
    @Override
    public List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws DAOException {
        try (Session session = getSession()) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            StringBuilder queryBuilder = new StringBuilder("MATCH (follower:User)-[:FOLLOWS]->(:User {id: $userId}) ");
            boolean hasUsernameCondition = StringUtils.isNotBlank(username);
            boolean hasLoggedUserIdCondition = StringUtils.isNotBlank(loggedUserId);
            if (hasUsernameCondition || hasLoggedUserIdCondition) {
                queryBuilder.append("WHERE ");
                if (hasUsernameCondition) {
                    queryBuilder.append("toLower(follower.username) CONTAINS toLower($username) ");
                    params.put("username", username);
                }
                if (hasLoggedUserIdCondition) {
                    if (hasUsernameCondition)
                        queryBuilder.append("AND ");
                    queryBuilder.append("follower.id <> $loggedUserId ");
                    params.put("loggedUserId", loggedUserId);
                }
            }
            queryBuilder.append("RETURN follower AS user ");
            queryBuilder.append("LIMIT 10");
            String query = queryBuilder.toString();

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
     * Retrieves a list of suggested users for a specific user based on common followings from the Neo4j database.
     * The method performs the following steps:
     * 1. Retrieve users that follow user's followings and have more than 5 common followings.
     * 2. Retrieve users that are followed by user's followings and have more than 5 connections.
     * 3. Retrieve users that follow user's followings.
     *
     * @param userId            The ID of the user for whom suggested users are to be retrieved.
     * @return                  A list of RegisteredUserDTO objects representing suggested users for the specified user.
     * @throws DAOException     If an error occurs while retrieving suggested users.
     */
    @Override
    public List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId, Integer limit) throws DAOException {
        try (Session session = getSession()) {
            int n = limit == null ? 5 : limit;
            int remaining;

            // suggest users that follow user's followings and have more than 5 common followings
            String query = """
                    MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User)<-[:FOLLOWS]-(suggested:User)
                    WHERE NOT (u)-[:FOLLOWS]->(suggested) AND u <> suggested
                    WITH suggested, COUNT(DISTINCT following) AS commonFollowings
                    WHERE commonFollowings > 5
                    RETURN suggested as user
                    ORDER BY commonFollowings DESC
                    LIMIT $n
                    """;
            Value params = parameters("userId", userId, "n", n);

            List<UserSummaryDTO> suggested = session.executeRead(
                    tx -> tx.run(query, params).list()
            ).stream()
                    .map(this::recordToUserSummaryDTO)
                    .collect(Collectors.toList());

            remaining = n - suggested.size();

            // if there are not enough suggestions, suggest users that are followed by the user's followings and have more than 5 connections
            if (remaining > 0) {
                String query2 = """
                        MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User)-[:FOLLOWS]->(suggested:User)
                        WHERE NOT (u)-[:FOLLOWS]->(suggested) AND u <> suggested
                        WITH suggested, COUNT(DISTINCT following) AS commonUsers
                        WHERE commonUsers > 5
                        RETURN suggested as user
                        ORDER BY commonUsers DESC
                        LIMIT $n
                        """;
                Value params2 = parameters("userId", userId, "n", n);

                List<Record> records = session.executeRead(tx -> tx.run(query2, params2).list());
                for (Record record : records) {
                    UserSummaryDTO userDTO = recordToUserSummaryDTO(record);
                    if (!suggested.contains(userDTO))
                        suggested.add(userDTO);
                    if (suggested.size() == n)
                        break;
                }

                remaining = n - suggested.size();
            }

            // if there are still not enough suggestions, suggest users that follow the user's followings
            if (remaining > 0) {
                String query3 = """
                        MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User)<-[:FOLLOWS]-(suggested:User)
                        WHERE NOT (u)-[:FOLLOWS]->(suggested) AND u <> suggested
                        WITH suggested, COUNT(DISTINCT following) AS commonFollowings
                        RETURN suggested as user
                        ORDER BY commonFollowings DESC
                        LIMIT $n
                        """;
                Value params3 = parameters("userId", userId, "n", n);

                List<Record> records = session.executeRead(tx -> tx.run(query3, params3).list());
                for (Record record : records) {
                    UserSummaryDTO userDTO = recordToUserSummaryDTO(record);
                    if (!suggested.contains(userDTO))
                        suggested.add(userDTO);
                    if (suggested.size() == n)
                        break;
                }
            }

            return suggested.isEmpty() ? null : suggested;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of suggested users for a specific user based on common likes from the Neo4j database.
     * The method performs the following steps:
     * 1. Retrieve users who like the same media content as the specified user in the last 6 month.
     * 2. Retrieve users who like the same media content as the specified user in the last year.
     * 3. Retrieve users who like the same media content as the specified user.
     *
     * @param userId            The ID of the user for whom suggested users are to be retrieved.
     * @param limit             The maximum number of suggested users to retrieve.
     * @param type              The type of media content to consider for the suggestions (Anime or Manga).
     * @return                  A list of RegisteredUserDTO objects representing suggested users for the specified user.
     * @throws DAOException     If an error occurs while retrieving suggested users.
     */
    @Override
    public List<UserSummaryDTO> suggestUsersByCommonLikes(String userId, Integer limit, MediaContentType type) throws DAOException {

        try (Session session = getSession()) {
            if (type == null) {
                throw new IllegalArgumentException("Media content type must be specified");
            }

            int n = limit == null ? 5 : limit;
            int remaining;

            StringBuilder queryBuilder = new StringBuilder();
            if (type == MediaContentType.ANIME)
                queryBuilder.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Anime)<-[:LIKE]-(suggested:User) ");
            else
                queryBuilder.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Manga)<-[:LIKE]-(suggested:User) ");
            queryBuilder.append("""
                    WHERE u <> suggested AND r.date >= date($date)
                    WITH suggested, COUNT(DISTINCT media) AS commonLikes
                    WHERE commonLikes > $min
                    RETURN suggested AS user, commonLikes
                    ORDER BY commonLikes DESC
                    LIMIT $n
                    """);
            String query1 = queryBuilder.toString();
            Value params1 = parameters("userId", userId, "n", n, "date", LocalDate.now().minusMonths(6), "min", 5);

            List<UserSummaryDTO> suggested = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            ).stream()
                    .map(this::recordToUserSummaryDTO)
                    .collect(Collectors.toList());

            remaining = n - suggested.size();

            if (remaining > 0) {
                Value params2 = parameters("userId", userId, "n", n, "date", LocalDate.now().minusYears(1), "min", 5);

                List<Record> records = session.executeRead(tx -> tx.run(query1, params2).list());
                for (Record record : records) {
                    UserSummaryDTO userDTO = recordToUserSummaryDTO(record);
                    if (!suggested.contains(userDTO))
                        suggested.add(userDTO);
                    if (suggested.size() == n)
                        break;
                }

                remaining = n - suggested.size();
            }

            if(remaining > 0) {
                StringBuilder queryBuilder3 = new StringBuilder();
                if (type == MediaContentType.ANIME)
                    queryBuilder3.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Anime)<-[:LIKE]-(suggested:User) ");
                else
                    queryBuilder3.append("MATCH (u:User {id: $userId})-[r:LIKE]->(media:Manga)<-[:LIKE]-(suggested:User) ");
                queryBuilder3.append("""
                        WHERE u <> suggested
                        WITH suggested, COUNT(DISTINCT media) AS commonLikes
                        RETURN suggested AS user, commonLikes
                        ORDER BY commonLikes DESC
                        LIMIT $n
                        """);
                String query2 = queryBuilder3.toString();
                Value params3 = parameters("userId", userId, "n", n);

                List<Record> records = session.executeRead(tx -> tx.run(query2, params3).list());
                for (Record record : records) {
                    UserSummaryDTO userDTO = recordToUserSummaryDTO(record);
                    if (!suggested.contains(userDTO))
                        suggested.add(userDTO);
                    if (suggested.size() == n)
                        break;
                }
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
    public void updateNumOfFollowings(String userId, Integer followed) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public void rateApp(String userId, Integer rating) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
}
