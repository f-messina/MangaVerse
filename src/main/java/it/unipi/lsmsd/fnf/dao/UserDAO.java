package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserDAO {
    void insert(User user) throws DAOException;
    void remove(ObjectId id) throws DAOException;
    RegisteredUser find(ObjectId id) throws DAOException;
    RegisteredUser find(String username) throws DAOException;
    List<RegisteredUserDTO> findAll() throws DAOException;
    void update(RegisteredUser user) throws DAOException;
}