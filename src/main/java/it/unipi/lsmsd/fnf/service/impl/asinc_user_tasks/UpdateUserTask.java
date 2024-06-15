package it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

/**
 * Asynchronous task for updating user nodes in the Neo4J database.
 * Priority = 8
 * @see Task
 * @see UserDAO
 * @see User
 */
public class UpdateUserTask extends Task {
    private final UserDAO userDAONeo4j;
    private final User user;

    /**
     * Constructor for the UpdateUserTask.
     *
     * @param user      The user to update.
     */
    public UpdateUserTask(User user) {
        super(8);
        this.userDAONeo4j = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
        this.user = user;
    }

    /**
     * Executes the job of updating a user in the Neo4J database.
     *
     * @throws BusinessException    if an error occurs during the update of the user.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            userDAONeo4j.updateUser(user);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
