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
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.CreateMediaTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.DeleteMediaTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.UpdateMediaTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.UpdateNumberOfLikesTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks.RemoveDeletedMediaReviewsTask;
import it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks.UpdateReviewRedundancyTask;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.ServiceLocator.getExecutorTaskService;
import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

/**
 * Implementation of MediaContentService that provides methods to interact with media content.
 * It uses DAOs to interact with the data repository and ExecutorTaskService to execute tasks in the background.
 * The methods access to Media Content Entity in the database, provide
 * operations to maintain consistency between collections, search functionality,
 * operations to get statistics and operations to get media content suggestions.
 * The methods, in general, execute a single DAO method. When needed to maintain
 * eventual consistency between collections, the methods execute multiple DAO methods,
 * executing the consistency operations in an asynchronous way.
 * @see MediaContentService
 * @see MediaContentDAO
 * @see ReviewDAO
 * @see ExecutorTaskService
 */
public class MediaContentServiceImpl implements MediaContentService {
    private static final MediaContentDAO<Anime> animeDAOMongoDB;
    private static final MediaContentDAO<Manga> mangaDAOMongoDB;
    private static final MediaContentDAO<Anime> animeDAONeo4J;
    private static final MediaContentDAO<Manga> mangaDAONeo4J;
    private static final ExecutorTaskService aperiodicExecutorTaskService;

    static {
        animeDAOMongoDB = getAnimeDAO(DataRepositoryEnum.MONGODB);
        mangaDAOMongoDB = getMangaDAO(DataRepositoryEnum.MONGODB);
        animeDAONeo4J = getAnimeDAO(DataRepositoryEnum.NEO4J);
        mangaDAONeo4J = getMangaDAO(DataRepositoryEnum.NEO4J);
        aperiodicExecutorTaskService = getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
    }

