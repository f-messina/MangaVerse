package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;
    void deleteReview (String id) throws BusinessException;
    void deleteByMedia(String mediaId) throws BusinessException;
    void update(ReviewDTO review) throws BusinessException;
    List<ReviewDTO> findByUser(String userId) throws BusinessException;
    List<ReviewDTO> findByMedia(String mediaId) throws BusinessException;

    //Service for mongoDB queries
    int averageRatingUser(String userId) throws BusinessException;

    Map<String, Double> ratingMediaContentByPeriod(MediaContentType type, String mediaContentId, String period) throws BusinessException;

    Map<PageDTO<? extends MediaContentDTO>, Double> suggestTopMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException;

    Map<String, Double> averageRatingByCriteria(String type) throws BusinessException;
}
