package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import java.util.Map;

public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;
    void updateReview(ReviewDTO reviewDTO) throws BusinessException;
    void deleteReview(String reviewId, String mediaId, MediaContentType mediaContentType) throws BusinessException;
    PageDTO<ReviewDTO> findByUser(String userId, int page) throws BusinessException;
    PageDTO<ReviewDTO> findByMedia(String mediaId, MediaContentType mediaType, int page) throws BusinessException;
    Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException;
    Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException;
    PageDTO<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException;

}
