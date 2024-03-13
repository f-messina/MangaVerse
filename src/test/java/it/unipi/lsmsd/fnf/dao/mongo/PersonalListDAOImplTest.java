package it.unipi.lsmsd.fnf.dao.mongo;

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

    public void testInsert() {
    }

    public void testUpdate() {
    }

    public void testAddToList() {
    }

    public void testRemoveFromList() {
    }

    public void testUpdateItem() {
    }

    public void testRemoveItem() {
    }

    public void testDelete() {
    }

    public void testDeleteByUser() {
    }

    public void testFindByUser() {
    }

    public void testFindAll() {
    }

    public void testFind() {
    }

    public void testFindPopularAnime() {
    }

    public void testTestFindPopularAnime() {
    }

    public void testFindPopularManga() {
    }

    public void testTestFindPopularManga() {
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