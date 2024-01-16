package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.ExceptionDAO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.List;

public interface UserDAO {

    //add user
    //remove user
    //search user by username
    //update user info
    //
    User addUser(User user) throws ExceptionDAO;
    void removeUser(String username) throws ExceptionDAO;
    RegisteredUser searchUserByUsername(String username) throws ExceptionDAO;
    void updateUserInfo(RegisteredUser user) throws ExceptionDAO;

}
