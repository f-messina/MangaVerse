package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.Objects;

/**
 * Asynchronous task for incrementing or decrementing the number of likes of a media content.
 * Priority = 6
 * @see Task
 * @see MediaContentDAO
 * @see MediaContent
 */
public class UpdateNumberOfLikesTask extends Task {
    private final MediaContentDAO<Anime> animeDAOMongo;
    private final MediaContentDAO<Manga> mangaDAOMongo;
    private final String mediaId;
    private final MediaContentType type;
    private final int increment;

    /**
     * Constructs an UpdateNumberOfLikesTask.
     *
     * @param mediaId       The id of the media content.
     * @param type          The type of the media content.
     * @param increment     The increment to apply to the number of likes.
     */
    public UpdateNumberOfLikesTask(String mediaId, MediaContentType type, int increment) {
        super(6);
        this.animeDAOMongo = DAOLocator.getAnimeDAO(DataRepositoryEnum.MONGODB);
        this.mangaDAOMongo = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.mediaId = mediaId;
        this.type = type;
        this.increment = increment;
    }

    /**
     * Executes the job of updating the number of likes of a media content.
     *
     * @throws BusinessException    if an error occurs during the update of the number of likes.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            // Update the number of likes
            if (type == MediaContentType.ANIME) {
                animeDAOMongo.updateNumOfLikes(mediaId, increment);
            } else {
                mangaDAOMongo.updateNumOfLikes(mediaId, increment);
            }

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
