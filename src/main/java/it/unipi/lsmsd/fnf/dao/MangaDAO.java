package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface MangaDAO {
    void insert(Manga manga) throws DAOException;
    void update(Manga manga) throws DAOException;
    Manga find(ObjectId id) throws DAOException;
    List<MangaDTO> search(String title) throws DAOException;
    List<MangaDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy) throws DAOException;
    void remove(ObjectId mangaId) throws DAOException;
}
