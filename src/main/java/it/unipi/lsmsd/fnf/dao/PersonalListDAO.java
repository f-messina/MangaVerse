package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

public interface PersonalListDAO {

    void insertList(PersonalListSummaryDTO listSummaryDTO) throws DAOException;

    void updateList(PersonalListSummaryDTO listSummaryDTO) throws DAOException;

    void deleteList(String userId, String listId) throws DAOException;

    void addToList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException;

    void removeFromList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException;

    void removeElementInListWithoutMedia() throws DAOException;
}
