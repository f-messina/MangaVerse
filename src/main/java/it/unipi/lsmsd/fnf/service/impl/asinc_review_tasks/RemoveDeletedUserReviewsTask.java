package it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.List;

/**
 * Asynchronous task for removing reviews written by a deleted user from the database
 * and refreshing the latest reviews where the reviews appear.
 * Priority = 4
 * @see Task
 * @see ReviewDAO
 * @see ReviewDTO
 */
public class RemoveDeletedUserReviewsTask extends Task {
    private final ReviewDAO reviewDAO;
    private final List<String> reviewsIds;

    /**
     * Constructs a RemoveDeletedUserReviewsTask.
     *
     * @param reviewsIds        The reviewIds of the deleted user.
     */
    public RemoveDeletedUserReviewsTask(List<String> reviewsIds) {
        super(4);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
        this.reviewsIds = reviewsIds;
    }

    /**
     * Executes the task to remove reviews written by the deleted user and refreshes latest reviews.
     *
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            // Delete the reviews written by the deleted user
            reviewDAO.deleteReviews(reviewsIds, "user");

            // Refresh the latest reviews where the reviews appear
            reviewDAO.refreshLatestReviewsOnUserDeletion(reviewsIds);

        } catch (DAOException e) {
            if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }
}
