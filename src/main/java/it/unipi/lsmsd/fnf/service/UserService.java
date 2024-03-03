package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;

public interface UserService {
    User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    RegisteredUser login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;
    void createNode(RegisteredUserDTO registeredUserDTO) throws BusinessException;
    List<RegisteredUserDTO> getFollowing(String userId) throws BusinessException;

    List<RegisteredUserDTO> getFollowers(String userId) throws BusinessException;
    /*
    List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException;
     */
}
