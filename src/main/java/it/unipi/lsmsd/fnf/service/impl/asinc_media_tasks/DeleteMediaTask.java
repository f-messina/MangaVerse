package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

/**
 * Asynchronous task for deleting media content from the Neo4J database.
 * Priority = 7
 * @see Task
 * @see MediaContentDAO
 * @see MediaContent
 */
public class DeleteMediaTask extends Task {
    private final MediaContentDAO<Anime> animeDAONeo4j;
    private final MediaContentDAO<Manga> mangaDAONeo4j;
    private final String id;
    private final MediaContentType type;

    /**
     * Constructs a DeleteMediaTask with the specified media content ID and type.
     *
     * @param id        The ID of the media content to be deleted.
     * @param type      The type of the media content (Anime or Manga).
     */
    public DeleteMediaTask(String id, MediaContentType type) {
        super(7);
        this.animeDAONeo4j = DAOLocator.getAnimeDAO(DataRepositoryEnum.NEO4J);
        this.mangaDAONeo4j = DAOLocator.getMangaDAO(DataRepositoryEnum.NEO4J);
        this.id = id;
        this.type = type;
    }

    /**
     * Executes the task to delete the media content from the data repository.
     *
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            // Delete the media content
            if (type.equals(MediaContentType.ANIME))
                animeDAONeo4j.deleteMediaContent(id);
            else
                mangaDAONeo4j.deleteMediaContent(id);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
