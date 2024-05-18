package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import java.util.Map;

public interface ReviewDAO {
    void saveReview(ReviewDTO reviewDTO) throws DAOException;
    void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws DAOException;
    void updateMediaRedundancy(MediaContentDTO mediaContentDTO) throws DAOException;
    void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException;
    void updateAverageRatingMedia() throws DAOException;
    void deleteReview(String reviewId) throws DAOException;
    void refreshLatestReviewsOnUserDeletion(String userId) throws DAOException;
    void deleteReviewsWithNoMedia() throws DAOException;
    void deleteReviewsByMedia(String mediaId) throws DAOException;
    void deleteReviewsWithNoAuthor() throws DAOException;
    void deleteReviewsByAuthor(String userId) throws DAOException;
    PageDTO<ReviewDTO> getReviewByUser(String userId, Integer page) throws DAOException;
    PageDTO<ReviewDTO> getReviewByMedia(String mediaId, MediaContentType type, Integer page) throws DAOException;
    Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws  DAOException; // MANAGER
    Map<String, Double> getMediaContentRatingByMonth (MediaContentType type, String mediaContentId, int year) throws DAOException; // MANAGER
    PageDTO<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteria, String type) throws DAOException;
}

