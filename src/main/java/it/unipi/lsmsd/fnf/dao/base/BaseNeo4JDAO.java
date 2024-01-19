package it.unipi.lsmsd.fnf.dao.base;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public abstract class BaseNeo4JDAO {
    private static final String NE04J_URI = "bolt://localhost:7687";
    private static final String NEO4J_USERNAME = "neo4j";
    private static final String NEO4J_PASSWORD = "neo4j/neo4j";

    private static final Driver driver = GraphDatabase.driver(
            NE04J_URI,
            AuthTokens.basic(NEO4J_USERNAME, NEO4J_PASSWORD)
    );

    public static Driver getDriver() {
        return driver;
    }

    public static void closeDriver() {
        if(driver != null) {
            driver.close();
        }
    }


}
