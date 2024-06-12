package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;

import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOMongoImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws DAOException {
        BaseMongoDBDAO.closeConnection();
    }

    @Test
    public void testCreateUser() throws DAOException {

        String username = "exampleUser";
        String email = "example@gmail.com";
        String password = "password";
        UserRegistrationDTO user = new UserRegistrationDTO();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        UserDAO userDAO = new UserDAOMongoImpl();
        try {
            userDAO.saveUser(user);
            System.out.println("User created: " + user.getId());
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
        BaseMongoDBDAO.closeConnection();
    }

    @Test
    public void testUpdate() {
        try {
            UserDAO userDAO = new UserDAOMongoImpl();
            UserSummaryDTO userSummaryDTO = userDAO.searchFirstNUsers("exampleUser", 1, null).getFirst();
            User user = new User();
            user.setId(userSummaryDTO.getId());
            user.setUsername("exampleUser2");
            user.setProfilePicUrl("https://example.com");
            user.setBirthday(LocalDate.of(1999, 12, 12));
            user.setGender(Gender.MALE);
            userDAO.updateUser(user);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }

    }

    @Test
    public void testRemove() {
        try {
            UserDAO userDAO = new UserDAOMongoImpl();
            UserSummaryDTO userSummaryDTO = userDAO.searchFirstNUsers("exampleUser", 1, null).getFirst();

            userDAO.deleteUser(userSummaryDTO.getId());
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    public void testAuthenticate() {
        String email = "example@gmail.com";
        String password = "password";
        UserDAO userDAO = new UserDAOMongoImpl();
        try {
            System.out.println(userDAO.authenticate(email, password));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    public void testRead() {
        try {
            UserDAO userDAO = new UserDAOMongoImpl();
            System.out.println(userDAO.readUser("6577877be68376234760585b", false, true));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    //OK for gender, location, birthday and joined_on
    @Test
    public void testGetDistribution() {
        try {
            UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
            Map<String, Integer> distribution = userDAO.getDistribution("location");
            System.out.println(distribution.toString());

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    @Test
    public void testAverageAppRating() {
        try {

            UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
            Map<String, Double> averageRating = userDAO.averageAppRating("gender");
            System.out.println(averageRating);

            Map<String, Double> averageRating2 = userDAO.averageAppRating("location");
            System.out.println(averageRating2);
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    @Test
    public void testAverageAppRatingByAgeRange() {
        try {

            UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
            Map<String, Double> averageRating = userDAO.averageAppRatingByAgeRange();
            System.out.println(averageRating);

        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void updateNumOfFollowersTest() {
        try {
            UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
            UserSummaryDTO userSummaryDTO = userDAO.searchFirstNUsers("exampleUser", 1, null).getFirst();

            userDAO.updateNumOfFollowers(userSummaryDTO.getId(), 5);
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    @Test
    public void updateNumOfFollowingsTest() {
        try {
            UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
            UserSummaryDTO userSummaryDTO = userDAO.searchFirstNUsers("exampleUser", 1, null).getFirst();

            userDAO.updateNumOfFollowings(userSummaryDTO.getId(), 10);
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }
}
