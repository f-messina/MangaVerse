package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

public class UpdateMediaRedundancyTask extends Task {

    private final MediaContentDAO<Anime> animeDAO;
    private final MediaContentDAO<Manga> mangaDAO;
    private final UserSummaryDTO userSummaryDTO;

    public UpdateMediaRedundancyTask(UserSummaryDTO userSummaryDTO) {
        super(5);
        this.animeDAO = DAOLocator.getAnimeDAO(DataRepositoryEnum.MONGODB);
        this.mangaDAO = DAOLocator.getMangaDAO(DataRepositoryEnum.MONGODB);
        this.userSummaryDTO = userSummaryDTO;
    }

    @Override
    public void executeJob() throws BusinessException {
        try{
            animeDAO.updateUserRedundancy(userSummaryDTO);
            mangaDAO.updateUserRedundancy(userSummaryDTO);

        } catch (DAOException e) {
           if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, "Error while updating review redundancy: " + e.getMessage());
           } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error while updating review redundancy: " + e.getMessage());
           }
        }
    }
}
