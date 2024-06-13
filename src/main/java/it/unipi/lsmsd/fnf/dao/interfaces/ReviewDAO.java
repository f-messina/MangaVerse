package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import java.util.List;
import java.util.Map;

/**
 * Interface for the DAO of the Review entity.
 * Provides methods to interact with the MongoDB database.
 * The methods provide crud operations and operations to maintain consistency between collections,
 * operations to get statistics and operations to get media content suggestions.
 */
public interface ReviewDAO {
    void saveReview(ReviewDTO reviewDTO) throws DAOException;
    void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws DAOException;
    void updateMediaRedundancy(MediaContentDTO mediaContentDTO, List<String> review_ids) throws DAOException;
    void updateUserRedundancy(UserSummaryDTO userSummaryDTO, List<String> reviewIds) throws DAOException;
    void updateAverageRatingMedia() throws DAOException;
    void deleteReview(String reviewId) throws DAOException;
    void refreshLatestReviewsOnUserDeletion(List<String> reviewsIds) throws DAOException;
    void deleteReviewsWithNoMedia() throws DAOException;
    void deleteReviewsWithNoAuthor() throws DAOException;
    void deleteReviews(List<String> reviewsIds, String elementDeleted) throws DAOException;
    PageDTO<ReviewDTO> getReviewByIdsList(List<String> reviewIds, Integer page, String docExcluded) throws DAOException;
    ReviewDTO isReviewedByUser(String userId, List<String> reviewIds) throws DAOException;
    Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws  DAOException; // MANAGER (CHART)
    Map<String, Double> getMediaContentRatingByMonth (MediaContentType type, String mediaContentId, int year) throws DAOException; // MANAGER (CHART)
    List<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteria, String type) throws DAOException;

}