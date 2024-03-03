package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import org.bson.types.ObjectId;

import java.util.List;

 public interface PersonalListDAO {
     List<PersonalListDTO> findByUser(ObjectId userId) throws DAOException;
     List<PersonalListDTO> findAll() throws DAOException;
     PersonalListDTO find(ObjectId id) throws DAOException;
     ObjectId insert(PersonalListDTO list) throws DAOException;
     void update(PersonalListDTO list) throws DAOException;
     void delete(ObjectId id) throws DAOException;
     void deleteByUser(ObjectId userId) throws DAOException;
     void addToList(ObjectId listId, MediaContentDTO anime) throws DAOException;
     void removeFromList(ObjectId listId, ObjectId animeId, MediaContentType type) throws DAOException;
     void updateItem(MediaContentDTO anime) throws DAOException;
     void removeItem(ObjectId animeId) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) throws DAOException;

     //MongoDB queries
     //Find tha anime most present in all of the lists
     List<AnimeDTO> popularAnime() throws DAOException;

     //Find tha anime most present in all of the lists
     List<MangaDTO> popularManga() throws DAOException;
 }
