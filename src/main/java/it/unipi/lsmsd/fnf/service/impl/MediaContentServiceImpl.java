package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public class MediaContentServiceImpl implements MediaContentService {
    private static final MediaContentDAO<Anime> animeDAO ;
    private static final MediaContentDAO<Manga> mangaDAO;
    private static final PersonalListDAO personalListDAO;
    private static final ReviewDAO reviewDAO;

    static {
        animeDAO = DAOLocator.getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = DAOLocator.getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
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
                    personalListDAO.updateItem(ModelToDtoMapper.animeToAnimeDTO((Anime) mediaContent));
                }
            } else if (mediaContent instanceof Manga) {
                mangaDAO.update((Manga) mediaContent);
                if (mediaContent.getTitle() != null || mediaContent.getImageUrl() != null) {
                    personalListDAO.updateItem(ModelToDtoMapper.mangaToMangaDTO((Manga) mediaContent));
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
    public MediaContent getMediaContent(String id, MediaContentType type) throws BusinessException {
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
    public List<? extends MediaContentDTO> searchMediaContent(Map<String, Object> filters, Map<String, Integer> orderBy, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                if (filters.containsKey("title") && filters.get("title") != null) {
                    return animeDAO.search((String) filters.get("title"));
                } else {
                    return animeDAO.search(filters, orderBy);
                }
            } else if (MediaContentType.MANGA.equals(type)) {
                if (filters.containsKey("title") && filters.get("title") != null) {
                    return mangaDAO.search((String) filters.get("title"));
                } else {
                    return mangaDAO.search(filters, orderBy);
                }
            } else {
                throw new BusinessException("Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }
}
