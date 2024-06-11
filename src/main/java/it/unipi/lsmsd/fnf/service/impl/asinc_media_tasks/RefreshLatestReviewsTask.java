package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

public class RefreshLatestReviewsTask extends Task {
    private final MediaContentDAO<Anime> animeDAO;
    private final MediaContentDAO<Manga> mangaDAO;
    private final String mediaId;
    private final String mediaType;

    public RefreshLatestReviewsTask(UserSummaryDTO userSummaryDTO, String mediaId, String mediaType) {
        super(4);
        this.animeDAO = DAOLocator.getAnimeDAO(DataRepositoryEnum.MONGODB);
        this.mangaDAO = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.mediaId = mediaId;
        this.mediaType = mediaType;
    }

    @Override
    public void executeJob() throws BusinessException {
        try{
            if (mediaType.equals("anime")) {
                animeDAO.refreshLatestReviews(mediaId, null);
            } else if (mediaType.equals("manga")) {
                mangaDAO.refreshLatestReviews(mediaId, null);
            } else {
                throw new BusinessException(BusinessExceptionType.INVALID_TYPE, "Invalid media type");
            }
        } catch (DAOException e) {
            if (e.getType() == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.NOT_FOUND, e.getMessage());
            } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }
}
