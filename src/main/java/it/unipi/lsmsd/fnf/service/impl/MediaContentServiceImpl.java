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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper.convertToDTO;

/**
 * The MediaContentServiceImpl class provides implementation for the MediaContentService interface.
 * It interacts with various DAOs to perform CRUD operations on media content entities and related operations.
 */
public class MediaContentServiceImpl implements MediaContentService {
    private static final MediaContentDAO<Anime> animeDAO;
    private static final MediaContentDAO<Manga> mangaDAO;
    private static final ReviewDAO reviewDAO;

    private static final PersonalListDAO personalListDAO;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;

    static {
        animeDAO = getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = getMangaDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
    }

    /**
     * Adds a new media content to the data repository.
     * @param mediaContent The media content to be added.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void addMediaContent(MediaContent mediaContent) throws BusinessException {
        try {
            if (mediaContent instanceof Anime anime) {
                if (StringUtils.isAnyEmpty(anime.getTitle(), anime.getImageUrl()) || anime.getEpisodeCount() == null)
                    throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Title, image URL and number of episodes are required");
                animeDAO.createMediaContent(anime);
            } else if (mediaContent instanceof Manga manga) {
                if (StringUtils.isAnyEmpty(manga.getTitle(), manga.getImageUrl()) || manga.getStartDate() == null || manga.getType() == null)
                    throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Title, image URL, start date and type are required");
                mangaDAO.createMediaContent(manga);
            }
        } catch (Exception e) {
            throw new BusinessException("Error adding media content",e);
        }
    }

    /**
     * Updates an existing media content in the data repository.
     * @param mediaContent The media content to be updated.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void updateMediaContent(MediaContent mediaContent) throws BusinessException {
        try {
            if (mediaContent instanceof Anime anime) {
                animeDAO.updateMediaContent(anime);
                if (mediaContent.getTitle() != null) {
                    reviewDAO.updateMediaRedundancy(new AnimeDTO(anime.getId(), anime.getTitle()));
                }
            } else if (mediaContent instanceof Manga manga) {
                mangaDAO.updateMediaContent(manga);
                if (mediaContent.getTitle() != null) {
                    reviewDAO.updateMediaRedundancy(new MangaDTO(manga.getId(), manga.getTitle()));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("Error updating the media content",e);
        }
    }

    /**
     * Removes a media content from the data repository.
     * @param mediaId The ID of the media content to be removed.
     * @param type The type of media content (Anime or Manga).
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void removeMediaContent(String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                animeDAO.deleteMediaContent(mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                mangaDAO.deleteMediaContent(mediaId);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE,"Invalid media content type");
            }
            // TODO: personalListDAO.removeElementInListWithoutMedia();
            // TODO: reviewDAO.deleteReviewsWithNoMedia();
        } catch (Exception e) {
            throw new BusinessException("Error removing the media content",e);
        }
    }


    /**
     * Retrieves media content by its ID.
     * @param mediaId The ID of the media content to retrieve.
     * @param type The type of media content (Anime or Manga).
     * @return The retrieved media content.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public MediaContent getMediaContentById(String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                return animeDAO.readMediaContent(mediaId);
            } else if (MediaContentType.MANGA.equals(type)) {
                return mangaDAO.readMediaContent(mediaId);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE,"Invalid media content type");
            }
        } catch (Exception e) {
            throw new BusinessException("Error finding media content by id",e);
        }
    }


    /**
     * Searches for media content based on specified filters.
     * @param filters The filters to apply during the search.
     * @param orderBy The order in which results should be returned.
     * @param page The page number of the search results.
     * @param type The type of media content (Anime or Manga).
     * @return A PageDTO containing the search results.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Searches for media content by title.
     * @param title The title of the media content to search for.
     * @param page The page number of the search results.
     * @param type The type of media content (Anime or Manga).
     * @return A PageDTO containing the search results.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Creates a node in the Neo4j database for the provided media content.
     * @param mediaContentDTO The media content DTO representing the node to be created.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Adds a like for the specified user to the given media content.
     * @param userId The ID of the user giving the like.
     * @param mediaId The ID of the media content to be liked.
     * @param type The type of media content (Anime or Manga).
     * @throws BusinessException If an error occurs during the operation.
     */
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


    /**
     * Removes a like given by the specified user from the provided media content.
     * @param userId The ID of the user whose like is to be removed.
     * @param mediaId The ID of the media content from which the like is to be removed.
     * @param type The type of media content (Anime or Manga).
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Retrieves a list of media content that a user has liked.
     * @param userId The ID of the user whose liked media content is to be retrieved.
     * @param type The type of media content (Anime or Manga).
     * @return A list of media content DTOs that the user has liked.
     * @throws BusinessException If an error occurs during the operation.
     */
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


    /**
     * Checks if a user has liked a particular media content.
     * @param userId The ID of the user.
     * @param mediaId The ID of the media content to check.
     * @param type The type of media content (Anime or Manga).
     * @return True if the user has liked the media content, false otherwise.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Retrieves a list of suggested media content for a given user.
     * @param userId The ID of the user for whom suggestions are to be retrieved.
     * @param type The type of media content (Anime or Manga).
     * @return A list of suggested media content DTOs.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Retrieves a list of trending media content for a given year.
     * @param year The year for which trending media content is to be retrieved.
     * @param type The type of media content (Anime or Manga).
     * @return A list of trending media content DTOs.
     * @throws BusinessException If an error occurs during the operation.
     */
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
