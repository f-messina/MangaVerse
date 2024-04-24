package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;

    void deleteReviewWithNoMedia() throws BusinessException;

    void deleteReviewWithNoAuthor() throws BusinessException;

    void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws BusinessException;
    void deleteReview (String id) throws BusinessException;
    PageDTO<ReviewDTO> findByUser(String userId, int page) throws BusinessException;
    PageDTO<ReviewDTO> findByMedia(String mediaId, MediaContentType mediaType, int page) throws BusinessException;

    //Service for mongoDB queries
    Double averageRatingUser(String userId) throws BusinessException;
    Map<String, Double> ratingMediaContentByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException;
    Map<String, Double> ratingMediaContentByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException;
    PageDTO<MediaContentDTO> suggestTopMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException;

}
