package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReviewDAO {
    ObjectId insert(ReviewDTO review) throws DAOException;
    void delete(ObjectId id) throws DAOException;
    void deleteByMedia(ObjectId mediaId) throws DAOException;
    void update(ReviewDTO review) throws DAOException;
    List<ReviewDTO> findByUser(ObjectId userId) throws DAOException;
    List<ReviewDTO> findByMedia(ObjectId mediaId) throws DAOException;


    //MongoDB queries
    //Find the average rating a user has given to media contents given the userId
    int averageRatingUser(ObjectId userId) throws DAOException;

    //Trend of the rating of a specific anime grouped by year
    int ratingAnimeYear(int year, ObjectId animeId) throws DAOException;

    //Trend of the rating of a specific anime grouped by month
    int ratingAnimeMonth(int month, int year, ObjectId animeId) throws DAOException;

    int ratingMangaYear(int year, ObjectId mangaId) throws DAOException;

    //Trend of the rating of a specific manga grouped by month
    int ratingMangaMonth(int month, int year, ObjectId mangaId) throws DAOException;

    //Average rating given by users of a certain age: select a year of birth and see what is the average rating in general
    int averageRatingByAge(int yearOfBirth) throws DAOException;

    //Average rating given by users of a certain location: select a location and see what is the average rating in general
    int averageRatingByLocation(String location) throws DAOException;

}

