package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import it.unipi.lsmsd.fnf.controller.exception.NotAuthorizedException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet for handling media content operations (like, search, review)
 * and loading the media content page.
 */
@WebServlet(urlPatterns = {"/manga", "/anime"})
public class MediaContentServlet extends HttpServlet {
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
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
        switch (request.getParameter("action")) {
            case "toggleLike" -> handleToggleLike(request, response);
            case "getReviews" -> handleGetReviewsByMediaId(request, response);
            case "searchByTitle" -> handleSearchByTitle(request,response);
            case "addReview" -> handleAddReview(request, response);
            case "updateReview" -> handleUpdateReview(request, response);
            case "deleteReview" -> handleDeleteReview(request, response);
            case null, default -> handleLoadPage(request,response);
        }
    }

    // Load the media content page
    // REQUESTED PARAMETERS:        mediaId
    private void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaId = request.getParameter("mediaId");
        if (mediaId == null) {
            response.sendRedirect("mainPage");
            return;
        }

        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";

        try {
            // Get the media content by ID
            MediaContent mediaContent = mediaContentService.getMediaContentById(mediaId, mediaType);
            if (mediaContent == null) {
                request.setAttribute("error", "Media not found");
                throw new Exception("Media not found");
            } else {
                request.setAttribute("media", mediaContent);

                // Check if the user is authenticated and is not a manager
                if (SecurityUtils.getAuthenticatedUser(request) != null && SecurityUtils.getAuthenticatedUser(request).getType().equals(UserType.USER)) {
                    String userId = SecurityUtils.getAuthenticatedUser(request).getId();

                    // Check if the user has liked the media content
                    request.setAttribute("isLiked", mediaContentService.isLiked(userId, mediaId, mediaType));

                    // Check if the user has reviewed the media content and get the review
                    if (mediaContent.getReviewIds() != null && !mediaContent.getReviewIds().isEmpty()) {
                        List<Review> latestReviews = mediaContent.getLatestReviews();
                        boolean found = false;
                        for (Review review : latestReviews) {
                            if (review.getUser().getId().equals(userId)) {
                                request.setAttribute("userReview", review);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            request.setAttribute("userReview", reviewService.isReviewedByUser(userId, mediaContent.getReviewIds()));
                        }
                    }
                }
            }

        } catch (Exception e) {
            targetJSP = "WEB-INF/jsp/error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    // Handles the like/unlike operation on a media content
    // REQUESTED PARAMETERS:        mediaId, mediaType
    // RESPONSE:                    JSON object with the "isLiked" boolean flag
    private void handleToggleLike(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        boolean isManga = mediaType.equals(MediaContentType.MANGA);
        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        String mediaId = request.getParameter("mediaId");

        try {
            // Check if the user is authorized to perform the operation
            SecurityUtils.isUserAuthorized(request, UserType.USER);

            //if is liked: unlike, else like and set the isLiked flag in the JSON response
            if (mediaContentService.isLiked(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME)) {
                mediaContentService.removeLike(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);
                jsonResponse.put("isLiked", false);

            } else {
                mediaContentService.addLike(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);
                jsonResponse.put("isLiked", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error occurred during like operation");

        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User is not authorized to perform this operation");
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Handles the search operation by title
    // REQUESTED PARAMETERS:        title, page
    // RESPONSE:                    JSON object with the search results
    //                              or an error message
    private void handleSearchByTitle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Create the date and date-time formatters
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

        // Register the formatters for serialization
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

        // Register the formatters for deserialization
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        objectMapper.registerModule(javaTimeModule);

        int page = request.getAttribute("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        MediaContentType mediaContentType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String title = request.getParameter("title");

        try {
            // Search the media content by title
            PageDTO<? extends MediaContentDTO> mediaList = mediaContentService.searchByTitle(title, page, mediaContentType);

            // Add the search results to the JSON response and set the success flag
            if (mediaList.getTotalCount() == 0) {
                jsonResponse.put("noResults", "No results found");

            } else {
                JsonNode mediaListNode = objectMapper.valueToTree(mediaList);
                jsonResponse.set("results", mediaListNode);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error occurred during search operation");

        } catch (IllegalArgumentException e) {
            jsonResponse.put("error", "Invalid JSON format for search filters");
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Handles the get reviews operation by media ID
    // REQUESTED PARAMETERS:        reviewIds, page
    // RESPONSE:                    JSON object with the reviews
    //                              or an error message
    private void handleGetReviewsByMediaId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

        // Register the formatters for serialization
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        // Register the formatters for deserialization
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        objectMapper.registerModule(javaTimeModule);

        List<String> reviewIds = Arrays.stream(objectMapper.readValue(request.getParameter("reviewIds"), String[].class)).toList();
        String pageString = request.getParameter("page");
        int page = 0;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }

        try {
            // Get the reviews by IDs list
            PageDTO<ReviewDTO> reviews = reviewService.getReviewsByIdsList(reviewIds, page, "media");

            // Convert the page to a JSON Object and set the success flag
            if (reviews == null || reviews.getTotalCount() == 0) {
                jsonResponse.put("notFoundError", true);

            } else {
                JsonNode reviewsJsonObject  = objectMapper.valueToTree(reviews);
                jsonResponse.set("reviews", reviewsJsonObject);
                jsonResponse.put("success", true);
            }

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error occurred during reviews retrieval");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Handles the add review operation
    // REQUESTED PARAMETERS:        mediaId, mediaTitle, mediaType, comment, rating
    // RESPONSE:                    JSON object with the success flag
    //                              or an error message
    private void handleAddReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        LoggedUserDTO loggedUser = SecurityUtils.getAuthenticatedUser(request);
        String mediaId = request.getParameter("mediaId");
        String mediaTitle = request.getParameter("mediaTitle");
        String mediaType = request.getParameter("mediaType");
        String comment = request.getParameter("comment");
        Integer rating = StringUtils.isNotEmpty(request.getParameter("rating")) ? Integer.parseInt(request.getParameter("rating")) : null;
        if (StringUtils.isBlank(mediaType)) {
            jsonResponse.put("error", "Invalid request parameters");

        } else {
            MediaContentDTO mediaContentDTO = mediaType.equals("anime") ? new AnimeDTO(mediaId, mediaTitle) : new MangaDTO(mediaId, mediaTitle);

            try {
                // Check if the user is authorized to perform the operation
                SecurityUtils.isUserAuthorized(request, UserType.USER);
                UserSummaryDTO userSummaryDTO = loggedUser.toUserModel().toSummaryDTO();

                // Add the review and set the success flag
                reviewService.addReview(new ReviewDTO(mediaContentDTO, userSummaryDTO, comment, rating));
                jsonResponse.put("success", true);

            } catch (BusinessException e) {
                jsonResponse.put("error", "Error occurred during review addition");

            } catch (NotAuthorizedException e) {
                jsonResponse.put("error", "User is not authorized to perform this operation");
            }
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Handles the update review operation
    // REQUESTED PARAMETERS:        reviewId, mediaId, mediaTitle, mediaType, comment, rating
    // RESPONSE:                    JSON object with the success flag
    //                              or an error message
    private void handleUpdateReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String reviewId = request.getParameter("reviewId");
        String mediaType = request.getParameter("mediaType");
        String mediaId = request.getParameter("mediaId");
        String mediaTitle = request.getParameter("mediaTitle");
        String comment = request.getParameter("comment");
        Integer rating = StringUtils.isNotEmpty(request.getParameter("rating")) ? Integer.parseInt(request.getParameter("rating")) : null;
        LoggedUserDTO loggedUser = SecurityUtils.getAuthenticatedUser(request);

        if (StringUtils.isBlank(mediaType)) {
            jsonResponse.put("error", "Invalid request parameters");

        } else {
            MediaContentDTO mediaContentDTO = mediaType.equals("anime") ? new AnimeDTO(mediaId, mediaTitle) : new MangaDTO(mediaId, mediaTitle);
            ReviewDTO reviewDTO = new ReviewDTO(reviewId, null, comment, rating, mediaContentDTO, loggedUser.toUserModel().toSummaryDTO());
            try {
                // Check if the user is authorized to perform the operation
                SecurityUtils.isUserAuthorized(request, UserType.USER);

                // Update the review and set the success flag
                reviewService.updateReview(reviewDTO);
                jsonResponse.put("success", true);

            } catch (BusinessException e) {
                jsonResponse.put("error", "Error occurred during review update");

            } catch (NotAuthorizedException e) {
                jsonResponse.put("error", "User is not authorized to perform this operation");
            }
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Handles the delete review operation
    // REQUESTED PARAMETERS:        reviewId, mediaId, mediaType, reviewsIds, latestReviewsIds
    // RESPONSE:                    JSON object with the success flag
    //                              or an error message
    private void handleDeleteReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        objectMapper.registerModule(javaTimeModule);

        String reviewId = request.getParameter("reviewId");
        String mediaId = request.getParameter("mediaId");
        MediaContentType mediaType = request.getParameter("mediaType").equals("anime") ? MediaContentType.ANIME : MediaContentType.MANGA;
        List<String> reviewsIds = Arrays.stream(objectMapper.readValue(request.getParameter("reviewsIds"), String[].class)).toList();
        List<String> latestReviewIds = Arrays.stream(objectMapper.readValue(request.getParameter("latestReviewsIds"), String[].class)).toList();

        try {
            // Check if the user is authorized to perform the operation
            SecurityUtils.isUserAuthorized(request, UserType.USER);

            // Delete the review and set the success flag
            reviewService.deleteReview(reviewId, mediaId, mediaType, reviewsIds, latestReviewIds.contains(reviewId));
            jsonResponse.put("success", true);

        } catch (BusinessException e) {
            jsonResponse.put("error", "Error occurred during review deletion");

        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User is not authorized to perform this operation");
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
