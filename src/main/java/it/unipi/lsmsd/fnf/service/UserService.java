package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

public interface UserService {
    User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    RegisteredUser login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
}
