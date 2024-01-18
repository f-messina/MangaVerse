package it.unipi.lsmsd.fnf.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface AnimeDAO {

    public void insert(Anime anime) throws DAOException;
    public void update(Anime anime) throws DAOException;
    public List<Anime> search(String title) throws DAOException;
    public List<Anime> search(Map<String, Object> filters, Map<String, Integer> orderBy) throws DAOException;
    public void remove(String animeId) throws DAOException;
}
