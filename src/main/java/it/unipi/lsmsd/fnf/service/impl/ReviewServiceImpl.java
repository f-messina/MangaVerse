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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            System.out.println("Review added");

            // add the redundant data to the media content
            if (review.getMediaContent() instanceof MangaDTO){
                mangaDAO.upsertReview(review);
            }

            else if (review.getMediaContent() instanceof AnimeDTO) {
                animeDAO.upsertReview(review);
            }

        } catch (DAOException e) {
            switch (e.getType()) {
                case DATABASE_ERROR:
                    throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
                case DUPLICATED_KEY:
                    throw new BusinessException(BusinessExceptionType.DUPLICATED_KEY, e.getMessage());
                default:
                    throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
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

            // update the redundant data in the media content
            if (reviewDTO.getMediaContent() instanceof MangaDTO)
                mangaDAO.upsertReview(reviewDTO);
            else if (reviewDTO.getMediaContent() instanceof AnimeDTO)
                animeDAO.upsertReview(reviewDTO);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Deletes a review from the data repository.
     * @param reviewId The ID of the review to be deleted.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteReview(String reviewId, String mediaId, MediaContentType mediaContentType, List<String> reviewIds) throws BusinessException {
        try {
            reviewDAO.deleteReview(reviewId);

            // delete the redundant data in the media content if it is in latest reviews
            if (mediaContentType.equals(MediaContentType.MANGA) && mangaDAO.isInLatestReviews(mediaId, reviewId))
                mangaDAO.refreshLatestReviews(mediaId, reviewIds);
            else if (animeDAO.isInLatestReviews(mediaId, reviewId))
                animeDAO.refreshLatestReviews(mediaId, reviewIds);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            System.out.println(e.getMessage());
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Finds all reviews submitted by a particular user.
     * @param reviewIds The IDs of the reviews submitted by the user.
     * @return A list of reviews submitted by the user.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public PageDTO<ReviewDTO> findByUser(List<String> reviewIds, Integer page) throws BusinessException {
        try {
         return reviewDAO.getReviewByUser(reviewIds, page);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Finds all reviews associated with a particular media content.
     * @param reviewIds The IDs of the reviews associated with the media content.
     * @return A list of reviews associated with the media content.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public PageDTO<ReviewDTO> findByMedia(List<String> reviewIds, MediaContentType type, Integer page) throws BusinessException {
        try{
            return reviewDAO.getReviewByMedia(reviewIds, type, page);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException {
        try {
            if (startYear < 0 || endYear < 0 || startYear > LocalDate.now().getYear() || endYear > LocalDate.now().getYear()) {
                throw new BusinessException("Invalid year");
            }
            return reviewDAO.getMediaContentRatingByYear(type, mediaContentId, startYear, endYear);
        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException {
        try {
            if (year < 0 || year > LocalDate.now().getYear()) {
                throw new BusinessException("Invalid year");
            }
            return reviewDAO.getMediaContentRatingByMonth(type, mediaContentId, year);

        } catch (DAOException e){
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public PageDTO<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException {
        try {
            return reviewDAO.suggestMediaContent(mediaContentType, criteria, type);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
