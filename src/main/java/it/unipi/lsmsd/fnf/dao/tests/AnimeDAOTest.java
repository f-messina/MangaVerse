package it.unipi.lsmsd.fnf.dao.tests;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.AnimeDAOImpl;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class AnimeDAOTest {

    public static void main(String[] args) {
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();

        // Test insert method
        Anime animeToInsert = createSampleAnime();
        ObjectId idAnimeInserted = testInsert(animeDAO, animeToInsert);
        animeToInsert.setId(idAnimeInserted);

        // Test update method
        Anime updatedAnime = createUpdatedAnime(animeToInsert);
        testUpdate(animeDAO, updatedAnime);

        // Test delete method
        testDelete(animeDAO, updatedAnime.getId());

        // Test find method
        testFind(animeDAO, animeToInsert.getId());

        // Test search method
        testSearch(animeDAO);
    }

    private static ObjectId testInsert(AnimeDAOImpl animeDAO, Anime anime) {
        try {
            System.out.println("Inserting anime: " + anime.getTitle());
            ObjectId id = animeDAO.insert(anime);
            System.out.println("Anime inserted successfully!");
            return id;
        } catch (DAOException e) {
            System.err.println("Error inserting anime: " + e.getMessage());
            return null;
        }
    }

    private static void testUpdate(AnimeDAOImpl animeDAO, Anime anime) {
        try {
            System.out.println("Updating anime: " + anime.getTitle());
            animeDAO.update(anime);
            System.out.println("Anime updated successfully!");
        } catch (DAOException e) {
            System.err.println("Error updating anime: " + e.getMessage());
        }
    }

    private static void testDelete(AnimeDAOImpl animeDAO, ObjectId animeId) {
        try {
            System.out.println("Deleting anime with ID: " + animeId);
            animeDAO.delete(animeId);
            System.out.println("Anime deleted successfully!");
        } catch (DAOException e) {
            System.err.println("Error deleting anime: " + e.getMessage());
        }
    }

    private static void testFind(AnimeDAOImpl animeDAO, ObjectId animeId) {
        try {
            System.out.println("Finding anime with ID: " + animeId);
            Anime foundAnime = animeDAO.find(animeId);
            if (foundAnime != null) {
                System.out.println("Found anime: " + foundAnime.getTitle());
            } else {
                System.out.println("Anime not found.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding anime: " + e.getMessage());
        }
    }

    private static void testSearch(AnimeDAOImpl animeDAO) {
        try {
            System.out.println("Searching for anime...");
            // Provide appropriate search criteria and order by parameters
            List<Map<String, Object>> filters = new ArrayList<>();
            Map<String, Integer> orderBy = singletonMap("average_score", 1);
            int page = 1;

            PageDTO<AnimeDTO> searchResult = animeDAO.search(filters, orderBy, page);

            System.out.println("Search result:");
            if (searchResult != null) {
                System.out.println("Total Count: " + searchResult.getTotalCount());
                for (AnimeDTO animeDTO : searchResult.getEntries()) {
                    System.out.println("Anime: " + animeDTO.getTitle() + ", Average Rating: " + animeDTO.getAverageRating());
                }
            } else {
                System.out.println("No search result.");
            }
        } catch (DAOException e) {
            System.err.println("Error searching for anime: " + e.getMessage());
        }
    }

    private static Anime createSampleAnime() {
        Anime anime = new Anime();
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample.jpg");
        anime.setAverageRating(9.0);
        anime.setEpisodeCount(12);
        anime.setProducers("StudioProduction I.G");
        anime.setTags(List.of("Action", "Adventure", "Comedy", "Drama", "Fantasy", "Magic", "Military", "Shounen"));
        anime.setYear(2019);
        anime.setStatus(Status.valueOf("FINISHED"));
        anime.setSynopsis("Sample synopsis");
        anime.setType(AnimeType.TV);

        // Set other properties as needed
        return anime;
    }

    private static Anime createUpdatedAnime(Anime anime) {
        Anime updatedAnime = new Anime();
        updatedAnime.setId(anime.getId());
        updatedAnime.setTitle("Updated Anime");
        updatedAnime.setImageUrl("updated.jpg");
        updatedAnime.setAverageRating(9.5);
        // Set other properties as needed
        return updatedAnime;
    }
}