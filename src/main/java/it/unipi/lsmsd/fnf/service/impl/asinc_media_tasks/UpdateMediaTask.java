package it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

public class UpdateMediaTask extends Task {
    private final UserDAO userDAONeo4j;
    private final User user;

    public UpdateMediaTask(User user) {
        super(8);
        this.userDAONeo4j = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
        this.user = user;
    }

    @Override
    public void executeJob() throws BusinessException {
        try {
            userDAONeo4j.updateUser(user);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}