package it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.List;

/**
 * Asynchronous task for updating the user or media redundancy of a review in the database.
 * Priority = 5
 * @see Task
 * @see ReviewDAO
 * @see MediaContentDTO
 * @see UserSummaryDTO
 * @see ReviewDTO
 */
public class UpdateReviewRedundancyTask extends Task {

    private final ReviewDAO reviewDAO;
    private final MediaContentDTO mediaContentDTO;
    private final UserSummaryDTO userSummaryDTO;
    private final List<String> reviewIds;

    /**
     * Constructs an UpdateReviewRedundancyTask.
     *
     * @param mediaContentDTO   The DTO representing the media content.
     * @param userSummaryDTO    The DTO representing the user summary.
     * @param reviewIds         The list of review IDs.
     */
    public UpdateReviewRedundancyTask(MediaContentDTO mediaContentDTO, UserSummaryDTO userSummaryDTO, List<String> reviewIds) {
        super(5);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
        this.mediaContentDTO = mediaContentDTO;
        this.userSummaryDTO = userSummaryDTO;
        this.reviewIds = reviewIds;
    }

    /**
     * Executes the task to update review redundancy.
     *
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try{
            // Update the user or media redundancy of the reviews
            if (userSummaryDTO != null)
                reviewDAO.updateUserRedundancy(userSummaryDTO, reviewIds);
            if (mediaContentDTO != null)
                reviewDAO.updateMediaRedundancy(mediaContentDTO, reviewIds);

        } catch (DAOException e) {
           if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, "Error while updating review redundancy: " + e.getMessage());
           } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, "Error while updating review redundancy: " + e.getMessage());
           }
        }
    }
}
