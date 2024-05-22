package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.*;
import it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks.RemoveDeletedMediaReviewsTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks.UpdateReviewRedundancyTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks.CreateUserTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks.UpdateUserTask;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.ServiceLocator.getExecutorTaskService;
import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

/**
 * The MediaContentServiceImpl class provides implementation for the MediaContentService interface.
 * It interacts with various DAOs to perform CRUD operations on media content entities and related operations.
 */
public class MediaContentServiceImpl implements MediaContentService {
    private static final MediaContentDAO<Anime> animeDAOMongoDB;
    private static final MediaContentDAO<Manga> mangaDAOMongoDB;
    private static final ReviewDAO reviewDAOMongoDB;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;
    private static final ExecutorTaskService aperiodicExecutorTaskService;

    static {
        animeDAOMongoDB = getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAOMongoDB = getMangaDAO(DataRepositoryEnum.MONGODB);
        reviewDAOMongoDB = getReviewDAO(DataRepositoryEnum.MONGODB);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
        aperiodicExecutorTaskService = getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
    }

    /**
     * Adds a new media content to the data repository.
     * @param mediaContent The media content to be added.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void saveMediaContent(MediaContent mediaContent) throws BusinessException {
        try {
            // Check if the media content has all the required fields and save it in the data repository
            if (mediaContent instanceof Anime anime) {
                if (StringUtils.isAnyEmpty(anime.getTitle(), anime.getImageUrl()) || anime.getEpisodeCount() == null)
                    throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Title, image URL and number of episodes are required");
                animeDAOMongoDB.saveMediaContent(anime);

            } else if (mediaContent instanceof Manga manga) {
                if (StringUtils.isAnyEmpty(manga.getTitle(), manga.getImageUrl()) || manga.getStartDate() == null || manga.getType() == null)
                    throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Title, image URL, start date and type are required");
                mangaDAOMongoDB.saveMediaContent(manga);
            }

            // Create a task which adds a new node Anime/Manga in Neo4j
            aperiodicExecutorTaskService.executeTask(new CreateMediaTask(mediaContent));

        } catch (DAOException e) {
            switch (e.getType()) {
                case DUPLICATED_KEY -> throw new BusinessException(BusinessExceptionType.DUPLICATED_KEY, e.getMessage());
                case DATABASE_ERROR -> throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
                default -> throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
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
                animeDAOMongoDB.updateMediaContent(anime);
                if (mediaContent.getTitle() != null)
                    reviewDAOMongoDB.updateMediaRedundancy(new AnimeDTO(anime.getId(), anime.getTitle()));
            } else if (mediaContent instanceof Manga manga) {
                mangaDAOMongoDB.updateMediaContent(manga);
                if (mediaContent.getTitle() != null)
                    reviewDAOMongoDB.updateMediaRedundancy(new MangaDTO(manga.getId(), manga.getTitle()));
            }

            // Create a task which update the node Anime/Manga in Neo4j
            if (mediaContent.getTitle() != null || mediaContent.getImageUrl() != null) {
                aperiodicExecutorTaskService.executeTask(new UpdateMediaTask(mediaContent));
                aperiodicExecutorTaskService.executeTask(new UpdateReviewRedundancyTask(mediaContent.toDTO(), null));
            }

        } catch (DAOException e) {
            switch (e.getType()) {
                case DUPLICATED_KEY -> throw new BusinessException(BusinessExceptionType.DUPLICATED_KEY, e.getMessage());
                case DATABASE_ERROR -> throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
                default -> throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Removes a media content from the data repository.
     * @param mediaId The ID of the media content to be removed.
     * @param type The type of media content (Anime or Manga).
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteMediaContent(String mediaId, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                animeDAOMongoDB.deleteMediaContent(mediaId);
            else
                mangaDAOMongoDB.deleteMediaContent(mediaId);

            // Create a task which delete the node Anime/Manga in Neo4j
            aperiodicExecutorTaskService.executeTask(new DeleteMediaTask(mediaId, type));

            // Create a task which delete the reviews of the media content in MongoDB
            aperiodicExecutorTaskService.executeTask(new RemoveDeletedMediaReviewsTask(mediaId));

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
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
            if (MediaContentType.ANIME.equals(type))
                return animeDAOMongoDB.readMediaContent(mediaId);
            else
                return mangaDAOMongoDB.readMediaContent(mediaId);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
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
    public PageDTO<MediaContentDTO> searchByFilter(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                return animeDAOMongoDB.search(filters, orderBy, page);
            else
                return mangaDAOMongoDB.search(filters, orderBy, page);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
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
    public PageDTO<MediaContentDTO> searchByTitle(String title, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                return animeDAOMongoDB.search(List.of(Map.of("title", title)), Map.of("score", 1), page);
            else
                return mangaDAOMongoDB.search(List.of(Map.of("title", title)), Map.of("score", 1), page);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
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
            if (MediaContentType.ANIME.equals(type))
                animeDAONeo4J.like(userId, mediaId);
            else
                mangaDAONeo4J.like(userId, mediaId);

            // Create a task which updates the number of likes in MongoDB
            UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(mediaId, type);
            aperiodicExecutorTaskService.executeTask(task);

        } catch (DAOException e) {
            handleDAOException(e);
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
            if (MediaContentType.ANIME.equals(type))
                animeDAONeo4J.unlike(userId, mediaId);
            else
                mangaDAONeo4J.unlike(userId, mediaId);

            // Create a task which updates the number of likes in MongoDB
            UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(mediaId, type);
            aperiodicExecutorTaskService.executeTask(task);

        } catch (DAOException e) {
            handleDAOException(e);
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
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.isLiked(userId, mediaId);
            else
                return mangaDAONeo4J.isLiked(userId, mediaId);

        } catch (DAOException e) {
            handleDAOException(e);
            return false;
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
    public List<? extends MediaContentDTO> getLikedMediaContent(String userId, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getLiked(userId, page);
            else
                return mangaDAONeo4J.getLiked(userId, page);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
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
    public List<? extends MediaContentDTO> getSuggestedMediaContent(String userId, MediaContentType type, Integer limit) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getSuggested(userId, limit);
            else
                return mangaDAONeo4J.getSuggested(userId, limit);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
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
    public Map<? extends MediaContentDTO, Integer> getTrendMediaContentByYear(int year, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getTrendMediaContentByYear(year);
            else
                return mangaDAONeo4J.getTrendMediaContentByYear(year);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByLikes(MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getMediaContentTrendByLikes();
            else
                return mangaDAONeo4J.getMediaContentTrendByLikes();

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    @Override
    public Map<String, Double> getBestCriteria (String criteria, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                if (!(criteria.equals("tags") || criteria.equals("producers") || criteria.equals("studios")))
                    throw new BusinessException("Invalid criteria");
                return animeDAOMongoDB.getBestCriteria(criteria, criteria.equals("tags"), page);
            } else {
                if (!(criteria.equals("genres") || criteria.equals("demographics") ||
                        criteria.equals("themes") || criteria.equals("authors") || criteria.equals("serializations")))
                    throw new BusinessException("Invalid criteria");

                boolean isArray = criteria.equals("genres") || criteria.equals("demographics") ||
                        criteria.equals("themes") || criteria.equals("authors");

                return mangaDAOMongoDB.getBestCriteria(criteria, isArray, page);
            }

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }
}
