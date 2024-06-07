package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MangaStatus;
import it.unipi.lsmsd.fnf.model.enums.MangaType;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;
import static it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO.getCollection;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MangaDAOMongoImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws Exception {
        BaseMongoDBDAO.closeConnection();
    }

    // test 1 : search for an manga by name
    // test 2 : search for an manga by filters
    @Test
    void searchTest() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();

        // test 1
        System.out.println("Search by title");
        assertDoesNotThrow(() -> {
            List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "na")), Map.of("title", 1), 1, false).getEntries();
            for (MediaContentDTO manga : mangaList) {
                System.out.println("Id: " + manga.getId() + ", Title: " + manga.getTitle());
            }
        });

        // test 2
        System.out.println("Search by filters");
        assertDoesNotThrow(() -> {
            for (int i = 1; i < 5; i++) {
                PageDTO<MediaContentDTO> mangaPage = mangaDAO.search(List.of(Map.of("$in",Map.of("genres", List.of("Fantasy", "Adventure")))), Map.of("title", 1), i, false);
                if (!mangaPage.getEntries().isEmpty()) {
                    for (MediaContentDTO manga : mangaPage.getEntries()) {
                        System.out.println("Id: " + manga.getId() + ", Title: " + manga.getTitle());
                    }
                }

            }
        });
    }

    // test 1 : save a new manga (before that, I try to find an manga with the same title and delete it)
    // test 2 : save a name with the same title of the previous one
    @Test
    void saveMediaContentTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        Manga manga = createSampleManga();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();

        if (mangaList.isEmpty()) {
            // test 1
            System.out.println("Manga to save: " + manga);
            assertDoesNotThrow(() -> mangaDAO.saveMediaContent(manga));
            System.out.println("Id manga created: " + manga.getId());
        } else {
            // test 2
            assertThrows(DAOException.class, () -> mangaDAO.saveMediaContent(manga));
            System.out.println("Manga already exists");
        }
    }

    // test 1 : update an existing manga (before that, I try to find the manga by title)
    // test 2 : update a non-existing manga
    @Test
    void updateMediaContentTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();
        // test 1
        if (!mangaList.isEmpty()) {
            MediaContentDTO mangaToUpdate = mangaList.getFirst();
            System.out.println("Manga to update: " + mangaToUpdate);
            Manga manga = new Manga();
            manga.setId(mangaToUpdate.getId());
            manga.setTitle("Updated Manga");
            assertDoesNotThrow(() -> mangaDAO.updateMediaContent(manga));
            System.out.println("Manga updated");
        }
        // test 2
        Manga manga = createSampleManga();
        manga.setId("6635632b4276578429f29384");
        manga.setTitle("Non-existing Manga");
        assertThrows(DAOException.class, () -> mangaDAO.updateMediaContent(manga));
        System.out.println("Non-existing Manga not found");
    }
    // test 1 : delete an existing manga (before that, I try to find the manga by title)
    // test 2 : delete a non-existing manga
    @Test
    void deleteMediaContentTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();
        // test 1
        if (!mangaList.isEmpty()) {
            MediaContentDTO mangaToDelete = mangaList.getFirst();
            System.out.println("Manga to delete: " + mangaToDelete);
            assertDoesNotThrow(() -> mangaDAO.deleteMediaContent(mangaToDelete.getId()));
            System.out.println("Manga deleted");
        }
        // test 2
        assertThrows(DAOException.class, () -> mangaDAO.deleteMediaContent("6635632b4276578429f29384"));
        System.out.println("Non-existent manga not deleted");
    }

    // test 1 : read an existing manga (before that, I try to find the manga by title)
    // test 2 : read a non-existing manga
    @Test
    void readMediaContentTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();

        // test 1
        if (!mangaList.isEmpty()) {
            MediaContentDTO mangaToRead = mangaList.getFirst();
            assertDoesNotThrow(() -> {
                Manga manga = mangaDAO.readMediaContent(mangaToRead.getId());
                System.out.println("Manga read: " + manga.toString());
            });
        }

        // test 2
        assertThrows(DAOException.class, () -> mangaDAO.readMediaContent("6635632b4276578429f29384"));
        System.out.println("Non-existent manga not found");
    }

    // test 1 : upsert a new review (before that, I try to find an manga by title)
    // test 2 : upsert a review for an existing manga
    @Test
    void upsertReviewTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();

        if (!mangaList.isEmpty()) {

            String mangaId = mangaList.getFirst().getId();
            String mangaTitle = mangaList.getFirst().getTitle();
            ReviewDTO review = createSampleReview();
            review.setMediaContent(new MangaDTO(mangaId, mangaTitle));

            if (!mangaDAO.isInLatestReviews(mangaId, review.getId())) {
                // test 1
                assertDoesNotThrow(() -> {
                    mangaDAO.upsertReview(review);
                    System.out.println("Review added");
                });
            } else {
                // test 2
                review.setRating(9);
                assertDoesNotThrow(() -> {
                    mangaDAO.upsertReview(review);
                    System.out.println("Review updated");
                });
            }
        }
    }

    // test 1 : refresh latest reviews with the last n reviews
    @Test
    void refreshLatestReviewsTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();
        List<String> review_ids = new ArrayList<>();
        review_ids = List.of("6635632b4276578429f29888", "6635632b4276578429f29889", "6635632b4276578429f29890");

        if (!mangaList.isEmpty()) {
            String mangaId = mangaList.getFirst().getId();

            // test 1
            List<String> finalReview_ids = review_ids;
            assertDoesNotThrow(() -> {
                mangaDAO.refreshLatestReviews(mangaId, finalReview_ids);
                System.out.println("Latest reviews refreshed");
            });
        }
    }

    // ATTENTION: USE IT TO RESET THE LATEST REVIEWS
    // test 1 : refresh all latest reviews
    @Test
    void refreshAllLatestReviewsTest(){
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        assertDoesNotThrow(() -> {
            mangaDAO.refreshAllLatestReviews();
            System.out.println("All latest reviews refreshed");
        });
    }

    @Test
    void isInLatestReviewsTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();

        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();
        if (!mangaList.isEmpty()) {
            String mangaId = mangaList.getFirst().getId();
            Manga manga = mangaDAO.readMediaContent(mangaId);
            if (!manga.getReviews().isEmpty()) {
                assertDoesNotThrow(() -> {
                    boolean isInLatestReviews = mangaDAO.isInLatestReviews(mangaId, manga.getReviews().getFirst().getId());
                    System.out.println("Review is in latest reviews: " + isInLatestReviews);
                });
            } else {
                assertDoesNotThrow(() -> {
                    boolean isInLatestReviews = mangaDAO.isInLatestReviews(mangaId, "6635632b4276578429f29343");
                    System.out.println("Review is in latest reviews: " + isInLatestReviews);
                });
            }
        }
    }

    @Test
    public void getBestCriteriaTest() {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        assertDoesNotThrow(() -> {
            Map<String, Double> bestManga = mangaDAO.getBestCriteria("genres", true, 1);
            for (Map.Entry<String, Double> entry : bestManga.entrySet()) {
                System.out.println("Genre: " + entry.getKey() + ", Average rating: " + entry.getValue());
            }
        });
    }

    @Test
    public void updateNumOfLikesTest() throws DAOException {
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        List<MediaContentDTO> mangaList = mangaDAO.search(List.of(Map.of("title", "Sample Manga")), Map.of("title", 1), 1, false).getEntries();
        if (!mangaList.isEmpty()) {
            String mangaId = mangaList.getFirst().getId();
            assertDoesNotThrow(() -> {
                mangaDAO.updateNumOfLikes(mangaId, 1);
                System.out.println("Number of reviews updated");
            });
        }
    }

    //Add a list of reviews id connected to the manga: DONE
    @Test
    public void addReviewsIdInMangaTest() throws DAOException {
        //Get list of anime ids
        MangaDAOMongoImpl mangaDAO = new MangaDAOMongoImpl();
        //Anime collection
        MongoCollection<Document> mangaCollection = getCollection("manga");
        //Reviews collection
        MongoCollection<Document> reviewsCollection = getCollection("reviews");

        try {
            mangaCollection.find().projection(include("_id")).forEach((Document manga) ->
            {
                List <String> reviewIds = new ArrayList<>();

                ObjectId id = manga.getObjectId("_id");
                reviewsCollection.find(eq("manga.id", id)).projection(include("_id")).forEach((Document review) -> reviewIds.add(review.getObjectId("_id").toHexString()));

                mangaCollection.updateOne(eq("_id", id), set("review_ids", reviewIds));

            });


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

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

    private ReviewDTO createSampleReview() {
        ReviewDTO review = new ReviewDTO();
        review.setId("6635632b4276578429f29888");
        review.setUser(new UserSummaryDTO("6635632b4276578429f29385", "exampleUser", "exampleUser.jpg"));
        review.setRating(7);
        review.setComment("Great manga");
        review.setDate(LocalDateTime.now());
        return review;
    }

}
