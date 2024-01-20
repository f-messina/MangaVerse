package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class UserDAOImpl extends BaseMongoDBDAO implements UserDAO {
  
    @Override
    public void insert(User user) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            user.setJoinedDate(LocalDate.now());
            Document userDoc = RegisteredUserToDocument(user);

            users.insertOne(userDoc);
        }
        catch (Exception e){
            throw new DAOException("Error adding new user", e);
        }
    }

    @Override
    public void update(RegisteredUser user) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = Filters.eq("_id", user.getId());
            Bson update = new Document("$set", RegisteredUserToDocument(user));

            users.updateOne(filter, update);
        } catch (Exception e){
            throw new DAOException("Error updating user information for user with id: "+ user.getId(), e);
        }
    }

    @Override
    public void remove(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = Filters.eq("_id", id);

            users.deleteOne(filter);
        }
        catch (Exception e){
            throw new DAOException("Error removing user", e);
        }
    }

    @Override
    public RegisteredUser find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = Filters.eq("_id", id);
            Bson projection = Projections.exclude("is_manager");

            Document userDocument = users.find(filter).projection(projection).first();

            return (userDocument != null)? documentToRegisteredUser(userDocument) : null;
        }
        catch (Exception e){
            throw new DAOException("Error searching user by username: "+ id, e);
        }
    }

    @Override
    public RegisteredUser find(String username) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = Filters.eq("username", username);
            Bson projection = Projections.exclude("is_manager");

            Document userDocument = users.find(filter).projection(projection).first();

            return (userDocument != null)? documentToRegisteredUser(userDocument) : null;
        }
        catch (Exception e){
            throw new DAOException("Error searching user by username: "+ username, e);
        }
    }

    public List<RegisteredUserDTO> findAll() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> users = mongoClient.getDatabase("mangaVerse").getCollection("users");

            Bson filter = Filters.eq("is_manager", false);
            Bson projection = Projections.include("username", "profilePicUrl");

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
        user.setProfilePicUrl(doc.getString("profilePicUrl"));
        return user;
    }

    private RegisteredUser documentToRegisteredUser(Document doc) {
        RegisteredUser user;

        if (doc.getBoolean("is_manager")) {
            Manager manager = new Manager();
            manager.setHiredDate(ConverterUtils.convertDateToLocalDate(doc.getDate("hired_on")));
            manager.setTitle(doc.getString("title"));
            user = manager;
        } else {
            User regularUser = new User();
            regularUser.setBirthday(ConverterUtils.convertDateToLocalDate(doc.getDate("birthday")));
            regularUser.setDescription(doc.getString("description"));
            regularUser.setGender(doc.getString("gender"));
            regularUser.setLocation(doc.getString("location"));
            user = regularUser;
        }

        user.setId(doc.getObjectId("_id"));
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setJoinedDate(ConverterUtils.convertDateToLocalDate(doc.getDate("joined_on")));
        user.setFullname(doc.getString("fullname"));
        user.setProfilePicUrl(doc.getString("profilePicUrl"));

        return user;
    }

    private Document RegisteredUserToDocument(RegisteredUser user) {
        Document doc = new Document();
        appendIfNotNull(doc, "username", user.getUsername());
        appendIfNotNull(doc, "password", user.getPassword());
        appendIfNotNull(doc, "email", user.getEmail());

        if (user.getJoinedDate() != null) {
            appendIfNotNull(doc, "joined_on", ConverterUtils.convertLocalDateToDate(user.getJoinedDate()));
        }
        appendIfNotNull(doc, "fullname", user.getFullname());
        appendIfNotNull(doc, "profilePicUrl", user.getProfilePicUrl());

        if (user instanceof Manager manager) {
            appendIfNotNull(doc, "title", manager.getTitle());
            appendIfNotNull(doc, "hired_on", ConverterUtils.convertLocalDateToDate(manager.getHiredDate()));
        } else if (user instanceof User regularUser) {
            appendIfNotNull(doc, "birthday", ConverterUtils.convertLocalDateToDate(regularUser.getBirthday()));
            appendIfNotNull(doc, "description", regularUser.getDescription());
            appendIfNotNull(doc, "gender", regularUser.getGender());
            appendIfNotNull(doc, "location", regularUser.getLocation());
        }

        return doc;
    }
}



