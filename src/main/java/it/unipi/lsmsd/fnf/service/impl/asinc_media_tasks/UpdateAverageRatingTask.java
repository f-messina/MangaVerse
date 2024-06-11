package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

/**
 * Task for updating the average rating of media content based on reviews.
 */
public class UpdateAverageRatingTask extends Task {
    private final ReviewDAO reviewDAO;

    /**
     * Constructs an UpdateAverageRatingTask.
     */
    public UpdateAverageRatingTask() {
        super(5);
        this.reviewDAO = DAOLocator.getReviewDAO(DataRepositoryEnum.MONGODB);
    }

    /**
     * Executes the task to update the average rating of media content.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            reviewDAO.deleteReviewsWithNoAuthor();
            reviewDAO.deleteReviewsWithNoMedia();
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
