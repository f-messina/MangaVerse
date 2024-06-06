package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tags.shaded.org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        String action = request.getParameter("action");

        switch (action) {
            case "toggleLike" -> handleToggleLike(request, response);
            case "getMediaContent" -> handleGetMediaContentById(request,response);
            case "searchByTitle" -> handleSearchByTitle(request,response);
            case null, default -> handleLoadPage(request,response);
        }
    }
    private void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaId = request.getParameter("mediaId");
        if (mediaId == null) {
            response.sendRedirect("mainPage");
            return;
        }

        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String targetJSP = mediaType.equals(MediaContentType.ANIME) ? "WEB-INF/jsp/anime.jsp" : "WEB-INF/jsp/manga.jsp";
        try {
            MediaContent mediaContent = mediaContentService.getMediaContentById(mediaId, mediaType);

            if (mediaContent == null) {
                request.setAttribute("error", "Media not found");
                targetJSP = "error.jsp";
                request.getRequestDispatcher(targetJSP).forward(request, response);
            }
            MediaContent mediaContent1 = mediaContentService.getMediaContentById(mediaId, mediaType);
            request.setAttribute("media", mediaContent1);
            request.setAttribute("reviews", reviewService.findByMedia(mediaContent1.getReviewIds(), mediaType, 1));
            if (SecurityUtils.getAuthenticatedUser(request) != null) {
                request.setAttribute("isLiked", mediaContentService.isLiked(SecurityUtils.getAuthenticatedUser(request).getId(), mediaId, mediaType));
            }
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            targetJSP = "error.jsp";
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleToggleLike(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        boolean isManga = mediaType.equals(MediaContentType.MANGA);
        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        String mediaId = request.getParameter("mediaId");
        try {
            //if is liked: unlike, else like
            logger.info("User " + userId + " is toggling like on " + mediaId);
            if (mediaContentService.isLiked(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME)) {
                mediaContentService.removeLike(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);
                request.setAttribute("isLiked", false);
            } else {
                mediaContentService.addLike(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);
                request.setAttribute("isLiked", true);
            }
            logger.info(mediaContentService.isLiked(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME) ? "Liked" : "Unliked");
        } catch (BusinessException e) {
            logger.error("Error occurred during like operation", e);
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"isLiked\": " + request.getAttribute("isLiked") + "}");
    }

    private void handleGetMediaContentById(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        String type = request.getParameter("type");
        String mediaId = request.getParameter("mediaId");

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // Register the formatters for serialization
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        // Register the formatters for deserialization
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        objectMapper.registerModule(javaTimeModule);

        if (type.equals("anime")){
            try {
                Anime anime = (Anime)mediaContentService.getMediaContentById(mediaId,MediaContentType.ANIME);
                JsonNode jsonNode = objectMapper.valueToTree(anime);
                jsonResponse.set("anime", jsonNode);
                jsonResponse.put("success", true);
            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.NOT_FOUND)){
                    jsonResponse.put("anime-search-failed","anime-not-found");
                }else{
                    jsonResponse.put("anime-search-failed","problem-on-searching-anime");
                }
            }
        }else if(type.equals("manga")){
            try {
                Manga manga = (Manga)mediaContentService.getMediaContentById(mediaId,MediaContentType.MANGA);
                JsonNode jsonNode = objectMapper.valueToTree(manga);
                jsonResponse.set("manga",jsonNode);
                jsonResponse.put("success", true);
            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.NOT_FOUND)){
                    jsonResponse.put("manga-search-failed","manga-not-found");
                }else{
                    jsonResponse.put("manga-search-failed","problem-on-searching-manga");
                }
            }
        }else{
            jsonResponse.put("error","Invalid type");
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleSearchByTitle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // Register the formatters for serialization
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        // Register the formatters for deserialization
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        objectMapper.registerModule(javaTimeModule);

        try {
            int page = request.getAttribute("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
            MediaContentType mediaContentType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
            String title = request.getParameter("title");

            PageDTO<? extends MediaContentDTO> mediaList = mediaContentService.searchByTitle(title, page, mediaContentType);

            // Add the search results to the JSON response
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
}
