package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

public class CreateMediaTask extends Task {
    private final MediaContentDAO<Anime> animeDAONeo4j;
    private final MediaContentDAO<Manga> mangaDAOMongo;
    private final MediaContent media;

    public CreateMediaTask(MediaContent media) {
        super(9);
        this.animeDAONeo4j = DAOLocator.getAnimeDAO(DataRepositoryEnum.NEO4J);
        this.mangaDAOMongo = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.media = media;
    }

    @Override
    public void executeJob() throws BusinessException {
        try {
            if (media instanceof Anime anime)
                animeDAONeo4j.saveMediaContent(anime);
            else if (media instanceof Manga manga)
                mangaDAOMongo.saveMediaContent(manga);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
