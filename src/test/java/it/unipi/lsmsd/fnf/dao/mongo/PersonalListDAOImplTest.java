package it.unipi.lsmsd.fnf.dao.mongo;

<<<<<<< HEAD
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
=======
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import junit.framework.TestCase;

import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.closeConnection;
import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.openConnection;

public class PersonalListDAOImplTest extends TestCase {
>>>>>>> noemi

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

    public void testPopularMediaContentList() {
        PersonalListDAOImpl personalListDAO = new PersonalListDAOImpl();
        try {
            openConnection();
            Map<PageDTO<? extends MediaContentDTO>, Integer> popularMediaContentMap = personalListDAO.popularMediaContentList(MediaContentType.ANIME);
            // Verifica che la mappa non sia nulla e che contenga almeno un elemento
            assertNotNull(popularMediaContentMap);
            assertFalse(popularMediaContentMap.isEmpty());

            // Stampa i titoli degli anime più popolari e il numero di liste in cui sono presenti
            for (Map.Entry<PageDTO<? extends MediaContentDTO>, Integer> entry : popularMediaContentMap.entrySet()) {
                PageDTO<? extends MediaContentDTO> pageDTO = entry.getKey();
                Integer totalLists = entry.getValue();
                for (MediaContentDTO content : pageDTO.getEntries()) {
                    System.out.println("Titolo: " + content.getTitle() + ", Numero di liste: " + totalLists);
                }
            }

            closeConnection();

        }
        catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }
}