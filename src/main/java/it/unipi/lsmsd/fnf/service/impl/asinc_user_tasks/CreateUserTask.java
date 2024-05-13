package it.unipi.lsmsd.fnf.service.impl.asinc_user_tasks;

import it.unipi.lsmsd.fnf.dao.DAOLocator;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import static it.unipi.lsmsd.fnf.service.exception.BusinessException.handleDAOException;

public class CreateUserTask extends Task {
    private final UserDAO userDAONeo4j;
    private final UserRegistrationDTO user;

    public CreateUserTask(UserRegistrationDTO user){
        super(9);
        this.userDAONeo4j = DAOLocator.getUserDAO(DataRepositoryEnum.NEO4J);
        this.user = user;
    }

    @Override
    public void executeJob() throws BusinessException {
        try {
            userDAONeo4j.saveUser(user);

        } catch (DAOException e) {
            handleDAOException(e);
        }
    }
}
