package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.*;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@WebServlet(urlPatterns = {"/mainPage", "/mainPage/anime", "/mainPage/manga"})
public class MainPageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MainPageServlet.class);
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final UserService userService = ServiceLocator.getUserService();
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
        request.setAttribute("isManga",  request.getServletPath().equals("/mainPage/manga"));
        request.setAttribute("isAnime", request.getServletPath().equals("/mainPage/anime"));

        if ((boolean) request.getAttribute("isManga")) {
            request.setAttribute("mangaGenres", Constants.MANGA_GENRES);
            request.setAttribute("mangaTypes", MangaType.values());
            request.setAttribute("mangaDemographics", MangaDemographics.values());
            request.setAttribute("mangaStatus", MangaStatus.values());
            targetJSP = "/WEB-INF/jsp/manga_main_page.jsp";
        } else if((boolean) request.getAttribute("isAnime")) {
            request.setAttribute("animeTags", Constants.ANIME_TAGS);
            request.setAttribute("animeTypes", AnimeType.values())  ;
            request.setAttribute("animeStatus", AnimeStatus.values());
            targetJSP = "/WEB-INF/jsp/anime_main_page.jsp";
        } else {
            response.sendRedirect("mainPage/manga");
            return;
        }

        switch (action){
            case "search" -> handleSearch(request,response);
            case "sortAndPaginate" -> handleSortAndPaginate(request,response);
            case "suggestions" -> handleSuggestion(request,response);
            case "toggleLike" -> handleToggleLike(request,response);
            //TODO: add the follow and unfollow cases, I have done the handle functions
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request,response);
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        boolean isManga = (boolean) request.getAttribute("isManga");

        // Create a JSON object to include information in the response
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        PageDTO<? extends MediaContentDTO> mediaContentList;
        if (isManga) {
            mediaContentList = new PageDTO<MangaDTO>();
        } else {
            mediaContentList = new PageDTO<AnimeDTO>();
        }

        // Search by title
        String searchTerm = request.getParameter("searchTerm");
        if (searchTerm != null) {
            try {
                MediaContentType mediaContentType = isManga ? MediaContentType.MANGA : MediaContentType.ANIME;
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
            MediaContentType mediaContentType = isManga ? MediaContentType.MANGA : MediaContentType.ANIME;
            List<Map<String, Object>> filters = isManga ? ConverterUtils.fromRequestToMangaFilters(request): ConverterUtils.fromRequestToAnimeFilters(request);

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

        if(SecurityUtils.getAuthenticatedUser(request) != null) {
            for (MediaContentDTO mediaContent : mediaContentList.getEntries()) {
                try {
                    String userId = SecurityUtils.getAuthenticatedUser(request).getId();
                    mediaContent.setIsLiked(mediaContentService.isLiked(userId, mediaContent.getId(), isManga ? MediaContentType.MANGA : MediaContentType.ANIME));
                } catch (BusinessException e) {
                    logger.error("Error occurred during search", e);
                    request.getRequestDispatcher("/error.jsp").forward(request, response);
                }
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
        String targetJSP = "/WEB-INF/jsp/manga_main_page.jsp";
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;

        request.setAttribute("page", page);
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleToggleLike(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        String mediaId = request.getParameter("mediaId");
        try {
            //if is liked: unlike, else like
            if (mediaContentService.isLiked(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME)) {
                mediaContentService.removeLike(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);
                request.setAttribute("isLiked", false);
            } else {
                mediaContentService.addLike(userId, mediaId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);
                request.setAttribute("isLiked", true);
            }
        } catch (BusinessException e) {
            logger.error("Error occurred during like operation", e);
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"isLiked\": " + request.getAttribute("isLiked") + "}");
    }

    private void handleSuggestion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String userId = SecurityUtils.getAuthenticatedUser(request).getId();
        boolean isManga = (boolean) request.getAttribute("isManga");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            List<? extends MediaContentDTO> suggestions = mediaContentService.getSuggestedMediaContent(userId, isManga ? MediaContentType.MANGA : MediaContentType.ANIME, 5);
            JsonNode suggestionsNode = objectMapper.valueToTree(suggestions);
            jsonResponse.set("suggestions", suggestionsNode);
        } catch (BusinessException e) {
            logger.error("Error occurred during suggestion operation", e);
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleFollow(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        Optional<String> followerUserId = Optional.ofNullable(request.getParameter("followerUserId"));
        if (followerUserId.isEmpty()) {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        Optional<String> followingUserId = Optional.ofNullable(request.getParameter("followingUserId"));
        if (followingUserId.isEmpty()) {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        try {
            String followerId = followerUserId.get();
            String followingId = followingUserId.get();
            userService.follow(followerId, followingId);
        } catch (BusinessException e) {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleUnfollow(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        Optional<String> followerUserId = Optional.ofNullable(request.getParameter("followerUserId"));
        if (followerUserId.isEmpty()) {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        Optional<String> followingUserId = Optional.ofNullable(request.getParameter("followingUserId"));
        if (followingUserId.isEmpty()) {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        try {
            String followerId = followerUserId.get();
            String followingId = followingUserId.get();
            userService.unfollow(followerId, followingId);
        } catch (BusinessException e) {
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

}
