package it.unipi.lsmsd.fnf.dao.base;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            default -> eq(fieldName, value);
        };
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
