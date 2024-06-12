package it.unipi.lsmsd.fnf.config;

import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;

import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppServletContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppServletContextListener.class);
    private static final ExecutorTaskService aperiodicTaskService = ServiceLocator.getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
    private static final ExecutorTaskService periodicTaskService = ServiceLocator.getExecutorTaskService(ExecutorTaskServiceType.PERIODIC);
    private static final TaskManager errorTaskManager = ServiceLocator.getErrorsTaskManager();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application startup - Opening database connections.");
        openConnections();
        startServices();
        System.out.println("startup complete");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application shutdown - Closing database connections.");
        closeConnections();
        stopServices();
        System.out.println("shutdown complete");
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

    private void startServices() {
        try {
            aperiodicTaskService.start();
        }catch (Exception e){
            logger.error("AppServletContextListener:contextInitialized: Error while starting AperiodicExecutorTaskService: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("AperiodicExecutorTaskService started");
        try {
            errorTaskManager.start();
        }catch (Exception e){
            logger.error("AppServletContextListener:contextInitialized: Error while starting ErrorTaskManager: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("ErrorTaskManager started");
        try {
            periodicTaskService.start();
        }catch (Exception e){
            logger.error("AppServletContextListener:contextInitialized: Error while starting PeriodicExecutorTaskService: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("PeriodicExecutorTaskService started");
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
        System.out.println("Neo4j connection closed");
    }

    private void stopServices() {
        try{
            aperiodicTaskService.stop();
        }catch (Exception e){
            logger.error("AppServletContextListener:contextDestroyed: Error while stopping AperiodicExecutorTaskService: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("AperiodicExecutorTaskService stopped");
        try{
            errorTaskManager.stop();
        }catch (Exception e){
            logger.error("AppServletContextListener:contextDestroyed: Error while stopping ErrorTaskManager: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("ErrorTaskManager stopped");
        try{
            periodicTaskService.stop();
        }catch (Exception e){
            logger.error("AppServletContextListener:contextDestroyed: Error while stopping PeriodicExecutorTaskService: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("PeriodicExecutorTaskService stopped");
    }
}

// signup: (gender and birthday)

// database: remove default images, update redundancies (user redundancy in review and media, media redundancy in review, neo4j num likes in anime/manga,
// neo4j num followers/followed in user, remove user/media in neo4j that doesn't exist in mongo)