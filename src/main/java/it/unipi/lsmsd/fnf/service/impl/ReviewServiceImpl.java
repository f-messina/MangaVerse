package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Map;

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
            reviewDAO.saveReview(review);
            if (review.getMediaContent() instanceof MangaDTO)
                mangaDAO.upsertReview(review);
            else if (review.getMediaContent() instanceof AnimeDTO)
                animeDAO.upsertReview(review);
        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DUPLICATED_KEY.equals(type))
                throw new BusinessException(BusinessExceptionType.DUPLICATED_KEY, "The user have already reviewed this media content.");
            else if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND,"The media content does not exist.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Updates an existing review in the data repository.
     * @param reviewDTO The review to be updated.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void updateReview(ReviewDTO reviewDTO) throws BusinessException {
        if (StringUtils.isEmpty(reviewDTO.getComment()) && reviewDTO.getRating() == null) {
            throw new BusinessException("The review must have a comment or a rating");
        }
        try {
            reviewDAO.updateReview(reviewDTO.getId(), reviewDTO.getComment(), reviewDTO.getRating());

            if (reviewDTO.getMediaContent() instanceof MangaDTO)
                mangaDAO.upsertReview(reviewDTO);
            else if (reviewDTO.getMediaContent() instanceof AnimeDTO)
                animeDAO.upsertReview(reviewDTO);
        } catch (DAOException e){
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, "The review is not found.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error updating the review");
        }
    }

    /**
     * Deletes a review from the data repository.
     * @param reviewId The ID of the review to be deleted.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteReview(String reviewId, String mediaId, MediaContentType mediaContentType) throws BusinessException {
        try {
            reviewDAO.deleteReview(reviewId);
            // TODO: delete in latest review
        } catch (DAOException e){
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, "The review is not found.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error deleting the review");
        }
    }
    @Override
    public void deleteReviewWithNoMedia() throws BusinessException{
        try {
            reviewDAO.deleteReviewsWithNoMedia();
        } catch (DAOException e){
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, "The review is not found.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error deleting the review");
        }
    }

    @Override
    public void deleteReviewWithNoAuthor() throws BusinessException{
        try {
            reviewDAO.deleteReviewsWithNoAuthor();
        } catch (DAOException e){
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, "The review is not found.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error deleting the review");
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
        } catch (DAOException e){
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, "Reviews not found for the user.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error finding reviews by user");
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
        } catch (DAOException e){
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.DATABASE_ERROR.equals(type))
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, "Reviews not found for the media.");
            else
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error finding reviews by media");
        }
    }


    //Service for mongoDB queries
    @Override
    public Double averageRatingUser(String userId) throws BusinessException {
        try {
            return reviewDAO.averageRatingUser(userId);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public Map<String, Double> ratingMediaContentByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException {
        try {
            if (startYear < 0 || endYear < 0 || startYear > LocalDate.now().getYear() || endYear > LocalDate.now().getYear()) {
                throw new BusinessException("Invalid year");
            }
            return reviewDAO.getMediaContentRatingByYear(type, mediaContentId, startYear, endYear);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public Map<String, Double> ratingMediaContentByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException {
        try {
            if (year < 0 || year > LocalDate.now().getYear()) {
                throw new BusinessException("Invalid year");
            }
            return reviewDAO.getMediaContentRatingByMonth(type, mediaContentId, year);
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public PageDTO<MediaContentDTO> suggestTopMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException {
        try {
            return reviewDAO.suggestMediaContent(mediaContentType, criteria, type);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    /*@Override
    public Map<String, Double> averageRatingByCriteria(String type) throws BusinessException {
        try {
            return reviewDAO.averageRatingByCriteria(type);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }*/
}
