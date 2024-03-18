package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.*;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Updates.pullByFilter;

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
                projection = fields(include("location", "birthday"),
                        excludeId());
            } else {
                projection = exclude("is_manager", "password");
            }

            return Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(this::documentToRegisteredUser)
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
    @Override
    public UserSummaryDTO authenticate(String email, String password) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("email", email),eq("password", password));
            Bson projection = exclude("is_manager", "password");

            return Optional.ofNullable(usersCollection.find(filter).projection(projection).first())
                    .map(doc -> new UserSummaryDTO(doc.getObjectId("_id").toString(), doc.getString("username"), doc.getString("picture")))
                    .orElseThrow (() -> new AuthenticationException("User not found"));

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
                        .map(this::documentToUserSummaryDTO)
                        .toList();
            } else {
                return usersCollection.find(filter).sort(sort).projection(projection).limit(n).into(new ArrayList<>())
                        .stream()
                        .map(this::documentToUserSummaryDTO)
                        .toList();
            }
        } catch (Exception e) {
            throw new DAOException("Error searching user by username: " + username, e);
        }
    }

    /**
     * Inserts a new personal list into the database.
     *
     * @param listSummaryDTO The personal list to be inserted.
     * @throws DAOException If an error occurs during the insertion process.
     */
    @Override
    public void insertList(PersonalListSummaryDTO listSummaryDTO) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("_id", new ObjectId(listSummaryDTO.getUserId())), ne("lists.name", listSummaryDTO.getName()));
            ObjectId listId = new ObjectId();
            Bson listDoc = new Document("id", listId).append("name", listSummaryDTO.getName());
            Bson update = push("lists", listDoc);

            if (usersCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("No list was inserted: user not found or list already exists");
            } else {
                listSummaryDTO.setListId(listId.toString());
                System.out.println("List inserted successfully");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates a personal list in the database.
     *
     * @param listSummaryDTO The personal list to be updated.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateList(PersonalListSummaryDTO listSummaryDTO) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            String userId = listSummaryDTO.getUserId();
            String listId = listSummaryDTO.getListId();
            String name = listSummaryDTO.getName();

            Bson filter = and(
                    eq("_id", new ObjectId(userId)),
                    nor(
                            and(
                                    eq("lists.name", name),
                                    ne("lists.id", new ObjectId(listId))
                            )
                    )
            );
            Bson update = set("lists.$[elem].name", name);
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", new ObjectId(listId))));

            UpdateResult result = usersCollection.updateOne(filter, update, options);
            if (result.getMatchedCount() == 0) {
                if (usersCollection.countDocuments(and(eq("_id", new ObjectId(userId)), eq("lists.id", new ObjectId(listId)))) == 0)
                    throw new MongoException("No list was updated: list not found");
                else
                    throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_NAME, "List with name " + name + " already exists");

            } else {
                System.out.println("List updated successfully");
            }
        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public void deleteList(String userId, String listId) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = pull("lists", eq("id", new ObjectId(listId)));

            if (usersCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("No list was deleted: list not found");

            } else {
                System.out.println("List deleted successfully");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Adds an element to a personal list in the database.
     *
     * @param userId The ID of the user who owns the list.
     * @param listId The ID of the list to add the element to.
     * @param mediaId The ID of the media to add to the list.
     * @param mediaType The type of media to add to the list.
     * @throws DAOException If an error occurs during the addition process.
     */
    @Override
    public void addToList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            String mediaTypeString = mediaType.name().toLowerCase();
            if (mediaType == MediaContentType.ANIME) {
                MongoCollection<Document> animeCollection = getCollection("anime");
                if(animeCollection.countDocuments(new Document("_id", new ObjectId(mediaId))) == 0)
                    throw new MongoException("Anime with id " + mediaId + " not found");
            } else if (mediaType == MediaContentType.MANGA) {
                MongoCollection<Document> mangaCollection = getCollection("manga");
                if(mangaCollection.countDocuments(new Document("_id", new ObjectId(mediaId))) == 0)
                    throw new MongoException("Manga with id " + mediaId + " not found");
            }
            Bson filter = and(
                    eq("_id", new ObjectId(userId)),
                    eq("lists.id", new ObjectId(listId))
            );
            Bson update = addToSet("lists.$[elem]." + mediaTypeString + "_list", new ObjectId(mediaId));
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", new ObjectId(listId))));

            UpdateResult result = usersCollection.updateOne(filter, update, options);
            if (result.getModifiedCount() == 0) {
                if (result.getMatchedCount() == 0)
                    throw new MongoException("List not found");
                else
                    throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_KEY, "Element already exists in the list");

            } else {
                System.out.println("Element added to the list successfully");

            }

        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Removes an element from a personal list in the database.
     *
     * @param userId The ID of the user who owns the list.
     * @param listId The ID of the list to remove the element from.
     * @param mediaId The ID of the media to remove from the list.
     * @param mediaType The type of media to remove from the list.
     * @throws DAOException If an error occurs during the removal process.
     */
    @Override
    public void removeFromList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            String mediaTypeString = mediaType.name().toLowerCase();
            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = pull("lists.$[elem]." + mediaTypeString + "_list", new ObjectId(mediaId));
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", new ObjectId(listId))));

            if (usersCollection.updateOne(filter, update, options).getModifiedCount() == 0) {
                throw new MongoException("No element was removed from the list: list not found");

            } else {
                System.out.println("Element removed from the list successfully");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Removes all the elements in the lists that are not present in the media collections.
     *
     * @throws DAOException If an error occurs during the removal process.
     */
    @Override
    public void removeElementInListWithoutMedia() throws DAOException {
        MongoCollection<Document> animeCollection = getCollection("anime");
        MongoCollection<Document> mangaCollection = getCollection("manga");
        MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

        try {
            List<ObjectId> animeId = animeCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();
            List<ObjectId> mangaId = mangaCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();


            Bson filter = exists("lists", true);
            Bson mangaFilter = nin("lists.$[].manga_list", mangaId);
            Bson animeFilter = nin("lists.$[].anime_list", animeId);
            Bson update = combine(pullByFilter(mangaFilter), pullByFilter(animeFilter));
            if(usersCollection.updateMany(filter, update).getModifiedCount() == 0){
                throw new MongoException("No inconsistency found in the anime and manga lists");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

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
                return aggregationResult.get(0).getDouble("averageAge");
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


    private UserSummaryDTO documentToUserSummaryDTO(Document doc) {
        UserSummaryDTO user = new UserSummaryDTO();
        user.setId(doc.getObjectId("_id").toString());
        user.setUsername(doc.getString("username"));
        user.setProfilePicUrl(doc.getString("picture"));
        user.setLocation(doc.getString("location"));
        Date birthDate = doc.getDate("birthday");
        if (birthDate != null)
            user.setBirthDate(ConverterUtils.dateToLocalDate(birthDate));
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
