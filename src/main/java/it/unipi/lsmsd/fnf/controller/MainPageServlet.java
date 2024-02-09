package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;


@WebServlet("/mainPage")
public class MainPageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
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
        String action = request.getParameter("action");
        request.setAttribute("mangaGenres", Constants.MANGA_GENRES);
        String targetJSP = "tests/manga_main_page_test.jsp";
        switch (action){
            case "search" -> handleSearch(request,response);
            case "changePage" -> handleChangePage(request,response);
            case "friendsActivities" -> handleFriendsActivities(request,response);
            case "suggestions" -> handleSuggestion(request,response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request,response);
        }

    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String type = request.getParameter("type");
        String targetJSP = "tests/manga_main_page_test.jsp";
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;

        // delete cookie of filters and search term
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("filters".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
                if ("searchTerm".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        if (type != null) {
            // Search by title
            String searchTerm = request.getParameter("searchTerm");
            if (searchTerm != null) {
                try {
                    MediaContentType mediaContentType = type.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME;
                    PageDTO<MangaDTO> mediaContentPage = (PageDTO<MangaDTO>) mediaContentService.searchByTitle(searchTerm, page, mediaContentType);
                    request.setAttribute("mediaContentPage", mediaContentPage);

                    // Save search term in a cookie
                    Cookie filtersCookie = new Cookie("searchTerm", encode(searchTerm, StandardCharsets.UTF_8));
                    filtersCookie.setMaxAge(3600); // Set the expiration time in seconds
                    response.addCookie(filtersCookie);
                } catch (BusinessException e) {
                    logger.error("Error occurred during search", e);
                    targetJSP = "error.jsp";
                }
            } else {
                // Search by filter
                List<String> gendersToHave = new ArrayList<>();
                List<String> gendersToAvoid = new ArrayList<>();

                for (String genre : Constants.MANGA_GENRES) {
                    if (request.getParameter(genre) != null) {
                        // Genre is selected
                        if (request.getParameter(genre).equals("select")) {
                            gendersToHave.add(genre);
                        } else if (request.getParameter(genre).equals("avoid")) {
                            gendersToAvoid.add(genre);
                        }
                    }
                }

                Map<String, Object> gendersToHaveMap = new HashMap<>();
                Map<String, Object> gendersToAvoidMap = new HashMap<>();

                if (!gendersToHave.isEmpty()) {
                    gendersToHaveMap = Map.of(request.getParameter("genreOperator").equals("and") ? "$all" : "$in",
                            Map.of("genres", gendersToHave));
                }
                if (!gendersToAvoid.isEmpty()) {
                    gendersToAvoidMap = Map.of("$nin", Map.of("genres", gendersToAvoid));
                    logger.info("gendersToAvoidMap: " + gendersToAvoidMap);
                }
                List<Map<String, Object>> filters = new ArrayList<>();
                if (!gendersToHaveMap.isEmpty()) {
                    filters.add(gendersToHaveMap);
                }
                if (!gendersToAvoidMap.isEmpty()) {
                    filters.add(gendersToAvoidMap);
                    logger.info("filtersServlet: " + filters);
                }

                // Save filters in a cookie
                ObjectMapper objectMapper = new ObjectMapper();
                String filtersJson = objectMapper.writeValueAsString(filters);
                String filtersJsonEncoded = encode(filtersJson, StandardCharsets.UTF_8);
                Cookie filtersCookie = new Cookie("filters", filtersJsonEncoded);
                filtersCookie.setMaxAge(3600); // Set the expiration time in seconds
                response.addCookie(filtersCookie);
                try {
                    MediaContentType mediaContentType = type.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME;
                    PageDTO<MangaDTO> mediaContentPage = (PageDTO<MangaDTO>) mediaContentService.searchByFilter(filters, Map.of("title",1), page, mediaContentType);
                    request.setAttribute("mediaContentPage", mediaContentPage);
                } catch (BusinessException e) {
                    logger.error("Error occurred during search", e);
                    targetJSP = "error.jsp";
                }
            }
        } else {
            // Invalid search parameters
            logger.warn("Invalid search parameters");
        }

        request.setAttribute("page", page);
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleChangePage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String targetJSP = "tests/manga_main_page_test.jsp";
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;

        // Get filters from cookies
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("searchTerm".equals(cookie.getName())) {
                    String searchTerm = decode(cookie.getValue(), StandardCharsets.UTF_8);
                    logger.info("Searching for " + cookie.getValue());
                    try {
                        MediaContentType mediaContentType = "manga".equals(request.getParameter("type")) ? MediaContentType.MANGA : MediaContentType.ANIME;
                        PageDTO<MangaDTO> mediaContentPage = (PageDTO<MangaDTO>) mediaContentService.searchByTitle(searchTerm, page, mediaContentType);
                        request.setAttribute("mediaContentPage", mediaContentPage);
                    } catch (BusinessException e) {
                        logger.error("Error occurred during search", e);
                        targetJSP = "error.jsp";
                    }
                } else if ("filters".equals(cookie.getName())) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String encodedValue = cookie.getValue();
                    String decodedJsonString = decode(encodedValue, StandardCharsets.UTF_8);
                    List<Map<String, Object>> filters = objectMapper.readValue(decodedJsonString, new TypeReference<>(){});
                    try {
                        MediaContentType mediaContentType = "manga".equals(request.getParameter("type")) ? MediaContentType.MANGA : MediaContentType.ANIME;
                        PageDTO<MangaDTO> mediaContentPage = (PageDTO<MangaDTO>) mediaContentService.searchByFilter(filters, Map.of("title",1), page, mediaContentType);
                        request.setAttribute("mediaContentPage", mediaContentPage);
                    } catch (BusinessException e) {
                        logger.error("Error occurred during search", e);
                        targetJSP = "error.jsp";
                    }
                }
            }
        }

        request.setAttribute("page", page);
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleFriendsActivities(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

    }
    private void handleSuggestion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

    }
}
