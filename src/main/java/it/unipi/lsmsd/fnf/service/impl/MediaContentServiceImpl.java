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
import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.animeToAnimeDTO;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.mangaToMangaDTO;
import static java.util.Collections.singletonMap;

public class MediaContentServiceImpl implements MediaContentService {
    private static final MediaContentDAO<Anime> animeDAO ;
    private static final MediaContentDAO<Manga> mangaDAO;
    private static final PersonalListDAO personalListDAO;
    private static final ReviewDAO reviewDAO;

    static {
        animeDAO = getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = getMangaDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
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
    public PageDTO<? extends MediaContentDTO> searchByFilter(Map<String, Object> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException {
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
                return animeDAO.search(singletonMap("title", title), singletonMap("score", null), page);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAO.search(singletonMap("title", title), singletonMap("score", null), page);
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }
}
