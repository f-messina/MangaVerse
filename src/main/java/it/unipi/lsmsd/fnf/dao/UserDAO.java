package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.List;

public interface UserDAO {
    String register(User user) throws DAOException;
    void remove(String id) throws DAOException;
    RegisteredUser authenticate(String email, String password) throws DAOException;
    RegisteredUser find(String id) throws DAOException;
    List<RegisteredUserDTO> search(String username) throws DAOException;
    List<RegisteredUserDTO> findAll() throws DAOException;
    void update(User user) throws DAOException;
}