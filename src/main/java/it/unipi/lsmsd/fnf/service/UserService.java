package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;

public interface UserService {
    void registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException;
    RegisteredUser login(String email, String password) throws BusinessException;
    void updateUserInfo(User user) throws BusinessException;
    void follow(String followerUserId, String followingUserId) throws BusinessException;
    void unfollow(String followerUserId, String followingUserId) throws BusinessException;
    void createNode(UserSummaryDTO userSummaryDTO) throws BusinessException;
    List<UserSummaryDTO> getFollowing(String userId) throws BusinessException;

    List<UserSummaryDTO> getFollowers(String userId) throws BusinessException;
    /*
    List<RegisteredUserDTO> suggestUsers(String userId) throws BusinessException;
     */
}
