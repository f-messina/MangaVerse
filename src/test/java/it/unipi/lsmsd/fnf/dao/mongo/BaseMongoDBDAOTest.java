package it.unipi.lsmsd.fnf.dao.mongo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseMongoDBDAOTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws Exception {
        BaseMongoDBDAO.closeConnection();
    }

    @Test
    void getMongoClientTest() {
        assertNotNull(BaseMongoDBDAO.getMongoClient());
        System.out.println("MongoClient is not null");
    }

    @Test
    void getDatabaseTest() {
        assertNotNull(BaseMongoDBDAO.getCollection("anime"));
        System.out.println("Database is not null");
    }
}
