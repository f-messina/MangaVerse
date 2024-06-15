package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;

/**
 * Asynchronous task for updating the average rating of media content.
 * It is executed periodically by the TaskExecutor.
 * Priority = 6
 * @see Task
 * @see ReviewDAO
 * @see ReviewDTO
 */
public class UpdateAverageRatingTask extends Task {
    private final ReviewDAO reviewDAO;

    /**
     * Constructs an UpdateAverageRatingTask.
     */
    public UpdateAverageRatingTask() {
        super(6);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
    }

    /**
     * Executes the task to update the average rating of media content.
     *
     * @throws BusinessException    If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            // Delete reviews with no author or media
            reviewDAO.deleteReviewsWithNoAuthor();
            reviewDAO.deleteReviewsWithNoMedia();

            // Update the average rating of media content
            reviewDAO.updateAverageRatingMedia();

        } catch (DAOException e) {
            if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }
}
