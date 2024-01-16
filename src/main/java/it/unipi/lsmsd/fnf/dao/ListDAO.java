package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import org.bson.types.ObjectId;

import java.util.List;

public interface ListDAO {
    public List<PersonalList> findByUserId(ObjectId userId);
    public List<PersonalList> findAll();
    public PersonalList find(ObjectId id);

    public void insert(PersonalListDTO list);
    public void insert(List<PersonalListDTO> lists);
    public void update(PersonalListDTO list);
    public void delete(ObjectId id);
    public void deleteByUser(ObjectId userId);
    public void insertAnime(ObjectId listId, AnimeDTO anime);
    public void insertManga(ObjectId listId, MangaDTO manga);
    public void removeAnime(ObjectId listId, ObjectId animeId);
    public void removeManga(ObjectId listId, ObjectId mangaId);
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria);
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value);
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria);
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value);
}
