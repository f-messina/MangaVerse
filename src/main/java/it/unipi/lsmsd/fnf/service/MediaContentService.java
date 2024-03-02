package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
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
    MediaContent getMediaContentById(String id, MediaContentType type) throws BusinessException;
    PageDTO<? extends MediaContentDTO> searchByFilter(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException;
    PageDTO<? extends MediaContentDTO> searchByTitle(String title, int page, MediaContentType type) throws BusinessException;
    void addLike(String userId, String animeId, MediaContentType type) throws BusinessException;
    void removeLike(String userId, String animeId, MediaContentType type) throws BusinessException;
    List<AnimeDTO> getLikedAnime(String userId) throws BusinessException;
    List<MangaDTO> getLikedManga(String userId) throws BusinessException;

    /*
    List<AnimeDTO> suggestAnime(String userId) throws BusinessException;

    List<MangaDTO> suggestManga(String userId) throws BusinessException;

    List<AnimeDTO> getTrendAnimeByYear(int year) throws BusinessException;

    List<MangaDTO> getTrendMangaByYear(int year) throws BusinessException;

    List<AnimeDTO> suggestAnimeByGenre(String userId) throws BusinessException;

    List<MangaDTO> suggestMangaByGenre(String userId) throws BusinessException;

    List<AnimeDTO> getAnimeTrendByGenre() throws BusinessException;

    List<MangaDTO> getMangaTrendByGenre() throws BusinessException;

    List<AnimeDTO> getAnimeTrendByLikes() throws BusinessException;

    List<MangaDTO> getMangaTrendByLikes() throws BusinessException;
    */
}
