package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.*;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.tags.shaded.org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manga", "/anime"})
public class MediaContentServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MediaContentServlet.class);
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final UserService userService = ServiceLocator.getUserService();
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
            request.setAttribute("reviews", reviewService.findByMedia(mediaId, mediaType, 1));
            if (SecurityUtils.getAuthenticatedUser(request) != null) {
                request.setAttribute("isLiked", mediaContentService.isLiked(SecurityUtils.getAuthenticatedUser(request).getId(), mediaId, mediaType));
            }
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            targetJSP = "error.jsp";
        }

        switch (action) {
            case "toggleLike" -> handleToggleLike(request, response);
            case "addReview" -> handleAddReview(request, response);
            case "deleteReview" -> handleDeleteReview(request, response);
            case "editReview" -> handleEditReview(request, response);
            case "showAllReviews" -> handleShowAllReviews(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }
    private void handleToggleLike(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String section = request.getParameter("section");
        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        String mediaId = request.getParameter("mediaId");

        if (userId == null) {
            jsonResponse.put("error", "You must be logged in to like a media");
        }
        else if (section == null || (!section.equals("manga") && !section.equals("anime"))) {
            jsonResponse.put("error", "Invalid section");
        }
        else if (mediaId == null) {
            jsonResponse.put("error", "Invalid media");
        }
        else {
            try {
                logger.info("User " + userId + " is toggling like on " + mediaId);

                //if is liked: unlike, else like
                boolean isLiked = mediaContentService.isLiked(userId, mediaId, MediaContentType.valueOf(section.toUpperCase()));
                if (isLiked) {
                    mediaContentService.removeLike(userId, mediaId, MediaContentType.valueOf(section.toUpperCase()));
                } else {
                    mediaContentService.addLike(userId, mediaId, MediaContentType.valueOf(section.toUpperCase()));
                }
                boolean currentStatus = mediaContentService.isLiked(userId, mediaId, MediaContentType.valueOf(section.toUpperCase()));
                jsonResponse.put("success", true);
                jsonResponse.put("isLiked", currentStatus);
                logger.info(currentStatus ? "Liked" : "Unliked");
            } catch (BusinessException e) {
                jsonResponse.put("error", "Error while toggling like");

            }
        }


        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleAddReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String section = request.getParameter("section");

        if (section == null || (!section.equals("manga") && !section.equals("anime"))) {
            jsonResponse.put("error", "Invalid section");
        }
        else {
            MediaContentType mediaType = MediaContentType.valueOf(section.toUpperCase());

            try {
                reviewService.addReview(ConverterUtils.fromRequestToReviewDTO(request, mediaType));
                jsonResponse.put("success", "Review added");
            } catch (BusinessException e) {
                if (e.getType() == BusinessExceptionType.EMPTY_FIELDS) {
                    jsonResponse.put("error", "The review must have a comment or a rating");
                }
                else if (e.getType() == BusinessExceptionType.DUPLICATED_KEY) {
                    jsonResponse.put("error", "You can't add more than one review for the same media");
                }
                else if(e.getType() == BusinessExceptionType.NOT_FOUND){
                    jsonResponse.put("error", "Media not found");
                }
                else {
                    jsonResponse.put("error", "Error while adding review");
                }
            }

        }


        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleDeleteReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String reviewId = request.getParameter("reviewId");
        String reviewUserId = String.valueOf(request.getParameter("reviewUserId").equals(SecurityUtils.getAuthenticatedUser(request).getId()));
        String mediaId = request.getParameter("mediaId");
        String section = request.getParameter("section");

        if (reviewId == null || mediaId == null || section == null) {
            jsonResponse.put("error", "Missing parameters");
        }
        else if (!reviewUserId.equals(SecurityUtils.getAuthenticatedUser(request).getId())) {
            jsonResponse.put("error", "You can't delete other user's reviews");
        }
        else {
            try {
                reviewService.deleteReview(reviewId, mediaId, MediaContentType.valueOf(section.toUpperCase()));
                jsonResponse.put("success", "Review deleted");
            } catch (BusinessException e) {
                if(e.getType() == BusinessExceptionType.NOT_FOUND){
                    jsonResponse.put("error", "Review not found");
                }
                else{
                    jsonResponse.put("error", "Error while deleting review");

                }
            }
        }

        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleEditReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();


        String reviewUserId = String.valueOf(request.getParameter("reviewUserId").equals(SecurityUtils.getAuthenticatedUser(request).getId()));
        String section = request.getParameter("section");

        if (section == null || (!section.equals("manga") && !section.equals("anime"))) {
            jsonResponse.put("error", "Invalid section");
        }
        else if (!reviewUserId.equals(SecurityUtils.getAuthenticatedUser(request).getId())) {
            jsonResponse.put("error", "You can't edit other user's reviews");
        }
        else {
            try {
                reviewService.updateReview(ConverterUtils.fromRequestToReviewDTO(request, MediaContentType.valueOf(section.toUpperCase())));
                jsonResponse.put("success", "Review updated");
            } catch (BusinessException e) {
                if (e.getType() == BusinessExceptionType.EMPTY_FIELDS) {
                    jsonResponse.put("error", "The review must have a comment or a rating");
                }
                else if (e.getType() == BusinessExceptionType.NOT_FOUND) {
                    jsonResponse.put("error", "Review not found");
                }
                else {
                    jsonResponse.put("error", "Error while updating review");
                }
            }
        }

        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
    //Add show all reviews (return pageDTO with all reviews)
    private void handleShowAllReviews (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String mediaId = request.getParameter("mediaId");
        String section = request.getParameter("section");

        if (mediaId == null || section == null) {
            jsonResponse.put("error", "Missing parameters");
        }
        else {
            try {
                PageDTO<ReviewDTO> reviews = reviewService.findByMedia(mediaId, MediaContentType.valueOf(section.toUpperCase()), 0);
                if (reviews == null) {
                    jsonResponse.put("error", "No reviews found");
                } else {
                    jsonResponse.put("reviews", objectMapper.writeValueAsString(reviews));
                    jsonResponse.put("success", "All reviews shown");
                }

            } catch (BusinessException e) {
                if(e.getType() == BusinessExceptionType.NOT_FOUND){
                    jsonResponse.put("error", "Media not found");
                }
                else {
                    jsonResponse.put("error", "Error while showing reviews");
                }
            }
        }

        // Set content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

}