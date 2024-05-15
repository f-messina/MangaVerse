package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

public class DeleteMediaTask extends Task {
    private final UserDAO neo4jUserDAO;
    private final String id;

    public DeleteMediaTask(String id) {
        super(7);
        this.neo4jUserDAO = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
        this.id = id;
    }

    @Override
    public void executeJob() throws BusinessException {
        try {
            neo4jUserDAO.deleteUser(id);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}