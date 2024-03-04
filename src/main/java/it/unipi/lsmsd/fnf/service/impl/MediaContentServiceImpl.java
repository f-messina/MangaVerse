package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;

import java.util.List;
import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.animeToAnimeDTO;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.mangaToMangaDTO;

public class MediaContentServiceImpl implements MediaContentService {
    private static final MediaContentDAO<Anime> animeDAO;
    private static final MediaContentDAO<Manga> mangaDAO;
    private static final PersonalListDAO personalListDAO;
    private static final ReviewDAO reviewDAO;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;

    static {
        animeDAO = getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = getMangaDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
    }

    @Override
    public void addMediaContent(MediaContent mediaContent) throws BusinessException {
        try {
            if (mediaContent instanceof Anime) {
                animeDAO.insert((Anime) mediaContent);
            } else if (mediaContent instanceof Manga) {
                mangaDAO.insert((Manga) mediaContent);
            }
        } catch (Exception e) {
            throw new BusinessException("Error adding media content",e);
        }
    }

    @Override
    public void updateMediaContent(MediaContent mediaContent) throws BusinessException {
        try {
            if (mediaContent instanceof Anime) {
                animeDAO.update((Anime) mediaContent);
                if (mediaContent.getTitle() != null || mediaContent.getImageUrl() != null) {
                    personalListDAO.updateItem(animeToAnimeDTO((Anime) mediaContent));
                }
            } else if (mediaContent instanceof Manga) {
                mangaDAO.update((Manga) mediaContent);
                if (mediaContent.getTitle() != null || mediaContent.getImageUrl() != null) {
                    personalListDAO.updateItem(mangaToMangaDTO((Manga) mediaContent));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("Error updating the media content",e);
        }
    }

    @Override
    public void removeMediaContent(String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAO.delete(mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAO.delete(mediaId);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE,"Invalid media content type");
            }
            personalListDAO.removeItem(mediaId);
            reviewDAO.deleteByMedia(mediaId);
        } catch (Exception e) {
            throw new BusinessException("Error removing the media content",e);
        }
    }

    @Override
    public MediaContent getMediaContentById(String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAO.find(mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAO.find(mediaId);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE,"Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error finding media content by id",e);
        }
    }

    @Override
    public PageDTO<? extends MediaContentDTO> searchByFilter(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAO.search(filters, orderBy, page);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAO.search(filters, orderBy, page);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE,"Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while searching",e);
        }
    }

    @Override
    public PageDTO<? extends MediaContentDTO> searchByTitle(String title, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAO.search(List.of(Map.of("title", title)), Map.of("score", 1), page);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAO.search(List.of(Map.of("title", title)), Map.of("score", 1), page);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE,"Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while searching",e);
        }
    }

    @Override
    public void createNode(MediaContentDTO mediaContentDTO) throws BusinessException {
        try {
            MediaContentType type = mediaContentDTO instanceof AnimeDTO ? MediaContentType.ANIME :
                    mediaContentDTO instanceof MangaDTO? MediaContentType.MANGA : null;
            if (MediaContentType.ANIME.equals(type))
                animeDAONeo4J.createNode(mediaContentDTO);
            else if (MediaContentType.MANGA.equals(type))
                mangaDAONeo4J.createNode(mediaContentDTO);
            else
                throw new BusinessException("Invalid media content type");
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void addLike(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAONeo4J.like(userId, mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAONeo4J.like(userId, mediaId);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while liking the media content.", e);
        }
    }


    @Override
    public void removeLike(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAONeo4J.unlike(userId, mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAONeo4J.unlike(userId, mediaId);
            } else {
                throw new BusinessException("Invalid media content type");
            }

        } catch (Exception e) {
            throw new BusinessException("Error while unliking media content.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getLikedMediaContent(String userId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getLiked(userId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getLiked(userId);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving liked media contents.", e);
        }
    }

    @Override
    public boolean isLiked(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.isLiked(userId, mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.isLiked(userId, mediaId);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while checking like.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getSuggestedMediaContent(String userId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getSuggested(userId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getSuggested(userId);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while suggesting anime.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getTrendMediaContentByYear(int year, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getTrendMediaContentByYear(year);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getTrendMediaContentByYear(year);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

   /*
    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByGenre(MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getMediaContentTrendByGenre();
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getMediaContentTrendByGenre();
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByLikes(MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getMediaContentTrendByLikes();
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getMediaContentTrendByLikes();
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<String> getMediaContentGenresTrend(MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getMediaContentGenresTrend();
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getMediaContentGenresTrend();
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }
    */
}