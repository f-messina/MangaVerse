package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void insertList() {
        PersonalListDAO personalListDAO = new PersonalListDAOImpl();
        try {
            PersonalListSummaryDTO list = new PersonalListSummaryDTO("65ef316a9d917e0222d8faf7","My top 10 anime 2.0");
            personalListDAO.insertList(list);
            System.out.println(list.getListId());
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void updateList() {
        PersonalListDAO personalListDAO = new PersonalListDAOImpl();
        try {
            personalListDAO.updateList(new PersonalListSummaryDTO("65ef316a9d917e0222d8faf7","65ef3ddaa9a4e01151cd6f28","My top 10 anime updated"));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void deleteList() {
        PersonalListDAO personalListDAO = new PersonalListDAOImpl();
        try {
            personalListDAO.deleteList("65ef316a9d917e0222d8faf7","65ef3ddaa9a4e01151cd6f28");
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void addToList() {
        PersonalListDAO personalListDAO = new PersonalListDAOImpl();
        try {
            personalListDAO.addToList("65ef316a9d917e0222d8faf7","65ef3ddaa9a4e01151cd6f28","65789bb52f5d29465d0abcfb", MediaContentType.ANIME);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void removeFromList() {
        PersonalListDAO personalListDAO = new PersonalListDAOImpl();
        try {
            personalListDAO.removeFromList("65ef316a9d917e0222d8faf7","65ef3ddaa9a4e01151cd6f28","65789bb52f5d29465d0abcfb", MediaContentType.ANIME);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void removeElementInListWithoutMedia() {
    }
}