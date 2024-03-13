package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.closeConnection;
import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.openConnection;

public class MangaDAOImplTest extends TestCase {


    public void testInsert() {
    }

    public void testUpdate() {
    }

    public void testFind() {
    }

    public void testSearch() {
    }

    public void testDelete() {
    }

    public void testDocumentToManga() {
    }

    //OK for genres, themes, demographics
    //Authors OK but it returns: id, role and name
    public void testGetBestCriteriaManga() {
        MangaDAOImpl mangaDAO = new MangaDAOImpl();
        try {
            openConnection();
            Map<String, Double> bestManga = mangaDAO.getBestCriteria("authors", true, 2);
            for (Map.Entry<String, Double> entry : bestManga.entrySet()) {
                System.out.println("Authors: " + entry.getKey() + ", Average rating: " + entry.getValue());
            }

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }
}