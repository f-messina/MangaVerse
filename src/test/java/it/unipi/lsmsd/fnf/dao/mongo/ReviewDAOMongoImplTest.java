package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.lsmsd.fnf.dto.PageDTO;
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

    @Test
    void createReview() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        ReviewDTO reviewDTO = createSampleReview();
        try {
            reviewDAO.saveReview(reviewDTO);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void updateReview() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        String reviewId = "657b300806c134f18882f2f6";
        String newComment = "This is a new comment";
        Integer newRating = 4;
        try {
            reviewDAO.updateReview(reviewId, newComment, newRating);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void updateMediaRedundancy() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        String mediaId = "65789bbc2f5d29465d0b18b7";
        String title = "Slayers Revolution";
        MediaContentDTO media = new AnimeDTO(mediaId, title, null);
        try {
            reviewDAO.updateMediaRedundancy(media);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void updateUserRedundancy() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        String userId = "6577877be68376234760596d";
        String username = "Dragon_Empress";
        String pictureUrl = "https://imgbox.com/7MaTkBQR";
        UserSummaryDTO user = new UserSummaryDTO(userId, username, pictureUrl);
        try {
            reviewDAO.updateUserRedundancy(user);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void deleteReview() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        String reviewId = "66291636521b86ea10824961";
        try {
            reviewDAO.deleteReview(reviewId);
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void deleteReviewsWithNoMedia() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        try {
            reviewDAO.deleteReviewsWithNoMedia();
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void deleteReviewsWithNoAuthor() {
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        try {
            reviewDAO.deleteReviewsWithNoAuthor();
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void getReviewByUser() {
        String userId = "6577877be68376234760596d";
        ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
        try {
            System.out.println(reviewDAO.getReviewByUser(userId, 1));
        } catch (DAOException e) {
            System.err.println(e.getMessage() + " " + e.getType());
        }
    }

    @Test
    void getReviewByMedia() {
    }

    @Test
    void averageRatingUser() {
    }

    @Test
    void ratingAnimeYear() {
    }

    @Test
    void ratingAnimeMonth() {
    }

    @Test
    void ratingMangaYear() {
    }

    @Test
    void ratingMangaMonth() {
    }

    @Test
    void averageRatingByAge() {
    }

    @Test
    void averageRatingByLocation() {
    }

    private ReviewDTO createSampleReview() {
        ReviewDTO review = new ReviewDTO();
        String userId = "65789bbc2f5d29465d0b18b7";
        String username = "giorgio";
        String pictureUrl = "profilepic";
        review.setUser(new UserSummaryDTO(userId, username, pictureUrl));
        String mediaId = "65789bbc2f5d29465d0b18b7";
        String mediaTitle = "Attack on Titan";
        String mediaPictureUrl = "mediaPic";
        review.setMediaContent(new AnimeDTO(mediaId, mediaTitle, mediaPictureUrl));
        review.setRating(5);
        review.setComment("This is a test review");
        return review;
    }

    @Test
    public void testAverageRatingUser() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
            Double averageRating = reviewDAO.averageRatingUser("6577877ce683762347606d98");
            System.out.println(averageRating);
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testRatingMediaContentByYearAnime() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByYear(MediaContentType.ANIME, "65789bbd2f5d29465d0b243e", 2010, 2020);

            System.out.println(averageRating.toString());

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    @Test
    public void testRatingMediaContentByYearManga() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByYear(MediaContentType.MANGA, "657ac622b34f5514b91ee5f0", 2010, 2020);

            System.out.println(averageRating.toString());

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    @Test
    public void testRatingAnimeByMonth() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByMonth(MediaContentType.ANIME, "65789bb72f5d29465d0ad8e8", 2022);
            System.out.println(averageRating.toString());

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    @Test
    public void testRatingMangaByMonth() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByMonth(MediaContentType.MANGA, "657ac625b34f5514b91efede", 2019);
            System.out.println(averageRating.toString());

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK but careful to what it returns
    @Test
    public void testSuggestTopAnimeLocation() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.ANIME, "Brazil", "location");

            System.out.println(pageDTO);

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }


    //OK but careful to what it returns

    @Test
    public void testSuggestTopMangaLocation() {
        try {
            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.MANGA, "Hungary", "location");

            System.out.println(pageDTO);

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    //OK but careful to what it returns
    @Test
    public void testSuggestTopAnimeBirthday() {
        try {

            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.ANIME, "1990", "birthday");

            System.out.println(pageDTO);


        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }
    //OK but careful to what it returns
    @Test
    public void testSuggestTopMangaBirthday() {
        try {

            ReviewDAOMongoImpl reviewDAO = new ReviewDAOMongoImpl();

            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.MANGA, "1990", "birthday");

            System.out.println(pageDTO);

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }
}
