package it.unipi.lsmsd.fnf.dao.base;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
/**
 * This abstract class serves as the base for Neo4j Data Access Objects (DAOs).
 * It provides methods for establishing connections to Neo4j and closing the connection.
 */
public abstract class BaseNeo4JDAO {
    private static final String NE04J_URI = "bolt://localhost:7687";
    private static final String NEO4J_USERNAME = "neo4j";
    private static final String NEO4J_PASSWORD = "neo4j/neo4j";

    private static final Driver driver = GraphDatabase.driver(
            NE04J_URI,
            AuthTokens.basic(NEO4J_USERNAME, NEO4J_PASSWORD)
    );
    /**
     * Retrieves the Neo4j driver instance for establishing connections.
     *
     * @return Neo4j Driver instance.
     */
    public static Driver getDriver() {
        return driver;
    }

    /**
     * Closes the Neo4j driver instance and releases any associated resources.
     */
    public static void closeDriver() {
        if(driver != null) {
            driver.close();
        }
    }


}
