package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.*;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@WebServlet(urlPatterns = {"/mainPage", "/mainPage/anime", "/mainPage/manga"})
public class MainPageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MainPageServlet.class);
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("mediaType", request.getServletPath().equals("/mainPage/anime") ? "anime" : "manga");
        switch (request.getParameter("action")) {
            case "search" -> handleSearch(request, response);
            case "viewAll" -> handleViewAll(request, response);
            case null, default -> handleLoadPage(request, response);
        }
    }

    private void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        String targetJSP;

        if (path.equals("/mainPage") || Objects.equals(request.getParameter("scroll"), "false")) {
            request.setAttribute("scroll", false);
        }

        logger.info("Loading main page for " + request.getAttribute("mediaType"));
        if (request.getAttribute("mediaType").equals("manga")) {
            request.setAttribute("mangaGenres", Constants.MANGA_GENRES);
            request.setAttribute("mangaTypes", MangaType.values());
            request.setAttribute("mangaDemographics", MangaDemographics.values());
            request.setAttribute("mangaStatus", MangaStatus.values());
            try {
                logger.info("Loading manga main page");
                request.setAttribute("trending", mediaContentService.getMediaContentTrendByLikes(6, MediaContentType.MANGA));
                logger.info("Trending manga loaded");
                if (SecurityUtils.getAuthenticatedUser(request) != null) {
                    request.setAttribute("suggestionsByLikes", mediaContentService.getSuggestedMediaContentByLikes(SecurityUtils.getAuthenticatedUser(request).getId(), MediaContentType.MANGA, 6));
                    request.setAttribute("suggestionsByFollowings", mediaContentService.getSuggestedMediaContentByFollowings(SecurityUtils.getAuthenticatedUser(request).getId(), MediaContentType.MANGA, 6));
                }
                targetJSP = "/WEB-INF/jsp/manga_main_page.jsp";
            } catch (BusinessException e) {
                targetJSP = "/error.jsp";
            }
        } else {
            request.setAttribute("animeTags", Constants.ANIME_TAGS);
            request.setAttribute("animeTypes", AnimeType.values());
            request.setAttribute("animeStatus", AnimeStatus.values());
            try {
                request.setAttribute("trending", mediaContentService.getMediaContentTrendByLikes(6, MediaContentType.ANIME));
                if (SecurityUtils.getAuthenticatedUser(request) != null) {
                    request.setAttribute("suggestionsByLikes", mediaContentService.getSuggestedMediaContentByLikes(SecurityUtils.getAuthenticatedUser(request).getId(), MediaContentType.ANIME, 6));
                    request.setAttribute("suggestionsByFollowings", mediaContentService.getSuggestedMediaContentByFollowings(SecurityUtils.getAuthenticatedUser(request).getId(), MediaContentType.ANIME, 6));
                }
                targetJSP = "/WEB-INF/jsp/anime_main_page.jsp";
            } catch (BusinessException e) {
                targetJSP = "/error.jsp";
            }
        }


        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        // Create a module to handle the serialization and deserialization of LocalDate and LocalDateTime objects
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        // Register the formatters for serialization
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        // Register the formatters for deserialization
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        // Register the module with the ObjectMapper
        objectMapper.registerModule(javaTimeModule);

        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        MediaContentType mediaContentType = request.getAttribute("mediaType").equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME;

        try {
            PageDTO<? extends MediaContentDTO> mediaList;
            List<Map<String, Object>> filters;

            if (request.getAttribute("mediaType").equals("manga")) {
                filters = ConverterUtils.fromRequestToMangaFilters(request);
            } else {
                filters = ConverterUtils.fromRequestToAnimeFilters(request);
            }

            // Take order parameter and direction
            String order = request.getParameter("sortParam");
            int direction = Integer.parseInt(request.getParameter("sortDirection"));
            Map<String, Integer> orderBy = Map.of(order, direction);

            mediaList = mediaContentService.searchByFilter(filters, orderBy, page, mediaContentType);

            // Add the search results to the JSON response
            if (mediaList.getTotalCount() == 0) {
                jsonResponse.put("noResults", "No results found");
            } else {
                JsonNode mediaListNode = objectMapper.valueToTree(mediaList);
                jsonResponse.set("mediaPage", mediaListNode);
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

    private void handleViewAll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        MediaContentType mediaContentType = request.getAttribute("mediaType").equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME;
        String nameList = request.getParameter("nameList");

        try {
            List<MediaContentDTO> mediaList;
            switch (nameList) {
                case "trends" -> mediaList = mediaContentService.getMediaContentTrendByLikes(30, mediaContentType);
                case "suggestionsByLikes" -> mediaList = mediaContentService.getSuggestedMediaContentByLikes(SecurityUtils.getAuthenticatedUser(request).getId(), mediaContentType, 30);
                case "suggestionsByFollowings" -> mediaList = mediaContentService.getSuggestedMediaContentByFollowings(SecurityUtils.getAuthenticatedUser(request).getId(), mediaContentType, 30);
                default -> {
                    jsonResponse.put("error", "Invalid nameList parameter");
                    response.getWriter().write(jsonResponse.toString());
                    return;
                }
            }
            jsonResponse.put("success", true);
            JsonNode mediaListArray = objectMapper.valueToTree(mediaList);
            jsonResponse.set("mediaList", mediaListArray);
        } catch (BusinessException e) {
            jsonResponse.put("error", "Error occurred during search operation");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}