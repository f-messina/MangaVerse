package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.AnimeDAOImpl;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.types.ObjectId;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import junit.framework.TestCase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;


import java.util.List;

import static reactor.core.publisher.Mono.when;

public class AnimeDAOImplTest extends TestCase {



    @Test
    public void testInsert() {
        // Creazione di un oggetto Anime di prova
        Anime animeToInsert = createSampleAnime();
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();

        try {
            ObjectId idAnimeInserted = animeDAO.insert(animeToInsert);

            // Verifica che l'ID restituito non sia nullo
            assertNotNull(String.valueOf(idAnimeInserted), "Inserted anime should have a non-null ID");

            // Verifica che l'oggetto Anime sia stato inserito correttamente
            Anime retrievedAnime = animeDAO.find(idAnimeInserted);
            assertNotNull(String.valueOf(retrievedAnime), "Inserted anime should be retrievable from the database");
            assertEquals(animeToInsert.getTitle(), retrievedAnime.getTitle(), "Inserted anime title should match");
            // Verificare altri campi se necessario
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
        anime.setStatus(Status.FINISHED); // Usa l'enumerazione direttamente
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