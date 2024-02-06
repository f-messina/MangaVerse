package it.unipi.lsmsd.fnf.dao.base;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

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
            return empty();
        } else if (filterMap.containsKey("title")) {
            return text((String) filterMap.get("title"));
        }
        List<Bson> filter = buildFilterInternal(filterMap);
        return and(filter);
    }

    protected List<Bson> buildFilterInternal(Map<String, Object> filterMap) {
        return filterMap.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    if (!key.startsWith("$")) {
                        return buildFieldFilter(key, entry.getValue());
                    }

                    return switch (key) {
                        case "$nor" -> nor(getFiltersList(entry.getValue()));
                        case "$and" -> and(getFiltersList(entry.getValue()));
                        case "$or" -> or(getFiltersList(entry.getValue()));
                        case "$not" -> not(buildFilterInternal((Map<String, Object>) entry.getValue()).getFirst());
                        default -> throw new IllegalArgumentException("Unsupported filter key: " + key);
                    };
                })
                .toList();
    }

    protected Bson buildFieldFilter(String fieldName, Object value) {
        return eq(fieldName, value);
    }

    protected List<Bson> getFiltersList(Object filters) {
        if (filters instanceof List<?>) {
            return ((List<?>) filters).stream()
                    .filter(filter -> filter instanceof Map)
                    .flatMap(filter -> buildFilterInternal((Map<String, Object>) filter).stream())
                    .toList();
        }

        return Collections.emptyList();
    }

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

    protected void appendIfNotNull(Document doc, String key, Object value) {
        if (value != null &&
                !(value instanceof String && (value.equals(Constants.NULL_STRING) || value.equals(Gender.UNKNOWN.name()))) &&
                !(value instanceof Date && value.equals(ConverterUtils.localDateToDate(Constants.NULL_DATE))) &&
                (StringUtils.isNotBlank(value.toString()) ||
                        (value instanceof List && CollectionUtils.isNotEmpty((List<?>) value)))) {
            doc.append(key, value);
        }
    }
}