    /**
     * Adds a new media content to the data repository.
     * Then, it triggers tasks to add the media content to the Neo4j database
     * in an asynchronous way.
     *
     * @param mediaContent          The media content to be added.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void saveMediaContent(MediaContent mediaContent) throws BusinessException {
        try {
            // Check if the media content has all the required fields and save it in the data repository
            if (mediaContent instanceof Anime anime) {
                if (StringUtils.isAnyEmpty(anime.getTitle(), anime.getImageUrl()) || anime.getEpisodeCount() == null)
                    throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Title, image URL and number of episodes are required");

                // Save the anime in MongoDB
                animeDAOMongoDB.saveMediaContent(anime);

            } else if (mediaContent instanceof Manga manga) {
                if (StringUtils.isAnyEmpty(manga.getTitle(), manga.getImageUrl()) || manga.getStartDate() == null || manga.getType() == null)
                    throw new BusinessException(BusinessExceptionType.EMPTY_FIELDS,"Title, image URL, start date and type are required");

                // Save the manga in MongoDB
                mangaDAOMongoDB.saveMediaContent(manga);

            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_INPUT, "Invalid media content type");
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
     * Updates an existing media content in the data repository and triggers associated tasks.
     * the method performs the following operations:
     * 1. Updates the media content in the MongoDB database.
     * 2. If the media content is an Anime or Manga, the title is modified and there are reviews,
     * it triggers a task to update the review redundancy in MongoDB.
     * 3. If the media content is an Anime or Manga and the title or image URL is modified,
     * it triggers a task to update the node Anime/Manga in Neo4j.
     *
     * @param mediaContent              The media content to be updated.
     * @param reviewIds                 The list of review IDs associated with the media content.
     * @throws BusinessException        If an error occurs during the operation.
     */
    @Override
    public void updateMediaContent(MediaContent mediaContent, List<String> reviewIds) throws BusinessException {
        try {
            if (mediaContent instanceof Anime anime) {

                // Update the anime in MongoDB
                animeDAOMongoDB.updateMediaContent(anime);

                // Create a task which updates the review redundancy in MongoDB if the title is not null and there are reviews
                if (mediaContent.getTitle() != null && reviewIds != null && !reviewIds.isEmpty())
                    aperiodicExecutorTaskService.executeTask(new UpdateReviewRedundancyTask(new AnimeDTO(anime.getId(), anime.getTitle()), null, reviewIds));
            } else if (mediaContent instanceof Manga manga) {

                // Update the manga in MongoDB
                mangaDAOMongoDB.updateMediaContent(manga);

                // Create a task which updates the review redundancy in MongoDB if the title is not null
                if (mediaContent.getTitle() != null && reviewIds != null && !reviewIds.isEmpty())
                    aperiodicExecutorTaskService.executeTask(new UpdateReviewRedundancyTask(new MangaDTO(manga.getId(), manga.getTitle()), null, reviewIds));
            }

            // Create a task which update the node Anime/Manga in Neo4j if the title or image URL is not null
            if (mediaContent.getTitle() != null || mediaContent.getImageUrl() != null) {
                aperiodicExecutorTaskService.executeTask(new UpdateMediaTask(mediaContent));
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
     * The method performs the following operations:
     * 1. Deletes the media content from the MongoDB database.
     * 2. Triggers a task to delete the node Anime/Manga in Neo4j.
     * 3. Triggers a task to remove the reviews associated with the media content in MongoDB.
     *
     * @param mediaId               The ID of the media content to be removed.
     * @param reviewIds             The list of review IDs associated with the media content.
     * @param type                  The type of media content (Anime or Manga).
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void deleteMediaContent(String mediaId, List<String> reviewIds, MediaContentType type) throws BusinessException {
        try {
            // Delete the media content in MongoDB
            if (MediaContentType.ANIME.equals(type))
                animeDAOMongoDB.deleteMediaContent(mediaId);
            else
                mangaDAOMongoDB.deleteMediaContent(mediaId);

            // Create a task which delete the node Anime/Manga in Neo4j
            aperiodicExecutorTaskService.executeTask(new DeleteMediaTask(mediaId, type));

            // Create a task which delete the reviews of the media content in MongoDB
            aperiodicExecutorTaskService.executeTask(new RemoveDeletedMediaReviewsTask(reviewIds));

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    /**
     * Retrieves media content by its ID.
     *
     * @param mediaId               The ID of the media content to retrieve.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      The retrieved media content.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public MediaContent getMediaContentById(String mediaId, MediaContentType type) throws BusinessException {
        try {
            // Read the media content from MongoDB
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
     * Searches for media content based on specified filters, ordering criteria and pagination.
     *
     * @param filters               The filters to apply during the search.
     * @param orderBy               The order in which results should be returned.
     * @param page                  The page number of the search results.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A PageDTO containing the search results.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public PageDTO<MediaContentDTO> searchByFilter(List<Pair<String, Object>> filters, Map<String, Integer> orderBy, int page, MediaContentType type) throws BusinessException {
        try {
            // Search for media content in MongoDB
            if (MediaContentType.ANIME.equals(type))
                return animeDAOMongoDB.search(filters, orderBy, page, false);
            else
                return mangaDAOMongoDB.search(filters, orderBy, page, false);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Searches for media content by title and pagination.
     *
     * @param title                 The title of the media content to search for.
     * @param page                  The page number of the search results.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A PageDTO containing the search results.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public PageDTO<MediaContentDTO> searchByTitle(String title, int page, MediaContentType type) throws BusinessException {
        try {
            // Search for media content in MongoDB
            if (MediaContentType.ANIME.equals(type))
                return animeDAOMongoDB.search(List.of(Pair.of("$regex", Pair.of("title", title))), Map.of("title", 1), page, true);
            else
                return mangaDAOMongoDB.search(List.of(Pair.of("$regex", Pair.of("title", title))), Map.of("title", 1), page, true);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Adds a like for the specified user to the given media content.
     * It triggers a task to update the number of likes in MongoDB in an asynchronous way.
     *
     * @param userId                The ID of the user giving the like.
     * @param mediaId               The ID of the media content to be liked.
     * @param type                  The type of media content (Anime or Manga).
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void addLike(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            // Add a like in Neo4j
            if (MediaContentType.ANIME.equals(type))
                animeDAONeo4J.like(userId, mediaId);
            else
                mangaDAONeo4J.like(userId, mediaId);

            // Create a task which updates the number of likes in MongoDB
            UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(mediaId, type, 1);
            aperiodicExecutorTaskService.executeTask(task);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }


    /**
     * Removes a like given by the specified user from the provided media content.
     * It triggers a task to update the number of likes in MongoDB in an asynchronous way.
     *
     * @param userId                The ID of the user whose like is to be removed.
     * @param mediaId               The ID of the media content from which the like is to be removed.
     * @param type                  The type of media content (Anime or Manga).
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void removeLike(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            // Remove a like in Neo4j
            if (MediaContentType.ANIME.equals(type))
                animeDAONeo4J.unlike(userId, mediaId);
            else
                mangaDAONeo4J.unlike(userId, mediaId);

            // Create a task which updates the number of likes in MongoDB
            UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(mediaId, type, -1);
            aperiodicExecutorTaskService.executeTask(task);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }

    /**
     * Checks if a user has liked a particular media content.
     *
     * @param userId                The ID of the user.
     * @param mediaId               The ID of the media content to check.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      True if the user has liked the media content, false otherwise.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public boolean isLiked(String userId, String mediaId, MediaContentType type) throws BusinessException {
        try {
            // Check if the user has liked the media content in Neo4j
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
     * Retrieves a page of media content that a user has liked.
     *
     * @param userId                The ID of the user whose liked media content is to be retrieved.
     * @param page                  The page number of the search results.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A list of media content DTOs that the user has liked.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public PageDTO<MediaContentDTO> getLikedMediaContent(String userId, int page, MediaContentType type) throws BusinessException {
        try {
            // Get the liked media content from Neo4j
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
     * Retrieves a list of suggested media content for a given user based on the user's followings.
     *
     * @param userId                The ID of the user for whom suggestions are to be retrieved.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A list of suggested media content DTOs.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<MediaContentDTO> getSuggestedMediaContentByFollowings(String userId, MediaContentType type, Integer limit) throws BusinessException {
        try {
            // Get the suggested media content from Neo4j
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getSuggestedByFollowings(userId, limit);
            else
                return mangaDAONeo4J.getSuggestedByFollowings(userId, limit);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    /**
     * Retrieves a list of suggested media content for a given user based on the user's likes.
     *
     * @param userId                The ID of the user for whom suggestions are to be retrieved.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A list of suggested media content DTOs.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<MediaContentDTO> getSuggestedMediaContentByLikes(String userId, MediaContentType type, Integer limit) throws BusinessException {
        try {
            // Get the suggested media content from Neo4j
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getSuggestedByLikes(userId, limit);
            else
                return mangaDAONeo4J.getSuggestedByLikes(userId, limit);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    /**
     * Retrieves trending media content based on the number of likes.
     *
     * @param year                  The year for which trending media content is to be retrieved.
     * @param limit                 The maximum number of media content items to retrieve.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A map containing the trending media content and their corresponding number of likes.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public Map<MediaContentDTO, Integer> getMediaContentTrendByYear(int year, Integer limit, MediaContentType type) throws BusinessException {
        try {
            // Check if the input is valid
            if (year < 0 || year > LocalDate.now().getYear() || limit < 0)
                throw new BusinessException(BusinessExceptionType.INVALID_INPUT, "Invalid input");

            // Get the trending media content from Neo4j
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getTrendMediaContentByYear(year, limit);
            else
                return mangaDAONeo4J.getTrendMediaContentByYear(year, limit);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }


    /**
     * Retrieves trending media content based on likes.
     *
     * @param limit                 The maximum number of media content items to retrieve.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A list of MediaContentDTO objects representing the trending media content.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public List<MediaContentDTO> getMediaContentTrendByLikes(Integer limit, MediaContentType type) throws BusinessException {
        try {
            // Get the trending media content from Neo4j
            if (MediaContentType.ANIME.equals(type))
                return animeDAONeo4J.getMediaContentTrendByLikes(limit);
            else
                return mangaDAONeo4J.getMediaContentTrendByLikes(limit);

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }

    /**
     * Retrieves the best criteria for filtering media content.
     *
     * @param criteria              The criteria for grouping media content.
     *                              The criteria can be tags, producers, studios for Anime
     *                              and genres, demographics, themes, authors, serializations for Manga.
     * @param page                  The page number for pagination.
     * @param type                  The type of media content (Anime or Manga).
     * @return                      A map containing the best criteria and their corresponding scores.
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public Map<String, Double> getBestCriteria (String criteria, int page, MediaContentType type) throws BusinessException {
        try {
            if (MediaContentType.ANIME.equals(type)) {
                // Check if the criteria is valid
                if (!(criteria.equals("tags") || criteria.equals("producers") || criteria.equals("studios")))
                    throw new BusinessException(BusinessExceptionType.INVALID_INPUT, "Invalid criteria");

                // Get the best criteria for Anime from MongoDB
                return animeDAOMongoDB.getBestCriteria(criteria, criteria.equals("tags"), page);

            } else {
                // Check if the criteria is valid
                if (!(criteria.equals("genres") || criteria.equals("demographics") ||
                        criteria.equals("themes") || criteria.equals("authors") || criteria.equals("serializations")))
                    throw new BusinessException(BusinessExceptionType.INVALID_INPUT, "Invalid criteria");

                boolean isArray = criteria.equals("genres") || criteria.equals("demographics") ||
                        criteria.equals("themes") || criteria.equals("authors");

                // Get the best criteria for Manga from MongoDB
                return mangaDAOMongoDB.getBestCriteria(criteria, isArray, page);
            }

        } catch (DAOException e) {
            handleDAOException(e);
            return null;
        }
    }
}
