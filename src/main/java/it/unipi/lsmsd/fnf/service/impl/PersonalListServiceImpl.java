package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class PersonalListServiceImpl implements PersonalListService {

    private static final PersonalListDAO personalListDAO;

    static {
        personalListDAO = DAOLocator.getPersonalListDAO(DataRepositoryEnum.MONGODB);
    }

    @Override
    public String insertList(PersonalList list) throws BusinessException {
        if (list.getName() == null) {
            throw new BusinessException(BusinessExceptionType.NO_NAME, "The list must have a name.");
        }
        if (list.getUser() == null) {
            throw new BusinessException(BusinessExceptionType.NO_USER, "The list must have a user.");
        }

        try {
            PersonalListDTO dto = ModelToDtoMapper.convertToDTO(list);
            return personalListDAO.insert(dto);
        } catch(DAOException e) {
            throw new BusinessException("Error while inserting list",e);
        }
    }

    @Override
    public void updateList(String listId, String listName, String userId) throws BusinessException {
        Objects.requireNonNull(listId, "The id can't be null.");

        if (listName == null || userId == null) {
            throw new BusinessException("At least the name or the user must be defined");
        }
        try {
            personalListDAO.update(listId, listName, userId);
        } catch (DAOException e) {
            throw new BusinessException("Error while updating the list",e);
        }
    }

    @Override
    public void addToList(String listId, MediaContentDTO content) throws BusinessException {
        try {
            personalListDAO.addToList(listId, content);
        } catch (DAOException e) {
            throw new BusinessException("Error while adding to list",e);
        }
    }

    @Override
    public void removeFromList(String listId, String mediaContentId, MediaContentType type) throws BusinessException {
        try {
            personalListDAO.removeFromList(listId, mediaContentId, type);
        } catch (DAOException e) {
            throw new BusinessException("Error while removing from list",e);
        }
    }

    @Override
    public void updateItemInList(MediaContentDTO content) throws BusinessException {
        try {
            personalListDAO.updateItem(content);
        } catch (DAOException e) {
            throw new BusinessException("Error update item in the list",e);
        }
    }

    @Override
    public void removeMediaContentList(String itemId) throws BusinessException {
        try {
            personalListDAO.removeItem(itemId);
        } catch (DAOException e) {
            throw new BusinessException("Error removing media content in List",e);
        }
    }

    @Override
    public void deleteList(String listId) throws BusinessException {
        try {
            personalListDAO.delete(listId);
        } catch (DAOException e) {
            throw new BusinessException("Error while deleting the list",e);
        }
    }

    @Override
    public void deleteListsByUser(String userId) throws BusinessException {
        try {
            personalListDAO.deleteByUser(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error deleting list by user",e);
        }
    }

    @Override
    public List<PersonalList> findListsByUser(String userId, boolean reducedInfo) throws BusinessException {
        try {
            List<PersonalListDTO> listDTOs = personalListDAO.findByUser(userId, reducedInfo);
            return listDTOs.stream()
                    .map(DtoToModelMapper::personalListDTOtoPersonalList)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (DAOException e) {
            throw new BusinessException("Error finding list by user",e);
        }
    }

    @Override
    public List<PersonalList> findAllLists() throws BusinessException {
        try {
            List<PersonalListDTO> personalListDTOS = personalListDAO.findAll();
            List<PersonalList> personalLists = new ArrayList<>();
            for(PersonalListDTO personalListDTO : personalListDTOS) {
                personalLists.add(DtoToModelMapper.personalListDTOtoPersonalList(personalListDTO));
            }
            return personalLists;
        } catch (DAOException e) {
            throw new BusinessException("Error finding all the lists",e);
        }
    }

    @Override
    public PersonalList findList(String id) throws BusinessException {
        try {
            PersonalListDTO personalListDTO = personalListDAO.find(id);
            return DtoToModelMapper.personalListDTOtoPersonalList(personalListDTO);
        } catch (DAOException e) {
            throw new BusinessException("Error finding the lists",e);
        }
    }

}
