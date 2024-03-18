package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
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
    }

    @Test
    void createNode() {
    }

    @Test
    void follow() {
    }

    @Test
    void unfollow() {
    }

    @Test
    void getFollowing() {
    }

    @Test
    void getFollowers() {
    }
}