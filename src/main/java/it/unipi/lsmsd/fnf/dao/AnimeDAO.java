package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface AnimeDAO {
    void insert(Anime anime) throws DAOException;
    void update(Anime anime) throws DAOException;
    Anime find(ObjectId id) throws DAOException;
    List<AnimeDTO> search(String title) throws DAOException;
    List<AnimeDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy) throws DAOException;
    void remove(ObjectId animeId) throws DAOException;
}
