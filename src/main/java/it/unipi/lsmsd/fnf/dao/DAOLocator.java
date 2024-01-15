package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.mongo.*;

public class DAOLocator {
    public static AnimeDAO getAnimeDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new AnimeDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static MangaDAO getMangaDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new MangaDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
    public static ListDAO getListDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new ListDAOImpl();
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
}
