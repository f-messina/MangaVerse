package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;
    void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws BusinessException;
    void deleteReview (String id) throws BusinessException;
    void deleteReviewByMedia(String mediaId) throws BusinessException;
    PageDTO<ReviewDTO> findByUser(String userId, int page) throws BusinessException;
    PageDTO<ReviewDTO> findByMedia(String mediaId, MediaContentType mediaType, int page) throws BusinessException;
}
