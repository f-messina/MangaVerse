package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonalListDAOImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws DAOException {
        BaseMongoDBDAO.closeConnection();
    }

    @Test
    void insert() {
    }

    @Test
    void update() {
    }

    @Test
    void addToList() {
    }

    @Test
    void removeFromList() {
    }

    @Test
    void updateItem() {
    }

    @Test
    void removeItem() {
    }

    @Test
    void delete() {
    }

    @Test
    void deleteByUser() {
    }

    @Test
    void findByUser() {
        String userId = "6577877ce683762347607459";
        PersonalListDAOImpl personalListDAO = new PersonalListDAOImpl();
        try {
            System.out.println(personalListDAO.findByUser(userId, true));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void findAll() {
    }

    @Test
    void find() {
    }

    @Test
    void popularAnime() {
    }

    @Test
    void popularManga() {
    }
}