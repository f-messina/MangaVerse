package it.unipi.lsmsd.fnf.config;

import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppServletContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application startup - Opening database connections.");
        openConnections();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application shutdown - Closing database connections.");
        System.out.println("Neo4j connection closed");
        closeConnections();
    }

    private void openConnections() {
        try{
            BaseMongoDBDAO.openConnection();
        } catch (DAOException e) {
            logger.error("AppServletContextListener:contextInitialized: Error while initializing MongoDB connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("MongoDB connection initialized");
        try {
            BaseNeo4JDAO.openConnection();
        }catch (DAOException e){
            logger.error("AppServletContextListener:contextInitialized: Error while initializing Neo4j connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("Neo4j connection initialized");
    }

    private void closeConnections() {
        try {
            BaseMongoDBDAO.closeConnection();
        } catch (DAOException e) {
            logger.error("CustomContextListener:contextDestroyed: Error while closing MongoDB connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("MongoDB connection closed");
        try {
            BaseNeo4JDAO.closeConnection();
        } catch (DAOException e) {
            logger.error("CustomContextListener:contextDestroyed: Error while closing Neo4j connection: " + e.getMessage());
            System.exit(1);
        }
    }
}