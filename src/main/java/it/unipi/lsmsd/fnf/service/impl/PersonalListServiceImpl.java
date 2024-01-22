package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.*;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.mapper.ModelToDtoMapper;
import it.unipi.lsmsd.fnf.service.mapper.DtoToModelMapper;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class PersonalListServiceImpl implements PersonalListService {

    private static final PersonalListDAO personalListDAO;




    static {
        personalListDAO = Objects.requireNonNull(DAOLocator.getPersonalListDAO(DataRepositoryEnum.MONGODB));
    }

    @Override
    public void insertList(PersonalList list) throws BusinessException {
        if (list == null) {
            throw new BusinessException(("The list can't be null."));
        }
        try {
            PersonalListDTO dto = ModelToDtoMapper.convertToDTO(list);
            personalListDAO.insert((dto));
        } catch(DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void updateList(String id, String name, User user) throws BusinessException {
        Objects.requireNonNull(id, "The id can't be null.");
        Objects.requireNonNull(name, "The name can't be null.");
        Objects.requireNonNull(user, "The user can't be null");
        try {
            ObjectId objectId = new ObjectId(id);
            List<PersonalListDTO> listDTOs = personalListDAO.findByUserId(objectId);
            for (PersonalListDTO listDTO : listDTOs) {
                listDTO.setName(name);
                RegisteredUserDTO userDTO = ModelToDtoMapper.convertToRegisteredUserDTO(user);
                listDTO.setUser(userDTO);
                personalListDAO.update(listDTO);
            }


        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void addToList(String listId, MediaContent content) throws BusinessException {
        try {
            ObjectId objectId = new ObjectId(listId);
            MediaContentDTO dto = ModelToDtoMapper.convertToDTO(content);
            personalListDAO.addToList(objectId, dto);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void removeFromList(String listId, MediaContent content) throws BusinessException {
        try {
            ObjectId listObjectId = new ObjectId(listId);
            ObjectId contentObjectId = new ObjectId(String.valueOf(content.getId()));
            MediaContentType contentType = MediaContentType.valueOf(content.getType().toUpperCase());
            personalListDAO.removeFromList(listObjectId, contentObjectId, contentType);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void updateItemInList(String id, MediaContent content) throws BusinessException {
        try {
            MediaContentDTO dto = ModelToDtoMapper.convertToDTO(content);
            personalListDAO.updateItem(dto);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void removeMediaContentList(String itemId) throws BusinessException {
        try {
            ObjectId itemObjectId = new ObjectId(itemId);
            personalListDAO.removeItem(itemObjectId);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void deleteList(String id) throws BusinessException {
        try {
            personalListDAO.delete(new ObjectId(id));
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public void deleteListsByUser(String userId) throws BusinessException {
        try {
            personalListDAO.deleteByUser(new ObjectId(userId));
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public List<PersonalList> findListsByUser(String userId) throws BusinessException {
        try {
            ObjectId userObjectId = new ObjectId(userId);
            List<PersonalListDTO> listDTOs = personalListDAO.findByUserId(userObjectId);
            List<PersonalList> lists = listDTOs.stream().map(DtoToModelMapper::convertFromDTO).collect(Collectors.toList());
            return lists;
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

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
            throw new BusinessException(e);
        }
    }

    @Override
    public PersonalList findList(ObjectId id) throws BusinessException {
        try {
            PersonalListDTO personalListDTO = personalListDAO.find(id);
            return ModelToDtoMapper.convertToPersonalList(personalListDTO);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

}
