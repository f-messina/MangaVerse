package it.unipi.lsmsd.fnf.service;

import it.unipi.lsmsd.fnf.service.impl.MediaContentServiceImpl;
import it.unipi.lsmsd.fnf.service.impl.ReviewServiceImpl;
import it.unipi.lsmsd.fnf.service.impl.UserServiceImpl;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;

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
}
