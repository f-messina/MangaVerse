package it.unipi.lsmsd.fnf.service.impl.asinc_review_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

/**
 * Task for removing reviews associated with deleted media content.
 */
import java.util.List;

public class RemoveDeletedMediaReviewsTask extends Task {
    private final ReviewDAO reviewDAO;
    private final List<String> reviewsIds;

    /**
     * Constructs a RemoveDeletedMediaReviewsTask.
     *
     * @param mediaId The ID of the deleted media content.
     */
    public RemoveDeletedMediaReviewsTask(String mediaId) {
    public RemoveDeletedMediaReviewsTask(List<String> reviewsIds) {
        super(4);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
        this.reviewsIds = reviewsIds;
    }

    /**
     * Executes the task to remove reviews associated with the deleted media content.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            reviewDAO.deleteReviews(reviewsIds, "media");

        } catch (DAOException e) {
            if(e.getType().equals(DAOExceptionType.DATABASE_ERROR)){
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            } else {
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
            }
        }
    }
}
