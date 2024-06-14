package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Task for updating the number of likes for media content.
 */
public class UpdateNumberOfLikesTask extends Task {
    private final MediaContentDAO<Anime> animeDAOMongo;
    private final MediaContentDAO<Anime> animeDAONeo4j;
    private final MediaContentDAO<Manga> mangaDAOMongo;
    private final MediaContentDAO<Manga> mangaDAONeo4j;
    private final String mediaId;
    private final MediaContentType type;

    /**
     * Constructs an UpdateNumberOfLikesTask.
     *
     * @param recipeId The ID of the media content.
     * @param type    The type of the media content.
     */
    public UpdateNumberOfLikesTask(String recipeId, MediaContentType type) {
        super(5);
        this.animeDAOMongo = DAOLocator.getAnimeDAO(DataRepositoryEnum.MONGODB);
        this.animeDAONeo4j = DAOLocator.getAnimeDAO(DataRepositoryEnum.NEO4J);
        this.mangaDAOMongo = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.mangaDAONeo4j = DAOLocator.getMangaDAO(DataRepositoryEnum.NEO4J);
        this.mediaId = recipeId;
        this.type = type;

    }

    /**
     * Executes the task to update the number of likes for media content.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {

            if (type == MediaContentType.ANIME) {
                Integer likes = animeDAONeo4j.getNumOfLikes(mediaId);
                animeDAOMongo.updateNumOfLikes(mediaId, likes);
            } else {
                Integer likes = mangaDAONeo4j.getNumOfLikes(mediaId);
                mangaDAOMongo.updateNumOfLikes(mediaId, likes);
            }

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
