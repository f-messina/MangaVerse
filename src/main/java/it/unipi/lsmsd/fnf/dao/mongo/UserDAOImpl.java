package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.metaTextScore;
import static com.mongodb.client.model.Updates.setOnInsert;

public class UserDAOImpl extends BaseMongoDBDAO implements UserDAO {
    private static final String COLLECTION_NAME = "users";

    @Override
    public String register(User user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            user.setJoinedDate(LocalDate.now());
            user.setProfilePicUrl("images/user%20icon%20-%20Kopya%20-%20Kopya.png");

            Bson filter = or(
                    eq("email", user.getEmail()),
                    eq("username", user.getUsername())
            );
            Bson update = setOnInsert(RegisteredUserToDocument(user));

            UpdateResult result = usersCollection.updateOne(filter, update, new UpdateOptions().upsert(true));

            if (result.getUpsertedId() == null) {
                // Check which field is causing the conflict
                Document existingUser = usersCollection.find(filter).first();
                if (existingUser != null) {
                    if (existingUser.getString("email").equals(user.getEmail()) && existingUser.getString("username").equals(user.getUsername())) {
                        throw new DAOException("Email and username already in use");
                    } else if (existingUser.getString("email").equals(user.getEmail())) {
                        throw new DAOException("Email already in use");
                    } else {
                        throw new DAOException("Username already in use");
                    }
                } else {
                    throw new DAOException("Error adding new user");
                }
            } else {
                return result.getUpsertedId().asObjectId().getValue().toString();
            }
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            throw new DAOException("Error adding new user", e);
        }
    }

    @Override
    public void update(User user) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            // Check if the new username already exists in the collection
            Bson usernameExistsFilter = eq("username", user.getUsername());
            if (usersCollection.countDocuments(usernameExistsFilter) > 0) {
                throw new DAOException("Username already exists in the collection");
            }

            // Update the document
            Bson filter = eq("_id", new ObjectId(user.getId()));
            Bson update = new Document("$set", RegisteredUserToDocument(user))
                    .append("$unset", UnsetDocument(user));

            UpdateResult results = usersCollection.updateOne(filter, update);
            if (results.getModifiedCount() == 0) {
                throw new DAOException("No user was updated");
            }
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            throw new DAOException("Error updating user information for user with id: " + user.getId(), e);
        }
    }

    @Override
    public void remove(String userId) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));

            usersCollection.deleteOne(filter);
        }
        catch (Exception e){
            throw new DAOException("Error removing user", e);
        }
    }

    @Override
    public RegisteredUser authenticate(String email, String password) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("email", email);
            Bson projection = exclude("is_manager");

            Document userDocument = usersCollection.find(filter).projection(projection).first();
            if (userDocument != null) {
                RegisteredUser user = documentToRegisteredUser(userDocument);
                if (user.getPassword().equals(password)) {
                    user.setPassword(null);
                    return user;
                } else {
                    throw new DAOException("Wrong password");
                }
            } else {
                throw new DAOException("User not found");
            }
        }
        catch (DAOException e){
            throw e;
        }
        catch (Exception e){
            throw new DAOException("Error authenticating user", e);
        }
    }

    @Override
    public RegisteredUser find(String userId) throws DAOException {
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

    @Override
    public List<RegisteredUserDTO> search(String username) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = text(username);
            Bson sort = metaTextScore("score");
            Bson projection = include("username", "picture");

            List<RegisteredUserDTO> result = new ArrayList<>();
            FindIterable<Document> results = usersCollection.find(filter).sort(sort).projection(projection);

            results.forEach(document -> {
                RegisteredUserDTO user = documentToRegisteredUserDTO(document);
                result.add(user);
            });

            return result;
        }
        catch (Exception e){
            throw new DAOException("Error searching user by username: "+ username, e);
        }
    }

    public List<RegisteredUserDTO> findAll() throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("is_manager", null);
            Bson projection = include("username", "picture");

            List<RegisteredUserDTO> result = new ArrayList<>();
            usersCollection.find(filter).projection(projection).forEach(document -> {
                RegisteredUserDTO user = documentToRegisteredUserDTO(document);
                result.add(user);
            });

            return result;
        }
        catch (Exception e){
            throw new DAOException("Error searching all the usersCollection", e);
        }
    }

    private RegisteredUserDTO documentToRegisteredUserDTO(Document doc) {
        RegisteredUserDTO user = new RegisteredUserDTO();
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
            User regularUser = new User();
            regularUser.setUsername(doc.getString("username"));
            regularUser.setBirthday(ConverterUtils.dateToLocalDate(doc.getDate("birthday")));
            regularUser.setDescription(doc.getString("description"));
            regularUser.setGender(Gender.fromString(doc.getString("gender")));
            regularUser.setLocation(doc.getString("location"));
            user = regularUser;
        }

        user.setId(doc.getObjectId("_id").toString());
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setJoinedDate(ConverterUtils.dateToLocalDate(doc.getDate("joined_on")));
        user.setFullname(doc.getString("fullname"));
        user.setProfilePicUrl(doc.getString("picture"));
        return user;
    }

    private Document RegisteredUserToDocument(RegisteredUser user) {
        Document doc = new Document();
        appendIfNotNull(doc, "password", user.getPassword());
        appendIfNotNull(doc, "email", user.getEmail());

        if (user.getJoinedDate() != null) {
            appendIfNotNull(doc, "joined_on", ConverterUtils.localDateToDate(user.getJoinedDate()));
        }
        appendIfNotNull(doc, "fullname", user.getFullname());
        appendIfNotNull(doc, "picture", user.getProfilePicUrl());

        if (user instanceof Manager manager) {
            appendIfNotNull(doc, "title", manager.getTitle());
            appendIfNotNull(doc, "hired_on", ConverterUtils.localDateToDate(manager.getHiredDate()));
        } else if (user instanceof User regularUser) {
            appendIfNotNull(doc, "username", regularUser.getUsername());
            appendIfNotNull(doc, "birthday", ConverterUtils.localDateToDate(regularUser.getBirthday()));
            appendIfNotNull(doc, "description", regularUser.getDescription());
            appendIfNotNull(doc, "gender", regularUser.getGender() != null ? regularUser.getGender().name() : null);
            appendIfNotNull(doc, "location", regularUser.getLocation());
        }

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

    //MongoDB complex queries
    //Find the distribution of genders between users
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

    // Methods available only in Neo4J
    @Override
    public void createNode(RegisteredUserDTO registeredUserDTO) throws DAOException {
    }
    @Override
    public void follow(String followerUserId, String followingUserId) throws DAOException {
    }
    @Override
    public void unfollow(String followerUserId, String followingUserId) throws DAOException {
    }
    @Override
    public List<RegisteredUserDTO> getFollowing(String userId) throws DAOException {
        return null;
    }
    @Override
    public List<RegisteredUserDTO> getFollowers(String userId) throws DAOException {
        return null;
    }
    @Override
    public List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException {
        return null;
    }
    @Override
    public void update(RegisteredUser user) throws DAOException {
    }
}