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
 * Task for creating new media content in the data repository.
 */
public class CreateMediaTask extends Task {
    private final MediaContentDAO<Anime> animeDAONeo4j;
    private final MediaContentDAO<Manga> mangaDAONeo4j;
    private final MediaContent media;

    /**
     * Constructs a CreateMediaTask with the specified media content.
     *
     * @param media The media content to be created.
     */
    public CreateMediaTask(MediaContent media) {
        super(9);
        this.animeDAONeo4j = DAOLocator.getAnimeDAO(DataRepositoryEnum.NEO4J);
        this.mangaDAONeo4j = DAOLocator.getMangaDAO(DataRepositoryEnum.NEO4J);
        this.media = media;
    }

    /**
     * Executes the task to create the media content in the data repository.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            if (media instanceof Anime anime)
                animeDAONeo4j.saveMediaContent(anime);
            else if (media instanceof Manga manga)
                mangaDAONeo4j.saveMediaContent(manga);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
