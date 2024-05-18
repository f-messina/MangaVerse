package it.unipi.lsmsd.fnf.service.impl;

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
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.utils.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

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
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            User user = new User();
            user.setId(userSummaryDTO.getId());
            user.setUsername("exampleUser2");
            user.setProfilePicUrl("profilePicUrl");
            user.setBirthday(LocalDate.of(1999, 12, 12));
            user.setGender(Gender.MALE);
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
            userService.deleteUser(userSummaryDTO.getId());
            System.out.println("User deleted: " + userSummaryDTO.getId());
            Thread.sleep(2*1000);
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
    void getFollowingTest() {
        try {
            UserService userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
            System.out.println(userService.getFollowing(userSummaryDTO.getId(), null));
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
            System.out.println(userService.getUserById(userSummaryDTO.getId()));
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
    void suggestUsersTest() {
        try {
            UserServiceImpl userService = new UserServiceImpl();
            UserSummaryDTO userSummaryDTO = userService.searchFirstNUsers("Xinil", 1, null).getFirst();
            System.out.println(userService.suggestUsers(userSummaryDTO.getId()));
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

    @Test
    void averageAppRatingByAgeRangeTest() {
        try {
            UserService userService = new UserServiceImpl();
            System.out.println(userService.averageAppRatingByAgeRange());
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }
}