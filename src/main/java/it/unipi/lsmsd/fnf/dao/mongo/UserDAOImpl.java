package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
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
  
    @Override
    public ObjectId register(User user) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            user.setJoinedDate(LocalDate.now());
            Bson filter = or(
                            eq("email", user.getEmail()),
                            eq("username", user.getUsername())
            );
            Bson update = setOnInsert(RegisteredUserToDocument(user));

            UpdateResult result = users.updateOne(filter, update, new UpdateOptions().upsert(true));
            if (result.getUpsertedId() == null) {
                throw new DAOException("Username or email already in use");
            } else {
                return result.getUpsertedId().asObjectId().getValue();
            }
        }
        catch (Exception e){
            throw new DAOException("Error adding new user", e);
        }
    }

    @Override
    public void update(RegisteredUser user) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = eq("_id", user.getId());
            Bson update = new Document("$set", RegisteredUserToDocument(user));

            UpdateResult results = users.updateOne(filter, update);
            if (results.getModifiedCount() == 0) {
                throw new DAOException("User not found");
            }
        } catch (Exception e){
            throw new DAOException("Error updating user information for user with id: "+ user.getId(), e);
        }
    }

    @Override
    public void remove(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = eq("_id", id);

            users.deleteOne(filter);
        }
        catch (Exception e){
            throw new DAOException("Error removing user", e);
        }
    }

    @Override
    public RegisteredUser authenticate(String email, String password) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = eq("email", email);
            Bson projection = exclude("is_manager");

            Document userDocument = users.find(filter).projection(projection).first();

            if (userDocument != null) {
                RegisteredUser user = documentToRegisteredUser(userDocument);
                if (user.getPassword().equals(password)) {
                    return user;
                } else {
                    throw new DAOException("Wrong password");
                }
            } else {
                throw new DAOException("User not found");
            }
        }
        catch (Exception e){
            throw new DAOException("Error authenticating user", e);
        }
    }

    @Override
    public RegisteredUser find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = eq("_id", id);
            Bson projection = exclude("is_manager", "password", "email");

            Document userDocument = users.find(filter).projection(projection).first();
            return (userDocument != null)? documentToRegisteredUser(userDocument) : null;
        }
        catch (Exception e){
            throw new DAOException("Error searching user by id: "+ id, e);
        }
    }

    @Override
    public List<RegisteredUserDTO> find(String username) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = text(username);
            Bson sort = metaTextScore("score");
            Bson projection = include("username", "picture");

            List<RegisteredUserDTO> result = new ArrayList<>();
            FindIterable<Document> results = users.find(filter).sort(sort).projection(projection);

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
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = eq("is_manager", null);
            Bson projection = include("username", "picture");

            List<RegisteredUserDTO> result = new ArrayList<>();
            users.find(filter).projection(projection).forEach(document -> {
                RegisteredUserDTO user = documentToRegisteredUserDTO(document);
                result.add(user);
            });

            return result;
        }
        catch (Exception e){
            throw new DAOException("Error searching all the users", e);
        }
    }

    private RegisteredUserDTO documentToRegisteredUserDTO(Document doc) {
        RegisteredUserDTO user = new RegisteredUserDTO();
        user.setId(doc.getObjectId("_id"));
        user.setUsername(doc.getString("username"));
        user.setProfilePicUrl(doc.getString("picture"));
        return user;
    }

    private RegisteredUser documentToRegisteredUser(Document doc) {
        RegisteredUser user;

        if (doc.getBoolean("is_manager") != null) {
            Manager manager = new Manager();
            manager.setHiredDate(ConverterUtils.convertDateToLocalDate(doc.getDate("hired_on")));
            manager.setTitle(doc.getString("title"));
            user = manager;
        } else {
            User regularUser = new User();
            regularUser.setUsername(doc.getString("username"));
            regularUser.setBirthday(ConverterUtils.convertDateToLocalDate(doc.getDate("birthday")));
            regularUser.setDescription(doc.getString("description"));
            regularUser.setGender(doc.getString("gender"));
            regularUser.setLocation(doc.getString("location"));
            user = regularUser;
        }

        user.setId(doc.getObjectId("_id"));
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setJoinedDate(ConverterUtils.convertDateToLocalDate(doc.getDate("joined_on")));
        user.setFullname(doc.getString("fullname"));
        user.setProfilePicUrl(doc.getString("picture"));
        return user;
    }

    private Document RegisteredUserToDocument(RegisteredUser user) {
        Document doc = new Document();
        appendIfNotNull(doc, "password", user.getPassword());
        appendIfNotNull(doc, "email", user.getEmail());

        if (user.getJoinedDate() != null) {
            appendIfNotNull(doc, "joined_on", ConverterUtils.convertLocalDateToDate(user.getJoinedDate()));
        }
        appendIfNotNull(doc, "fullname", user.getFullname());
        appendIfNotNull(doc, "picture", user.getProfilePicUrl());

        if (user instanceof Manager manager) {
            appendIfNotNull(doc, "title", manager.getTitle());
            appendIfNotNull(doc, "hired_on", ConverterUtils.convertLocalDateToDate(manager.getHiredDate()));
        } else if (user instanceof User regularUser) {
            appendIfNotNull(doc, "username", regularUser.getUsername());
            appendIfNotNull(doc, "birthday", ConverterUtils.convertLocalDateToDate(regularUser.getBirthday()));
            appendIfNotNull(doc, "description", regularUser.getDescription());
            appendIfNotNull(doc, "gender", regularUser.getGender());
            appendIfNotNull(doc, "location", regularUser.getLocation());
        }

        return doc;
    }

    //MongoDB queries
    //Find the distribution of genders between users
    @Override
    public List<Document> getGenderDistribution() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$group: {_id: \"$gender\", count: { $sum: 1 }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

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
    public int averageAgeUsers() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$project: {age: {$divide: [{ $subtract: [new Date(), \"$birthday\"] },\n" +
                    "          1000 * 60 * 60 * 24 * 365 ] }}\n"));
            pipeline.add(Document.parse("{ $group: {_id: null, averageAge: { $avg: \"$age\" }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result.get(0).getInteger("averageAge");
        }
        catch (Exception e){
            throw new DAOException("Error getting average age of users", e);
        }
    }

    //Find the distribution of users by location
    @Override
    public List<Document> getLocationDistribution() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$group: {_id: \"$location\", count: { $sum: 1 }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

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
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$project: {age: {$divide: [{ $subtract: [new Date(), \"$birthday\"] },\n" +
                    "          1000 * 60 * 60 * 24 * 365 ] }}\n"));
            pipeline.add(Document.parse("{$bucket: {groupBy: \"$age\", boundaries: [13, 20, 40, 50], default: \"Other\", output: { count: { $sum: 1 } }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result;
        }
        catch (Exception e){
            throw new DAOException("Error getting users by age range", e);
        }
    }


    //Find how many users registered for each year
    @Override
    public List<Document> getUsersRegisteredByYear() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{ $group: { _id: { $year: { $toDate: \"$joined_on\" } }, count: { $sum: 1 } } }"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result;
        } catch (Exception e) {
            throw new DAOException("Error getting users registered by year", e);
        }
    }

    //Find average app_rating based on the age of users
    @Override
    public int averageAppRatingByAge (int yearOfBirth) throws DAOException{
        try (MongoClient monngoClient = getConnection()) {
            MongoCollection<Document> users = monngoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: {\"birthday\": { $gte: ISODate(\"" + yearOfBirth + "-01-01T00:00:00.000Z\"),        " +
                    "$lt: ISODate(\"" + (yearOfBirth+1) + "-01-01T00:00:00.000Z\")}}}"));
            pipeline.add(Document.parse("{$group: {_id: null, averageAppRating: { $avg: \"$app_rating\" }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result.get(0).getInteger("averageAppRating");
        }
        catch (Exception e){
            throw new DAOException("Error getting average app rating by age", e);
        }

    }
    //Find average app_rating based on the location of users
    @Override
    public int averageAppRatingByLocation (String location) throws DAOException{
        try (MongoClient monngoClient = getConnection()) {
            MongoCollection<Document> users = monngoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"location\": \"" + location + "    }  }"));
            pipeline.add(Document.parse("{$group: {_id: null, averageAppRating: { $avg: \"$app_rating\" }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result.get(0).getInteger("averageAppRating");
        }
        catch (Exception e){
            throw new DAOException("Error getting average app rating by age", e);
        }

    }
    //Find average app_rating based on the gender of users
    @Override
    public List<Document> averageAppRatingByGender () throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$group: {_id: \"$gender\", averageAppRating: { $avg: \"$app_rating\" }}}"));

            AggregateIterable<Document> aggregationResult = users.aggregate(pipeline);

            List<Document> result = new ArrayList<>();
            aggregationResult.into(result);

            return result;


        }
        catch (Exception e){
            throw new DAOException("Error getting genre distribution", e);
        }
    }

    @Override
    public void createUserNode(String id, String username, String picture) throws DAOException {

    }

    @Override
    public void followUser(String followerUserId, String followingUserId) throws DAOException {

    }

    @Override
    public void unfollowUser(String followerUserId, String followingUserId) throws DAOException {

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


}