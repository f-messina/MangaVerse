package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.AnimeDAOImpl;
import it.unipi.lsmsd.fnf.dao.mongo.MangaDAOImpl;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.utils.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@WebServlet("/mainPage")
public class MainPageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private String standardTargetJSP;
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
            HttpSession session = request.getSession(true);
            if (session.getAttribute(Constants.AUTHENTICATED_USER_KEY)!= null){
                standardTargetJSP = "main--registered-user.jsp";
            }else{
                standardTargetJSP = "main-page.jsp";
            }
            String targetJSP = standardTargetJSP;
            switch (action){
                case "search" -> handleSearch(request,response);
                case "filter" -> handleFilter(request,response);
                case "friends_activities" -> handleFriendsActivities(request,response);
                case "suggestions" -> handleSuggestion(request,response);
                case null, default -> request.getRequestDispatcher(targetJSP).forward(request,response);
            }

    }
    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        String searchTerm = request.getParameter("searchTerm");
        String targetJSP = standardTargetJSP; // default to main-page.jsp

        if (type != null && searchTerm != null) {
            switch (type) {
                case "manga":
                    // Search for manga by name
                    try {
                        MangaDAOImpl mangaDAO = new MangaDAOImpl();
                        Map<String, Object> filters = Map.of("title", searchTerm);
                        PageDTO<MangaDTO> mangaPage = mangaDAO.search(filters, null, 1);
                        request.setAttribute("mangaPage", mangaPage);
                        targetJSP = "manga-search-result.jsp"; // Set the target JSP to show manga search results
                    } catch (DAOException e) {
                        logger.error("Error occurred during manga search", e);
                    }
                    break;
                case "anime":
                    // Search for anime by name
                    try {
                        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
                        Map<String, Object> filters = Map.of("title", searchTerm);
                        PageDTO<AnimeDTO> animePage = animeDAO.search(filters, null, 1);
                        request.setAttribute("animePage", animePage);
                        targetJSP = "anime-search-result.jsp"; // Set the target JSP to show anime search results
                    } catch (DAOException e) {
                        logger.error("Error occurred during anime search", e);
                        // Handle DAOException
                    }
                    break;
                default:
                    // Invalid search type
                    logger.warn("Invalid search type: " + type);
            }
        } else {
            // Invalid search parameters
            logger.warn("Invalid search parameters");
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String type = request.getParameter("type");
        String targetJSP = standardTargetJSP; // default to main-page.jsp

        if (type != null) {
            switch (type) {
                case "manga":
                    // Filter manga
                    try {
                        MangaDAOImpl mangaDAO = new MangaDAOImpl();
                        Map<String, Object> filters = new HashMap<>();

                        String mangaAuthor = request.getParameter("mangaAuthor");
                        if (mangaAuthor != null && !mangaAuthor.isEmpty()) {
                            filters.put("author", mangaAuthor);
                        }
                        String[] mangaGenres = request.getParameterValues("mangaGenre");
                        if (mangaGenres != null && mangaGenres.length > 0) {
                             filters.put("genres", mangaGenres);
                        }
                        String [] mangaTypes = request.getParameterValues("mangaType");
                        if (mangaTypes != null && mangaTypes.length > 0) {
                            filters.put("types", mangaTypes);
                        }
                        String[] mangaPublishingStatus = request.getParameterValues("mangaPublishingStatus");
                        if (mangaPublishingStatus != null && mangaPublishingStatus.length>0){
                            filters.put("publishing status", mangaPublishingStatus);
                        }
                        String mangaStartYear = request.getParameter("mangaStartYear");
                        String mangaEndYear = request.getParameter("mangaEndYear");
                        if (mangaStartYear != null && !mangaStartYear.isEmpty()) {
                            filters.put("startYear", mangaStartYear);
                        }
                        if (mangaEndYear != null && !mangaEndYear.isEmpty()) {
                            filters.put("endYear", mangaEndYear);
                        }
                        PageDTO<MangaDTO> mangaPage = mangaDAO.search(filters, null, 1);
                        request.setAttribute("mangaPage", mangaPage);
                        targetJSP = "manga-search-result.jsp"; // Set the target JSP to show manga search results
                    } catch (DAOException e) {
                        logger.error("Error occurred during manga filtering", e);
                    }
                    break;
                case "anime":
                    // Filter anime
                    try {
                        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
                        Map<String, Object> filters = new HashMap<>();

                        String [] animeTypes = request.getParameterValues("animeType");
                        if (animeTypes != null && animeTypes.length > 0) {
                            filters.put("types", animeTypes);
                        }
                         String animePublishingStatus = request.getParameter("animePublishingStatus");
                         if (animePublishingStatus != null && !animePublishingStatus.isEmpty()) {
                             filters.put("publishingStatus", animePublishingStatus);
                         }
                        String animeStartYear = request.getParameter("animeStartYear");
                        String animeEndYear = request.getParameter("animeEndYear");
                        if (animeStartYear != null && !animeStartYear.isEmpty()) {
                            filters.put("startYear", animeStartYear);
                        }
                        if (animeEndYear != null && !animeEndYear.isEmpty()) {
                            filters.put("endYear", animeEndYear);
                        }

                        // Perform the search with the applied filters
                        PageDTO<AnimeDTO> animePage = animeDAO.search(filters, null, 1);
                        request.setAttribute("animePage", animePage);
                        targetJSP = "anime-search-result.jsp"; // Set the target JSP to show anime search results
                    } catch (DAOException e) {
                        logger.error("Error occurred during anime filtering", e);
                    }
                    break;
                default:
                    // Invalid search type
                    logger.warn("Invalid search type: " + type);
            }
        } else {
            // Invalid search parameters
            logger.warn("Invalid search parameters");
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }

    private void handleFriendsActivities(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

    }
    private void handleSuggestion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

    }
}

