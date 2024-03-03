package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;

public interface ReviewService {
    void addReview (ReviewDTO review) throws BusinessException;
    void deleteReview (String id) throws BusinessException;
    void deleteByMedia(String mediaId) throws BusinessException;
    void update(ReviewDTO review) throws BusinessException;
    List<ReviewDTO> findByUser(String userId) throws BusinessException;
    List<ReviewDTO> findByMedia(String mediaId) throws BusinessException;
}
