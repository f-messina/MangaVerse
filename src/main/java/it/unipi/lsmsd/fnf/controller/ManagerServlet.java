package it.unipi.lsmsd.fnf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.unipi.lsmsd.fnf.controller.exception.NotAuthorizedException;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.enums.UserType;
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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Year;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Servlet for handling manager operations and loading the manager page.
 */
@WebServlet("/manager")
public class ManagerServlet extends HttpServlet {

    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final UserService userService = ServiceLocator.getUserService();
    private static final ReviewService reviewService = ServiceLocator.getReviewService();

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

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ExecutionException, InterruptedException {
        switch (request.getParameter("action")) {
            case "getAnimeDefaultAnalytics" -> handleGetAnimeDefaultAnalytics(request, response);
            case "getMangaDefaultAnalytics" -> handleGetMangaDefaultAnalytics(request, response);
            case "getBestCriteria" -> handleBestCriteria(request, response);
            case "getAverageRatingByYear" -> handleMediaContentAverageRatingByYear(request, response);
            case "getAverageRatingByMonth" -> handleMediaContentAverageRatingByMonth(request, response);
            case "getDistribution" -> handleUsersDistribution(request, response);
            case "getAverageAppRatingByCriteria" -> handleUsersAverageAppRatingCriteria(request, response);
            case "getTrendMediaContentByYear" ->  handleTrendMediaContentByYear(request, response);
            case null, default -> handleLoadPage(request, response);
        }
    }

    public void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoggedUserDTO loggedUser = SecurityUtils.getAuthenticatedUser(request);

        // Redirect to the auth page if the user is not authenticated
        if (loggedUser == null) {
            response.sendRedirect("auth");
            return;

        // Redirect to the profile page if the user is not a manager
        } else if (!loggedUser.getType().equals(UserType.MANAGER)) {
            response.sendRedirect("profile");
            return;
        }

        String targetJSP = "WEB-INF/jsp/manager.jsp";
        try {
            // Get the distribution of users by gender
            Map<String, Integer> distribution = userService.getDistribution("gender");
            request.setAttribute("distribution", distribution);

            // Get the average app rating by gender
            Map<String, Double> averageAppRating = userService.averageAppRating("gender");
            request.setAttribute("averageAppRating", averageAppRating);

        } catch (BusinessException e) {
            targetJSP = "WEB-INF/jsp/error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
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
            // Check if the user is authorized to perform the operation
            SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

            // Get the results from the threads
            Map<String, Double> bestAnimeCriteria = bestAnimeCriteriaFuture.get();
            Map<MediaContentDTO, Integer> trendAnimeByYear = trendAnimeByYearFuture.get();

            // Create the JSON response with the results and the success flag
            JsonNode bestAnimeCriteriaJson = objectMapper.valueToTree(bestAnimeCriteria);
            JsonNode trendAnimeByYearJson = objectMapper.valueToTree(trendAnimeByYear);
            jsonResponse.set("bestCriteria", bestAnimeCriteriaJson);
            jsonResponse.set("trendByYear", trendAnimeByYearJson);
            jsonResponse.put("success", true);

        } catch (InterruptedException | ExecutionException e) {
            jsonResponse.put("error", "An error occurred while processing the request");
        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User is not authorized to perform this operation");
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
            // Check if the user is authorized to perform the operation
            SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

            // Get the results from the threads
            Map<String, Double> bestMangaCriteria = bestAnimeCriteriaFuture.get();
            Map<MediaContentDTO, Integer> trendMangaByYear = trendMangaByYearFuture.get();

            // Create the JSON response with the results and the success flag
            JsonNode bestMangaCriteriaJson = objectMapper.valueToTree(bestMangaCriteria);
            JsonNode trendMangaByYearJson = objectMapper.valueToTree(trendMangaByYear);
            jsonResponse.set("bestCriteria", bestMangaCriteriaJson);
            jsonResponse.set("trendByYear", trendMangaByYearJson);
            jsonResponse.put("success", true);

        } catch (InterruptedException | ExecutionException e) {
            jsonResponse.put("error", "An error occurred while processing the request");
        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User is not authorized to perform this operation");
        }

