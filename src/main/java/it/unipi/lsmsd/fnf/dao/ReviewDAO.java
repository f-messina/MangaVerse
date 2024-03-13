package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

public interface ReviewDAO {
    //MongoDB queries
    public void createReview(ReviewDTO reviewDTO) throws DAOException;
    public void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws DAOException;
    void updateMediaRedundancy(MediaContentDTO mediaContentDTO) throws DAOException;
    void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException;
    public void deleteReview(String reviewId) throws DAOException;
    public void deleteReviewsWithNoMedia() throws DAOException;
    public void deleteReviewsWithNoAuthor() throws DAOException;
    PageDTO<ReviewDTO> getReviewByUser(String userId, Integer page) throws DAOException;
    public PageDTO<ReviewDTO> getReviewByMedia(String mediaId, MediaContentType type, Integer page) throws DAOException;
    int averageRatingUser(String userId) throws DAOException;
    int ratingAnimeYear(int year, String animeId) throws DAOException;
    int ratingAnimeMonth(int month, int year, String animeId) throws DAOException;
    int ratingMangaYear(int year, String mangaId) throws DAOException;
    int ratingMangaMonth(int month, int year, String mangaId) throws DAOException;
    int averageRatingByAge(int yearOfBirth) throws DAOException;
    int averageRatingByLocation(String location) throws DAOException;
}

