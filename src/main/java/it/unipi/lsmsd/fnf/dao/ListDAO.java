package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import org.bson.types.ObjectId;

import java.util.List;

 public interface ListDAO {
     List<PersonalListDTO> findByUserId(ObjectId userId) throws DAOException;
     List<PersonalListDTO> findAll() throws DAOException;
     PersonalListDTO find(ObjectId id) throws DAOException;
     void insert(PersonalListDTO list) throws DAOException;
     void changeName(ObjectId ID, String name) throws DAOException;
     void delete(ObjectId id) throws DAOException;
     void deleteByUser(ObjectId userId) throws DAOException;
     void addAnime(ObjectId listId, AnimeDTO anime) throws DAOException;
     void addManga(ObjectId listId, MangaDTO manga) throws DAOException;
     void removeAnime(ObjectId listId, ObjectId animeId) throws DAOException;
     void removeManga(ObjectId listId, ObjectId mangaId) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) throws DAOException;
}
