package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO.getCollection;
import static org.junit.jupiter.api.Assertions.*;

import it.unipi.lsmsd.fnf.dto.PageDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ReviewDAOMongoImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws DAOException {
        BaseMongoDBDAO.closeConnection();
    }


    // test 1: save anime review
    // test 2: save manga review
    @Test
    void saveReviewTest() throws DAOException {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        ReviewDTO reviewAnimeDTO = createSampleAnimeReview();
        assertDoesNotThrow(() -> {
            reviewDAO.saveReview(reviewAnimeDTO);
            System.out.println("Review created: " + reviewAnimeDTO.getId());
        });

        // test 2
        ReviewDTO reviewMangaDTO = createSampleMangaReview();
        assertDoesNotThrow(() -> {
            reviewDAO.saveReview(reviewMangaDTO);
            System.out.println("Review created: " + reviewMangaDTO.getId());
        });
    }

    // test 1: update anime review
    // test 2: update manga review
    // test 3: update Non-existent review
    @Test
    void updateReviewTest() throws DAOException {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        List<String> reviewIds = new ArrayList<>();
        reviewIds = List.of("66360c83bbca010b06d85622", "66360c83bbca010b06d85623", "66360c83bbca010b06d85624");
        List<ReviewDTO> reviewList = reviewDAO.getReviewByMedia(reviewIds, MediaContentType.ANIME, 1).getEntries();
        if (!reviewList.isEmpty()) {
            ReviewDTO review = reviewList.getFirst();
            assertDoesNotThrow(() -> reviewDAO.updateReview(review.getId(), "This is a new comment", 4));
            System.out.println("Anime review updated: " + review.getId());
        }

        // test 2
        reviewList = reviewDAO.getReviewByMedia(reviewIds, MediaContentType.MANGA, 1).getEntries();
        if (!reviewList.isEmpty()) {
            ReviewDTO review = reviewList.getFirst();
            assertDoesNotThrow(() -> reviewDAO.updateReview(review.getId(), "This is a new comment", 4));
            System.out.println("Manga review updated: " + review.getId());
        }

        // test 3
        assertThrows(DAOException.class, () -> reviewDAO.updateReview("N6635fe844276578429fe4422", "This is a new comment", 4));
        System.out.println("Non-existent review update failed");
    }

    // test 1: update anime redundancy
    // test 2: update manga redundancy
    @Test
    void updateMediaRedundancyTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        String animeId = "663606354276578429fe47e9";
        String animeTitle = "Updated Anime";
        AnimeDTO anime = new AnimeDTO(animeId, animeTitle, null);
        // it throws an exception if there are no reviews for the media or the media info are not updated
        assertDoesNotThrow(() -> reviewDAO.updateMediaRedundancy(anime));
        System.out.println("Anime redundancy updated: " + animeId);

        // test 2
        String mediaId = "6635fe844276578429fe445d";
                String mangaTitle = "Updated Manga";
        MangaDTO manga = new MangaDTO(mediaId, mangaTitle, null);
        // it throws an exception if there are no reviews for the media or the media info are not updated
        assertDoesNotThrow(() -> reviewDAO.updateMediaRedundancy(manga));
        System.out.println("Manga redundancy updated: " + mediaId);
    }

    // test 1: update user redundancy
    // test 2: update Non-existent user redundancy
    @Test
    void updateUserRedundancyTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        String userId = "66360c83bbca010b06d85602";
        String username = "Updated User";
        String pictureUrl = "https://imgbox.com/7MaTkBQR";
        UserSummaryDTO user = new UserSummaryDTO(userId, username, pictureUrl);
        // it throws an exception if there are no reviews for the user or the user info are not updated
        assertDoesNotThrow(() -> reviewDAO.updateUserRedundancy(user));
        System.out.println("User redundancy updated: " + userId);

        // test 2
        assertThrows(DAOException.class, () -> reviewDAO.updateUserRedundancy(new UserSummaryDTO("66360c83bbca010b06d85666", "Updated User", "https://imgbox.com/7MaTkBQR")));
        System.out.println("Non-existent user redundancy update failed");
    }

   //Add a list of reviews id connected to the users: DONE
    @Test
    public void addReviewsIdToUsersTest() throws DAOException {
        //Get list of users ids
        UserDAOMongoImpl userDAO = new UserDAOMongoImpl();
        //Anime collection
        MongoCollection<Document> userCollection = getCollection("users");
        //Reviews collection
        MongoCollection<Document> reviewsCollection = getCollection("reviews");

        try {
            userCollection.find().projection(include("_id")).forEach((Document user) ->
            {
                List <String> reviewIds = new ArrayList<>();

                ObjectId id = user.getObjectId("_id");
                reviewsCollection.find(eq("user.id", id)).projection(include("_id")).forEach((Document review) -> reviewIds.add(review.getObjectId("_id").toHexString()));

                userCollection.updateOne(eq("_id", id), set("review_ids", reviewIds));

            });
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // test 1: update average rating
    @Test
    void updateAverageRatingMediaTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        assertDoesNotThrow(reviewDAO::updateAverageRatingMedia);
        System.out.println("Average rating updated");
    }

    // test 1: delete review
    // test 2: delete non-existent review
    @Test
    void deleteReviewTest() throws DAOException {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        List<String> reviewIds = new ArrayList<>();
        reviewIds = List.of("66360c83bbca010b06d85622", "66360c83bbca010b06d85623", "66360c83bbca010b06d85624");

        // test 1
        reviewDAO.getReviewByUser(reviewIds, 1).getEntries().forEach(review -> {
            assertDoesNotThrow(() -> reviewDAO.deleteReview(review.getId()));
            System.out.println("Review deleted: " + review.getId());
        });

        // test 2
        assertThrows(DAOException.class, () -> reviewDAO.deleteReview("66360c83bbca010b06d85622"));
        System.out.println("Non-existent review delete failed");

    }

    @Test
    void deleteReviewsWithNoMediaTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        try {
            reviewDAO.deleteReviewsWithNoMedia();
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void deleteReviewsWithNoAuthorTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        try {
            reviewDAO.deleteReviewsWithNoAuthor();
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    // test 1 : refresh latest reviews on user deletion
    @Test
    void refreshLatestReviewsOnUserDeletionTest() throws DAOException {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        assertDoesNotThrow(() -> {
            reviewDAO.refreshLatestReviewsOnUserDeletion("6647f6fd47d52d299e9ebf23");
            System.out.println("Latest reviews refreshed on user deletion");
        });
    }

    // test 1: get review by user
    // test 2: get non-existent review by user
    @Test
    void getReviewByUserTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        List<String> reviewIds = new ArrayList<>();
        reviewIds = List.of("66360c83bbca010b06d85622", "66360c83bbca010b06d85623", "66360c83bbca010b06d85624");

        List<String> finalReviewIds = reviewIds;
        assertDoesNotThrow(() -> {
            PageDTO<ReviewDTO> reviews =  reviewDAO.getReviewByUser(finalReviewIds, 1);
            for (ReviewDTO review : reviews.getEntries()) {
                System.out.println(review);
            }
        });

        // test 2
        List<String> finalReviewIds1 = reviewIds;
        assertThrows(DAOException.class, () -> reviewDAO.getReviewByUser(finalReviewIds1, 1));
        System.out.println("Non-existent review retrieval failed");
    }

    // test 1: get anime review
    // test 2: get manga review
    // test 3: get non-existent review
    @Test
    void getReviewByMediaTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        List<String> reviewIds = new ArrayList<>();
        reviewIds = List.of("66360c83bbca010b06d85622", "66360c83bbca010b06d85623", "66360c83bbca010b06d85624");

        List<String> finalReviewIds = reviewIds;
        assertDoesNotThrow(() -> {
            PageDTO<ReviewDTO> reviews = reviewDAO.getReviewByMedia(finalReviewIds, MediaContentType.ANIME, 1);
            for (ReviewDTO review : reviews.getEntries()) {
                System.out.println(review);
            }
        });

        // test 2
        List<String> finalReviewIds1 = reviewIds;
        assertDoesNotThrow(() -> {
            PageDTO<ReviewDTO> reviews = reviewDAO.getReviewByMedia(finalReviewIds1, MediaContentType.MANGA, 1);
            for (ReviewDTO review : reviews.getEntries()) {
                System.out.println(review);
            }
        });

        // test 3
        List<String> finalReviewIds2 = reviewIds;
        assertThrows(DAOException.class, () -> reviewDAO.getReviewByMedia(finalReviewIds2, MediaContentType.MANGA, 1));
        System.out.println("Non-existent review retrieval failed");
    }

    // test 1: get anime rating by year
    // test 2: get manga rating by year
    // test 3: get non-existent media rating by year
    @Test
    public void getMediaContentRatingByYearTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        assertDoesNotThrow(() -> {
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByYear(MediaContentType.ANIME, "65789bb52f5d29465d0abcfb", 2010, 2020);
            System.out.println(averageRating.toString());
        });

        // test 2
        assertDoesNotThrow(() -> {
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByYear(MediaContentType.MANGA, "657ac622b34f5514b91ee5f0", 2010, 2020);
            System.out.println(averageRating.toString());
        });

        // test 3
        assertThrows(DAOException.class, () -> reviewDAO.getMediaContentRatingByYear(MediaContentType.ANIME, "657ac622b34f5514b91ee511", 2010, 2020));
        System.out.println("Non-existent media rating retrieval failed");
    }

    // test 1: get anime rating by month
    // test 2: get manga rating by month
    // test 3: get non-existent media rating by month
    @Test
    public void getMediaContentRatingByMonthTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        assertDoesNotThrow(() -> {
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByMonth(MediaContentType.ANIME, "65789bb52f5d29465d0abcfb", 2020);
            System.out.println(averageRating.toString());
        });
        // test 2
        assertDoesNotThrow(() -> {
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByMonth(MediaContentType.MANGA, "657ac61bb34f5514b91ea226", 2010);
            System.out.println(averageRating.toString());
        });

        // test 3
        assertThrows(DAOException.class, () -> reviewDAO.getMediaContentRatingByMonth(MediaContentType.ANIME, "6635fe844276578429fe4422", 2022));
        System.out.println("Non-existent media rating retrieval failed");
    }

    // test 1: suggest anime by location
    // test 2: suggest manga by location
    // test 3: suggest anime by birthday
    // test 4: suggest manga by birthday
    // test 5: suggest media by non-existent location
    // test 6: suggest media by non-existent birthday
    @Test
    public void suggestMediaContentTest() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

        // test 1
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.ANIME, "location", "Brazil");
            System.out.println(pageDTO);
        });

        // test 2
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.MANGA, "location", "Hungary");
            System.out.println(pageDTO);
        });

        // test 3
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.ANIME, "birthday", "1990");
            System.out.println(pageDTO);
        });

        // test 4
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.MANGA, "birthday", "1990");
            System.out.println(pageDTO);
        });

        // test 5
        assertThrows(DAOException.class, () -> reviewDAO.suggestMediaContent(MediaContentType.ANIME, "location", "Non-existent"));
        System.out.println("Non-existent location media suggestion failed");

        // test 6
        assertThrows(DAOException.class, () -> reviewDAO.suggestMediaContent(MediaContentType.MANGA, "birthday", "1300"));
        System.out.println("Non-existent birthday media suggestion failed");
    }

    private ReviewDTO createSampleAnimeReview(){
        ReviewDTO review = new ReviewDTO();
        review.setUser(new UserSummaryDTO("66360c83bbca010b06d85602", "exampleUser", "images/user%20icon%20-%20Kopya%20-%20Kopya.png"));
        review.setMediaContent(new AnimeDTO("663606354276578429fe47e9", "Sample Anime", "Sample Cover URL"));
        review.setRating(5);
        review.setComment("This is a test review");
        return review;
    }

    private ReviewDTO createSampleMangaReview() {
        ReviewDTO review = new ReviewDTO();
        review.setUser(new UserSummaryDTO("66360c83bbca010b06d85602", "exampleUser", "images/user%20icon%20-%20Kopya%20-%20Kopya.png"));
        review.setMediaContent(new MangaDTO("6635fe844276578429fe445d", "Sample Manga", "Sample Cover URL"));
        review.setRating(5);
        review.setComment("This is a test review");
        return review;
    }

    @Test
    public void removeNullComments() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        try {
            Bson filter = and(exists("comment", true), eq("comment", null));
            Bson update = unset("comment");
            getCollection("reviews").updateMany(filter, update);
            System.out.println("Null comments removed");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
