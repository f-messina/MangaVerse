package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;

public interface UserService {
    User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    RegisteredUser login(String username, String password) throws BusinessException;


    void createUserNode(String id, String username, String picture) throws BusinessException;

    void followUser(String followerUserId, String followingUserId) throws BusinessException;

    void unfollowUser(String followerUserId, String followingUserId) throws BusinessException;

    List<RegisteredUserDTO> getFollowing(String userId) throws BusinessException;

    List<RegisteredUserDTO> getFollowers(String userId) throws BusinessException;

    List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException;
}
