package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

public class DeleteMediaTask extends Task {
    private final MediaContentDAO<Anime> animeDAONeo4j;
    private final MediaContentDAO<Manga> mangaDAOMongo;
    private final String id;
    private final MediaContentType type;

    public DeleteMediaTask(String id, MediaContentType type) {
        super(7);
        this.animeDAONeo4j = DAOLocator.getAnimeDAO(DataRepositoryEnum.NEO4J);
        this.mangaDAOMongo = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.id = id;
        this.type = type;
    }

    @Override
    public void executeJob() throws BusinessException {
        try {
            if (type.equals(MediaContentType.ANIME))
                animeDAONeo4j.deleteMediaContent(id);
            else
                mangaDAOMongo.deleteMediaContent(id);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}