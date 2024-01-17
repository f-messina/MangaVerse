package it.unipi.lsmsd.fnf.dao.base;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
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

}
