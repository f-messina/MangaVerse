package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import org.neo4j.driver.Record;

import java.util.List;

public interface Neo4JDAO {

    void likeMediaContent(String userId, String mediaId) throws DAOException;


    void followUser(String followerUserId, String followingUserId) throws DAOException;


    void unlikeMediaContent(String userId, String mediaId) throws DAOException;


    void unfollowUser(String followerUserId, String followingUserId) throws DAOException;


    //List<MediaContentDTO> getLikedMediaContents(String userId) throws DAOException;


    List<AnimeDTO> getLikedAnime(String userId) throws DAOException;

    List<MangaDTO> getLikedManga(String userId) throws DAOException;

    List<RegisteredUserDTO> getFollowing(String userId) throws DAOException;

    List<RegisteredUserDTO> getFollowers(String userId) throws DAOException;


    List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException;

    ;
    //List<Record> suggestMediaContents(String userId) throws DAOException;


    List<AnimeDTO> suggestAnime(String userId) throws DAOException;

    List<MangaDTO> suggestManga(String userId) throws DAOException;

    //List<Record> getTrendByYear(int year) throws DAOException;


    List<AnimeDTO> getTrendAnimeByYear(int year) throws DAOException;

    List<MangaDTO> getTrendMangaByYear(int year) throws DAOException;

    //List<Record> getMediaContentByGenre(String genre) throws DAOException;

    List<AnimeDTO> getAnimeByGenre(String genre) throws DAOException;

    List<MangaDTO> getMangaByGenre(String genre) throws DAOException;

    //Suggest the media content based on the most liked genres of a user
    //List<Record> suggestMediaContentByGenre(String userId) throws DAOException;

    List<AnimeDTO> suggestAnimeByGenre(String userId) throws DAOException;

    List<MangaDTO> suggestMangaByGenre(String userId) throws DAOException;

    //List<Record> getGenresTrendByYear(int year) throws DAOException;

    List<AnimeDTO> getAnimeGenresTrendByYear(int year) throws DAOException;

    List<MangaDTO> getMangaGenresTrendByYear(int year) throws DAOException;

    //Suggest media contents based on the top 3 genres that appear the most
    //List<Record> getTrendByGenre() throws DAOException;

    List<AnimeDTO> getAnimeTrendByGenre() throws DAOException;

    List<MangaDTO> getMangaTrendByGenre() throws DAOException;

    //Show the trends of the likes in general
    //List<Record> getTrendByLikes() throws DAOException;

    List<AnimeDTO> getAnimeTrendByLikes() throws DAOException;

    List<MangaDTO> getMangaTrendByLikes() throws DAOException;

    //Show the trends of the genres in general
    //List<Record> getGenresTrend() throws DAOException;

    List<AnimeDTO> getAnimeGenresTrend() throws DAOException;

    List<MangaDTO> getMangaGenresTrend() throws DAOException;
}
