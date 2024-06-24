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
 * It is a singleton class that provides the DAOs to the application.
 * The class provides methods to retrieve the appropriate DAO for handling Anime, Manga, User, and Review operations.
 * The methods check also if the specified data repository is supported (MongoDB or Neo4j).
 * @see MediaContentDAO
 * @see UserDAO
 * @see ReviewDAO
 */
public class DAOLocator {
    private static final AnimeDAOMongoImpl animeDAOMongo = new AnimeDAOMongoImpl();
    private static final MangaDAOMongoImpl mangaDAOMongo = new MangaDAOMongoImpl();
    private static final UserDAOMongoImpl userDAOMongo = new UserDAOMongoImpl();
    private static final ReviewDAOMongoImpl reviewDAOMongo = new ReviewDAOMongoImpl();
    private static final AnimeDAONeo4JImpl animeDAONeo4J = new AnimeDAONeo4JImpl();
    private static final MangaDAONeo4JImpl mangaDAONeo4J = new MangaDAONeo4JImpl();
    private static final UserDAONeo4JImpl userDAONeo4J = new UserDAONeo4JImpl();

    /**
     * Retrieves the appropriate DAO for handling Anime-related operations based on the data repository.
     *
     * @param dataRepositoryEnum                The enum representing the data repository (MongoDB or Neo4j).
     * @return                                  The MediaContentDAO for Anime operations based on the specified data repository.
     * @throws UnsupportedOperationException    If the specified data repository is not supported.
     */
    public static MediaContentDAO<Anime> getAnimeDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return animeDAOMongo;
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return animeDAONeo4J;
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the appropriate DAO for handling Manga-related operations based on the data repository.
     *
     * @param dataRepositoryEnum                The enum representing the data repository (MongoDB or Neo4j).
     * @return                                  The MediaContentDAO for Manga operations based on the specified data repository.
     * @throws UnsupportedOperationException    If the specified data repository is not supported.
     */
    public static MediaContentDAO<Manga> getMangaDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return mangaDAOMongo;
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return mangaDAONeo4J;
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the appropriate DAO for handling User-related operations based on the data repository.
     *
     * @param dataRepositoryEnum                The enum representing the data repository (MongoDB or Neo4j).
     * @return                                  The UserDAO for User operations based on the specified data repository.
     * @throws UnsupportedOperationException    If the specified data repository is not supported.
     */
    public static UserDAO getUserDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return userDAOMongo;
        } else if (DataRepositoryEnum.NEO4J.equals(dataRepositoryEnum)) {
            return userDAONeo4J;
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }

    /**
     * Retrieves the DAO for handling Review-related operations based on the data repository.
     *
     * @param dataRepositoryEnum                The enum representing the data repository (MongoDB).
     * @return                                  The ReviewDAO for Review operations based on the specified data repository.
     * @throws UnsupportedOperationException    If the specified data repository is not supported.
     */
    public static ReviewDAO getReviewDAO(DataRepositoryEnum dataRepositoryEnum){
        if (DataRepositoryEnum.MONGODB.equals(dataRepositoryEnum)){
            return reviewDAOMongo;
        }
        throw new UnsupportedOperationException("Data repository not supported: " + dataRepositoryEnum);
    }
}
