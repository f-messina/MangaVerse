package it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

/**
 * Task for removing reviews written by a deleted user.
 */
public class RemoveDeletedUserReviewsTask extends Task {
    private final ReviewDAO reviewDAO;
    private final String userId;

    /**
     * Constructs a RemoveDeletedUserReviewsTask.
     *
     * @param userId The ID of the deleted user.
     */
    public RemoveDeletedUserReviewsTask(String userId) {
        super(5);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
        this.userId = userId;
    }

    /**
     * Executes the task to remove reviews written by the deleted user and refreshes latest reviews.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            reviewDAO.deleteReviewsByAuthor(userId);
            reviewDAO.refreshLatestReviewsOnUserDeletion(userId);

        } catch (DAOException e) {
            if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }
}
