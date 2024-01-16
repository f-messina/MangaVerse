package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.ExceptionDAO;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.Document;

import java.util.Date;


public class UserDAOImpl extends BaseMongoDBDAO implements UserDAO {
    final MongoClient mongoClient = getConnection();
    MongoDatabase database = mongoClient.getDatabase("mangaVerse");
    MongoCollection<Document> users = database.getCollection("users");


    @Override
    public User addUser(User user) throws ExceptionDAO {
        try(mongoClient){

            // Set the current date for joinedDate
            user.setJoinedDate(new Date());

            //Create a new user
            Document newUser = new Document()
                    .append("email",user.getEmail())
                    .append("password", user.getPassword())
                    .append("fullname", user.getFullname())
                    .append("picture", user.getprofilePicUrl())
                    .append("username", user.getUsername())
                    .append("gender", user.getGender())
                    .append("birthday", user.getBirthdate())
                    .append("location", user.getLocation())
                    .append("joined_date", user.getJoinedDate())
                    .append("description", user.getDescription());

            users.insertOne(newUser);

        }
        catch (Exception e){
            throw new ExceptionDAO("Error adding new user");
        }
        return user;
    }

    @Override
    public void removeUser(String username) throws ExceptionDAO {
        try(mongoClient){
            Document filter = new Document("username", username);
            users.deleteOne(filter);

        }
        catch (Exception e){
            throw new ExceptionDAO("Error removing user");
        }
    }

    @Override
    public RegisteredUser searchUserByUsername(String username) throws ExceptionDAO {
        try(mongoClient) {
            // Create a filter to find the user by username
            Document filter = new Document("username", username);

            // Find the document in the collection
            Document userDocument = users.find(filter).first();

            if (userDocument != null) {
                // Convert the Document to a RegisteredUser object
                User user = new User();
                user.setId(userDocument.getString("_id"));
                user.setUsername(userDocument.getString("username"));
                user.setPassword(userDocument.getString("password"));
                user.setEmail(userDocument.getString("email"));
                user.setFullname(userDocument.getString("fullname"));
                user.setprofilePicUrl(userDocument.getString("profilePicUrl"));
                user.setJoinedDate(userDocument.getDate("joinedDate"));

                user.setBirthdate(userDocument.getDate("birthdate"));
                user.setDescription(userDocument.getString("description"));
                user.setGender(userDocument.getString("gender"));
                user.setLocation(userDocument.getString("location"));

                return user;
            }else {
                throw new ExceptionDAO("User not found with the username: " + username);
            }
        }
        catch (Exception e){
            throw new ExceptionDAO("Error searching user by username: "+ username);
        }
    }

    @Override
    public void updateUserInfo(RegisteredUser user) throws ExceptionDAO {
        try(mongoClient){
            Document filter = new Document("_id", user.getId());
            Document update = new Document()
                    .append("username", user.getUsername())
                    .append("password", user.getPassword())
                    .append("email", user.getEmail())
                    .append("fullname", user.getFullname())
                    .append("profilePicUrl", user.getprofilePicUrl());

            // Check if the user is a Manager
            if (user instanceof Manager manager) {
                update.append("title", manager.getTitle());
            }
            // Check if the user is a User
            if (user instanceof User regularUser) {
                update.append("birthdate", regularUser.getBirthdate())
                        .append("description", regularUser.getDescription())
                        .append("gender", regularUser.getGender())
                        .append("location", regularUser.getLocation());
            }
            users.updateOne(filter,new Document("$set", update));
        } catch (Exception e){
            throw new ExceptionDAO("Error updating user information for user with id: "+user.getId());
        }
    }
}



