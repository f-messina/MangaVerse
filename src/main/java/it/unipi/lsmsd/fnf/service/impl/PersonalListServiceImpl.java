package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
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

/**
 * The PersonalListServiceImpl class provides implementation for the PersonalListService interface.
 * It handles CRUD operations and other functionalities related to personal lists.
 */
public class PersonalListServiceImpl implements PersonalListService {

    private static final PersonalListDAO personalListDAO;

    static {
        personalListDAO = DAOLocator.getPersonalListDAO(DataRepositoryEnum.MONGODB);
    }

    /**
     * Inserts a new personal list into the data repository.
     * @param list The personal list to be inserted.
     * @return The ID of the inserted list.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Updates an existing personal list in the data repository.
     * @param listId The ID of the list to be updated.
     * @param listName The new name for the list.
     * @param user The user associated with the list.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void updateList(String listId, String listName, User user) throws BusinessException {
        Objects.requireNonNull(listId, "The id can't be null.");

        if (listName == null && user == null) {
            throw new BusinessException("At least the name or the user must be defined");
        }
        try {
            List<PersonalListDTO> listDTOs = personalListDAO.findByUser(listId, false);
            for (PersonalListDTO listDTO : listDTOs) {
                listDTO.setName(listName);
                RegisteredUserDTO userDTO = ModelToDtoMapper.convertToRegisteredUserDTO(user);
                listDTO.setUser(userDTO);
                personalListDAO.update(listDTO);
            }
        } catch (DAOException e) {
            throw new BusinessException("Error while updating the list",e);
        }
    }

    /**
     * Adds a media content to a personal list.
     * @param listId The ID of the list to which the content is to be added.
     * @param content The media content to be added.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void addToList(String listId, MediaContentDTO content) throws BusinessException {
        try {
            personalListDAO.addToList(listId, content);
        } catch (DAOException e) {
            throw new BusinessException("Error while adding to list",e);
        }
    }

    /**
     * Removes a media content from a personal list.
     * @param listId The ID of the list from which the content is to be removed.
     * @param mediaContentId The ID of the media content to be removed.
     * @param type The type of media content (Anime or Manga).
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void removeFromList(String listId, String mediaContentId, MediaContentType type) throws BusinessException {
        try {
            personalListDAO.removeFromList(listId, mediaContentId, type);
        } catch (DAOException e) {
            throw new BusinessException("Error while removing from list",e);
        }
    }

    /**
     * Updates a media content item in a personal list.
     * @param content The media content to be updated.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void updateItemInList(MediaContentDTO content) throws BusinessException {
        try {
            personalListDAO.updateItem(content);
        } catch (DAOException e) {
            throw new BusinessException("Error update item in the list",e);
        }
    }

    /**
     * Removes a media content from a personal list.
     * @param itemId The ID of the media content item to be removed.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void removeMediaContentList(String itemId) throws BusinessException {
        try {
            personalListDAO.removeItem(itemId);
        } catch (DAOException e) {
            throw new BusinessException("Error removing media content in List",e);
        }
    }


    /**
     * Deletes a personal list from the data repository.
     * @param listId The ID of the list to be deleted.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteList(String listId) throws BusinessException {
        try {
            personalListDAO.delete(listId);
        } catch (DAOException e) {
            throw new BusinessException("Error while deleting the list",e);
        }
    }

    /**
     * Deletes all personal lists associated with a user.
     * @param userId The ID of the user whose lists are to be deleted.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public void deleteListsByUser(String userId) throws BusinessException {
        try {
            personalListDAO.deleteByUser(userId);
        } catch (DAOException e) {
            throw new BusinessException("Error deleting list by user",e);
        }
    }


    /**
     * Finds all personal lists associated with a user.
     * @param userId The ID of the user.
     * @param reducedInfo Flag indicating whether to fetch reduced information for the lists.
     * @return A list of personal lists.
     * @throws BusinessException If an error occurs during the operation.
     */
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

    /**
     * Finds all personal lists in the data repository.
     * @return A list of personal lists.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public List<PersonalList> findAllLists() throws BusinessException {
        try {
            List<PersonalListDTO> personalListDTOS = personalListDAO.findAll();
            List<PersonalList> personalLists = new ArrayList<>();
            for(PersonalListDTO personalListDTO : personalListDTOS) {
                personalLists.add(ModelToDtoMapper.convertToPersonalList(personalListDTO));
            }
            return personalLists;
        } catch (DAOException e) {
            throw new BusinessException("Error finding all the lists",e);
        }
    }

    /**
     * Finds a personal list by its ID.
     * @param id The ID of the list to find.
     * @return The personal list.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public PersonalList findList(String id) throws BusinessException {
        try {
            PersonalListDTO personalListDTO = personalListDAO.find(id);
            return ModelToDtoMapper.convertToPersonalList(personalListDTO);
        } catch (DAOException e) {
            throw new BusinessException("Error finding the lists",e);
        }
    }

}
