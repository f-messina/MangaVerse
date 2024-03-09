package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class UserDAOImplTest {

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
        UserDAOImpl userDAO = new UserDAOImpl();
        try {
            userDAO.createUser(user);
            System.out.println("User created: " + user.getId());
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
        BaseMongoDBDAO.closeConnection();
    }

    @Test
    public void testUpdate() {
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testAuthenticate() {
        String email = "rrussell@example.com";
        String password = "08128d06e8073a8d8eb055852bf5744d3477e16fed096b86557a7a233c71d791";
        UserDAOImpl userDAO = new UserDAOImpl();
        try {
            System.out.println(userDAO.authenticate(email, password));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    public void testFind() {
    }

    @Test
    public void testTestFind() {
    }

    @Test
    public void testFindAll() {
    }
}