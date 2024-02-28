package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;
import static it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper.userRegistrationDTOToUser;

public class UserServiceImpl implements UserService {

    private static final UserDAO userDAO;
    private static final PersonalListDAO personalListDAO;
    private static final ReviewDAO reviewDAO;

    static {
        userDAO = getUserDAO(DataRepositoryEnum.MONGODB);
        personalListDAO = getPersonalListDAO(DataRepositoryEnum.MONGODB);
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
    }

    @Override
    public User registerUserAndLogin(UserRegistrationDTO userRegistrationDTO) throws BusinessException {
        try {
            // Validation checks for empty fields
            if (StringUtils.isAnyEmpty(
                    userRegistrationDTO.getUsername(),
                    userRegistrationDTO.getPassword(),
                    userRegistrationDTO.getEmail()
            )) {
                throw new BusinessException(BusinessExceptionType.EMPTY_USERNAME_PSW_EMAIL,"Username, password and email cannot be empty");
            }

            User user = userRegistrationDTOToUser(userRegistrationDTO);
            user.setId(userDAO.register(user));
            return user;

        } catch (DAOException e) {
            DAOExceptionType type = e.getType();

            if (DAOExceptionType.TAKEN_EMAIL.equals(type)) {
                throw new BusinessException(BusinessExceptionType.TAKEN_EMAIL,"Email is already taken");
            }
            else if(DAOExceptionType.TAKEN_USERNAME.equals(type)) {
                throw new BusinessException(BusinessExceptionType.TAKEN_USERNAME,"Username is taken");
            }else {
                throw new BusinessException("DAOException during registration operation", e);
            }

        } catch (Exception e) {
            throw new BusinessException("Error registering user", e);
        }
    }

    @Override
    public RegisteredUser login(String email, String password) throws BusinessException {
        // Validation checks for empty fields
        if (StringUtils.isEmpty(email))
            throw new BusinessException(BusinessExceptionType.EMPTY_EMAIL,"Email cannot be empty");
        if (StringUtils.isEmpty(password))
            throw new BusinessException(BusinessExceptionType.EMPTY_PSW,"Password cannot be empty");

        try {
            RegisteredUser registeredUser = userDAO.authenticate(email, password);
            if (registeredUser instanceof User user) {
                user.setLists(personalListDAO.findByUser(user.getId())
                        .stream()
                        .map(DtoToModelMapper::personalListDTOtoPersonalList)
                        .collect(Collectors.toCollection(ArrayList::new)));
                user.setReviews(reviewDAO.findByUser(user.getId())
                        .stream()
                        .map(DtoToModelMapper::reviewDTOtoReview)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
            return registeredUser;

        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            switch (type) {
                case DAOExceptionType.WRONG_EMAIL:
                    throw new BusinessException(BusinessExceptionType.INVALID_EMAIL,"Invalid email");
                case DAOExceptionType.WRONG_PSW:
                    throw new BusinessException(BusinessExceptionType.WRONG_PSW,"Wrong password");
                default:
                    throw new BusinessException("DAOException during authenticating operation", e);
            }
        } catch (Exception e) {
            throw new BusinessException("Error authenticating user", e);
        }
    }


    @Override
    public void updateUserInfo(User user) throws BusinessException {
        try {
            userDAO.update(user);
        } catch (DAOException e) {
            DAOExceptionType type = e.getType();
            if (DAOExceptionType.TAKEN_USERNAME.equals(type)) {
                throw new BusinessException(BusinessExceptionType.TAKEN_USERNAME,"Username already in use");
            } else {
                throw new BusinessException(e);
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }
}
