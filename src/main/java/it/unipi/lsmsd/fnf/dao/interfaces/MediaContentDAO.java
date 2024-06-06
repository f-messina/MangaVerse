package it.unipi.lsmsd.fnf.dao.interfaces;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;

import java.util.List;
import java.util.Map;

public interface MediaContentDAO<T extends MediaContent> {

    // MongoDB specific methods
    void saveMediaContent(T mediaContent) throws DAOException;
    void updateMediaContent(T mediaContent) throws DAOException;
    T readMediaContent(String id) throws DAOException;
    void deleteMediaContent(String id) throws DAOException;
    PageDTO<MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, boolean reducedInfo) throws DAOException;
    void upsertReview(ReviewDTO reviewDTO) throws DAOException;
    void refreshLatestReviews(String animeId, List<String> reviewIds) throws DAOException;
    boolean isInLatestReviews(String mediaId, String reviewId) throws DAOException;
    void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException;
    Map<String, Double> getBestCriteria(String criteria, boolean isArray, int page) throws DAOException; // MANAGER (TABLE)
    void updateNumOfLikes(String mediaId, Integer likes) throws DAOException;

    // Neo4J specific methods
    void like(String userId, String mediaContentId) throws DAOException;
    void unlike(String userId, String mediaContentId) throws DAOException;
    boolean isLiked(String userId, String mediaId) throws DAOException;
    Integer getNumOfLikes(String mediaId) throws DAOException;
    PageDTO<MediaContentDTO> getLiked(String userId, int page) throws DAOException;
    List<MediaContentDTO> getSuggestedByFollowings(String userId, Integer limit) throws DAOException;
    List<MediaContentDTO> getSuggestedByLikes(String userId, Integer limit) throws DAOException;
    Map<MediaContentDTO, Integer> getTrendMediaContentByYear(int year, Integer limit) throws DAOException; // MANAGER (TABLE OR ORDERED BAR CHART)
    List<MediaContentDTO> getMediaContentTrendByLikes(Integer limit) throws DAOException;
}
