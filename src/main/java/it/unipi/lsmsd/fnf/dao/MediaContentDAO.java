package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;

import java.util.List;
import java.util.Map;

public interface MediaContentDAO<T extends MediaContent> {
    // MongoDB specific methods
    void createMediaContent(T mediaContent) throws DAOException;
    void updateMediaContent(T mediaContent) throws DAOException;
    T readMediaContent(String id) throws DAOException;
    void deleteMediaContent(String id) throws DAOException;
    PageDTO<? extends MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException;
    void updateLatestReview (ReviewDTO reviewDTO) throws DAOException;

    // Neo4J specific methods
    <E extends MediaContentDTO> void createNode(E mediaContentDTO) throws DAOException;
    void like(String userId, String mediaContentId) throws DAOException;
    void unlike(String userId, String mediaContentId) throws DAOException;
    boolean isLiked(String userId, String mediaId) throws DAOException;
    List<? extends MediaContentDTO> getLiked(String userId) throws DAOException;
    List<? extends MediaContentDTO> getSuggested(String userId) throws DAOException;
    List<? extends MediaContentDTO> getTrendMediaContentByYear(int year) throws DAOException;
    List<String> getMediaContentGenresTrendByYear(int year) throws DAOException;
    List<? extends MediaContentDTO> getMediaContentTrendByGenre() throws DAOException;
    List<? extends MediaContentDTO> getMediaContentTrendByLikes() throws DAOException;
    List<String> getMediaContentGenresTrend() throws DAOException;
}
