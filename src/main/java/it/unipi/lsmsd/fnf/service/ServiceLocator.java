package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.impl.*;
import it.unipi.lsmsd.fnf.service.interfaces.*;

/**
 * This class is a service locator that provides the services to the application.
 * It is a singleton class that provides the services to the application.
 * @see UserService
 * @see ReviewService
 * @see MediaContentService
 * @see TaskManager
 * @see ExecutorTaskService
 */
public class ServiceLocator {
    private static final UserService userService = new UserServiceImpl();
    private static final ReviewService reviewService = new ReviewServiceImpl();
    private static final MediaContentService mediaContentService = new MediaContentServiceImpl();
    public static UserService getUserService() {
        return userService;
    }
    public static ReviewService getReviewService() {
        return reviewService;
    }
    public static MediaContentService getMediaContentService() {
        return mediaContentService;
    }
    public static TaskManager getErrorsTaskManager() {
        return ErrorTaskManager.getInstance();
    }
    public static ExecutorTaskService getExecutorTaskService(ExecutorTaskServiceType type) {
        return switch (type) {
            case APERIODIC -> AperiodicExecutorTaskServiceImpl.getInstance(getErrorsTaskManager());
            case PERIODIC -> PeriodicExecutorTaskServiceImpl.getInstance();
        };
    }
}
