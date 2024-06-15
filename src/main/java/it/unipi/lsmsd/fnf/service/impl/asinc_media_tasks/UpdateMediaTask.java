package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

/**
 * Asynchronous task for updating media content in the Neo4J database.
 * Priority = 8
 * @see Task
 * @see MediaContentDAO
 * @see MediaContent
 */
public class UpdateMediaTask extends Task {
    private final MediaContentDAO<Anime> animeDAONeo4j;
    private final MediaContentDAO<Manga> mangaDAONeo4j;
    private final MediaContent media;


    /**
     * Constructs an UpdateMediaTask.
     *
     * @param media         The media content to be updated.
     */
    public UpdateMediaTask(MediaContent media) {
        super(8);
        this.animeDAONeo4j = DAOLocator.getAnimeDAO(DataRepositoryEnum.NEO4J);
        this.mangaDAONeo4j = DAOLocator.getMangaDAO(DataRepositoryEnum.NEO4J);
        this.media = media;
    }

    /**
     * Executes the task to update media content.
     *
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            // Update the media content
            if (media instanceof Anime anime)
                animeDAONeo4j.updateMediaContent(anime);
            else if (media instanceof Manga manga)
                mangaDAONeo4j.updateMediaContent(manga);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
