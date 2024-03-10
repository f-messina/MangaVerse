package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDAOImplTest {

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
    }

    @Test
    void updateReview() {
    }

    @Test
    void updateMediaRedundancy() {
    }

    @Test
    void updateUserRedundancy() {
    }

    @Test
    void deleteReview() {
    }

    @Test
    void deleteReviewsWithNoMedia() {
    }

    @Test
    void deleteReviewsWithNoAuthor() {
    }

    @Test
    void getReviewByUser() {
        String userId = "6577877be68376234760596d";
        ReviewDAOImpl reviewDAO = new ReviewDAOImpl();
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
}