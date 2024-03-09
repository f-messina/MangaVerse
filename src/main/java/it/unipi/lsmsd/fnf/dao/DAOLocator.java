package it.unipi.lsmsd.fnf.dao;


import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.mongo.*;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
/**
 * This class provides a locator for various Data Access Objects (DAOs) based on the specified data repository.
 */
public class DAOLocator {

    /**
     * Retrieves the appropriate DAO for handling Anime-related operations based on the data repository.
     *
     * @param dataRepositoryEnum The enum representing the data repository (MongoDB or Neo4j).
     * @return The MediaContentDAO for Anime operations based on the specified data repository.
     * @throws UnsupportedOperationException If the specified data repository is not supported.
     */
    public static MediaContentDAO<Anime> getAnimeDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new it.unipi.lsmsd.fnf.dao.mongo.AnimeDAOImpl();
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new it.unipi.lsmsd.fnf.dao.neo4j.AnimeDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the appropriate DAO for handling Manga-related operations based on the data repository.
     *
     * @param dataRepositoryEnum The enum representing the data repository (MongoDB or Neo4j).
     * @return The MediaContentDAO for Manga operations based on the specified data repository.
     * @throws UnsupportedOperationException If the specified data repository is not supported.
     */
    public static MediaContentDAO<Manga> getMangaDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new it.unipi.lsmsd.fnf.dao.mongo.MangaDAOImpl();
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new it.unipi.lsmsd.fnf.dao.neo4j.MangaDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the DAO for handling personal lists based on the data repository.
     *
     * @param dataRepositoryEnum The enum representing the data repository (MongoDB).
     * @return The PersonalListDAO for personal list operations based on the specified data repository.
     * @throws UnsupportedOperationException If the specified data repository is not supported.
     */
    public static PersonalListDAO getPersonalListDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new PersonalListDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the appropriate DAO for handling User-related operations based on the data repository.
     *
     * @param dataRepositoryEnum The enum representing the data repository (MongoDB or Neo4j).
     * @return The UserDAO for User operations based on the specified data repository.
     * @throws UnsupportedOperationException If the specified data repository is not supported.
     */
    public static UserDAO getUserDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new it.unipi.lsmsd.fnf.dao.mongo.UserDAOImpl();
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new it.unipi.lsmsd.fnf.dao.neo4j.UserDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the DAO for handling Review-related operations based on the data repository.
     *
     * @param dataRepositoryEnum The enum representing the data repository (MongoDB).
     * @return The ReviewDAO for Review operations based on the specified data repository.
     * @throws UnsupportedOperationException If the specified data repository is not supported.
     */
    public static ReviewDAO getReviewDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return new ReviewDAOImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
}
