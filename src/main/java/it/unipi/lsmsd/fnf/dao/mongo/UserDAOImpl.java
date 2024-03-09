package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.*;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;

/**
 * Implementation of UserDAO interface for MongoDB data access.
 * Provides methods for user registration, authentication, updating, and removal,
 * as well as methods for querying user data and performing statistical analyses.
 */
public class UserDAOImpl extends BaseMongoDBDAO implements UserDAO {
    private static final String COLLECTION_NAME = "users";

    /**
     * Registers a new user in the system.
     *
     * @param user The User object to be registered.
     * @throws DAOException If an error occurs during registration,
     *                      such as email or username already in use.
     */
    @Override
    public void createUser(UserRegistrationDTO user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            boolean usernameExists = usersCollection.countDocuments(eq("username", user.getUsername())) != 0;
            boolean emailExists = usersCollection.countDocuments(eq("email", user.getEmail())) != 0;
            if(usernameExists && emailExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_KEY, "Username and email already in use");
            else if(usernameExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_USERNAME, "Username already in use");
            else if(emailExists)
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_EMAIL, "Email already in use");

            String image = "images/user%20icon%20-%20Kopya%20-%20Kopya.png";
            Optional.ofNullable(usersCollection.insertOne(RegisteredUserToDocument(user, image)).getInsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { user.setId(id); return id; })
                    .orElseThrow(() -> new MongoException("No user was inserted"));

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (DuplicatedException e) {
            switch (e.getType()) {
                case DUPLICATED_USERNAME:
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
                    .append("$unset", UnsetDocument(user));
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

            if (usersCollection.deleteOne(eq("_id", new ObjectId(userId))).getDeletedCount() == 0) {
                throw new MongoException("No user was deleted");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
    @Override
    public RegisteredUser authenticate(String email, String password) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("email", email);
            Bson projection = exclude("is_manager");
            RegisteredUser user = Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(this::documentToRegisteredUser)
                    .orElseThrow (() -> new AuthenticationException("User not found"));
            if (!user.getPassword().equals(password)) {
                throw new AuthenticationException("Wrong password");
            }

            user.setPassword(null);
            return user;
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (AuthenticationException e) {
            throw new DAOException(DAOExceptionType.AUTHENTICATION_ERROR, e.getMessage());

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
    public RegisteredUser getById(String userId) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson projection = exclude("is_manager", "password");

            Document userDocument = usersCollection.find(filter).projection(projection).first();
            return (userDocument != null)? documentToRegisteredUser(userDocument) : null;
        }
        catch (Exception e){
            throw new DAOException("Error searching user by id: "+ userId, e);
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
                        .map(this::documentToRegisteredUserDTO)
                        .toList();
            } else {
                return usersCollection.find(filter).sort(sort).projection(projection).limit(n).into(new ArrayList<>())
                        .stream()
                        .map(this::documentToRegisteredUserDTO)
                        .toList();
            }
        } catch (Exception e) {
            throw new DAOException("Error searching user by username: " + username, e);
        }
    }

    //MongoDB complex queries
    //Find the distribution of genders between users
    /**
     * Retrieves the distribution of genders among users.
     *
     * @return A list of MongoDB documents representing the distribution of genders.
     * @throws DAOException If an error occurs while retrieving the gender distribution.
     */
    @Override
    public List<Document> getGenderDistribution() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$group: {_id: \"$gender\", count: { $sum: 1 }}}"));

            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result;


        }
        catch (Exception e){
            throw new DAOException("Error getting genre distribution", e);
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
    public Integer averageAgeUsers() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("""
                    {$project: {age: {$divide: [{ $subtract: [new Date(), "$birthday"] },
                              1000 * 60 * 60 * 24 * 365 ] }}
                    """));
            pipeline.add(Document.parse("{ $group: {_id: null, averageAge: { $avg: \"$age\" }}}"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result.getFirst().getInteger("averageAge");
        }
        catch (Exception e){
            throw new DAOException("Error getting average age of usersCollection", e);
        }
    }

    //Find the distribution of users by location
    /**
     * Retrieves the distribution of users by location.
     *
     * @return A list of MongoDB documents representing the distribution of users by location.
     * @throws DAOException If an error occurs while retrieving the location distribution.
     */
    @Override
    public List<Document> getLocationDistribution() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$group: {_id: \"$location\", count: { $sum: 1 }}}"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result;
        }
        catch (Exception e){
            throw new DAOException("Error getting location distribution", e);
        }
    }

    //Find how many users there are grouped by age range
    /**
     * Retrieves the number of users grouped by age range.
     *
     * @return A list of MongoDB documents representing the number of users in each age range.
     * @throws DAOException If an error occurs while retrieving users by age range.
     */
    @Override
    public List<Document> getUsersByAgeRange() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("""
                    {$project: {age: {$divide: [{ $subtract: [new Date(), "$birthday"] },
                              1000 * 60 * 60 * 24 * 365 ] }}
                    """));
            pipeline.add(Document.parse("{$bucket: {groupBy: \"$age\", boundaries: [13, 20, 40, 50], default: \"Other\", output: { count: { $sum: 1 } }}}"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result;
        }
        catch (Exception e){
            throw new DAOException("Error getting usersCollection by age range", e);
        }
    }

    //Find how many users registered for each year
    /**
     * Retrieves the number of users registered for each year.
     *
     * @return A list of MongoDB documents representing the number of users registered each year.
     * @throws DAOException If an error occurs while retrieving users registered by year.
     */
    @Override
    public List<Document> getUsersRegisteredByYear() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{ $group: { _id: { $year: { $toDate: \"$joined_on\" } }, count: { $sum: 1 } } }"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result;
        } catch (Exception e) {
            throw new DAOException("Error getting usersCollection registered by year", e);
        }
    }

    //Find average app_rating based on the age of users
    /**
     * Calculates the average app rating based on the age of users.
     *
     * @param yearOfBirth The year of birth to calculate the average app rating for.
     * @return The average app rating for users born in the specified year.
     * @throws DAOException If an error occurs while calculating the average app rating.
     */
    @Override
    public Integer averageAppRatingByAge (Integer yearOfBirth) throws DAOException{
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: {\"birthday\": { $gte: ISODate(\"" + yearOfBirth + "-01-01T00:00:00.000Z\"),        " +
                    "$lt: ISODate(\"" + (yearOfBirth+1) + "-01-01T00:00:00.000Z\")}}}"));
            pipeline.add(Document.parse("{$group: {_id: null, averageAppRating: { $avg: \"$app_rating\" }}}"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result.getFirst().getInteger("averageAppRating");
        }
        catch (Exception e){
            throw new DAOException("Error getting average app rating by age", e);
        }

    }
    //Find average app_rating based on the location of users
    /**
     * Calculates the average app rating based on the location of users.
     *
     * @param location The location to calculate the average app rating for.
     * @return The average app rating for users in the specified location.
     * @throws DAOException If an error occurs while calculating the average app rating.
     */
    @Override
    public Integer averageAppRatingByLocation (String location) throws DAOException{
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"location\": \"" + location + "    }  }"));
            pipeline.add(Document.parse("{$group: {_id: null, averageAppRating: { $avg: \"$app_rating\" }}}"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result.getFirst().getInteger("averageAppRating");
        }
        catch (Exception e){
            throw new DAOException("Error getting average app rating by age", e);
        }

    }
    //Find average app_rating based on the gender of users
    /**
     * Calculates the average app rating based on the gender of users.
     *
     * @return A list of MongoDB documents representing the average app rating for each gender.
     * @throws DAOException If an error occurs while calculating the average app rating.
     */
    @Override
    public List<Document> averageAppRatingByGender () throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$group: {_id: \"$gender\", averageAppRating: { $avg: \"$app_rating\" }}}"));
            AggregateIterable<Document> aggregationResult = usersCollection.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);
            return result;
        }
        catch (Exception e){
            throw new DAOException("Error getting genre distribution", e);
        }
    }

    private UserSummaryDTO documentToRegisteredUserDTO(Document doc) {
        UserSummaryDTO user = new UserSummaryDTO();
        user.setId(doc.getObjectId("_id").toString());
        user.setUsername(doc.getString("username"));
        user.setProfilePicUrl(doc.getString("picture"));
        return user;
    }

    private RegisteredUser documentToRegisteredUser(Document doc) {
        RegisteredUser user;

        if (doc.getBoolean("is_manager") != null) {
            Manager manager = new Manager();
            manager.setHiredDate(ConverterUtils.dateToLocalDate(doc.getDate("hired_on")));
            manager.setTitle(doc.getString("title"));
            user = manager;
        } else {
            User normalUser = new User();
            normalUser.setUsername(doc.getString("username"));
            normalUser.setBirthday(ConverterUtils.dateToLocalDate(doc.getDate("birthday")));
            normalUser.setDescription(doc.getString("description"));
            normalUser.setGender(Gender.fromString(doc.getString("gender")));
            normalUser.setLocation(doc.getString("location"));
            user = normalUser;
        }

        user.setId(doc.getObjectId("_id").toString());
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setJoinedDate(ConverterUtils.dateToLocalDate(doc.getDate("joined_on")));
        user.setFullname(doc.getString("fullname"));
        user.setProfilePicUrl(doc.getString("picture"));
        return user;
    }

    private Document RegisteredUserToDocument(UserRegistrationDTO user, String image) {
        return getDocument(user.getPassword(), user.getEmail(), LocalDate.now(),
                user.getFullname(), image, user.getUsername(),
                user.getBirthday(), null, user.getGender(), user.getLocation());
    }

    private Document RegisteredUserToDocument(User user) {
        return getDocument(user.getPassword(), user.getEmail(), user.getJoinedDate(),
                user.getFullname(), user.getProfilePicUrl(), user.getUsername(),
                user.getBirthday(), user.getDescription(), user.getGender(), user.getLocation());
    }

    private Document getDocument(String password, String email, LocalDate joinedDate, String fullname, String profilePicUrl, String username, LocalDate birthday, String description, Gender gender, String location) {
        Document doc = new Document();
        appendIfNotNull(doc, "password", password);
        appendIfNotNull(doc, "email", email);

        if (joinedDate != null) {
            appendIfNotNull(doc, "joined_on", ConverterUtils.localDateToDate(joinedDate));
        }
        appendIfNotNull(doc, "fullname", fullname);
        appendIfNotNull(doc, "picture", profilePicUrl);
        appendIfNotNull(doc, "username", username);
        appendIfNotNull(doc, "birthday", ConverterUtils.localDateToDate(birthday));
        appendIfNotNull(doc, "description", description);
        appendIfNotNull(doc, "gender", gender != null ? gender.name() : null);
        appendIfNotNull(doc, "location", location);

        return doc;
    }

    private Document UnsetDocument(User registeredUser) {
        Document doc = new Document();
        if (registeredUser.getFullname() != null && registeredUser.getFullname().equals(Constants.NULL_STRING))
            doc.append("fullname", 1);
        if (registeredUser.getBirthday() != null && registeredUser.getBirthday().equals(Constants.NULL_DATE))
            doc.append("birthday", 1);
        if (registeredUser.getLocation() != null && registeredUser.getLocation().equals(Constants.NULL_STRING))
            doc.append("location", 1);
        if (registeredUser.getDescription() != null && registeredUser.getDescription().equals(Constants.NULL_STRING))
            doc.append("description", 1);
        if (registeredUser.getGender() != null && registeredUser.getGender().equals(Gender.UNKNOWN))
            doc.append("gender", 1);

        return doc;
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
