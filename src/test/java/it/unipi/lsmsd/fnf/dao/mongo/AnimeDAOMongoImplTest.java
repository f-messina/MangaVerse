package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;
import static it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO.getCollection;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // test 1 : search for an anime by name
    // test 2 : search for an anime by filters
    @Test
    void searchTest() {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();

        // test 1
        System.out.println("Search by title");
        assertDoesNotThrow(() -> {
            List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Attack on Titan")), Map.of("title", 1), 1, false).getEntries();
            for (MediaContentDTO anime : animeList) {
                System.out.println("Id: " + anime.getId() + ", Title: " + anime.getTitle());
            }
        });

        // test 2
        System.out.println("Search by filters");
        assertDoesNotThrow(() -> {
            for (int i = 1; i < 5; i++) {
                PageDTO<MediaContentDTO> animePage = animeDAO.search(List.of(Pair.of("$in",Map.of("tags", List.of("school clubs", "manwha")))), Map.of("title", 1), i, false);
                if (!animePage.getEntries().isEmpty()) {
                    for (MediaContentDTO anime : animePage.getEntries()) {
                        System.out.println("Id: " + anime.getId() + ", Title: " + anime.getTitle());
                    }
                }

            }
        });
    }

    // test 1 : save a new anime (before that, I try to find an anime with the same title and delete it)
    // test 2 : save a name with the same title of the previous one
    @Test
    void saveMediaContentTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        Anime anime = createSampleAnime();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();

        if (animeList.isEmpty()) {
            // test 1
            System.out.println("Anime to save: " + anime);
            assertDoesNotThrow(() -> animeDAO.saveMediaContent(anime));
            System.out.println("Id anime created: " + anime.getId());
        } else {
            // test 2
            assertThrows(DAOException.class, () -> animeDAO.saveMediaContent(anime));
            System.out.println("Anime already exists");
        }
    }

    // test 1 : update an existing anime (before that, I try to find the anime by title)
    // test 2 : update a non-existing anime
    @Test
    void updateMediaContentTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();

        // test 1
        if (!animeList.isEmpty()) {
            MediaContentDTO animeToUpdate = animeList.getFirst();
            System.out.println("Anime to update: " + animeToUpdate);
            Anime anime = new Anime();
            anime.setId(animeToUpdate.getId());
            anime.setTitle("Updated Anime");
            assertDoesNotThrow(() -> animeDAO.updateMediaContent(anime));
            System.out.println("Anime updated");
        }

        // test 2
        Anime anime = createSampleAnime();
        anime.setId("6635632b4276578429f29384");
        anime.setTitle("Non-existing Anime");
        assertThrows(DAOException.class, () -> animeDAO.updateMediaContent(anime));
        System.out.println("Non-existing Anime not found");
    }

    // test 1 : delete an existing anime (before that, I try to find the anime by title)
    // test 2 : delete a non-existing anime
    @Test
    void deleteMediaContentTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();

        // test 1
        if (!animeList.isEmpty()) {
            MediaContentDTO animeToDelete = animeList.getFirst();
            System.out.println("Anime to delete: " + animeToDelete);
            assertDoesNotThrow(() -> animeDAO.deleteMediaContent(animeToDelete.getId()));
            System.out.println("Anime deleted");
        }

        // test 2
        assertThrows(DAOException.class, () -> animeDAO.deleteMediaContent("6635632b4276578429f29384"));
        System.out.println("Non-existent anime not deleted");
    }

    // test 1 : read an existing anime (before that, I try to find the anime by title)
    // test 2 : read a non-existing anime
    @Test
    void readMediaContentTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "\"Bungaku Shoujo\" Movie")), Map.of("title", 1), 1, false).getEntries();

        // test 1
        if (!animeList.isEmpty()) {
            MediaContentDTO animeToRead = animeList.getFirst();
            assertDoesNotThrow(() -> {
                Anime anime = animeDAO.readMediaContent(animeToRead.getId());
                System.out.println("Anime read: " + anime.toString());
            });
        }

        // test 2
        //assertThrows(DAOException.class, () -> animeDAO.readMediaContent("65789bb52f5d29465d0abd00"));
        //System.out.println("Non-existent anime not found");
    }

    // test 1 : upsert a new review (before that, I try to find an anime by title)
    // test 2 : upsert a review for an existing anime
    @Test
    void upsertReviewTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();

        if (!animeList.isEmpty()) {

            String animeId = animeList.getFirst().getId();
            String animeTitle = animeList.getFirst().getTitle();
            ReviewDTO review = createSampleReview();
            review.setMediaContent(new AnimeDTO(animeId, animeTitle));

            if (!animeDAO.isInLatestReviews(animeId, review.getId())) {
                // test 1
                assertDoesNotThrow(() -> {
                    animeDAO.upsertReview(review);
                    System.out.println("Review added");
                });
            } else {
                // test 2
                review.setRating(9);
                assertDoesNotThrow(() -> {
                    animeDAO.upsertReview(review);
                    System.out.println("Review updated");
                });
            }
        }
    }

    // test 1 : refresh latest reviews with the last n reviews
    @Test
    void refreshLatestReviewsTest() throws DAOException {
        List<String> review_ids = new ArrayList<>();
        review_ids.add("6635632b4276578429f29388");
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();
        if (!animeList.isEmpty()) {
            String animeId = animeList.getFirst().getId();

            // test 1
            assertDoesNotThrow(() -> {
                animeDAO.refreshLatestReviews(animeId, review_ids);
                System.out.println("Latest reviews refreshed");
            });
        }
    }

    // ATTENTION: USE IT TO RESET THE LATEST REVIEWS
    // test 1 : refresh all latest reviews
    @Test
    void refreshAllLatestReviewsTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();

            animeDAO.refreshAllLatestReviews();
            System.out.println("All latest reviews refreshed");

    }

    @Test
    void isInLatestReviewsTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();

        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();
        if (!animeList.isEmpty()) {
            String animeId = animeList.getFirst().getId();
            Anime anime = animeDAO.readMediaContent(animeId);
            if (!anime.getLatestReviews().isEmpty()) {
                assertDoesNotThrow(() -> {
                    boolean isInLatestReviews = animeDAO.isInLatestReviews(animeId, anime.getLatestReviews().getFirst().getId());
                    System.out.println("Review is in latest reviews: " + isInLatestReviews);
                });
            } else {
                assertDoesNotThrow(() -> {
                    boolean isInLatestReviews = animeDAO.isInLatestReviews(animeId, "6635632b4276578429f29343");
                    System.out.println("Review is in latest reviews: " + isInLatestReviews);
                });
            }
        }
    }

    @Test
    void updateUserRedundancyTest() {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        UserSummaryDTO user = new UserSummaryDTO("66360c83bbca010b06d85602", "exampleUser", "exampleUser.jpg");
        assertDoesNotThrow(() -> {
            animeDAO.updateUserRedundancy(user);
            System.out.println("User redundancy updated");
        });
    }

    @Test
    public void getBestCriteriaTest() {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        assertDoesNotThrow(() -> {
            Map<String, Double> bestAnime = animeDAO.getBestCriteria("tags", true, 2);
            for (Map.Entry<String, Double> entry : bestAnime.entrySet()) {
                System.out.println("Tag: " + entry.getKey() + ", Average rating: " + entry.getValue());
            }
        });
    }

    @Test
    public void updateNumOfLikesTest() throws DAOException {
        AnimeDAOMongoImpl animeDAO = new AnimeDAOMongoImpl();
        List<MediaContentDTO> animeList = animeDAO.search(List.of(Pair.of("title", "Sample Anime")), Map.of("title", 1), 1, false).getEntries();
        if (!animeList.isEmpty()) {
            String animeId = animeList.getFirst().getId();
            assertDoesNotThrow(() -> {
                animeDAO.updateNumOfLikes(animeId, 1);
                System.out.println("Number of likes updated");
            });
        }
    }

    @Test
    public void addReviewsIdTest() {
        MongoCollection<Document> animeCollection = getCollection("anime");
        MongoCollection<Document> reviewsCollection = getCollection("reviews");

        try {
            animeCollection.find().projection(include("_id")).forEach((Document anime) ->
            {
                List <String> reviewIds = new ArrayList<>();

                ObjectId id = anime.getObjectId("_id");
                reviewsCollection.find(eq("anime.id", id)).projection(include("_id")).forEach((Document review) -> reviewIds.add(review.getObjectId("_id").toHexString()));

                animeCollection.updateOne(eq("_id", id), set("review_ids", reviewIds));

            });




        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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

    private ReviewDTO createSampleReview() {
        ReviewDTO review = new ReviewDTO();
        review.setId("6635632b4276578429f29888");
        review.setUser(new UserSummaryDTO("6635632b4276578429f29385", "exampleUser", "exampleUser.jpg"));
        review.setRating(7);
        review.setComment("Great anime");
        review.setDate(LocalDateTime.now());
        return review;
    }
}