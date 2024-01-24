package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.UserDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;

import java.util.List;

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
            if (userRegistrationDTO.getUsername() == null || userRegistrationDTO.getUsername().isEmpty())
                throw new BusinessException("Username cannot be empty");
            if (userRegistrationDTO.getPassword() == null || userRegistrationDTO.getPassword().isEmpty())
                throw new BusinessException("Password cannot be empty");
            if (userRegistrationDTO.getEmail() == null || userRegistrationDTO.getEmail().isEmpty())
                throw new BusinessException("Email cannot be empty");
            User user = userRegistrationDTOToUser(userRegistrationDTO);
            user.setId(userDAO.register(user));
            return user;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public RegisteredUser login(String username, String password) throws BusinessException {
        try {
            RegisteredUser registeredUser = userDAO.authenticate(username, password);
            if (registeredUser instanceof User user) {
                List<PersonalListDTO> personalLists = personalListDAO.findByUser(user.getId());
                user.setLists(personalLists.stream().map(DtoToModelMapper::personalListDTOtoPersonalList).toList());
                List<ReviewDTO> reviews = reviewDAO.findByUser(user.getId());
                user.setReviews(reviews.stream().map(DtoToModelMapper::reviewDTOtoReview).toList());
            }

            return registeredUser;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }
}
