package it.unipi.lsmsd.fnf.dao.mongo;


import com.mongodb.client.FindIterable;

import com.mongodb.MongoException;

import com.mongodb.client.model.*;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.*;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;

import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.utils.DocumentUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import static com.mongodb.client.model.Sorts.metaTextScore;
import static com.mongodb.client.model.Updates.setOnInsert;

import static com.mongodb.client.model.Sorts.ascending;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.RegisteredUserToDocument;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.UsertToUnsetUserFieldsDocument;


/**
 * Implementation of UserDAO interface for MongoDB data access.
 * Provides methods for user registration, authentication, updating, and removal,
 * as well as methods for querying user data and performing statistical analyses.
 */
public class UserDAOMongoImpl extends BaseMongoDBDAO implements UserDAO {
    private static final String COLLECTION_NAME = "users";

    /**
     * Registers a new user in the system.
     *
     * @param user The User object to be registered.
     * @throws DAOException If an error occurs during registration,
     *                      such as email or username already in use.
     */
    @Override
    public void saveUser(UserRegistrationDTO user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            boolean usernameExists = usersCollection.countDocuments(eq("username", user.getUsername())) != 0;
            boolean emailExists = usersCollection.countDocuments(eq("email", user.getEmail())) != 0;
            if(usernameExists && emailExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_KEY, "Username and email already in use");
            else if(usernameExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_NAME, "Username already in use");
            else if(emailExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_EMAIL, "Email already in use");

            Optional.ofNullable(usersCollection.insertOne(RegisteredUserToDocument(user, Constants .DEFAULT_PROFILE_PICTURE)).getInsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { user.setId(id); return id; })
                    .orElseThrow(() -> new MongoException("No user was inserted"));

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
     *
     * @param user The User object containing the updated information.
     * @throws DAOException If an error occurs during the update process,
     *                      such as the username already exists.
     */
    @Override
    public void updateUser(User user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            // Check if the new username already exists in the collection
            Bson usernameExistsFilter = and(
                    eq("username", user.getUsername()),
                    ne("_id", new ObjectId(user.getId()))
            );
            if (usersCollection.countDocuments(usernameExistsFilter) > 0) {
                throw new DuplicatedException("Username already exists in the collection");
            }

            // Update the document in the collection and check if the update was successful
            Bson filter = eq("_id", new ObjectId(user.getId()));
            Bson update = new Document("$set", RegisteredUserToDocument(user))
                    .append("$unset", UsertToUnsetUserFieldsDocument(user));
            if (usersCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("No user was updated");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_USERNAME, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Removes a user from the system based on their ID.
     *
     * @param userId The ID of the user to be removed.
     * @throws DAOException If an error occurs while removing the user.
     */
    @Override
    public void deleteUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));

            if (usersCollection.deleteOne(filter).getDeletedCount() == 0) {
                throw new MongoException("No user was deleted");
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
     * @param userId The ID of the user to retrieve.
     * @return The retrieved user, or null if not found.
     * @throws DAOException If an error occurs while retrieving the user.
     */
    @Override
    public RegisteredUser readUser(String userId, boolean onlyStatsInfo) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson projection;
            if (onlyStatsInfo) {
                projection = fields(include("location", "birthday"), excludeId());
            } else {
                projection = exclude("is_manager", "password");
            }

            return Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(DocumentUtils::documentToRegisteredUser)
                    .orElseThrow(() -> new MongoException("User not found"));
        }
        catch (Exception e){
            throw new DAOException("Error searching user by id: "+ userId, e);
        }
    }

    /**
     * Authenticates a user based on their email and password.
     *
     * @param email    The email of the user to authenticate.
     * @param password The password of the user to authenticate.
     * @return The authenticated user.
     * @throws DAOException If authentication fails due to incorrect email or password.
     */



    public LoggedUserDTO authenticate(String email, String password) throws DAOException {

        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("email", email),eq("password", password));
            Bson projection = include("username", "picture", "is_manager");

            return Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(doc -> {
                        LoggedUserDTO user = new LoggedUserDTO();
                        user.setId(doc.getObjectId("_id").toString());
                        user.setUsername(doc.getString("username"));
                        user.setProfilePicUrl(doc.getString("picture"));
                        user.setType(doc.getBoolean("is_manager") != null ? UserType.MANAGER : UserType.USER);
                        return user;
                    })
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
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
     * @param username The username to search for.
     * @return A list of RegisteredUserDTO objects matching the search criteria.
     * @throws DAOException If an error occurs while searching for users.
     */
    @Override
    public List<UserSummaryDTO> searchFirstNUsers(String username, Integer n, String loggedUser) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter;
            if (StringUtils.isNotBlank(username)) {
                filter = and(regex("username", username, "ix"), eq("is_manager", null));
            } else {
                filter = eq("is_manager", null);
            }
            if (StringUtils.isNotBlank(loggedUser)) {
                filter = and(filter, ne("username", loggedUser));
            }
            Bson sort = ascending("username");
            Bson projection = include("username", "picture");

