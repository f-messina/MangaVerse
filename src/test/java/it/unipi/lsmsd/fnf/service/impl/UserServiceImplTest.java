package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
        BaseNeo4JDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws DAOException {
        BaseMongoDBDAO.closeConnection();
        BaseNeo4JDAO.closeConnection();
    }

    @Test
    void registerUserAndLogin() {
        UserRegistrationDTO user = new UserRegistrationDTO();
        user.setEmail("regtest@example.com");
        user.setPassword("passwordtest");
        user.setUsername("Reg");

        UserServiceImpl userService = new UserServiceImpl();

        try {
            userService.registerUserAndLogin(user);
            System.out.println(userService.login(user.getEmail(), user.getPassword()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void login() {
        String email = "rrussell@example.com";
        String password = "08128d06e8073a8d8eb055852bf5744d3477e16fed096b86557a7a233c71d791";
        UserServiceImpl userService = new UserServiceImpl();
        try {
            System.out.println(userService.login(email, password));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateUserInfo() {
        User user = new User();
        //I test on the user from registration and login (initial name: Reg)
        user.setId("663bef6501eead57062a9ba8");
        user.setUsername("RegUpdateTest");

        UserServiceImpl userService = new UserServiceImpl();
        try {
            userService.updateUserInfo(user);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void deleteUser() {
        UserServiceImpl userService = new UserServiceImpl();

        //I delete the user from registration and login (initial name: Reg)
        String userId = "6a9ba8";
        try {
            userService.deleteUser(userId);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }


    }

    @Test
    void getUserById() {
        UserServiceImpl userService = new UserServiceImpl();
        String userId = "6577877be683762347605859";
        try {
            System.out.println(userService.getUserById(userId));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Test //Not working
    void createNode() {
        UserSummaryDTO user = new UserSummaryDTO();
        user.setId("663bef6501eead57062a9ba8");
        user.setUsername("TestNode");
        user.setProfilePicUrl("https://example.com/profilepic.jpg");


        UserServiceImpl userService = new UserServiceImpl();
        try {
            userService.createNode(user);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void follow() {
        UserServiceImpl userService = new UserServiceImpl();
        String followerUserId = "6577877be683762347605869";
        String followingUserId = "6577877be68376234760586a";
        try {
            userService.follow(followerUserId, followingUserId);
            System.out.println("User followed successfully");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void followSelf() {
        UserServiceImpl userService = new UserServiceImpl();
        String followerUserId = "6577877be683762347605869";
        try {
            userService.follow(followerUserId, followerUserId);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void unfollow() {
        UserServiceImpl userService = new UserServiceImpl();
        String followerUserId = "6577877be683762347605869";
        String followingUserId = "6577877be68376234760586a";
        try {
            userService.unfollow(followerUserId, followingUserId);
            System.out.println("User unfollowed successfully");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void getFollowing() {
        UserServiceImpl userService = new UserServiceImpl();
        String userId = "6577877be683762347605869";
        try {
            System.out.println(userService.getFollowing(userId));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void getFollowers() {
        UserServiceImpl userService = new UserServiceImpl();
        String userId = "6577877be683762347605869";
        try {
            System.out.println(userService.getFollowers(userId));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void suggestUsers() {
        UserServiceImpl userService = new UserServiceImpl();
        String userId = "6577877be683762347605869";
        try {
            System.out.println(userService.suggestUsers(userId));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Test
    void searchFirstNUsers() {

    }

    @Test
    void getDistribution() {
        UserServiceImpl userService = new UserServiceImpl();
        try {
            System.out.println(userService.getDistribution("birthday"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Test
    void averageAgeUsers() { //Not working
        UserServiceImpl userService = new UserServiceImpl();
        try {
            System.out.println(userService.averageAgeUsers());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Test
    void averageAppRating() { //Not working
        UserServiceImpl userService = new UserServiceImpl();
        try {
            System.out.println(userService.averageAppRating("age"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Test
    void averageAppRatingByAge() { //Result is not coherent with the expected one
        UserServiceImpl userService = new UserServiceImpl();
        try {
            System.out.println(userService.averageAppRatingByAgeRange());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}