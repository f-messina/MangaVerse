package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;


import org.bson.types.ObjectId;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.animeToAnimeDTO;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.mangaToMangaDTO;
import static java.util.Collections.singletonMap;

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
            throw new BusinessException(e);
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
            throw new BusinessException(e);
        }
    }

    @Override
    public void removeMediaContent(String id, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAO.delete(new ObjectId(id));
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAO.delete(new ObjectId(id));
            } else {
                throw new BusinessException("Invalid media content type");
            }
            personalListDAO.removeItem(new ObjectId(id));
            reviewDAO.deleteByMedia(new ObjectId(id));
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public MediaContent getMediaContentById(String id, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAO.find(new ObjectId(id));
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAO.find(new ObjectId(id));
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException(e);
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
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException(e);
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
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void createMediaContentNode(String id, String title, String picture, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                animeDAONeo4J.createMediaContentNode(id, title, picture);
            else if (MediaContentType.MANGA.equals(type))
                mangaDAONeo4J.createMediaContentNode(id, title, picture);

        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void likeMediaContent(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAONeo4J.likeMediaContent(userId, mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAONeo4J.likeMediaContent(userId, mediaId);
            } else {
                throw new BusinessException("Invalid media content type");
            }


        } catch (Exception e) {
            throw new BusinessException("Error while liking the media content.", e);
        }
    }


    @Override
    public void unlikeMediaContent(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAONeo4J.unlikeMediaContent(userId, mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAONeo4J.unlikeMediaContent(userId, mediaId);
            } else {
                throw new BusinessException("Invalid media content type");
            }

        } catch (Exception e) {
            throw new BusinessException("Error while unliking media content.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getLikedMediaContents(String userId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getLikedMediaContent(userId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getLikedMediaContent(userId);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving liked media contents.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getLikedAnime(String userId, MediaContentType type) throws BusinessException {
        try {
            if(MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getLikedMediaContent(userId);
            else if(MediaContentType.MANGA.equals(type))
                return mangaDAONeo4J.getLikedMediaContent(userId);
            else
                throw new BusinessException("Invalid media content type");
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving liked anime.", e);
        }
    }

    @Override
    public List<? extends MediaContentDTO> getLikedManga(String userId, MediaContentType type) throws BusinessException {
        try {
            if(MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getLikedMediaContent(userId);
            else if(MediaContentType.MANGA.equals(type))
                return mangaDAONeo4J.getLikedMediaContent(userId);
            else
                throw new BusinessException("Invalid media content type");

        } catch (Exception e) {
            throw new BusinessException("Error while retrieving liked manga.", e);
        }
    }



    @Override
    public List<? extends MediaContentDTO> suggestMediaContent(String userId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.suggestMediaContent(userId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.suggestMediaContent(userId);
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

    //Show the trends of the genres for year


    @Override
    public List<String> getMediaContentGenresTrendByYear(int year, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAONeo4J.getMediaContentGenresTrendByYear(year);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAONeo4J.getMediaContentGenresTrendByYear(year);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }



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



    //Show the trends of the likes in general


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

    //Show the trends of the genres in general


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

}