package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.mongo.*;
import it.unipi.lsmsd.fnf.dao.neo4j.AnimeDAONeo4JImpl;
import it.unipi.lsmsd.fnf.dao.neo4j.MangaDAONeo4JImpl;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

public class DAOLocator {
    public static MediaContentDAO<Anime> getAnimeDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new AnimeDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static MediaContentDAO<Manga> getMangaDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new MangaDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static PersonalListDAO getPersonalListDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new PersonalListDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static UserDAO getUserDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new UserDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static ReviewDAO getReviewDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new ReviewDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static MediaContentDAO<Anime> getAnimeNeo4JDAO(DataRepositoryEnum dataRepositoryEnum) {
        if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new AnimeDAONeo4JImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static MediaContentDAO<Manga> getMangaNeo4JDAO(DataRepositoryEnum dataRepositoryEnum) {
        if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new MangaDAONeo4JImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    public static UserDAO getUserDAONeo4J(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)){
            return new UserDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
}