            if (n == null) {
                return usersCollection.find(filter).sort(sort).projection(projection).into(new ArrayList<>())
                        .stream()
                        .map(DocumentUtils::documentToUserSummaryDTO)
                        .toList();
            } else {
                return usersCollection.find(filter).sort(sort).projection(projection).limit(n).into(new ArrayList<>())
                        .stream()
                        .map(DocumentUtils::documentToUserSummaryDTO)
                        .toList();
            }
        } catch (Exception e) {
            throw new DAOException("Error searching user by username: " + username, e);
        }
    }

    //MongoDB complex queries
    //Find the distribution of genders, of ages, of locations
    @Override
    public Map<String, Integer> getDistribution (String criteria) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            //criteria can be: gender, location, birthday, joined_on

            List<Document> pipeline = new ArrayList<>();
            if (criteria.equals("birthday") || criteria.equals("joined_on")) {
                pipeline.add(Document.parse("{$project: { year: { $year: \"$" + criteria +"\" },  app_rating: 1 }}"));
                pipeline.add(Document.parse("{$group: {_id: \"$year\", count: { $sum: 1 }}}"));
                pipeline.add(Document.parse("{$sort: {count: -1}}"));
            } else if (criteria.equals("location") || criteria.equals("gender")) {
                pipeline.add(Document.parse("{$group: {_id: \"$" + criteria + "\", count: { $sum: 1 }}}"));
                pipeline.add(Document.parse("{$sort: {count: -1}}"));


            } else
                System.out.println("Criteria not valid");


            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());

            System.out.println(aggregationResult);

            Map<String,Integer> map = new LinkedHashMap<>();
            for (Document doc : aggregationResult) {
                if (criteria.equals("birthday") || criteria.equals("joined_on"))
                    map.put(String.valueOf(doc.getInteger("_id")), doc.getInteger("count"));

                else if (criteria.equals("location") || criteria.equals("gender"))
                    map.put(doc.getString("_id"), doc.getInteger("count"));
            }
            return map;
        }
        catch (Exception e){
            throw new DAOException("Error getting distribution", e);
        }
    }


    //Find the average age of users
    /**
     * Calculates the average age of users.
     *
     * @return The average age of users.
     * @throws DAOException If an error occurs while calculating the average age.
     */
    @Override
    public Double averageAgeUsers() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = new ArrayList<>();

            pipeline.add(Aggregates.match(Filters.exists("birthday")));

            pipeline.add(Aggregates.project(Projections.computed("age",
                    new Document("$let", new Document("vars", new Document("dob", "$birthday"))
                            .append("in", new Document("$floor", new Document("$divide",
                                    Arrays.asList(new Document("$subtract", Arrays.asList(new Date(), "$$dob")),
                                            1000L * 60 * 60 * 24 * 365))))))));

            pipeline.add(Aggregates.group(null, Accumulators.avg("averageAge", "$age")));

            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());

            if (!aggregationResult.isEmpty()) {
                return aggregationResult.getFirst().getDouble("averageAge");
            } else {
                return null; // o un valore predefinito a tua scelta
            }
        }
        catch (Exception e){
            throw new DAOException("Error getting average age of usersCollection", e);
        }
    }

    //Find average app_rating based on location and gender.
    @Override
    public Map<String, Double> averageAppRating (String criteria) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            //criteria can be: location and gender

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: {\"" + criteria + "\": { $exists: true }}}"));

            pipeline.add(Document.parse("{$group: {_id: \"$" + criteria + "\", averageAppRating: { $avg: \"$app_rating\" }}}"));

            pipeline.add(Document.parse("{$sort: {averageAppRating: -1}}"));

            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<String,Double> map = new LinkedHashMap<>();
            for (Document doc : aggregationResult) {

                map.put(doc.getString("_id"), doc.getDouble("averageAppRating"));

            }
            return map;


        }
        catch (Exception e){
            throw new DAOException("Error getting average app rating", e);
        }
    }

    //Find the average app_rating of users based on group af ages
    @Override
    public Map<String, Double> averageAppRatingByAgeRange() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = new ArrayList<>();

            Bson projectStage = Aggregates.project(
                    fields(
                            Projections.computed("age", new Document("$divide",
                                    Arrays.asList(
                                            new Document("$subtract", Arrays.asList(new Date(), "$birthday")),
                                            1000L * 60 * 60 * 24 * 365
                                    )
                            )),
                            Projections.include("app_rating")
                    )
            );
            pipeline.add(projectStage);

            List<Long> boundaries = Arrays.asList(0L, 13L, 20L, 40L, 50L);
            BsonField[] outputFields = {
                    new BsonField("averageAppRating", new Document("$avg", "$app_rating"))
            };

            BucketOptions options = new BucketOptions()
                    .defaultBucket(50L)
                    .output(outputFields);

            Bson bucketStage = Aggregates.bucket("$age", boundaries, options);
            pipeline.add(bucketStage);


            List<Document> aggregationResult = usersCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<String, Double> map = new LinkedHashMap<>();

            for (Document doc : aggregationResult) {

                String ageRange = convertIntegerToAgeRange(doc.getLong("_id"));
                map.put(ageRange, doc.getDouble("averageAppRating"));



            }
            return map;

        }
        catch (Exception e){
            throw new DAOException("Error getting average app rating by age range", e);
        }
    }

    private String convertIntegerToAgeRange(Long age) {
        if (age == null || age < 0) {
            return "Unknown";
        } else if (age <= 13) {
            return "0-13";
        } else if (age <= 20) {
            return "13-20";
        } else if (age <= 40) {
            return "20-40";
        } else if (age <= 50) {
            return "40-50";
        } else {
            return "50+";
        }
    }

    // Methods available only in Neo4J
    @Override
    public void createNode(UserSummaryDTO userSummaryDTO) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public void follow(String followerUserId, String followingUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> getFollowing(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> getFollowers(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<UserSummaryDTO> suggestUsers(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
}
