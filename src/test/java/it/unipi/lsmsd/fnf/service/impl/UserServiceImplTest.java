package it.unipi.lsmsd.fnf.service.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks.UpdateNumberOfFollowedTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks.UpdateNumberOfFollowersTask;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.utils.Constants;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO.getSession;
import static it.unipi.lsmsd.fnf.service.ServiceLocator.getExecutorTaskService;

class UserServiceImplTest {
    private static final ExecutorTaskService aperiodicTaskService = ServiceLocator.getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
    private static final TaskManager errorTaskManager = ServiceLocator.getErrorsTaskManager();

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
        BaseNeo4JDAO.openConnection();
        aperiodicTaskService.start();
        errorTaskManager.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        BaseMongoDBDAO.closeConnection();
        BaseNeo4JDAO.closeConnection();
        aperiodicTaskService.stop();
        errorTaskManager.stop();
    }

    @Test
    void signupTest() {
        UserRegistrationDTO user = new UserRegistrationDTO();
        user.setUsername("exampleUser");
        user.setEmail("example@gmail.com");
        user.setPassword("password");
        UserServiceImpl userService = new UserServiceImpl();
        try {
            userService.signup(user);
            System.out.println("User created: " + user);
            Thread.sleep(2*1000);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void loginTest() {
        String email = "example@gmail.com";
        String password = "password";
        UserServiceImpl userService = new UserServiceImpl();
        try {
            System.out.println(userService.login(email, password));
            Thread.sleep(2*1000);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateUserInfoTest() {
        try {
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById("6577877be68376234760596d", false);
            user.setUsername("Dragon_Empress");
            user.setLocation("Columbus, Georgia"); //put back to Columbus, Georgia
            userService.updateUserInfo(user);
            Thread.sleep(2*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteUserTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            userService.deleteUser(userSummaryDTO.getId(), null);
            System.out.println("User deleted: " + userSummaryDTO.getId());
            Thread.sleep(4*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void followTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            UserSummaryDTO userSummaryDTO2 = userService.searchFirstNUsers("exampleUser2", 1, null).getFirst();
            System.out.println(userSummaryDTO);
            System.out.println(userSummaryDTO2);
            userService.follow(userSummaryDTO.getId(), userSummaryDTO2.getId());
            Thread.sleep(2*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void unfollowTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            UserSummaryDTO userSummaryDTO2 = userService.searchFirstNUsers("exampleUser2", 1, null).getFirst();
            System.out.println(userSummaryDTO);
            System.out.println(userSummaryDTO2);
            userService.unfollow(userSummaryDTO.getId(), userSummaryDTO2.getId());
            Thread.sleep(2*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getFollowingsTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            System.out.println(userService.getFollowings(userSummaryDTO.getId(), null));
            Thread.sleep(2*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getFollowersTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser2", 1, null).getFirst();
            System.out.println(userService.getFollowers(userSummaryDTO.getId(), null));
            Thread.sleep(2*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserByIdTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            System.out.println(userService.getUserById(userSummaryDTO.getId(), false));
            Thread.sleep(2*1000);
        } catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void isFollowingTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            UserSummaryDTO userSummaryDTO2 = userService.searchFirstNUsers("exampleUser2", 1, null).getFirst();
            System.out.println(userService.isFollowing(userSummaryDTO.getId(), userSummaryDTO2.getId()));
            Thread.sleep(2*1000);
        }
        catch (BusinessException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void searchFirstNUsersTest() {
        try {
            UserServiceImpl userService = new UserServiceImpl();
            System.out.println(userService.searchFirstNUsers("exampleUser", 1, null));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    //Test works but returns an empty list
    void suggestUsersTest() {
        try {
            UserServiceImpl userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("Xinil", 1, null).getFirst();
            System.out.println(userService.suggestUsersByCommonFollowings(userSummaryDTO.getId()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void getDistributionTest() {
        try {
            UserService userService = new UserServiceImpl();
            System.out.println(userService.getDistribution("location"));
            System.out.println(userService.getDistribution("gender"));
            System.out.println(userService.getDistribution("birthday"));
            System.out.println(userService.getDistribution("joined_on"));
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

        @Test
    void averageAppRatingTest() {
        try {
            UserService userService = new UserServiceImpl();
            System.out.println(userService.averageAppRating("location"));
            System.out.println(userService.averageAppRating("gender"));
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Get the number of following of the users

    @Test
    void getNumberOfFollowingsTest() {
        ExecutorTaskService aperiodicExecutorTaskService = getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);

        try {
            List<String> usersIds = getUserIds();
            for(String userId : usersIds) {
                UpdateNumberOfFollowedTask task = new UpdateNumberOfFollowedTask(userId);
                aperiodicExecutorTaskService.executeTask(task);
            }
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    //Get the number of followers of the users
    @Test
    void getNumberOfFollowersTest() {
        ExecutorTaskService aperiodicExecutorTaskService = getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);

        try {
            List<String> usersIds = getUserIds();
            for(String userId : usersIds) {
                UpdateNumberOfFollowersTask task1 = new UpdateNumberOfFollowersTask(userId);
                aperiodicExecutorTaskService.executeTask(task1);
            }
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    //Get users Ids
    List<String> getUserIds() {
        MongoCollection<Document> usersCollection = BaseMongoDBDAO.getCollection("users");
        List<String> usersIds = new ArrayList<>();
        usersCollection.find().projection(new Document("_id", 1))
                .map(doc -> doc.getObjectId("_id").toHexString())
                .into(usersIds);

        return usersIds;
    }

    // IMPORTANT: The following tests are for the purpose of removing the default image from the database
    //Run this test also for anime and manga(before find the default image in the frontend)
    @Test
    public void updateDefaultProfilePictureOnMongoDB() throws Exception {
        MongoCollection<Document> usersCollection = BaseMongoDBDAO.getCollection("users");

        // Update filter: target documents with "picture" field equal to the old URL
        Bson updateFilter = new Document("picture", "https://imgbox.com/7MaTkBQR");

        // Find all matching documents
        FindIterable<Document> matchingUsers = usersCollection.find(updateFilter);

        // Iterate through matching documents and update each one
        for (Document userDocument : matchingUsers) {
            User user = new User();
            user.setId(userDocument.getObjectId("_id").toHexString());
            user.setProfilePicUrl(Constants.NULL_STRING);
            // Retrieve the updated user data using UserServiceImpl.getUserById(userId); // Assuming you need it
            UserServiceImpl userService = new UserServiceImpl();
            userService.updateUserInfo(user);
        }
        System.out.println("Profile picture(s) updated successfully for all matching users.");
        Thread.sleep(2*60*1000);
    }
    // default images: https://imgbox.com/7MaTkBQR, images/account-icon.png
    // image test: https://thypix.com/wp-content/uploads/2021/10/manga-profile-picture-82.jpg

}
