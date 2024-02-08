package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
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
import it.unipi.lsmsd.fnf.dao.Neo4JDAO;


import org.bson.types.ObjectId;

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
    private static final Neo4JDAO neo4JDAO;

    static {
        animeDAO = getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = getMangaDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        neo4JDAO = DAOLocator.getNeo4JDAO(DataRepositoryEnum.NEO4J);
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

    @Override
    public void likeMediaContent(String userId, String mediaId) throws BusinessException {
        try {
            neo4JDAO.likeMediaContent(userId, mediaId);

        } catch (DAOException e) {
            throw new BusinessException("Error while liking the media content.", e);
        }
    }


    @Override
    public void unlikeMediaContent(String userId, String mediaId) throws BusinessException {
        try {
            neo4JDAO.unlikeMediaContent(userId, mediaId);
        } catch (DAOException e) {
            throw new BusinessException("Error while unliking media content.", e);
        }
    }

    /*@Override
    public List<MediaContentDTO> getLikedMediaContents(String userId) throws BusinessException {
        try {
            return neo4JDAO.getLikedMediaContents(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving liked media contents.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> getLikedAnime(String userId) throws BusinessException {
        try {
            return neo4JDAO.getLikedAnime(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving liked anime.", e);
        }
    }

    @Override
    public List<MangaDTO> getLikedManga(String userId) throws BusinessException {
        try {
            return neo4JDAO.getLikedManga(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving liked manga.", e);
        }
    }

    /*@Override
    public List<Record> suggestMediaContents(String userId) throws BusinessException {
        try {
            return neo4JDAO.suggestMediaContents(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while suggesting media contents.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> suggestAnime(String userId) throws BusinessException {
        try {
            return neo4JDAO.suggestAnime(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while suggesting anime.", e);
        }
    }

    @Override
    public List<MangaDTO> suggestManga(String userId) throws BusinessException {
        try {
            return neo4JDAO.suggestManga(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while suggesting manga.", e);
        }
    }


    /*@Override
    public List<Record> getTrendByYear(int year) throws BusinessException {
        try {
            return neo4JDAO.getTrendByYear(year);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> getTrendAnimeByYear(int year) throws BusinessException {
        try {
            return neo4JDAO.getTrendAnimeByYear(year);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<MangaDTO> getTrendMangaByYear(int year) throws BusinessException {
        try {
            return neo4JDAO.getTrendMangaByYear(year);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    /*@Override
    public List<Record> getMediaContentByGenre(String genre) throws BusinessException {
        try {
            return neo4JDAO.getMediaContentByGenre(genre);
        } catch (DAOException e) {
            throw new BusinessException("Error while getting media content by genre.", e);
        }

    }*/

    @Override
    public List<AnimeDTO> getAnimeByGenre(String genre) throws BusinessException {
        try {
            return neo4JDAO.getAnimeByGenre(genre);
        } catch (DAOException e) {
            throw new BusinessException("Error while getting anime by genre.", e);
        }

    }

    @Override
    public List<MangaDTO> getMangaByGenre(String genre) throws BusinessException {
        try {
            return neo4JDAO.getMangaByGenre(genre);
        } catch (DAOException e) {
            throw new BusinessException("Error while getting manga by genre.", e);
        }

    }

    /*@Override
    public List<Record> suggestMediaContentByGenre(String userId) throws BusinessException {
        try {
            return neo4JDAO.suggestMediaContentByGenre(userId);
        } catch(DAOException e) {
            throw new BusinessException("Error while suggesting media content by genre.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> suggestAnimeByGenre(String userId) throws BusinessException {
        try {
            return neo4JDAO.suggestAnimeByGenre(userId);
        } catch(DAOException e) {
            throw new BusinessException("Error while suggesting anime by genre.", e);
        }
    }

    @Override
    public List<MangaDTO> suggestMangaByGenre(String userId) throws BusinessException {
        try {
            return neo4JDAO.suggestMangaByGenre(userId);
        } catch(DAOException e) {
            throw new BusinessException("Error while suggesting manga by genre.", e);
        }
    }

    //Show the trends of the genres for year
    /*@Override
    public List<Record> getGenresTrendByYear(int year) throws BusinessException {
        try {
            return neo4JDAO.getGenresTrendByYear(year);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeGenresTrendByYear(int year) throws BusinessException {
        try {
            return neo4JDAO.getAnimeGenresTrendByYear(year);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<AnimeDTO> getMangaGenresTrendByYear(int year) throws BusinessException {
        try {
            return neo4JDAO.getAnimeGenresTrendByYear(year);
        } catch (DAOException e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }


    /*@Override
    public List<Record> getTrendByGenre() throws BusinessException {
        try {
            return neo4JDAO.getTrendByGenre();
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeTrendByGenre() throws BusinessException {
        try {
            return neo4JDAO.getAnimeTrendByGenre();
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<MangaDTO> getMangaTrendByGenre() throws BusinessException {
        try {
            return neo4JDAO.getMangaTrendByGenre();
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    //Show the trends of the likes in general
    /*@Override
    public List<Record> getTrendByLikes() throws BusinessException {
        try {
            return neo4JDAO.getTrendByLikes();
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeTrendByLikes() throws BusinessException {
        try {
            return neo4JDAO.getAnimeTrendByLikes();
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<MangaDTO> getMangaTrendByLikes() throws BusinessException {
        try {
            return neo4JDAO.getMangaTrendByLikes();
        } catch(Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    //Show the trends of the genres in general
    /*@Override
    public List<Record> getGenresTrend() throws BusinessException {
        try {
            return neo4JDAO.getGenresTrend();
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeGenresTrend() throws BusinessException {
        try {
            return neo4JDAO.getAnimeGenresTrend();
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }

    @Override
    public List<MangaDTO> getMangaGenresTrend() throws BusinessException {
        try {
            return neo4JDAO.getMangaGenresTrend();
        } catch (Exception e) {
            throw new BusinessException("Error while retrieving the trend.", e);
        }
    }
}