package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;

public interface PersonalListService {
    //Insert list
    void insertList(String userId, String name) throws BusinessException;

    //Update list with id, name and user
    void updateList(String listId, String listName, String userId) throws BusinessException;

    void addToList(String listId, MediaContentDTO content) throws BusinessException;

    //Remove list removing anime or manga from list
    void removeFromList(String listId, String mediaContentId, MediaContentType type) throws BusinessException;

    //Update list with name and picture
    void updateItemInList(MediaContentDTO content) throws BusinessException;

    //Remove anime or manga list
    void removeMediaContentList(String itemId) throws BusinessException;

    //Delete the entire list
    void deleteList(String id) throws BusinessException;

    //Delete lists by user
    void deleteListsByUser(String userId) throws BusinessException;

    //Find lists by user ID
    List<PersonalList> findListsByUser(String userId, boolean reducedInfo) throws BusinessException;

    //Find all lists
    List<PersonalList> findAllLists() throws BusinessException;

    PersonalList findList(String id) throws BusinessException;
}
