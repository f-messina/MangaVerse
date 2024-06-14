package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDAONeo4JImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseNeo4JDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws DAOException {
        BaseNeo4JDAO.closeConnection();
    }

    @Test
    public void testCreateUser() throws DAOException {
        String id = "6647de5ad1c31b63a0dc7857";
        String username = "exampleUser";
        UserRegistrationDTO user = new UserRegistrationDTO();
        user.setUsername(username);
        user.setId(id);
        UserDAO userDAO = new UserDAONeo4JImpl();
        try {
            userDAO.saveUser(user);
            System.out.println("User created: " + user.getId());
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void follow() {
    }

    @Test
    void unfollow() {
    }

    @Test
    void isFollowing() {
    }

    @Test
    void getNumOfFollowers() {
    }

    @Test
    void getNumOfFollowed() {
    }

    @Test
    void getFollowedUsers() {
    }

    @Test
    void getFollowers() {
    }

    @Test
    void suggestUsersByCommonFollows() {
    }

    @Test
    void suggestUsersByCommonLikes() {
    }
}