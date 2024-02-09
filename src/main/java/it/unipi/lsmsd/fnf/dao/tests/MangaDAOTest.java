package it.unipi.lsmsd.fnf.dao.tests;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.MangaDAOImpl;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class MangaDAOTest {

    public static void main(String[] args) {
        MangaDAOImpl mangaDAO = new MangaDAOImpl();

        // Test insert method
        Manga mangaToInsert = createSampleManga();
        ObjectId idMangaInserted = testInsert(mangaDAO, mangaToInsert);
        mangaToInsert.setId(idMangaInserted);

        // Test update method
        Manga updatedManga = createUpdatedManga(mangaToInsert);
        testUpdate(mangaDAO, updatedManga);

        // Test delete method
        testDelete(mangaDAO, updatedManga.getId());

        // Test find method
        testFind(mangaDAO, mangaToInsert.getId());

        // Test search method
        testSearch(mangaDAO);
    }

    private static ObjectId testInsert(MangaDAOImpl mangaDAO, Manga manga) {
        try {
            System.out.println("Inserting manga: " + manga.getTitle());
            ObjectId id = mangaDAO.insert(manga);
            System.out.println("Manga inserted successfully!");
            return id;
        } catch (DAOException e) {
            System.err.println("Error inserting manga: " + e.getMessage());
            return null;
        }
    }

    private static void testUpdate(MangaDAOImpl mangaDAO, Manga manga) {
        try {
            System.out.println("Updating manga: " + manga.getTitle());
            mangaDAO.update(manga);
            System.out.println("Manga updated successfully!");
        } catch (DAOException e) {
            System.err.println("Error updating manga: " + e.getMessage());
        }
    }

    private static void testDelete(MangaDAOImpl mangaDAO, ObjectId mangaId) {
        try {
            System.out.println("Deleting manga with ID: " + mangaId);
            mangaDAO.delete(mangaId);
            System.out.println("Manga deleted successfully!");
        } catch (DAOException e) {
            System.err.println("Error deleting manga: " + e.getMessage());
        }
    }

    private static void testFind(MangaDAOImpl mangaDAO, ObjectId mangaId) {
        try {
            System.out.println("Finding manga with ID: " + mangaId);
            Manga foundManga = mangaDAO.find(mangaId);
            if (foundManga != null) {
                System.out.println("Found manga: " + foundManga.getTitle());
            } else {
                System.out.println("Manga not found.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding manga: " + e.getMessage());
        }
    }

    private static void testSearch(MangaDAOImpl mangaDAO) {
        try {
            System.out.println("Searching for manga...");
            // Provide appropriate search criteria and order by parameters
            List<Map<String, Object>> filters = new ArrayList<>();
            Map<String, Integer> orderBy = singletonMap("average_rating", 1);
            int page = 1;

            PageDTO<MangaDTO> searchResult = mangaDAO.search(filters, orderBy, page);

            System.out.println("Search result:");
            if (searchResult != null) {
                System.out.println("Total Count: " + searchResult.getTotalCount());
                for (MangaDTO mangaDTO : searchResult.getEntries()) {
                    System.out.println("Manga: " + mangaDTO.getTitle() + ", Average Rating: " + mangaDTO.getAverageRating());
                }
            } else {
                System.out.println("No search result.");
            }
        } catch (DAOException e) {
            System.err.println("Error searching for manga: " + e.getMessage());
        }
    }

    private static Manga createSampleManga() {
        Manga manga = new Manga();
        manga.setTitle("Sample Manga");
        manga.setImageUrl("sample.jpg");
        manga.setAverageRating(9.0);
        manga.setGenres(List.of("Action", "Adventure", "Fantasy"));
        manga.setDemographics(List.of("Shounen"));
        manga.setSerializations(List.of("Weekly Shonen Jump"));
        manga.setStartDate("2020-01-01");
        manga.setEndDate("2022-12-31");
        manga.setStatus(Status.valueOf("ONGOING"));
        manga.setSynopsis("Sample synopsis");
        manga.setType("Manga");
        manga.setBackground("Sample background");
        manga.setTitleEnglish("Sample Manga (English)");
        manga.setTitleJapanese("サンプルマンガ");

        // Set other properties as needed
        return manga;
    }

    private static Manga createUpdatedManga(Manga manga) {
        Manga updatedManga = new Manga();
        updatedManga.setId(manga.getId());
        updatedManga.setTitle("Updated Manga");
        updatedManga.setImageUrl("updated.jpg");
        updatedManga.setAverageRating(9.5);
        // Set other properties as needed
        return updatedManga;
    }
}
