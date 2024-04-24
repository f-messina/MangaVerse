package it.unipi.lsmsd.fnf.dao;


import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.interfaces.UserDAO;
import it.unipi.lsmsd.fnf.dao.mongo.*;
import it.unipi.lsmsd.fnf.dao.neo4j.AnimeDAONeo4JImpl;
import it.unipi.lsmsd.fnf.dao.neo4j.MangaDAONeo4JImpl;
import it.unipi.lsmsd.fnf.dao.neo4j.UserDAONeo4JImpl;
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
            return new AnimeDAOMongoImpl();
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new AnimeDAONeo4JImpl();
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
            return new MangaDAOMongoImpl();
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new MangaDAONeo4JImpl();
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
            return new UserDAOMongoImpl();
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return new UserDAONeo4JImpl();
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
            return new ReviewDAOMongoImpl();
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
}
