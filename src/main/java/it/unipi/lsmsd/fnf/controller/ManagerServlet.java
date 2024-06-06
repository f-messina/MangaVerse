package it.unipi.lsmsd.fnf.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.neo4j.driver.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@WebServlet("/manager")
public class ManagerServlet extends HttpServlet {

    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final UserService userService = ServiceLocator.getUserService();
    private static final ReviewService reviewService = ServiceLocator.getReviewService();


    //DoGet and DoPost methods as the other servlets
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //Process request method to execute task based on the type
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ExecutionException, InterruptedException {
        String action = request.getParameter("action");

        switch (action) {
            case "getAnimeDefaultAnalytics" -> handleGetAnimeDefaultAnalytics(request, response);
            case "getMangaDefaultAnalytics" -> handleGetMangaDefaultAnalytics(request, response);
            case "getBestCriteria" -> handleBestCriteria(request, response);
            case "getAverageRatingByYear" -> handleMediaContentAverageRatingByYear(request, response); // Asynchronous request for anime and manga
            case "getAverageRatingByMonth" -> handleMediaContentAverageRatingByMonth(request, response); // Asynchronous request for anime and manga
            case "getDistribution" -> handleUsersDistribution(request, response); // Asynchronous request for user
            case "getAverageAppRatingByCriteria" -> handleUsersAverageAppRatingCriteria(request, response); // Asynchronous request for user
            case "getTrendMediaContentByYear" ->  handleTrendMediaContentByYear(request, response); // Asynchronous request for anime and manga
            case null, default -> handleLoadPage(request, response);
        }
    }

    public void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoggedUserDTO loggedUser = SecurityUtils.getAuthenticatedUser(request);

        if (loggedUser == null) {
            response.sendRedirect("auth");
            return;

        } else if (!loggedUser.getType().equals(UserType.MANAGER)) {
            response.sendRedirect("profile");
            return;
        }

        try {
            Map<String, Integer> distribution = userService.getDistribution("gender");
            request.setAttribute("distribution", distribution);
            Map<String, Double> averageAppRating = userService.averageAppRating("gender");
            request.setAttribute("averageAppRating", averageAppRating);
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/manager.jsp").forward(request, response);
    }

