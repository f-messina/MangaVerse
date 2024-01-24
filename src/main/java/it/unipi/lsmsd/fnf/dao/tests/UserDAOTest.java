package it.unipi.lsmsd.fnf.dao.tests;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.UserDAOImpl;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

public class UserDAOTest {

    public static void main(String[] args) {
        UserDAOImpl userDAO = new UserDAOImpl();

        // Test register method
        User userToRegister = createSampleUser();
        ObjectId id = testRegister(userDAO, userToRegister);
        userToRegister.setId(id);

        // Test update method
        User updatedUser = createUpdatedUser(userToRegister);
        testUpdate(userDAO, updatedUser);

        // Test find method
        testFind(userDAO, userToRegister.getId());

        // Test authenticate method
        testAuthenticate(userDAO, userToRegister.getEmail());

        // Test find by username method
        testFindByUsername(userDAO, updatedUser.getUsername());

        // Test find all method
        testFindAll(userDAO);

        // Test remove method
        testRemove(userDAO, userToRegister.getId());
    }

    private static ObjectId testRegister(UserDAOImpl userDAO, User user) {
        try {
            System.out.println("Registering user: " + user.getUsername());
            ObjectId id = userDAO.register(user);
            System.out.println("User registered successfully!");
            return id;
        } catch (DAOException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return null;
        }
    }

    private static void testUpdate(UserDAOImpl userDAO, User user) {
        try {
            System.out.println("Updating user: " + user.getUsername());
            userDAO.update(user);
            System.out.println("User updated successfully!");
        } catch (DAOException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    private static void testFind(UserDAOImpl userDAO, ObjectId userId) {
        try {
            System.out.println("Finding user with ID: " + userId);
            RegisteredUser foundUser = userDAO.find(userId);
            if (foundUser != null) {
                System.out.println("Found user: " + ((User) foundUser).getUsername());
            } else {
                System.out.println("User not found.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
    }

    private static void testAuthenticate(UserDAOImpl userDAO, String email) {
        try {
            System.out.println("Authenticating user...");
            RegisteredUser authenticatedUser = userDAO.authenticate(email, "password");
            System.out.println("User authenticated: " + ((User) authenticatedUser).getUsername());
        } catch (DAOException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
    }

    private static void testFindByUsername(UserDAOImpl userDAO, String username) {
        try {
            System.out.println("Finding user by username: " + username);
            List<RegisteredUserDTO> users = userDAO.find(username);
            if (!users.isEmpty()) {
                System.out.println("Found user(s):");
                for (RegisteredUserDTO user : users) {
                    System.out.println("Username: " + user.getUsername() + ", Profile Pic: " + user.getProfilePicUrl());
                }
            } else {
                System.out.println("No users found with the given username.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
    }

    private static void testFindAll(UserDAOImpl userDAO) {
        try {
            System.out.println("Finding all users...");
            List<RegisteredUserDTO> users = userDAO.findAll();
            if (!users.isEmpty()) {
                System.out.println("All users:");
                for (RegisteredUserDTO user : users) {
                    System.out.println("Username: " + user.getUsername() + ", Profile Pic: " + user.getProfilePicUrl());
                }
            } else {
                System.out.println("No users found.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
    }

    private static void testRemove(UserDAOImpl userDAO, ObjectId userId) {
        try {
            System.out.println("Removing user with ID: " + userId);
            userDAO.remove(userId);
            System.out.println("User removed successfully!");
        } catch (DAOException e) {
            System.err.println("Error removing user: " + e.getMessage());
        }
    }

    private static User createSampleUser() {
        User user = new User();
        user.setEmail("sample@example.com");
        user.setUsername("sample_user");
        user.setPassword("password");
        user.setFullname("Sample User");
        user.setProfilePicUrl("profile.jpg");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setDescription("Sample description");
        user.setGender("Male");
        user.setLocation("Sample City");

        // Set other properties as needed
        return user;
    }

    private static User createUpdatedUser(User user) {
        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setUsername("updated_user");
        updatedUser.setFullname("Updated User");
        updatedUser.setProfilePicUrl("updated_profile.jpg");
        // Set other properties as needed
        return updatedUser;
    }
}

