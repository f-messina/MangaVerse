package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.closeConnection;
import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.openConnection;

public class UserDAOImplTest extends TestCase {

    public void testRegister() {
    }

    public void testUpdate() {
    }

    public void testRemove() {
    }

    public void testAuthenticate() {
    }

    public void testFind() {
    }

    public void testTestFind() {
    }

    public void testFindAll() {
    }

    //OK for gender, location, birthday and joined_on

    public void testGetDistribution() {
        try {
            openConnection();
            UserDAOImpl userDAO = new UserDAOImpl();
            Map<String, Integer> distribution = userDAO.getDistribution("location");
            System.out.println(distribution.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    public void testAverageAgeUsers() {
        try {
            openConnection();
            UserDAOImpl userDAO = new UserDAOImpl();
            Double averageAge = userDAO.averageAgeUsers();
            System.out.println(averageAge);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

   //Location, gender OK
    public void testAverageAppRating() {
        try {
            openConnection();
            UserDAOImpl userDAO = new UserDAOImpl();
            Map<String, Double> averageRating = userDAO.averageAppRating("gender");
            System.out.println(averageRating);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    public void testAverageAppRatingByAgeRange() {
        try {
            openConnection();
            UserDAOImpl userDAO = new UserDAOImpl();
            Map<String, Double> averageRating = userDAO.averageAppRatingByAgeRange();
            System.out.println(averageRating);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }
}