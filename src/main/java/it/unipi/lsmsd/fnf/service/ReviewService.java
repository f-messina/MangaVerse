package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
<<<<<<< HEAD
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

=======
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

>>>>>>> noemi
public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;
    void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws BusinessException;
    void deleteReview (String id) throws BusinessException;
    void deleteReviewByMedia(String mediaId) throws BusinessException;
<<<<<<< HEAD
    PageDTO<ReviewDTO> findByUser(String userId, int page) throws BusinessException;
    PageDTO<ReviewDTO> findByMedia(String mediaId, MediaContentType mediaType, int page) throws BusinessException;
=======
    void updateReview(ReviewDTO review) throws BusinessException;
    List<ReviewDTO> findByUser(String userId) throws BusinessException;
    List<ReviewDTO> findByMedia(String mediaId) throws BusinessException;

    //Service for mongoDB queries
    Double averageRatingUser(String userId) throws BusinessException;

    Map<String, Double> ratingMediaContentByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException;

    Map<String, Double> ratingMediaContentByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException;

    PageDTO<MediaContentDTO> suggestTopMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException;

    //Map<String, Double> averageRatingByCriteria(String type) throws BusinessException;
>>>>>>> noemi
}
