package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface MediaContentDAO<T extends MediaContent> {
    ObjectId insert(T mediaContent) throws DAOException;
    void update(T mediaContent) throws DAOException;
    T find(ObjectId id) throws DAOException;
    void delete(ObjectId id) throws DAOException;
    PageDTO<? extends MediaContentDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy, int page) throws DAOException;
    void updateLatestReview (ReviewDTO reviewDTO) throws DAOException;
    //Create a Neo4J Anime node
    void createMediaContentNode(String id, String title, String picture) throws DAOException;

    //like a media content OK
    void likeMediaContent(String userId, String mediaContentId) throws DAOException;


    // unlike a media content OK
    void unlikeMediaContent(String userId, String mediaContentId) throws DAOException;


    List<? extends MediaContentDTO> getLikedMediaContent(String userId) throws DAOException;


    List<? extends MediaContentDTO> suggestMediaContent(String userId) throws DAOException;



    List<? extends MediaContentDTO> getTrendMediaContentByYear(int year) throws DAOException;




    List<String> getMediaContentGenresTrendByYear(int year) throws DAOException;



    List<? extends MediaContentDTO> getMediaContentTrendByGenre() throws DAOException;



    List<? extends MediaContentDTO> getMediaContentTrendByLikes() throws DAOException;


    List<String> getMediaContentGenresTrend() throws DAOException;
}
