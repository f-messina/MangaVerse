package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class serves as the base for MongoDB Data Access Objects (DAOs).
 * It provides methods for establishing connections to MongoDB and closing the connection.
 */
public abstract class BaseMongoDBDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseMongoDBDAO.class);
    private static final String PROTOCOL = "mongodb://";
    private static final String MONGO_HOST = "localhost";
    private static final String MONGO_PORT1 = "27018";
    private static final String MONGO_PORT2 = "27019";
    private static final String MONGO_PORT3 = "27020";
    private static final String MONGO_DB = "mangaVerse";
    private static final MongoClientSettings settings;
    private static MongoClient mongoClient;

    static {
        ConnectionString connectionString = new ConnectionString(String.format("%s%s:%s,%s:%s,%s:%s/%s", PROTOCOL, MONGO_HOST, MONGO_PORT1, MONGO_HOST, MONGO_PORT2, MONGO_HOST, MONGO_PORT3, MONGO_DB));
        settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .writeConcern(WriteConcern.W1)
                .readPreference(ReadPreference.nearest())
                .retryWrites(true)
                .readConcern(ReadConcern.LOCAL)
                .build();
    }

    /**
     * Opens a connection to the database
     *
     * @throws DAOException     If an error occurs while opening the connection
     */
    public static void openConnection() throws DAOException {

        if(mongoClient == null){
            try {
                mongoClient = MongoClients.create(settings);
            } catch (Exception e) {
                logger.error("BaseMongoDBDAO: Error while connecting to MongoDB (openConnection): " + e.getMessage());
                throw new DAOException(e.getMessage());
            }
        }
    }

    /**
     * Returns the MongoClient object representing the connection to the database
     *
     * @return      MongoClient object representing the connection to the database
     */
    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Returns the collection with the specified name from the database
     *
     * @param collectionName    Name of the collection to retrieve
     * @return                  MongoCollection object representing the specified collection
     */
    public static MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase(MONGO_DB).getCollection(collectionName);
    }

    /**
     * Closes the connection to the database
     *
     * @throws DAOException     If an error occurs while closing the connection or if the connection was not previously opened
     */
    public static void closeConnection() throws DAOException {
        if(mongoClient != null){
            try {
                mongoClient.close();
            } catch (Exception e) {
                throw new DAOException(e.getMessage());
            }
        }
        else {
            throw new DAOException("Error while closing MongoDB connection: connection was not previously opened");
        }
    }
}
