package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

/**
 * The ReviewServiceImpl class provides implementation for the ReviewService interface.
 * It handles CRUD operations and other functionalities related to reviews.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final ReviewDAO reviewDAO;

    static {
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
    }

    /**
     * Adds a new review to the data repository.
     * @param review The review to be added.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Deletes a review from the data repository.
     * @param reviewId The ID of the review to be deleted.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteReview(String reviewId) throws BusinessException {
        try {
            reviewDAO.delete(reviewId);
        } catch (Exception e){
            throw new BusinessException("Error deleting review",e);
        }
    }

    /**
     * Deletes all reviews associated with a particular media content.
     * @param mediaId The ID of the media content whose reviews are to be deleted.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteReviewByMedia(String mediaId) throws BusinessException {
        try {
            reviewDAO.deleteByMedia(mediaId);
        } catch (Exception e){
            throw new BusinessException("Error deleting by media",e);
        }
    }

    /**
     * Updates an existing review in the data repository.
     * @param review The updated review.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Finds all reviews submitted by a particular user.
     * @param userId The ID of the user.
     * @return A list of reviews submitted by the user.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public List<ReviewDTO> findByUser(String userId) throws BusinessException {
        try {
         return reviewDAO.findByUser(userId);
        } catch (Exception e){
            throw new BusinessException("Error finding media by user",e);
        }
    }

    /**
     * Finds all reviews associated with a particular media content.
     * @param mediaId The ID of the media content.
     * @return A list of reviews associated with the media content.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public List<ReviewDTO> findByMedia(String mediaId) throws BusinessException {
        try{
            return reviewDAO.findByMedia(mediaId);
        } catch (Exception e){
            throw new BusinessException("Error finding review by media",e);
        }
    }
}
