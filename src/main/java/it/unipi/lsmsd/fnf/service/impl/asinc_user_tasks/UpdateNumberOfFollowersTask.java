package it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.Objects;

/**
 * Asynchronous task for updating the number of followers for a user.
 * Priority = 6
 * @see Task
 * @see UserDAO
 * @see User
 */
public class UpdateNumberOfFollowersTask extends Task {
    private final UserDAO mongoDbUserDAO;
    private final String userId;
    private final int increment;

    /**
     * Constructs an UpdateNumberOfFollowersTask.
     *
     * @param userId            The id of the user to update.
     * @param increment         The increment to apply to the number of followers.
     */
    public UpdateNumberOfFollowersTask(String userId, int increment) {
        super(6);
        this.mongoDbUserDAO = DAOLocator.getUserDAO(DataRepositoryEnum.MONGODB);
        this.userId = userId;
        this.increment = increment;
    }

    /**
     * Executes the job of updating the number of followers for a user.
     *
     * @throws BusinessException    if an error occurs during the update of the number of followers.
     */
    @Override
    public void executeJob() throws BusinessException {
        try{
            // Update the number of followers
            mongoDbUserDAO.updateNumOfFollowers(userId, increment);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
