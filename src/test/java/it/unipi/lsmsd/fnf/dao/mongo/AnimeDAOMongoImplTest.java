package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class AnimeDAOMongoImplTest {

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
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        Anime anime = createSampleAnime();
        try {

            animeDAO.saveMediaContent(anime);
            System.out.println("Anime created: " + anime.getId());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void updateMediaContent() {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        Anime anime = createSampleAnime();
        anime.setId("662912c3521b86ea108246eb");
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
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        try {
            animeDAO.deleteMediaContent("662912c3521b86ea108246eb");
            System.out.println("Anime deleted");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void readMediaContent() {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        try {
            Anime anime = animeDAO.readMediaContent("65789bb52f5d29465d0abd4d");
            System.out.println("Anime read: " + anime.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void search() {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
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

    @Test
    public void testGetBestCriteriaAnime() {

        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        try {
            Map<String, Double> bestAnime = animeDAO.getBestCriteria("tags", true, 2);
            for (Map.Entry<String, Double> entry : bestAnime.entrySet()) {
                System.out.println("Tag: " + entry.getKey() + ", Average rating: " + entry.getValue());
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private Anime createSampleAnime() {
        Anime anime = new Anime();
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample.jpg");
        anime.setEpisodeCount(12);
        anime.setProducers("StudioProduction I.G");
        anime.setYear(2019);
        anime.setStatus(AnimeStatus.FINISHED);
        return anime;
    }
}