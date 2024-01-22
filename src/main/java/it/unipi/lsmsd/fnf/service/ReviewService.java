package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReviewService {

    void addReview (ReviewDTO review) throws BusinessException;
    void deleteReview (String id) throws BusinessException;
    void deleteByMedia(String mediaId) throws BusinessException;
    void update(ReviewDTO review) throws BusinessException;
    List<ReviewDTO> findByUser(String userId) throws BusinessException;
    List<ReviewDTO> findByMedia(String mediaId) throws BusinessException;
    List<ReviewDTO> findByUserAndMedia(String userId, String mediaId) throws BusinessException;
}
