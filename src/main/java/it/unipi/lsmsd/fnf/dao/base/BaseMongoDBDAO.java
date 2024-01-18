package it.unipi.lsmsd.fnf.dao.base;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BaseMongoDBDAO {
    private static final String PROTOCOL = "mongodb://";
    private static final String MONGO_HOST = "localhost";
    private static final String MONGO_PORT = "27017";

    private static final String connectionString = String.format("%s%s:%s", PROTOCOL, MONGO_HOST, MONGO_PORT);

    public static MongoClient getConnection() {
        ConnectionString uri = new ConnectionString(connectionString);

        return MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(uri)
                        .build()
        );
    }
    public static void closeConnection(MongoClient mongoClient){
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    protected Bson buildFilter(Map<String, Object> filterMap) {
        if (filterMap == null || filterMap.isEmpty()) {
            return Filters.empty();
        }
        List<Bson> filter = buildFilterInternal(filterMap);
        return Filters.and(filter);
    }

    protected List<Bson> buildFilterInternal(Map<String, Object> filterMap) {
        List<Bson> filters = new ArrayList<>();

        for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
            String key = entry.getKey();

            // Handle regular field filters
            if (!(key.startsWith("$"))) {
                filters.add(buildFieldFilter(key, entry.getValue()));
                continue;
            }

            switch (key) {
                case "$nor" -> {
                    List<Bson> norFilters = getFiltersList(entry.getValue());
                    filters.add(Filters.nor(norFilters));
                }
                case "$and" -> {
                    List<Bson> andFilters = getFiltersList(entry.getValue());
                    filters.add(Filters.and(andFilters));
                }
                case "$or" -> {
                    List<Bson> orFilters = getFiltersList(entry.getValue());
                    filters.add(Filters.or(orFilters));
                }
                case "$not" -> {
                    Bson notFilter = buildFilterInternal((Map<String, Object>) entry.getValue()).getFirst();
                    filters.add(Filters.not(notFilter));
                }
            }
        }

        return filters;
    }

    protected Bson buildFieldFilter(String fieldName, Object value) {
        return Filters.eq(fieldName, value);
    }

    protected List<Bson> getFiltersList(Object filters) {
        List<Bson> filtersList = new ArrayList<>();
        for (Object filter : (List<?>) filters) {
            filtersList.add(buildFilterInternal((Map<String, Object>) filter).getFirst());
        }
        return filtersList;
    }

    protected List<Bson> buildSort(Map<String, Integer> orderBy) {
        List<Bson> sortList = new ArrayList<>();
        if (orderBy != null && !orderBy.isEmpty()) {
            for (Map.Entry<String, Integer> entry : orderBy.entrySet()) {
                if (entry.getValue() == 1) {
                    sortList.add(Sorts.ascending(entry.getKey()));
                } else if (entry.getValue() == -1) {
                    sortList.add(Sorts.descending(entry.getKey()));
                }
            }
        }
        return sortList;
    }
}
