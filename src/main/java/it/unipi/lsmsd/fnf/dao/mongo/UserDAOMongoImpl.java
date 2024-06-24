package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.exception.*;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.enums.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.DocumentUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.inc;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.RegisteredUserToDocument;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.UserToUnsetUserFieldsDocument;

/**
 * Implementation of UserDAO interface for MongoDB data access.
 * Provides methods for user registration, authentication, updating, and removal,
 * as well as methods for querying user data and performing statistical analyses.
 * @see BaseMongoDBDAO
 * @see UserDAO
 * @see User
 */
public class UserDAOMongoImpl extends BaseMongoDBDAO implements UserDAO {
    private static final String COLLECTION_NAME = "users";

    /**
     * Registers a new user in the system.
     * The method performs the following steps:
     * 1. Checks if the username and email are already in use.
     * 2. Inserts the new user into the database.
     *
     * @param user              The User object to be registered.
     * @throws DAOException     If an error occurs during the registration process,
     *                          such as the username or email already being in use.
     */
    @Override
    public void saveUser(UserRegistrationDTO user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            boolean usernameExists = usersCollection.countDocuments(eq("username", user.getUsername())) != 0;
            boolean emailExists = usersCollection.countDocuments(eq("email", user.getEmail())) != 0;
            if(usernameExists && emailExists)
                throw new DuplicatedException(DuplicatedExceptionType.GENERIC, "UserDAOMongoImpl: saveUser: Username and email already in use");
            else if(usernameExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_NAME, "UserDAOMongoImpl: saveUser: Username already in use");
            else if(emailExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_EMAIL, "UserDAOMongoImpl: saveUser: Email already in use");

            Optional.ofNullable(usersCollection.insertOne(RegisteredUserToDocument(user)).getInsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { user.setId(id); return id; })
                    .orElseThrow(() -> new MongoException("UserDAOMongoImpl: saveUser: Error saving user"));

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (DuplicatedException e) {
            switch (e.getType()) {
                case DUPLICATED_NAME:
                    throw new DAOException(DAOExceptionType.DUPLICATED_USERNAME, e.getMessage());
                case DUPLICATED_EMAIL:
                    throw new DAOException(DAOExceptionType.DUPLICATED_EMAIL, e.getMessage());
                default:
                    throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());
            }
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Updates the information of an existing user in the system.
     * The method performs the following steps:
     * 1. Checks if the username is already in use by another user (when the username is changed).
     * 2. Updates the user information in the database.
     *
     * @param user              The User object (containing only the updated information).
     * @throws DAOException     If an error occurs during the update process,
     *                          such as the username already exists.
     */
    @Override
    public void updateUser(User user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            // Check if the username is already in use by another user (when the username is changed)
            if (user.getUsername() != null) {
                Bson usernameExistsFilter = and(
                        eq("username", user.getUsername()),
                        ne("_id", new ObjectId(user.getId()))
                );
                if (usersCollection.countDocuments(usernameExistsFilter) > 0) {
                    throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_NAME, "UserDAOMongoImpl: updateUser: Username already in use");
                }
            }

            // Update the document in the collection and check if the update was successful
            Bson filter = eq("_id", new ObjectId(user.getId()));
            Bson update = new Document("$set", RegisteredUserToDocument(user))
                    .append("$unset", UserToUnsetUserFieldsDocument(user));

            UpdateResult result = usersCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: updateUser: No user found");
            } else if (result.getModifiedCount() == 0) {
                throw new IllegalArgumentException("UserDAOMongoImpl: updateUser: No changes made to the user");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_USERNAME, e.getMessage());

        } catch (IllegalArgumentException e) {
            throw new DAOException(DAOExceptionType.NO_CHANGES, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Removes a user from the system based on their ID.
     *
     * @param userId            The ID of the user to be removed.
     * @throws DAOException     If an error occurs while removing the user.
     */
    @Override
    public void deleteUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));

            if (usersCollection.deleteOne(filter).getDeletedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: deleteUser: No user found");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Retrieves a user from the system based on their ID.
     *
     * @param userId                The ID of the user to retrieve.
     * @param onlyStatsInfo         If true, only retrieves the user's location and birthday.
     *                              If false, retrieves all user information except for the manager flag,
     *                              password, email, joined_on, and app_rating.
     * @param isLoggedUserInfo      If true, retrieves all user information except for the manager flag.
     * @return                      The retrieved user, or null if not found.
     * @throws DAOException         If an error occurs while retrieving the user.
     */
    @Override
    public RegisteredUser readUser(String userId, boolean onlyStatsInfo, boolean isLoggedUserInfo) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("_id", new ObjectId(userId)), exists("is_manager", false));
            Bson projection;
            if (onlyStatsInfo) {
                projection = fields(include("location", "birthday"), excludeId());
            } else if (isLoggedUserInfo) {
                projection = exclude("is_manager", "joined_on");
            } else {
                projection = exclude("is_manager", "password", "email", "joined_on", "app_rating", "reviews_ids");
            }

            return Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(DocumentUtils::documentToRegisteredUser)
                    .orElseThrow(() -> new MongoException("UserDAOMongoImpl: readUser: No user found"));

        } catch (MongoException e){
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e){
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Authenticates a user based on their email and password.
     *
     * @param email             The email of the user to authenticate.
     * @param password          The password of the user to authenticate.
     * @return                  The authenticated user.
     * @throws DAOException     If authentication fails due to incorrect email or password.
     */
    public LoggedUserDTO authenticate(String email, String password) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("email", email),eq("password", password));
            Bson projection = include("username", "picture", "is_manager", "location", "birthday");

            return Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(doc -> {
                        LoggedUserDTO user = new LoggedUserDTO();
                        user.setId(doc.getObjectId("_id").toString());
                        user.setUsername(doc.getString("username"));
                        user.setProfilePicUrl(doc.getString("picture"));
                        user.setLocation(doc.getString("location"));
                        user.setBirthday(ConverterUtils.dateToLocalDate(doc.getDate("birthday")));
                        user.setType(doc.getBoolean("is_manager") != null ? UserType.MANAGER : UserType.USER);
                        return user;
                    })
                    .orElseThrow(() -> new AuthenticationException("UserDAOMongoImpl: authenticate: Authentication failed"));
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (AuthenticationException e) {
            throw new DAOException(DAOExceptionType.AUTHENTICATION_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Searches for users based on their username.
     *
     * @param username          The username to search for.
     *                          If null or empty, all users are returned.
     * @param n                 The maximum number of users to return.
     * @param loggedUser        The username of the logged user.
     *                          If not null, the logged user is excluded from the search results.
     * @return                  A list of RegisteredUserDTO objects matching the search criteria.
     * @throws DAOException     If an error occurs while searching for users.
     */
    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter;
            if (StringUtils.isNotBlank(username)) {
                filter = and(regex("username", "^"  + username, "ix"), eq("is_manager", null));
            } else {
                filter = eq("is_manager", null);
            }
            if (StringUtils.isNotBlank(loggedUser)) {
                filter = and(filter, ne("username", loggedUser));
            }
            Bson sort = descending("followers");
            Bson projection = include("username", "picture");

            int limit = (n == null) ? 0 : n;

            List<Document> documents = usersCollection.find(filter).sort(sort).projection(projection)
                    .limit(limit)
                    .into(new ArrayList<>());

            return Optional.of(documents)
                    .filter(docs -> !docs.isEmpty())
                    .orElseThrow(() -> new MongoException("No results found for the given query"))
                    .stream()
                    .map(DocumentUtils::documentToUserSummaryDTO)
                    .toList();

        } catch (MongoException e){
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e){
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Calculates the distribution of users based on a given search criterion.
     *
     * @param criteria          The criterion on which to base the distribution.
     *                          Criteria values: "gender", "location", "birthday", or "joined_on".
     * @return                  A map where each key represents a unique value of the specified criterion,
     *                          and the corresponding value is the count of users having that value.
     * @throws DAOException     If there's an issue with the database or a generic error occurs.
     */
    @Override
    public Map<String, Integer> getDistribution(String criteria) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = new ArrayList<>();
            if (criteria.equals("birthday") || criteria.equals("joined_on")) {
                pipeline.addAll(List.of(
                        match(exists(criteria)),
                        project(fields(computed("year", new Document("$year", "$" + criteria)), include("app_rating" ))),
                        group("$year", sum("count", 1)),
                        sort(descending("count"))));
            } else if (criteria.equals("location") || criteria.equals("gender")) {
                pipeline.addAll(List.of(
                        match(exists(criteria)),
                        project(fields(include(criteria, "app_rating"))),
                        group("$" + criteria, sum("count", 1)),
                        sort(descending("count"))));
            } else {
                throw new Exception("UserDAOMongoImpl: getDistribution: Invalid criteria");
            }

            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());
            if (aggregationResult.isEmpty()) {
                throw new MongoException("UserDAOMongoImpl: getDistribution: No data found");
            }

            Map<String,Integer> map = new LinkedHashMap<>();
            for (Document doc : aggregationResult) {
                if (criteria.equals("birthday") || criteria.equals("joined_on")) {
                    map.put(String.valueOf(doc.getInteger("_id")), doc.getInteger("count"));
                } else {
                    map.put(doc.getString("_id"), doc.getInteger("count"));
                }
            }
            return map;

        } catch (MongoException e){
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e){
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Calculates the average application rating based on the specified search criteria.
     *
     * @param criteria          The criteria on which to base the calculation.
     *                          Criteria values: "gender", "location", "birthday", "joined_on".
     * @return                  A map where each key represents a unique value of the specified criterion,
     *                          and the corresponding value is the average application rating associated with that criterion.
     * @throws DAOException     If there's an issue with the database or a generic error occurs.
     */
    @Override
    public Map<String, Double> averageAppRating(String criteria) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = List.of(
                    match(and(exists(criteria), exists("app_rating"))),
                    group("$" + criteria, avg("averageAppRating", "$app_rating")),
                    sort(descending("averageAppRating"))
            );

            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());
            if (aggregationResult.isEmpty()) {
                throw new MongoException("UserDAOMongoImpl: averageAppRating: No data found");
            }

            Map<String,Double> map = new LinkedHashMap<>();
            for (Document doc : aggregationResult) {
                map.put(doc.getString("_id"), doc.getDouble("averageAppRating"));
            }
            return map;

        } catch (MongoException e){
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        }
        catch (Exception e){
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Calculates the average app rating for users grouped by age ranges.
     * The age ranges are defined as follows:
     * - 0-13 years
     * - 13-20 years
     * - 20-30 years
     * - 30-40 years
     * - 40-50 years
     * - 50+ years
     *
     * @return                  A map containing the average app rating for each age range.
     * @throws DAOException     If an error occurs during database operations.
     */
    @Override
    public Map<String, Double> averageAppRatingByAgeRange() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            // Define the boundaries for the age ranges and the output fields
            List<Long> boundaries = Arrays.asList(0L, 13L, 20L, 30L, 40L, 50L);
            BsonField[] outputFields = {
                    new BsonField("avg_app_rating", new Document("$avg", "$app_rating"))
            };
            BucketOptions options = new BucketOptions()
                    .defaultBucket(50L)
                    .output(outputFields);

            List<Bson> pipeline = List.of(
                    match(and(exists("birthday"), exists("app_rating"))),
                    project(fields(
                            computed("age", new Document("$floor", new Document("$divide",
                            Arrays.asList(
                                    new Document("$subtract", Arrays.asList(new Date(), "$birthday")),
                                    1000L * 60 * 60 * 24 * 365
                            )))),
                            include("app_rating")
                    )),
                    bucket("$age", boundaries, options)
            );

            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());
            for(Document doc : aggregationResult) {
                System.out.println(doc);
            }
            if (aggregationResult.isEmpty()) {
                throw new MongoException("UserDAOMongoImpl: averageAppRatingByAgeRange: No data found");
            }

            Map<String, Double> map = new LinkedHashMap<>();
            for (Document doc : aggregationResult) {
                String ageRange = convertIntegerToAgeRange(doc.getLong("_id"));
                map.put(ageRange, doc.getDouble("avg_app_rating"));
            }

            return map;

        } catch (MongoException e){
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e){
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    private String convertIntegerToAgeRange(Long age) {
        if (age == 0) {
            return "0-13";
        } else if (age == 13) {
            return "13-20";
        } else if (age == 20) {
            return "20-30";
        } else if (age == 30) {
            return "30-40";
        } else if (age == 40) {
            return "40-50";
        } else {
            return "50+";
        }
    }

    /**
     * Updates the number of followers redundancy for the user with the specified user ID.
     *
     * @param userId            The ID of the user whose number of followers to update.
     * @param increment         The increment to apply to the number of followers.
     * @throws DAOException     If an error occurs during database operations,
     *                          such as if the user is not found or the number
     *                          of followers is not updated.
     */
    @Override
    public void updateNumOfFollowers(String userId, Integer increment) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = inc("followers", increment);

            UpdateResult result = usersCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: updateNumOfFollowers: User not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: updateNumOfFollowers: Number of followers not updated");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates the number of followings redundancy for the user with the specified user ID.
     *
     * @param userId            The ID of the user whose number of followed users to update.
     * @param increment         The increment to apply to the number of followed users.
     * @throws DAOException     If an error occurs during database operations,
     *                          such as if the user is not found or the number
     *                          of followed users is not updated.
     */
    @Override
    public void updateNumOfFollowings(String userId, Integer increment) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = inc("followings", increment);

            UpdateResult result = usersCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: updateNumOfFollowers: User not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: updateNumOfFollowers: Number of followers not updated");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates the app rating for a user identified by their user ID.
     *
     * @param userId            The ID of the user whose app rating is to be updated.
     * @param rating            The new app rating to be set for the user.
     * @throws DAOException     If an error occurs while updating the app rating.
     */
    @Override
    public void rateApp(String userId, Integer rating) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = new Document("$set", new Document("app_rating", rating));

            UpdateResult result = usersCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: rateApp: User not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("UserDAOMongoImpl: rateApp: App rating not updated");
            }
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    // Methods available only in Neo4J

    @Override
    public void follow(String followerUserId, String followingUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public boolean isFollowing(String followerUserId, String followedUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public Integer getNumOfFollowers(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public Integer getNumOfFollowed(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> searchFollowing(String userId, String username, String loggedUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> searchFollowers(String userId, String username, String loggedUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> suggestUsersByCommonFollowings(String userId, Integer limit) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> suggestUsersByCommonLikes(String userId, Integer limit, MediaContentType type) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
}
