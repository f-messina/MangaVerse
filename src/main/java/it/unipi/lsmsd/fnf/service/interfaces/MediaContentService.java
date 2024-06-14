package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Interface for the MediaContent service.
 * Provides methods to interact with the MediaContent entity.
 * The methods access to MediaContent entity in the database and provide
 * operations to maintain consistency between collections, search functionality,
 * operations to get statistics and operations to get media content suggestions.
 * The methods, in general, execute a single DAO method. When needed to maintain
 * eventual consistency between collections, the methods execute multiple DAO methods,
 * executing the consistency operations in an asynchronous way.
 * @see MediaContent
 * @see MediaContentDTO
 */
public interface MediaContentService {
    void saveMediaContent(MediaContent mediaContent) throws BusinessException;
    void updateMediaContent(MediaContent mediaContent, List<String> reviewIds) throws BusinessException;
    void deleteMediaContent(String id, List<String> reviewIds, MediaContentType type) throws BusinessException;
    MediaContent getMediaContentById(String id, MediaContentType type) throws BusinessException;
    PageDTO<MediaContentDTO> searchByFilter(List<Pair<String, Object>> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException;
    PageDTO<MediaContentDTO> searchByTitle(String title, int page, MediaContentType type) throws BusinessException;
    void addLike(String userId, String animeId, MediaContentType type) throws BusinessException;
    void removeLike(String userId, String animeId, MediaContentType type) throws BusinessException;
    boolean isLiked(String userId, String mediaId, MediaContentType type) throws BusinessException;
    PageDTO<MediaContentDTO> getLikedMediaContent(String userId, int page, MediaContentType type) throws BusinessException;
    List<MediaContentDTO> getSuggestedMediaContentByFollowings(String userId, MediaContentType type, Integer limit) throws BusinessException;
    List<MediaContentDTO> getSuggestedMediaContentByLikes(String userId, MediaContentType type, Integer limit) throws BusinessException;
    Map<MediaContentDTO, Integer> getMediaContentTrendByYear(int year, Integer limit, MediaContentType type) throws BusinessException;
    List<MediaContentDTO> getMediaContentTrendByLikes(Integer limit, MediaContentType type) throws BusinessException;
    Map<String, Double> getBestCriteria(String criteria, int page, MediaContentType type) throws BusinessException;


}
