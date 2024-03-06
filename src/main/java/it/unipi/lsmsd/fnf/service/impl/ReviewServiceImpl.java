package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

public class ReviewServiceImpl implements ReviewService {

    private static final ReviewDAO reviewDAO;

    static {
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
    }

    @Override
    public void addReview(ReviewDTO review) throws BusinessException {
        if (StringUtils.isEmpty(review.getComment()) && review.getRating() == null) {
            throw new BusinessException("The review must have a comment or a rating");
        }
        try{
            reviewDAO.insert(review);
        } catch (Exception e){
            throw new BusinessException("Error adding review",e);
        }
    }

    @Override
    public void deleteReview(String reviewId) throws BusinessException {
        try {
            reviewDAO.delete(reviewId);
        } catch (Exception e){
            throw new BusinessException("Error deleting review",e);
        }
    }

    @Override
    public void deleteReviewByMedia(String mediaId) throws BusinessException {
        try {
            reviewDAO.deleteByMedia(mediaId);
        } catch (Exception e){
            throw new BusinessException("Error deleting by media",e);
        }
    }

    @Override
    public void updateReview(ReviewDTO review) throws BusinessException {
        if (StringUtils.isEmpty(review.getComment()) && review.getRating() == null) {
            throw new BusinessException("The review must have a comment or a rating");
        }
        try {
            reviewDAO.update(review);
        } catch (Exception e){
            throw new BusinessException("Error updating the review",e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(String userId) throws BusinessException {
        try {
         return reviewDAO.findByUser(userId);
        } catch (Exception e){
            throw new BusinessException("Error finding media by user",e);
        }
    }

    @Override
    public List<ReviewDTO> findByMedia(String mediaId) throws BusinessException {
        try{
            return reviewDAO.findByMedia(mediaId);
        } catch (Exception e){
            throw new BusinessException("Error finding review by media",e);
        }
    }
}
