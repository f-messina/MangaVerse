package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface MediaContentDAO<T extends MediaContent> {
    void insert(T mediaContent) throws DAOException;
    void update(T mediaContent) throws DAOException;
    T find(ObjectId id) throws DAOException;
    void delete(ObjectId id) throws DAOException;
    List<? extends MediaContentDTO> search(String title) throws DAOException;
    List<? extends MediaContentDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy) throws DAOException;
}