    public void handleGetAnimeDefaultAnalytics(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        //Thread 1: getBestCriteria: tags for anime, genres for manga Map<String, Double> getBestCriteria
        Future<Map<String, Double>> bestAnimeCriteriaFuture = executorService.submit(() -> {
            try {
                return mediaContentService.getBestCriteria("tags", 1, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        });

        //Thread 2: getTrendMediaContentByYear with year = 2021 List<? extends MediaContentDTO> getTrendMediaContentByYear
        Future<Map<MediaContentDTO, Integer>> trendAnimeByYearFuture = executorService.submit(() -> {
            try {
                return mediaContentService.getMediaContentTrendByYear(2024, Constants.PAGE_SIZE, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        });

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            Map<String, Double> bestAnimeCriteria = bestAnimeCriteriaFuture.get();
            Map<MediaContentDTO, Integer> trendAnimeByYear = trendAnimeByYearFuture.get();
            jsonResponse.put("success", true);

            JsonNode bestAnimeCriteriaJson = objectMapper.valueToTree(bestAnimeCriteria);
            JsonNode trendAnimeByYearJson = objectMapper.valueToTree(trendAnimeByYear);
            jsonResponse.set("bestCriteria", bestAnimeCriteriaJson);
            jsonResponse.set("trendByYear", trendAnimeByYearJson);
        } catch (InterruptedException | ExecutionException e) {
            jsonResponse.put("error", "An error occurred while processing the request");
        }

        // Shut down the ExecutorService
        executorService.shutdown();

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    public void handleGetMangaDefaultAnalytics(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        //Create threads
        //Thread 1: getBestCriteria: genres for manga Map<String, Double> getBestCriteria
        Future<Map<String, Double>> bestAnimeCriteriaFuture = executorService.submit(() -> {
            try {
                return mediaContentService.getBestCriteria("genres", 1, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        });


        //Thread 2: getTrendMediaContentByYear with year = 2021 List<? extends MediaContentDTO> getTrendMediaContentByYear
        Future<Map<MediaContentDTO, Integer>> trendMangaByYearFuture = executorService.submit(() -> {
            try {
                return mediaContentService.getMediaContentTrendByYear(2024, Constants.PAGE_SIZE, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        });

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        try {
            Map<String, Double> bestMangaCriteria = bestAnimeCriteriaFuture.get();
            Map<MediaContentDTO, Integer> trendMangaByYear = trendMangaByYearFuture.get();
            jsonResponse.put("success", true);

            JsonNode bestMangaCriteriaJson = objectMapper.valueToTree(bestMangaCriteria);
            JsonNode trendMangaByYearJson = objectMapper.valueToTree(trendMangaByYear);
            jsonResponse.set("bestCriteria", bestMangaCriteriaJson);
            jsonResponse.set("trendByYear", trendMangaByYearJson);
        } catch (InterruptedException | ExecutionException e) {
            jsonResponse.put("error", "An error occurred while processing the request");
        }

        // Shut down the ExecutorService
        executorService.shutdown();

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }


    private void handleBestCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");
        String mediaType = request.getParameter("type");
        int page = Integer.parseInt(request.getParameter("page"));

        if (mediaType == null) {
            jsonResponse.put("error", "Section not specified");
        } else {
            try {
                Map<String, Double> bestCriteria = mediaContentService.getBestCriteria(criteria, page, mediaType.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME);
                if(bestCriteria.isEmpty()){
                    jsonResponse.put("error", "No data available");
                }
                jsonResponse.put("success", true);
                JsonNode bestCriteriaJson = objectMapper.valueToTree(bestCriteria);
                jsonResponse.set("results", bestCriteriaJson);
            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.INVALID_INPUT)) {
                    jsonResponse.put("error", "Invalid criteria");
                } else {
                    jsonResponse.put("error", "An error occurred while processing the request");
                }
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleMediaContentAverageRatingByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        int startYear = Integer.parseInt(request.getParameter("startYear"));
        int endYear = Integer.parseInt(request.getParameter("endYear"));
        String mediaType = request.getParameter("type");
        String mediaId = request.getParameter("mediaId");

        int currentYear = Year.now().getValue();

        if (mediaType == null) {
            jsonResponse.put("error", "Section not specified");
        }
        //if start year or end year is greater than current year, throw exception
        else if (startYear < 0 || endYear < 0 || endYear > currentYear || startYear > endYear) {
            jsonResponse.put("error", "Invalid year range");
        } else {
            try {
                Map<String, Double> averageRatingByYear = reviewService.getMediaContentRatingByYear(mediaType.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME, mediaId, startYear, endYear);
                if(averageRatingByYear.isEmpty()){
                    jsonResponse.put("error", "No data available");
                } else {
                    jsonResponse.put("success", true);
                    JsonNode averageRatingByYearJson = objectMapper.valueToTree(averageRatingByYear);
                    jsonResponse.set("results", averageRatingByYearJson);
                }

            } catch (BusinessException e) {
                jsonResponse.put("error", "An error occurred while processing the request");
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private void handleMediaContentAverageRatingByMonth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String mediaId = request.getParameter("mediaId");
        int year = Integer.parseInt(request.getParameter("year"));
        String mediaType = request.getParameter("type");

        int currentYear = Year.now().getValue();

        if (mediaType == null) {
            jsonResponse.put("error", "Section not specified");
        }
        else if (year < 0 || year > currentYear) {
            throw new IllegalArgumentException("Year must be valid");
        }
        else if (mediaId == null) {
            jsonResponse.put("error", "Media content not specified");
        }
        else {
            try {
                Map<String, Double> averageRatingByMonth = reviewService.getMediaContentRatingByMonth(mediaType.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME, mediaId, year);
                if (averageRatingByMonth.isEmpty()) {
                    jsonResponse.put("error", "No data available");
                } else {
                    jsonResponse.put("success", true);
                    JsonNode averageRatingByMonthJson = objectMapper.valueToTree(averageRatingByMonth);
                    jsonResponse.set("results", averageRatingByMonthJson);
                }

            } catch (BusinessException e) {
                jsonResponse.put("error", "An error occurred while processing the request");
            }


        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

    }

    private void handleUsersDistribution(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");
        UserService userService = ServiceLocator.getUserService();



        if (!(criteria.equals("gender") || criteria.equals("location") || criteria.equals("birthday") || criteria.equals("joined_on"))) {
            jsonResponse.put("error", "Criteria not supported");
        }
        else {
            try {
                Map<String, Integer> distribution = userService.getDistribution(criteria);
                jsonResponse.put("success", true);

                JsonNode distributionJson = objectMapper.valueToTree(distribution);
                jsonResponse.set("results", distributionJson);
            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.NOT_FOUND)) {
                    jsonResponse.put("noData", "No data available");
                } else {
                    jsonResponse.put("error", "An error occurred while processing the request");

                }
            }

        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

    }

    private void handleUsersAverageAppRatingCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");

        if (!(criteria.equals("gender") || criteria.equals("location") || criteria.equals("age"))) {
            throw new IllegalArgumentException("Criteria not supported");
        }
        else {
            try {
                Map<String, Double> averageAppRating = userService.averageAppRating(criteria);
                jsonResponse.put("success", true);

                JsonNode averageAppRatingJson = objectMapper.valueToTree(averageAppRating);
                jsonResponse.set("results", averageAppRatingJson);
            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.NOT_FOUND)) {
                    jsonResponse.put("noData", "No data available");
                } else {
                    jsonResponse.put("error", "An error occurred while processing the request");
                }
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    //Asynchronous request
    private void handleTrendMediaContentByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        int year = Integer.parseInt(request.getParameter("year"));
        String section = request.getParameter("section");
        int currentYear = Year.now().getValue();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        if (section == null) {
            jsonResponse.put("error", "Section not specified");
        }
        else if (year < 0 || year > currentYear) {
            throw new IllegalArgumentException("Year must be valid");
        }
        else {
            try {
                Map<MediaContentDTO, Integer> trendMediaContentByYear = mediaContentService.getMediaContentTrendByYear(year, Constants.PAGE_SIZE, section.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME);
                if(trendMediaContentByYear.isEmpty()){
                    jsonResponse.put("error", "No data available");
                }
                jsonResponse.put("success", true);

                JsonNode trendMediaContentByYearJson = objectMapper.valueToTree(trendMediaContentByYear);
                jsonResponse.set("trendMediaContentByYear", trendMediaContentByYearJson);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

    }
}
