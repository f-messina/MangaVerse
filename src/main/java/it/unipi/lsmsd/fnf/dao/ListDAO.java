package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import org.bson.types.ObjectId;

import java.util.List;

public interface ListDAO {
    public List<PersonalList> findByUserId(ObjectId userId) throws DAOException;
    public List<PersonalList> findAll() throws DAOException;
    public PersonalList find(ObjectId id) throws DAOException;
    public void insert(PersonalListDTO list) throws DAOException;
    public void insert(List<PersonalListDTO> lists) throws DAOException;
    public void update(PersonalListDTO list) throws DAOException;
    public void delete(ObjectId id) throws DAOException;
    public void deleteByUser(ObjectId userId) throws DAOException;
    public void insertAnime(ObjectId listId, AnimeDTO anime) throws DAOException;
    public void insertManga(ObjectId listId, MangaDTO manga) throws DAOException;
    public void removeAnime(ObjectId listId, ObjectId animeId) throws DAOException;
    public void removeManga(ObjectId listId, ObjectId mangaId) throws DAOException;
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) throws DAOException;
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) throws DAOException;
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) throws DAOException;
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) throws DAOException;
}
