package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.nin;
import static org.junit.jupiter.api.Assertions.*;

class AnimeDAOImplTest {

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
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        Anime anime = createSampleAnime();
        try {

            animeDAO.createMediaContent(anime);
            System.out.println("Anime created: " + anime.getId());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateMediaContent() {
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        Anime anime = createSampleAnime();
        anime.setId("65ee4f4f44567e63565fd124");
        anime.setTitle("Updated Anime");
        try {
            animeDAO.updateMediaContent(anime);
            System.out.println("Anime updated: " + anime.getId());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void deleteMediaContent() {
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        try {
            animeDAO.deleteMediaContent("65ee4f4f44567e63565fd124");
            System.out.println("Anime deleted");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void readMediaContent() {
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        try {
            Anime anime = animeDAO.readMediaContent("65ee4f4f44567e63565fd124");
            System.out.println("Anime read: " + anime.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void search() {
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        try {
            PageDTO<AnimeDTO> animePage = animeDAO.search(List.of(Map.of("$in",Map.of("tags", List.of("school clubs", "manwha")))), Map.of("title", 1), 1);
            System.out.println("Anime found: " + animePage.getTotalCount());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateLatestReview() {
    }

    private Anime createSampleAnime() {
        Anime anime = new Anime();
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample.jpg");
        anime.setEpisodeCount(12);
        anime.setProducers("StudioProduction I.G");
        anime.setYear(2019);
        anime.setStatus(Status.FINISHED);
        return anime;
    }
}