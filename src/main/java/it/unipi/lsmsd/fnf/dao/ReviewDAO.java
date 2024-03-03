package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;

import java.util.List;

public interface ReviewDAO {
    //MongoDB queries
    String insert(ReviewDTO review) throws DAOException;
    void delete(String id) throws DAOException;
    void deleteByMedia(String mediaId) throws DAOException;
    void update(ReviewDTO review) throws DAOException;
    List<ReviewDTO> findByUser(String userId) throws DAOException;
    List<ReviewDTO> findByMedia(String mediaId) throws DAOException;
    List<ReviewDTO> findByUserAndMedia(String userId, String mediaId) throws DAOException;
    int averageRatingUser(String userId) throws DAOException;
    int ratingAnimeYear(int year, String animeId) throws DAOException;
    int ratingAnimeMonth(int month, int year, String animeId) throws DAOException;
    int ratingMangaYear(int year, String mangaId) throws DAOException;
    int ratingMangaMonth(int month, int year, String mangaId) throws DAOException;
    int averageRatingByAge(int yearOfBirth) throws DAOException;
    int averageRatingByLocation(String location) throws DAOException;
}

