package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
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

        String username = "pa";
        String email = "flavio@gmail.com";
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
        UserDAO userDAO = new UserDAOMongoImpl();
        User user = new User();
        user.setId("65ebb6a3ccd5770d242c6f57");
        user.setUsername("flavio");
        user.setProfilePicUrl("profilepic");
        user.setBirthday(LocalDate.of(1999, 12, 12));
        user.setGender(Gender.MALE);
        try {
            userDAO.updateUser(user);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }

    }

    @Test
    public void testRemove() {
        UserDAO userDAO = new UserDAOMongoImpl();
        String userId = "65ebb6a3ccd5770d242c6f57";
        try {
            userDAO.deleteUser(userId);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    public void testAuthenticate() {
        String email = "rrussell@example.com";
        String password = "08128d06e8073a8d8eb055852bf5744d3477e16fed096b86557a7a233c71d791";
        UserDAO userDAO = new UserDAOMongoImpl();
        try {
            System.out.println(userDAO.authenticate(email, password));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    public void testRead() {
        String userId = "6577877be68376234760585a";
        UserDAO userDAO = new UserDAOMongoImpl();
        try {
            System.out.println(userDAO.readUser(userId, false));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    public void testTestFind() {
    }

    @Test
    public void testFindAll() {
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
    public void testAverageAgeUsers() {
        try {

            UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
            Double averageAge = userDAO.averageAgeUsers();
            System.out.println(averageAge);


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
}