package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.BusinessExceptionType;
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
            response.sendRedirect("mainPage");
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
            request.setAttribute("reviews", reviewService.findByMedia(mediaId, mediaType, 1
            ));
            if (SecurityUtils.getAuthenticatedUser(request) != null) {
                request.setAttribute("isLiked", mediaContentService.isLiked(SecurityUtils.getAuthenticatedUser(request).getId(), mediaId, mediaType));
                request.setAttribute("lists", (SecurityUtils.getAuthenticatedUser(request).getLists()));
            }
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
        String result;
        try {
            reviewService.addReview(ConverterUtils.fromRequestToReviewDTO(request, mediaType));
            result = "{\"success\": \"Review added\"}";
        } catch (BusinessException e) {
            if (e.getType() == BusinessExceptionType.EMPTY_FIELDS) {
                result = "{\"error\": \"The review must have a comment or a rating\"}";
            } else {
                result = "{\"error\": \"Error while adding review\"}";
            }
        }

        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result);
    }

    private void handleDeleteReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reviewId = request.getParameter("reviewId");

        if (!request.getParameter("reviewUserId").equals(SecurityUtils.getAuthenticatedUser(request).getId())) {
            request.setAttribute("error", "You can't delete other user's reviews");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        String result;
        try {
            reviewService.deleteReview(reviewId);
            result = "{\"success\": \"Review deleted\"}";
        } catch (Exception e) {
            result = "{\"error\": \"Error while deleting review, try again later\"}";
        }

        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result);
    }

    private void handleEditReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());

        if (!request.getParameter("reviewUserId").equals(SecurityUtils.getAuthenticatedUser(request).getId())) {
            request.setAttribute("error", "You can't edit other user's reviews");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        String result;
        try {
            reviewService.updateReview(ConverterUtils.fromRequestToReviewDTO(request, mediaType));
            result = "{\"success\": \"Review updated\"}";
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            result = "{\"error\": \"Error while updating review, try again later\"}";
        }

        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result);
    }
}