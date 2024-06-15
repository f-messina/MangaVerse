package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

/**
 * Implementation of the ReviewService interface.
 * This class provides the methods to manage reviews in the database.
 * The methods access to Review Entities in the database,
 * provide operations to maintain consistency between reviews and media content and user data,
 * operations to get statistics about reviews and media content, and to suggest media content to users.
 * @see ReviewService
 * @see ReviewDAO
 * @see ReviewDTO
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
     * Adds a new review to the data repository and add is id as redundancy
     * in the user and media document associated in the reviews_ids list.
     * The review must have a comment or a rating.
     * The review is added to the media content it is associated with (Manga or Anime)
     * in the latest reviews list.
     *
     * @param review                The review to be added.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void addReview(ReviewDTO review) throws BusinessException {
        try{
            if (StringUtils.isEmpty(review.getComment()) && review.getRating() == null) {
                throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS, "The review must have a comment or a rating");
            }

            // save the review
            reviewDAO.saveReview(review);

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
     * The review must have a comment or a rating.
     * The review is updated in the media content it is associated with
     * (Manga or Anime) inside the latest reviews list.
     *
     * @param reviewDTO             The review to be updated.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void updateReview(ReviewDTO reviewDTO) throws BusinessException {
        if (StringUtils.isEmpty(reviewDTO.getComment()) && reviewDTO.getRating() == null) {
            throw new BusinessException("The review must have a comment or a rating");
        }
        try {
            // update the review
            reviewDAO.updateReview(reviewDTO.getId(), reviewDTO.getComment(), reviewDTO.getRating());
            reviewDTO.setDate(LocalDateTime.now());

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
     * Deletes a review from the data repository and removes
     * its id from the user and media document associated in the reviews_ids list.
     * If the review is in the latest reviews list of the media content it is associated with,
     * the review is removed from the list and the list is updated.
     *
     * @param reviewId              The ID of the review to be deleted.
     * @param mediaId               The ID of the media content the review is associated with.
     * @param mediaContentType      The type of the media content the review is associated with.
     * @param reviewIds             The IDs of the reviews associated with the media content.
     *                              Used to update the latest reviews list in the media content.
     * @param isInLatestReviews     A flag indicating whether the review is in the latest reviews.
     *                              If true, the latest reviews list in the media content is updated.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void deleteReview(String reviewId, String mediaId, MediaContentType mediaContentType, List<String> reviewIds, boolean isInLatestReviews) throws BusinessException {
        try {
            // delete the review
            reviewDAO.deleteReview(reviewId);

            // delete the redundant data in the media content if it is in latest reviews
            if (mediaContentType.equals(MediaContentType.MANGA) && isInLatestReviews)
                mangaDAO.refreshLatestReviews(mediaId, reviewIds);
            else if (isInLatestReviews)
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
     * Retrieves a page of reviews from the data repository.
     * The reviews are filtered by the IDs in the reviewIds list.
     * The reviews are sorted by date in descending order.
     *
     * @param reviewIds             The IDs of the reviews to retrieve.
     * @param page                  The page number to retrieve.
     * @param docExcluded           The name of the nested document to exclude ("user" or "media").
     *                              If null, no document is excluded.
     *                              If "user", the user document is excluded (used for displaying reviews of a user).
     *                              If "media", the media document is excluded (used for displaying reviews of a media content).
     * @return                      A PageDTO object containing the reviews.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public PageDTO<ReviewDTO> getReviewsByIdsList(List<String> reviewIds, Integer page, String docExcluded) throws BusinessException {
        try {
            // get the reviews by IDs list
            return reviewDAO.getReviewByIdsList(reviewIds, page, docExcluded);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Checks if the selected user has already reviewed the specified media content.
     *
     * @param userId                The ID of the logged user.
     * @param reviewIds             The IDs of the reviews to check against.
     * @return                      A ReviewDTO object representing the review if found, otherwise null.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public ReviewDTO isReviewedByUser(String userId, List<String> reviewIds) throws BusinessException {
        try {
            // check if the user has already reviewed the media content
            return reviewDAO.isReviewedByUser(userId, reviewIds);

        } catch (DAOException e) {
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the average rating of the specified media content by year within the given range of years.
     *
     * @param type                  The type of media content (Anime or Manga).
     * @param mediaContentId        The ID of the media content.
     * @param startYear             The start year of the range.
     * @param endYear               The end year of the range.
     * @return                      A map containing the average ratings of the media content by year.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws BusinessException {
        try {
            if (startYear < 0 || endYear < 0 || startYear > LocalDate.now().getYear() || endYear > LocalDate.now().getYear()) {
                throw new BusinessException("Invalid year");
            }

            // get the average rating of the media content by year
            return reviewDAO.getMediaContentRatingByYear(type, mediaContentId, startYear, endYear);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the average rating of the specified media content by month for the given year.
     *
     * @param type                  The type of media content (Anime or Manga).
     * @param mediaContentId        The ID of the media content.
     * @param year                  The year for which to retrieve ratings.
     * @return                      A map containing the average ratings of the media content by month.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws BusinessException {
        try {
            if (year < 0 || year > LocalDate.now().getYear()) {
                throw new BusinessException("Invalid year");
            }

            // get the average rating of the media content by month
            return reviewDAO.getMediaContentRatingByMonth(type, mediaContentId, year);

        } catch (DAOException e){
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Suggests media content based on the specified criteria.
     *
     * @param mediaContentType      The type of media content (Anime or Manga).
     * @param criteriaType          The type of criteria to use for the suggestion.
     *                              Possible values: "location", "birthday".
     * @param criteriaValue         The value of the criteria to use for the suggestion.
     * @return                      A list of MediaContentDTO objects representing the suggested media content.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteriaType, String criteriaValue) throws BusinessException {
        try {

            // suggest media content based on the criteria
            return reviewDAO.suggestMediaContent(mediaContentType, criteriaType, criteriaValue);

        } catch (DAOException e){
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
