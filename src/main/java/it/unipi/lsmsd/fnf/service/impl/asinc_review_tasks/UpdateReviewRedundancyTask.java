package it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

public class UpdateReviewRedundancyTask extends Task {

    private final ReviewDAO reviewDAO;
    private final MediaContentDTO mediaContentDTO;
    private final UserSummaryDTO userSummaryDTO;

    public UpdateReviewRedundancyTask(MediaContentDTO mediaContentDTO, UserSummaryDTO userSummaryDTO) {
        super(5);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
        this.mediaContentDTO = mediaContentDTO;
        this.userSummaryDTO = userSummaryDTO;
    }

    @Override
    public void executeJob() throws BusinessException {
        try{
            if (userSummaryDTO != null)
                reviewDAO.updateUserRedundancy(userSummaryDTO);
            if (mediaContentDTO != null)
                reviewDAO.updateMediaRedundancy(mediaContentDTO);

        } catch (DAOException e) {
           if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, "Error while updating review redundancy: " + e.getMessage());
           } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error while updating review redundancy: " + e.getMessage());
           }
        }
    }
}
