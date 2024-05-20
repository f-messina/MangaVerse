package it.unipi.lsmsd.fnf.service.interfaces;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface MediaContentService {
    void saveMediaContent(MediaContent mediaContent) throws BusinessException;
    void updateMediaContent(MediaContent mediaContent) throws BusinessException;
    void deleteMediaContent(String id, MediaContentType type) throws BusinessException;
    MediaContent getMediaContentById(String id, MediaContentType type) throws BusinessException;
    PageDTO<MediaContentDTO> searchByFilter(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException;
    PageDTO<MediaContentDTO> searchByTitle(String title, int page, MediaContentType type) throws BusinessException;
    void addLike(String userId, String animeId, MediaContentType type) throws BusinessException;
    void removeLike(String userId, String animeId, MediaContentType type) throws BusinessException;
    boolean isLiked(String userId, String mediaId, MediaContentType type) throws BusinessException;
    List<? extends MediaContentDTO> getLikedMediaContent(String userId, MediaContentType type) throws BusinessException;
    List<? extends MediaContentDTO> getSuggestedMediaContent(String userId, MediaContentType type, Integer limit) throws BusinessException;
    Map<? extends MediaContentDTO, Integer> getTrendMediaContentByYear(int year, MediaContentType type) throws BusinessException;
    List<? extends MediaContentDTO> getMediaContentTrendByLikes(MediaContentType type) throws BusinessException;
    Map<String, Double> getBestCriteria(String criteria, int page, MediaContentType type) throws BusinessException;


}
