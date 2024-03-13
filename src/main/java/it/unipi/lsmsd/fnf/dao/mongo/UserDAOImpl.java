package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
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
import java.util.*;

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
                        throw new DAOException(DAOExceptionType.TAKEN_EMAIL_USERNAME,"Email and username already in use");
                    } else if (existingUser.getString("email").equals(user.getEmail())) {
                        throw new DAOException(DAOExceptionType.TAKEN_EMAIL,"Email already in use");
                    } else {
                        throw new DAOException(DAOExceptionType.TAKEN_USERNAME,"Username already in use");
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
                throw new DAOException(DAOExceptionType.TAKEN_USERNAME,"Username already exists in the collection");
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
                    throw new DAOException(DAOExceptionType.WRONG_PSW,"Wrong password");
                }
            } else {
                throw new DAOException(DAOExceptionType.WRONG_EMAIL,"User not found");
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
            // TODO: change gender name() to ToString()
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
                return aggregationResult.get(0).getDouble("averageAge");
            } else {
                return null; // o un valore predefinito a tua scelta
            }
        }
        catch (Exception e){
            throw new DAOException("Error getting average age of usersCollection", e);
        }
    }


    //Find average app_rating based on the birthday, location and gender.
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
    public Map<String, Double> averageAppRatingByAgeRange () throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = new ArrayList<>();

            Bson projectStage = Aggregates.project(
                    Projections.fields(
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
        if (age == 0) {
            return("0-13");
        } else if (age == 13) {
            return("13-20");
        } else if (age == 20) {
            return ("20-40");
        } else if (age == 40) {
            return("40-50");
        } else {
            return("50+");
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
