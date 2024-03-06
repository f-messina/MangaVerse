package it.unipi.lsmsd.fnf.dao.base;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * This abstract class serves as the base for Neo4j Data Access Objects (DAOs).
 * It provides methods for establishing connections to Neo4j and closing the connection.
 */
public abstract class BaseNeo4JDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseNeo4JDAO.class);
    private static final String PROTOCOL = "bolt://";
    private static final String NEO4J_HOST = "localhost";
    private static final String NEO4J_PORT = "7687";
    private static final String NEO4J_USERNAME = "neo4j";
    private static final String NEO4J_PASSWORD = "neo4j/neo4j";
    private static Driver driver = null;

    /**
     * Opens a connection to the graph database
     *
     * @throws DAOException CONNECTION_ERROR if an error occurs while opening the connection
     */
    public static void openConnection() throws DAOException {
        if(driver == null){
            try {
                String uri = String.format("%s%s:%s", PROTOCOL, NEO4J_HOST, NEO4J_PORT);
                driver = GraphDatabase.driver(uri, AuthTokens.basic(NEO4J_USERNAME, NEO4J_PASSWORD));
            } catch (Exception e) {
                logger.error("Neo4jBaseDAO: Error while connecting to Neo4j (openConnection): " + e.getMessage());
                throw new DAOException(e.getMessage());
            }
        }
    }

    /**
     * Closes the connection to the graph database
     *
     * @throws DAOException CONNECTION_ERROR if an error occurs while closing the connection
     */
    public static void closeConnection() throws DAOException {
        if(driver != null){
            try {
                driver.close();
                driver = null;
            } catch (Exception e) {
                logger.error("Neo4jBaseDAO: Error while closing connection to Neo4j (closeConnection): " + e.getMessage());
                throw  new DAOException(e.getMessage());
            }
        }
        else {
            logger.error("Neo4jBaseDAO: Connection to Neo4j not opened (closeConnection)");
            throw new DAOException("Connection to Neo4j not opened");
        }
    }

    /**
     * Returns a session to the graph database
     *
     * @throws DAOException CONNECTION_ERROR if an error occurs if the connection was not previously opened
     */
    public static Session getSession() throws DAOException {
        if(driver == null){
            logger.error("Neo4jBaseDAO: Connection to Neo4j not opened (getSession)");
            throw new DAOException("Connection to Neo4j not opened");
        }
        return driver.session(SessionConfig.builder().withDatabase("neo4j").build());
    }
}
