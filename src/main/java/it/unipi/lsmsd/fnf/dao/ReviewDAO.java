package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import java.util.List;
import java.util.Map;

public interface ReviewDAO {
    //MongoDB queries
    String insert(ReviewDTO review) throws DAOException;
    void delete(String id) throws DAOException;
    void deleteByMedia(String mediaId) throws DAOException;
    void update(ReviewDTO review) throws DAOException;
    List<ReviewDTO> findByUser(String userId) throws DAOException;
    List<ReviewDTO> findByMedia(String mediaId) throws DAOException;
    int averageRatingUser(String userId) throws DAOException;
    //int ratingAnimeYear(int year, String animeId) throws DAOException;
    //int ratingAnimeMonth(int month, int year, String animeId) throws DAOException;
    //int ratingMangaYear(int year, String mangaId) throws DAOException;
    //int ratingMangaMonth(int month, int year, String mangaId) throws DAOException;
    //int averageRatingByAge(int yearOfBirth) throws DAOException;
    //int averageRatingByLocation(String location) throws DAOException;

    Map<String, Double> ratingMediaContentByPeriod(MediaContentType type, String mediaContentId, String period) throws  DAOException;

    //For users: suggestions based on age and location. For example: show the 25 anime or manga with highest average rating in Italy.
    Map<PageDTO<? extends MediaContentDTO>, Double> suggestTopMediaContent(MediaContentType mediaContentType, String criteria, String type) throws DAOException//Use documentToAnimeDTO from AnimeDAOImpl
    ;

    Map<String, Double> averageRatingByCriteria(String type) throws DAOException;
}

