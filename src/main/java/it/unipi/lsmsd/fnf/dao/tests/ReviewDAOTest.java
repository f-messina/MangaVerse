package it.unipi.lsmsd.fnf.dao.tests;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.ReviewDAOImpl;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class ReviewDAOTest {

    public static void main(String[] args) {
        ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

        // Test insert method
        ReviewDTO reviewToInsert = createSampleReview();
        ObjectId id = testInsert(reviewDAO, reviewToInsert);
        reviewToInsert.setId(id);

        // Test update method
        ReviewDTO updatedReview = createUpdatedReview(reviewToInsert);
        testUpdate(reviewDAO, updatedReview);
        
        // Test delete method
        testDelete(reviewDAO, updatedReview.getId());

        // Test deleteByMedia method
        testDeleteByMedia(reviewDAO, reviewToInsert.getMediaContent().getId());

        // Test findByUser method
        testFindByUser(reviewDAO, reviewToInsert.getUser().getId());

        // Test findByMedia method
        testFindByMedia(reviewDAO, reviewToInsert.getMediaContent().getId());
    }

    private static ObjectId testInsert(ReviewDAOImpl reviewDAO, ReviewDTO review) {
        try {
            System.out.println("Inserting review...");
            ObjectId id = reviewDAO.insert(review);
            System.out.println("Review inserted successfully!");
            return id;
        } catch (DAOException e) {
            System.err.println("Error inserting review: " + e.getMessage());
            return null;
        }
    }

    private static void testUpdate(ReviewDAOImpl reviewDAO, ReviewDTO review) {
        try {
            System.out.println("Updating review...");
            reviewDAO.update(review);
            System.out.println("Review updated successfully!");
        } catch (DAOException e) {
            System.err.println("Error updating review: " + e.getMessage());
        }
    }

    private static void testDelete(ReviewDAOImpl reviewDAO, ObjectId reviewId) {
        try {
            System.out.println("Deleting review...");
            reviewDAO.delete(reviewId);
            System.out.println("Review deleted successfully!");
        } catch (DAOException e) {
            System.err.println("Error deleting review: " + e.getMessage());
        }
    }

    private static void testDeleteByMedia(ReviewDAOImpl reviewDAO, ObjectId mediaId) {
        try {
            System.out.println("Deleting reviews by media ID...");
            reviewDAO.deleteByMedia(mediaId);
            System.out.println("Reviews deleted successfully!");
        } catch (DAOException e) {
            System.err.println("Error deleting reviews by media ID: " + e.getMessage());
        }
    }

    private static void testFindByUser(ReviewDAOImpl reviewDAO, ObjectId userId) {
        try {
            System.out.println("Finding reviews by user ID...");
            List<ReviewDTO> reviews = reviewDAO.findByUser(userId);
            if (!reviews.isEmpty()) {
                System.out.println("Reviews found:");
                for (ReviewDTO review : reviews) {
                    printReviewDetails(review);
                }
            } else {
                System.out.println("No reviews found for the user.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding reviews by user ID: " + e.getMessage());
        }
    }

    private static void testFindByMedia(ReviewDAOImpl reviewDAO, ObjectId mediaId) {
        try {
            System.out.println("Finding reviews by media ID...");
            List<ReviewDTO> reviews = reviewDAO.findByMedia(mediaId);
            if (!reviews.isEmpty()) {
                System.out.println("Reviews found:");
                for (ReviewDTO review : reviews) {
                    printReviewDetails(review);
                }
            } else {
                System.out.println("No reviews found for the media.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding reviews by media ID: " + e.getMessage());
        }
    }

    private static ReviewDTO createSampleReview() {
        RegisteredUserDTO user = createSampleUser();
        AnimeDTO anime = createSampleAnime();
        ReviewDTO review = new ReviewDTO();
        review.setUser(user);
        review.setMediaContent(anime);
        review.setComment("A great anime!");
        review.setRating(5);
        return review;
    }

    private static ReviewDTO createUpdatedReview(ReviewDTO review) {
        ReviewDTO updatedReview = new ReviewDTO();
        updatedReview.setId(review.getId());
        updatedReview.setUser(review.getUser());
        updatedReview.setMediaContent(review.getMediaContent());
        updatedReview.setComment("An updated comment.");
        updatedReview.setRating(4);
        return updatedReview;
    }

    private static RegisteredUserDTO createSampleUser() {
        RegisteredUserDTO user = new RegisteredUserDTO();
        user.setId(new ObjectId());
        user.setUsername("john_doe");
        user.setProfilePicUrl("profile_pic.jpg");
        return user;
    }

    private static AnimeDTO createSampleAnime() {
        AnimeDTO anime = new AnimeDTO();
        anime.setId(new ObjectId());
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample_anime.jpg");
        return anime;
    }

    private static void printReviewDetails(ReviewDTO review) {
        System.out.println("Review ID: " + review.getId());
        if (review.getUser() != null) {
            System.out.println("User: " + review.getUser().getUsername());
        }
        if (review.getMediaContent() != null) {
            System.out.println("Media Title: " + review.getMediaContent().getTitle());
        }
        if (review.getComment() != null) {
            System.out.println("Comment: " + review.getComment());
        }
        if (review.getRating() != null) {
            System.out.println("Rating: " + review.getRating());
        }
        System.out.println("------");
    }
}
