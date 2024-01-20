package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface MediaContentService {
    void addMediaContent(MediaContent mediaContent) throws BusinessException;
    void updateMediaContent(MediaContent mediaContent) throws BusinessException;
    void removeMediaContent(String id, MediaContentType type) throws BusinessException;
    MediaContent getMediaContent(String id, MediaContentType type) throws BusinessException;
    List<? extends MediaContentDTO> searchMediaContent(Map<String, Object> filters, Map<String, Integer> orderBy, MediaContentType type) throws BusinessException;
}
