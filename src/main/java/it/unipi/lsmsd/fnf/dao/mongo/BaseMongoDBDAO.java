package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
/**
 * This abstract class serves as the base for MongoDB Data Access Objects (DAOs).
 * It provides methods for establishing connections to MongoDB, building filters and sorts,
 * and appending values to documents.
 */
public abstract class BaseMongoDBDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseMongoDBDAO.class);
    private static final String PROTOCOL = "mongodb://";
    private static final String MONGO_HOST1 = "localhost";
    private static final String MONGO_PORT = "27017";
    private static final String MONGO_DB = "mangaVerse";
    private static final MongoClientSettings settings;
    private static MongoClient mongoClient;

    static {
        ConnectionString connectionString = new ConnectionString(String.format("%s%s:%s/%s", PROTOCOL, MONGO_HOST1, MONGO_PORT, MONGO_DB));
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
     * @throws DAOException CONNECTION_ERROR if an error occurs while opening the connection
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

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Returns the collection with the specified name from the database
     *
     * @param collectionName Name of the collection to retrieve
     * @return MongoCollection object representing the specified collection
     */
    public static MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase(MONGO_DB).getCollection(collectionName);
    }

    /**
     * Closes the connection to the database
     *
     * @throws DAOException CONNECTION_ERROR if an error occurs while closing the connection or if the connection was not previously opened
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

    /**
     * Builds a MongoDB filter based on the provided filter list.
     *
     * @param filterList List of maps representing the filter criteria.
     * @return Bson filter object representing the constructed filter.
     */
    protected Bson buildFilter(List<Map<String, Object>> filterList) {
        if (filterList == null || filterList.isEmpty()) {
            return empty();
        } else if (filterList.getFirst().containsKey("title")) {
            return text((String) filterList.getFirst().get("title"));
        } else {
            List<Bson> filter = buildFilterInternal(filterList);
            return and(filter);
        }
    }

    /**
     * Recursively builds a MongoDB filter based on nested filter conditions.
     *
     * @param filterList List of maps representing the filter criteria.
     * @return List of Bson objects representing the constructed filter.
     */
    protected List<Bson> buildFilterInternal(List<Map<String, Object>> filterList) {
        return filterList.stream()
                .map(filter -> {
                    Map.Entry<String, Object> entry = filter.entrySet().iterator().next();
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    return switch (key) {
                        case "$and" -> and(buildFilterInternal((List<Map<String, Object>>) value));
                        case "$or" -> or(buildFilterInternal((List<Map<String, Object>>) value));
                        default -> buildFieldFilter(key, value);
                    };
                })
                .toList();
    }


    /**
     * Builds a field-specific filter for MongoDB.
     *
     * @param fieldName Name of the field to filter on.
     * @param value     Value of the field for filtering.
     * @return Bson object representing the constructed field filter.
     */
    protected Bson buildFieldFilter(String fieldName, Object value) {
        return switch (fieldName) {
            case "$all" -> {
                Map.Entry<String, Object> entry = ((Map<String, Object>) value).entrySet().iterator().next();
                yield all(entry.getKey(), (List<?>) entry.getValue());
            }
            case "$in" ->  {
                Map.Entry<String, Object> entry = ((Map<String, Object>) value).entrySet().iterator().next();
                yield in(entry.getKey(), (List<?>) entry.getValue());
            }
            case "$nin" -> {
                Map.Entry<String, Object> entry = ((Map<String, Object>) value).entrySet().iterator().next();
                yield nin(entry.getKey(), (List<?>) entry.getValue());
            }
            case "$gte" -> {
                Map.Entry<String, Object> entry = ((Map<String, Object>) value).entrySet().iterator().next();
                yield gte(entry.getKey(), entry.getValue());
            }
            case "$lte" -> {
                Map.Entry<String, Object> entry = ((Map<String, Object>) value).entrySet().iterator().next();
                yield lte(entry.getKey(), entry.getValue());
            }
            case "$exists" -> {
                Map.Entry<String, Object> entry = ((Map<String, Object>) value).entrySet().iterator().next();
                yield exists(entry.getKey(), (Boolean) entry.getValue());
            }
            default -> eq(fieldName, value);
        };
    }



    /**
     * Builds a sort specification for MongoDB based on the provided ordering criteria.
     *
     * @param orderBy Map representing the fields to sort by and their corresponding order.
     * @return Bson object representing the constructed sort specification.
     */
    protected Bson buildSort(Map<String, Integer> orderBy) {
        if (orderBy != null && orderBy.containsKey("score")) {
            return metaTextScore("score");
        }

        List<Bson> sortList = new ArrayList<>();
        Optional.ofNullable(orderBy)
                .ifPresent(map ->
                        sortList.addAll(map.entrySet().stream()
                                .map(entry -> entry.getValue() == 1 ? ascending(entry.getKey()) : descending(entry.getKey()))
                                .toList()
                        )
                );

        return orderBy(sortList);
    }
}
