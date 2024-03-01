package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;
import it.unipi.lsmsd.fnf.model.enums.MangaDemographics;
import it.unipi.lsmsd.fnf.model.enums.MangaType;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/mainPage", "/mainPage/anime", "/mainPage/manga"})
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
        String targetJSP;

        if (request.getServletPath().equals("/mainPage/manga")) {
            request.setAttribute("mangaGenres", Constants.MANGA_GENRES);
            request.setAttribute("mangaTypes", MangaType.values());
            request.setAttribute("mangaDemographics", MangaDemographics.values());
            request.setAttribute("mangaStatus", Constants.MANGA_STATUS);
            targetJSP = "/tests/manga_main_page_test.jsp";
        } else if(request.getServletPath().equals("/mainPage/anime")) {
            request.setAttribute("animeTags", Constants.ANIME_TAGS);
            request.setAttribute("animeTypes", AnimeType.values())  ;
            request.setAttribute("animeStatus", Constants.ANIME_STATUS);
            targetJSP = "/tests/anime_main_page_test.jsp";
        } else {
            response.sendRedirect("mainPage/manga");
            return;
        }

        switch (action){
            case "search" -> handleSearch(request,response);
            case "sortAndPaginate" -> handleSortAndPaginate(request,response);
            case "suggestions" -> handleSuggestion(request,response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request,response);
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getServletPath().equals("/mainPage/manga") ? "manga" : "anime";
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;



        // Create a JSON object to include information in the response
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        PageDTO<?> mediaContentList;
        if (type.equals("manga")) {
            mediaContentList = new PageDTO<MangaDTO>();
        } else {
            mediaContentList = new PageDTO<AnimeDTO>();
        }

        // Search by title
        String searchTerm = request.getParameter("searchTerm");
        if (searchTerm != null) {
            try {
                MediaContentType mediaContentType = type.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME;
                if (searchTerm.isEmpty()) {
                    Map<String, Integer> orderBy;
                    if (request.getParameter("orderBy") != null) {
                        String[] orderArray = request.getParameter("orderBy").split(" ");
                        orderBy = Map.of(orderArray[0], Integer.parseInt(orderArray[1]));
                    } else {
                        orderBy = Map.of("title", 1);
                    }
                    mediaContentList = mediaContentService.searchByFilter(null, orderBy, page, mediaContentType);
                } else {
                    mediaContentList = mediaContentService.searchByTitle(searchTerm, page, mediaContentType);
                }
            } catch (BusinessException e) {
                logger.error("Error occurred during search", e);
                request.getRequestDispatcher("/error.jsp").forward(request, response);
            }
        } else {
            MediaContentType mediaContentType = type.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME;
            List<Map<String, Object>> filters = type.equals("manga") ? ConverterUtils.fromRequestToMangaFilters(request): ConverterUtils.fromRequestToAnimeFilters(request);

            // Take order parameter and direction
            String[] orderArray = request.getParameter("orderBy").split(" ");
            Map<String, Integer> orderBy = Map.of(orderArray[0], Integer.parseInt(orderArray[1]));

            // Add the search parameters to the JSON response
            jsonResponse.put("orderBy", request.getParameter("orderBy"));
            try {
                mediaContentList = mediaContentService.searchByFilter(filters, orderBy, page, mediaContentType);
            } catch (BusinessException e) {
                logger.error("Error occurred during search", e);
                request.getRequestDispatcher("/error.jsp").forward(request, response);
            }
        }

        // Add the search results to the JSON response
        JsonNode mediaContentListNode = objectMapper.valueToTree(mediaContentList);
        jsonResponse.set("mediaContentList", mediaContentListNode);

        // Add the number of page selected to the JSON response
        jsonResponse.put("page", page);

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }


    private void handleSortAndPaginate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String targetJSP = "/tests/manga_main_page_test.jsp";
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;

        request.setAttribute("page", page);
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleSuggestion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    }
}