        // Shut down the ExecutorService
        executorService.shutdown();

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the best criteria for the specified media type
    // REQUEST PARAMETERS:  criteria, type, page
    // RESPONSE:            JSON object with the map of criteria with the relative score and a success flag
    //                      or an error message if the operation failed
    private void handleBestCriteria(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");
        String mediaType = request.getParameter("type");
        int page = Integer.parseInt(request.getParameter("page"));

        if (StringUtils.isBlank(mediaType)) {
            jsonResponse.put("error", "Section not specified");
        } else {
            try {
                SecurityUtils.isUserAuthorized(request, UserType.MANAGER);
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
            } catch (NotAuthorizedException e) {
                jsonResponse.put("error", "User is not authorized to perform this operation");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the average rating by year for the specified media content
    // REQUEST PARAMETERS:  startYear, endYear, type, mediaId
    // RESPONSE:            JSON object with the map of years with the relative average rating and a success flag
    //                      or an error message if the operation failed
    private void handleMediaContentAverageRatingByYear(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        int startYear = Integer.parseInt(request.getParameter("startYear"));
        int endYear = Integer.parseInt(request.getParameter("endYear"));
        String mediaType = request.getParameter("type");
        String mediaId = request.getParameter("mediaId");

        if (StringUtils.isBlank(mediaType)) {
            jsonResponse.put("error", "Section not specified");

        } else if (StringUtils.isBlank(mediaId)) {
            jsonResponse.put("error", "Media content not specified");
        } else {
            try {
                // Check if the user is authorized to perform the operation
                SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

                // Get the average rating by year for the specified media content
                Map<String, Double> averageRatingByYear = reviewService.getMediaContentRatingByYear(mediaType.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME, mediaId, startYear, endYear);

                // Create the JSON response with the average rating data and the success flag if the data is not empty
                if(averageRatingByYear.isEmpty()){
                    jsonResponse.put("error", "No data available");
                } else {
                    jsonResponse.put("success", true);
                    JsonNode averageRatingByYearJson = objectMapper.valueToTree(averageRatingByYear);
                    jsonResponse.set("results", averageRatingByYearJson);
                }

            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.INVALID_INPUT))
                    jsonResponse.put("error", "Invalid input");
                else
                    jsonResponse.put("error", "An error occurred while processing the request");

            } catch (NotAuthorizedException e) {
                jsonResponse.put("error", "User is not authorized to perform this operation");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the average rating by month for the specified media content
    // REQUEST PARAMETERS:  mediaId, year, type
    // RESPONSE:            JSON object with the map of months and average rating and a success flag
    //                      or an error message if the operation failed
    private void handleMediaContentAverageRatingByMonth(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
                // Check if the user is authorized to perform the operation
                SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

                // Get the average rating by month for the specified media content
                Map<String, Double> averageRatingByMonth = reviewService.getMediaContentRatingByMonth(mediaType.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME, mediaId, year);

                // Create the JSON response with the average rating data and the success flag if the data is not empty
                if (averageRatingByMonth.isEmpty()) {
                    jsonResponse.put("error", "No data available");
                } else {
                    jsonResponse.put("success", true);
                    JsonNode averageRatingByMonthJson = objectMapper.valueToTree(averageRatingByMonth);
                    jsonResponse.set("results", averageRatingByMonthJson);
                }

            } catch (BusinessException e) {
                if (e.getType().equals(BusinessExceptionType.INVALID_INPUT))
                    jsonResponse.put("error", "Invalid input");
                else
                    jsonResponse.put("error", "An error occurred while processing the request");

            } catch (NotAuthorizedException e) {
                jsonResponse.put("error", "User is not authorized to perform this operation");
            }

        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the distribution of users by the specified criteria
    // REQUEST PARAMETERS:  criteria
    // RESPONSE:            JSON object with the map of criteria with the relative number of users and a success flag
    //                      or an error message if the operation failed
    private void handleUsersDistribution(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");
        UserService userService = ServiceLocator.getUserService();

        try {
            // Check if the user is authorized to perform the operation
            SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

            // Get the distribution of users by the specified criteria
            Map<String, Integer> distribution = userService.getDistribution(criteria);

            // Create the JSON response with the distribution data and the success flag
            jsonResponse.put("success", true);
            JsonNode distributionJson = objectMapper.valueToTree(distribution);
            jsonResponse.set("results", distributionJson);

        } catch (BusinessException e) {
            switch (e.getType()) {
                case INVALID_INPUT -> jsonResponse.put("error", "Criteria not supported");
                case NOT_FOUND -> jsonResponse.put("noData", "No data available");
                default -> jsonResponse.put("error", "An error occurred while processing the request");
            }

        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User is not authorized to perform this operation");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the average app rating by the specified criteria
    // REQUEST PARAMETERS:  criteria
    // RESPONSE:            JSON object with the map of criteria with the relative average app rating and a success flag
    //                      or an error message if the operation failed
    private void handleUsersAverageAppRatingCriteria(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");

        try {
            // Check if the user is authorized to perform the operation
            SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

            // Get the average app rating by the specified criteria
            Map<String, Double> averageAppRating = userService.averageAppRating(criteria);

            // Create the JSON response with the average app rating data and the success flag
            jsonResponse.put("success", true);
            JsonNode averageAppRatingJson = objectMapper.valueToTree(averageAppRating);
            jsonResponse.set("results", averageAppRatingJson);

        } catch (BusinessException e) {
            switch (e.getType()) {
                case INVALID_INPUT -> jsonResponse.put("error", "Criteria not supported");
                case NOT_FOUND -> jsonResponse.put("noData", "No data available");
                default -> jsonResponse.put("error", "An error occurred while processing the request");
            }

        } catch (NotAuthorizedException e) {
            jsonResponse.put("error", "User is not authorized to perform this operation");
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    // Get the trend of media content by year for the specified media type
    // REQUEST PARAMETERS:  year, type
    // RESPONSE:            JSON object with the map of media content with the relative number of reviews and a success flag
    //                      or an error message if the operation failed
    private void handleTrendMediaContentByYear(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        int year = Integer.parseInt(request.getParameter("year"));
        String mediaType = request.getParameter("type");

        int currentYear = Year.now().getValue();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        if (mediaType == null) {
            jsonResponse.put("error", "Section not specified");
        }
        else if (year < 0 || year > currentYear) {
            throw new IllegalArgumentException("Year must be valid");
        }
        else {
            try {
                // Check if the user is authorized to perform the operation
                SecurityUtils.isUserAuthorized(request, UserType.MANAGER);

                // Get the trend of media content by year for the specified media type
                Map<MediaContentDTO, Integer> trendMediaContentByYear = mediaContentService.getMediaContentTrendByYear(year, Constants.PAGE_SIZE, mediaType.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME);
                if(trendMediaContentByYear.isEmpty()){
                    jsonResponse.put("error", "No data available");
                }

                // Create the JSON response with the trend data and the success flag
                Map<String, Integer> trendMediaContentByYearMapSerialized = new LinkedHashMap<>();
                trendMediaContentByYear.forEach((key, value) -> {
                    try {
                        trendMediaContentByYearMapSerialized.put(objectMapper.writeValueAsString(key), value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
                jsonResponse.put("success", true);
                JsonNode trendMediaContentByYearJson = objectMapper.valueToTree(trendMediaContentByYearMapSerialized);
                jsonResponse.set("results", trendMediaContentByYearJson);

            } catch (BusinessException e) {
                throw new RuntimeException(e);
            } catch (NotAuthorizedException e) {
                jsonResponse.put("error", "User is not authorized to perform this operation");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

    }
}
