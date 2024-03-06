package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import it.unipi.lsmsd.fnf.utils.UserUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(urlPatterns = {"/manga", "/anime"})
public class MediaContentServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MediaContentServlet.class);
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final PersonalListService personalListService = ServiceLocator.getPersonalListService();
    private static final ReviewService reviewService = ServiceLocator.getReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaId = request.getParameter("mediaId");
        if (mediaId == null) {
            response.sendRedirect("/mainPage");
            return;
        }

        String action = request.getParameter("action");
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";
        try {
            MediaContent mediaContent = mediaContentService.getMediaContentById(mediaId, mediaType);
            if (mediaContent == null) {
                request.setAttribute("error", "Media not found");
                targetJSP = "error.jsp";
                request.getRequestDispatcher(targetJSP).forward(request, response);
            }
            request.setAttribute("media", mediaContentService.getMediaContentById(mediaId, mediaType));
            request.setAttribute("lists", (SecurityUtils.getAuthenticatedUser(request).getLists()));
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            targetJSP = "error.jsp";
        }

        switch (action) {
            case "addToList" -> handleAddToList(request, response);
            case "toggleLike" -> handleToggleLike(request, response);
            case "addReview" -> handleAddReview(request, response);
            case "deleteReview" -> handleDeleteReview(request, response);
            case "editReview" -> handleEditReview(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    private void handleAddToList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String listId = request.getParameter("listId");
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";

        try {
            personalListService.addToList(listId, (MediaContentDTO) request.getAttribute("media"));
            request.setAttribute("success", "Media added to list");
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            request.setAttribute("error", "Error while adding media to list");
            targetJSP = "error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleToggleLike(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        String mediaId = request.getParameter("mediaId");
        try {
            MediaContentType contentType = isManga ? MediaContentType.MANGA : MediaContentType.ANIME;

            if (UserUtils.isLiked(request)) {
                mediaContentService.removeLike(userId, mediaId, contentType);
            } else {
                mediaContentService.addLike(userId, mediaId, contentType);
            }

            request.setAttribute("isLiked", !UserUtils.isLiked(request));
            UserUtils.updateUserSession(request);
        } catch (BusinessException e) {
            logger.error("Error occurred during like operation", e);
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"isLiked\": " + request.getAttribute("isLiked") + "}");
    }

    private void handleAddReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";
        try {
            reviewService.addReview(ConverterUtils.fromRequestToReviewDTO(request, mediaType));
            request.setAttribute("success", "Review added");
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            request.setAttribute("error", "Error while adding review");
            targetJSP = "error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleDeleteReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reviewId = request.getParameter("reviewId");

        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";

        if (!request.getParameter("reviewUserId").equals(SecurityUtils.getAuthenticatedUser(request).getId())) {
            request.setAttribute("error", "You can't delete other user's reviews");
            targetJSP = "error.jsp";
            request.getRequestDispatcher(targetJSP).forward(request, response);
            return;
        }
        try {
            reviewService.deleteReview(reviewId);
            request.setAttribute("success", "Review deleted");
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            request.setAttribute("error", "Error while deleting review");
            targetJSP = "error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleEditReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";

        if (!request.getParameter("reviewUserId").equals(SecurityUtils.getAuthenticatedUser(request).getId())) {
            request.setAttribute("error", "You can't edit other user's reviews");
            targetJSP = "error.jsp";
            request.getRequestDispatcher(targetJSP).forward(request, response);
            return;
        }

        try {
            reviewService.updateReview(ConverterUtils.fromRequestToReviewDTO(request, mediaType));
            request.setAttribute("success", "Review updated");
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            request.setAttribute("error", "Error while updating review");
            targetJSP = "error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}
