package it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.Objects;

/**
 * Task for updating the number of followers for a specific user.
 */
public class UpdateNumberOfFollowersTask extends Task {
    private final UserDAO mongoDbUserDAO;
    private final UserDAO neo4jUserDAO;
    private final String userId;

    /**
     * Constructs an UpdateNumberOfFollowersTask.
     *
     * @param userId The ID of the user whose number of followers needs to be updated.
     */
    public UpdateNumberOfFollowersTask(String userId) {
        super(5);
        this.userId = userId;
        this.mongoDbUserDAO = DAOLocator.getUserDAO(DataRepositoryEnum.MONGODB);
        this.neo4jUserDAO = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
    }

    /**
     * Executes the task to update the number of followers for the specified user.
     *
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void executeJob() throws BusinessException {
        try{
            // Get the number of followers
            Integer followed = neo4jUserDAO.getNumOfFollowers(userId);

            // Update the number of followers
            mongoDbUserDAO.updateNumOfFollowers(userId, followed);

        } catch (DAOException e) {
            if (Objects.requireNonNull(e.getType()) == DAOExceptionType.DATABASE_ERROR) {
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            }
            throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
