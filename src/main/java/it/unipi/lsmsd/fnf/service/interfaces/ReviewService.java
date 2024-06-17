package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import java.util.List;
import java.util.Map;

/**
 * Interface for the Review service.
 * Provides methods to interact with the Review entity.
 * The methods access to Review entity in the database and provide
 * operations to maintain consistency between collections,
 * operations to get statistics and operations to get media content suggestions.
 * The methods, in general, execute a single DAO method. When needed to maintain
 * eventual consistency between collections, the methods execute multiple DAO methods,
 * executing the consistency operations in a synchronous and ordered way.
 * @see ReviewDTO
 */
public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;
    void updateReview(ReviewDTO reviewDTO) throws BusinessException;
    void deleteReview(String reviewId, String mediaId, MediaContentType mediaContentType, List<String> reviewIds, boolean contains) throws BusinessException;
    PageDTO<ReviewDTO> getReviewsByIdsList(List<String> reviewIds, Integer page, String docExcluded) throws BusinessException;
    ReviewDTO isReviewedByUser(String userId, List<String> reviewIds) throws BusinessException;
    Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException;
    Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException;
    List<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException;
}
