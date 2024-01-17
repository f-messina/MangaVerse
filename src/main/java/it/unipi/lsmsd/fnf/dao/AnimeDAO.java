package it.unipi.lsmsd.fnf.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public interface AnimeDAO {

    public void insert(Anime anime) throws DAOException;
    public void update(Anime anime) throws DAOException;
    public Anime find(ObjectId id) throws DAOException;
    public Anime searchByTitle(String title) throws DAOException;
    public List<Anime> searchByYear(int year) throws DAOException;
    public List<Anime> searchByTags(List<String> tags) throws DAOException;
    public void remove(String animeId) throws DAOException;
}
