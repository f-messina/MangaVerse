package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MangaStatus;
import it.unipi.lsmsd.fnf.model.enums.MangaType;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;

import static org.junit.jupiter.api.Assertions.*;

class MangaDAOMongoImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws Exception {
        BaseMongoDBDAO.closeConnection();
    }

    @Test
    void createMediaContent() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        Manga manga = createSampleManga();
        try {
            mangaDAO.saveMediaContent(manga);
            System.out.println("Manga created: " + manga.getId());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateMediaContent() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        Manga manga = createSampleManga();
        manga.setId("65ee545c0eae444a1cf95640");
        manga.setTitle("Updated Manga");
        try {
            mangaDAO.updateMediaContent(manga);
            System.out.println("Manga updated: " + manga.getId());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void deleteMediaContent() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        try {
            mangaDAO.deleteMediaContent("65ee545c0eae444a1cf95640");
            System.out.println("Manga deleted");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void readMediaContent() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        try {
            Manga manga = mangaDAO.readMediaContent("65ee545c0eae444a1cf95640");
            System.out.println("Manga read: " + manga.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void search() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        try {
            PageDTO<MangaDTO> mangaPage = mangaDAO.search(List.of(Map.of("$in",Map.of("genres", List.of("Fantasy", "Horror")))), Map.of("title", 1), 1);
            System.out.println("manga found: " + mangaPage.getTotalCount());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateLatestReview() {
    }

    private Manga createSampleManga() {
        Manga manga = new Manga();
        manga.setTitle("Sample Manga");
        manga.setGenres(List.of("Sample Genre"));
        manga.setType(MangaType.MANGA);
        manga.setStartDate(LocalDate.of(2021, 1, 1));
        manga.setStatus(MangaStatus.ONGOING);
        manga.setImageUrl("Sample Cover URL");
        return manga;
    }

    @Test
    public void testGetBestCriteriaManga() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        try {
            Map<String, Double> bestManga = mangaDAO.getBestCriteria("authors", true, 2);
            for (Map.Entry<String, Double> entry : bestManga.entrySet()) {
                System.out.println("Authors: " + entry.getKey() + ", Average rating: " + entry.getValue());
            }
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }
}