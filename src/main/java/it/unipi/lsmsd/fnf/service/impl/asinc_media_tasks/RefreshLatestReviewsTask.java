package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

/**
 * Task for refreshing the latest reviews of a media content.
 */
public class RefreshLatestReviewsTask extends Task {
    private final MediaContentDAO<Anime> animeDAO;
    private final MediaContentDAO<Manga> mangaDAO;
    private final String mediaId;
    private final String mediaType;

    /**
     * Constructs a RefreshLatestReviewsTask with the specified parameters.
     *
     * @param userSummaryDTO The user summary DTO.
     * @param mediaId        The ID of the media content to refresh the reviews.
     * @param mediaType      The type of the media content (anime or manga).
     */
    public RefreshLatestReviewsTask(UserSummaryDTO userSummaryDTO, String mediaId, String mediaType) {
        super(4);
        this.animeDAO = DAOLocator.getAnimeDAO(DataRepositoryEnum.MONGODB);
        this.mangaDAO = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.mediaId = mediaId;
        this.mediaType = mediaType;
    }

    /**
     * Executes the task to refresh the latest reviews of the specified media content.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
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
