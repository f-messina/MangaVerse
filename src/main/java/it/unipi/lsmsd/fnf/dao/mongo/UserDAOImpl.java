package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class UserDAOImpl extends BaseMongoDBDAO implements UserDAO {

    private RegisteredUser documentToRegisteredUser(Document doc) {
        RegisteredUser user;
        if (doc.getBoolean("is_manager")) {
            user = new Manager();
            ((Manager) user).setHiredDate(ConverterUtils.convertDateToLocalDate(doc.getDate("hired_on")));
            ((Manager) user).setTitle(doc.getString("title"));
        } else {
            user = new User();
            if (doc.getDate("birthday") != null) {
                ((User) user).setBirthday(ConverterUtils.convertDateToLocalDate(doc.getDate("birthday")));
            }
            if (doc.getString("description") != null) {
                ((User) user).setDescription(doc.getString("description"));
            }
            if (doc.getString("gender") != null) {
                ((User) user).setGender(doc.getString("gender"));
            }
            if (doc.getString("location") != null) {
                ((User) user).setLocation(doc.getString("location"));
            }
        }
        user.setId(doc.getObjectId("_id"));
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setJoinedDate(ConverterUtils.convertDateToLocalDate(doc.getDate("joined_on")));
        if (doc.getString("fullname") != null) {
            user.setFullname(doc.getString("fullname"));
        }
        if (doc.getString("profilePicUrl") != null) {
            user.setprofilePicUrl(doc.getString("profilePicUrl"));
        }

        return user;
    }

    private Document RegisteredUserToDocument(RegisteredUser user) {
        Document doc = new Document()
                .append("username", user.getUsername())
                .append("password", user.getPassword())
                .append("email", user.getEmail());
        if (user.getJoinedDate() != null) {
            doc.append("joined_on", ConverterUtils.convertLocalDateToDate(user.getJoinedDate()));
        }
        if (user.getFullname() != null) {
            doc.append("fullname", user.getFullname());
        }
        if (user.getprofilePicUrl() != null) {
            doc.append("profilePicUrl", user.getprofilePicUrl());
        }

        // Check if the user is a Manager
        if (user instanceof Manager manager) {
            doc.append("title", manager.getTitle())
                    .append("hired_on", ConverterUtils.convertLocalDateToDate(manager.getHiredDate()));
        } else if (user instanceof User regularUser) { // Check if the user is a User
            if (regularUser.getBirthday() != null) {
                doc.append("birthday", ConverterUtils.convertLocalDateToDate(regularUser.getBirthday()));
            }
            if (regularUser.getDescription() != null) {
                doc.append("description", regularUser.getDescription());
            }
            if (regularUser.getGender() != null) {
                doc.append("gender", regularUser.getGender());
            }
            if (regularUser.getLocation() != null) {
                doc.append("location", regularUser.getLocation());
            }
        }
        return doc;
    }
  
    @Override
    public void insert(User user) throws DAOException {


        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");
            // Set the current date for joinedDate
            user.setJoinedDate(LocalDate.now());

            users.insertOne(RegisteredUserToDocument(user));
        }
        catch (Exception e){
            throw new DAOException("Error adding new user");
        }
    }

    @Override
    public void remove(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Document filter = new Document("_id", id);
            users.deleteOne(filter);
        }
        catch (Exception e){
            throw new DAOException("Error removing user");
        }
    }

    @Override
    public RegisteredUser find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            // Create a filter to find the user by username
            Document filter = new Document("_id", id);

            // Find the document in the collection
            Document userDocument = users.find(filter).first();

            if (userDocument != null) {
                return documentToRegisteredUser(userDocument);
            } else {
                throw new DAOException("User not found with the username: " + id);
            }
        }
        catch (Exception e){
            throw new DAOException("Error searching user by username: "+ id);
        }
    }

    @Override
    public RegisteredUser find(String username) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            // Create a filter to find the user by username
            Document filter = new Document("username", username);

            // Find the document in the collection
            Document userDocument = users.find(filter).first();

            if (userDocument != null) {
                return documentToRegisteredUser(userDocument);
            } else {
                throw new DAOException("User not found with the username: " + username);
            }
        }
        catch (Exception e){
            throw new DAOException("Error searching user by username: "+ username);
        }
    }

    public List<RegisteredUser> findAll() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            List<RegisteredUser> result = new ArrayList<>();
            users.find().forEach(document -> {
                RegisteredUser user = documentToRegisteredUser(document);
                result.add(user);
            });
            return result;
        }
        catch (Exception e){
            throw new DAOException("Error searching all the users");
        }
    }
    @Override
    public void update(RegisteredUser user) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Document filter = new Document("_id", user.getId());
            users.updateOne(filter,new Document("$set", RegisteredUserToDocument(user)));
        } catch (Exception e){
            throw new DAOException("Error updating user information for user with id: "+ user.getId());
        }
    }
}



