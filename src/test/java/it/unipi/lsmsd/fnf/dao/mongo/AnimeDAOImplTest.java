package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import junit.framework.TestCase;

import java.util.List;

public class AnimeDAOImplTest extends TestCase {

    public void testInsert() {
        Anime animeToInsert = createSampleAnime();
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();

        try {
            String idAnimeInserted = animeDAO.insert(animeToInsert);

            // Verifica che l'ID restituito non sia nullo

            // Verifica che l'oggetto Anime sia stato inserito correttamente
            Anime retrievedAnime = animeDAO.find(idAnimeInserted);
            assertEquals(animeToInsert.getTitle(), retrievedAnime.getTitle(), "Inserted anime title should match");

        }  catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }
    private Anime createSampleAnime() {
        Anime anime = new Anime();
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample.jpg");
        anime.setAverageRating(9.0);
        anime.setEpisodeCount(12);
        anime.setProducers("StudioProduction I.G");
        anime.setTags(List.of("Action", "Adventure", "Comedy", "Drama", "Fantasy", "Magic", "Military", "Shounen"));
        anime.setSeason("SUMMER");
        anime.setYear(2019);
        anime.setStatus(Status.FINISHED);
        anime.setSynopsis("Sample synopsis");
        return anime;
    }

    public void testUpdate() {
    }

    public void testDelete() {
    }

    public void testFind() {
    }

    public void testSearch() {
    }
}