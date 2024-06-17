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
 * Asynchronous task for deleting a user from the Neo4J database.
 * Priority = 7
 * @see Task
 * @see UserDAO
 * @see User
 */
public class DeleteUserTask extends Task {
    private final UserDAO neo4jUserDAO;
    private final String id;

    /**
     * Constructs a DeleteUserTask.
     *
     * @param id        The ID of the user to delete.
     */
    public DeleteUserTask(String id) {
        super(7);
        this.neo4jUserDAO = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
        this.id = id;
    }

    /**
     * Executes the job of deleting a user from the Neo4J database.
     *
     * @throws BusinessException    if an error occurs during the deletion of the user.
     */
    @Override
    public void executeJob() throws BusinessException {
        try {
            neo4jUserDAO.deleteUser(id);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
