package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

/**
 * The ReviewServiceImpl class provides implementation for the ReviewService interface.
 * It handles CRUD operations and other functionalities related to reviews.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final ReviewDAO reviewDAO;
    private static final MediaContentDAO<Manga> mangaDAO;
    private static final MediaContentDAO<Anime> animeDAO;

    static {
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = getMangaDAO(DataRepositoryEnum.MONGODB);
        animeDAO = getAnimeDAO(DataRepositoryEnum.MONGODB);
    }

    /**
     * Adds a new review to the data repository.
     * @param review The review to be added.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void addReview(ReviewDTO review) throws BusinessException {
        if (StringUtils.isEmpty(review.getComment()) && review.getRating() == null) {
            throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS, "The review must have a comment or a rating");
        }
        try{
            reviewDAO.createReview(review);
            if (review.getMediaContent() instanceof MangaDTO)
                mangaDAO.updateLatestReview(review);
            else if (review.getMediaContent() instanceof AnimeDTO)
                animeDAO.updateLatestReview(review);
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
            reviewDAO.deleteReview(reviewId);
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
            reviewDAO.deleteReview(mediaId);
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
            reviewDAO.updateReview(review);
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
    public PageDTO<ReviewDTO> findByUser(String userId, int page) throws BusinessException {
        try {
         return reviewDAO.getReviewByUser(userId, page);
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
    public PageDTO<ReviewDTO> findByMedia(String mediaId, MediaContentType mediaType, int page) throws BusinessException {
        try{
            return reviewDAO.getReviewByMedia(mediaId, mediaType, page);
        } catch (Exception e){
            throw new BusinessException("Error finding review by media",e);
        }
    }
}
