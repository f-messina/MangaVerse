package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import java.util.List;

public interface Neo4JDAO {
    void likeAnime(String userId, String animeId) throws DAOException;
    void likeManga(String userId, String mangaId) throws DAOException;
    void unlikeAnime(String userId, String animeId) throws DAOException;
    void unlikeManga(String userId, String mangaId) throws DAOException;
    boolean isLiked(String userId, String mediaId, MediaContentType type) throws DAOException;
    void followUser(String followerUserId, String followingUserId) throws DAOException;
    void unfollowUser(String followerUserId, String followingUserId) throws DAOException;
    boolean isFollowing(String followerUserId, String followingUserId) throws DAOException;
    List<AnimeDTO> getLikedAnime(String userId) throws DAOException;
    List<MangaDTO> getLikedManga(String userId) throws DAOException;
    List<RegisteredUserDTO> getFollowing(String userId) throws DAOException;
    List<RegisteredUserDTO> getFollowers(String userId) throws DAOException;

    /*
    List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException;
    List<AnimeDTO> suggestAnime(String userId) throws DAOException;
    List<MangaDTO> suggestManga(String userId) throws DAOException;
    List<AnimeDTO> getTrendAnimeByYear(int year) throws DAOException;
    List<MangaDTO> getTrendMangaByYear(int year) throws DAOException;
    List<AnimeDTO> getAnimeByGenre(String genre) throws DAOException;
    List<MangaDTO> getMangaByGenre(String genre) throws DAOException;
    List<String> getAnimeGenresTrendByYear(int year) throws DAOException;
    List<String> getMangaGenresTrendByYear(int year) throws DAOException;
    List<AnimeDTO> getAnimeTrendByGenre() throws DAOException;
    List<MangaDTO> getMangaTrendByGenre() throws DAOException;
    List<AnimeDTO> getAnimeTrendByLikes() throws DAOException;
    List<MangaDTO> getMangaTrendByLikes() throws DAOException;
    List<String> getAnimeGenresTrend() throws DAOException;
    List<String> getMangaGenresTrend() throws DAOException;

     */
}
