package it.unipi.lsmsd.fnf.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@WebServlet("/manager")
public class ManagerServlet extends HttpServlet {

    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final UserService userService = ServiceLocator.getUserService();
    private static final ReviewService reviewService = ServiceLocator.getReviewService();
    private static final Logger logger = LoggerFactory.getLogger(ManagerServlet.class);


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
            case "getUserDefaultAnalytics" -> handleGetUserDefaultAnalytics(request, response);
            case "getBestCriteria" -> handleBestCriteria(request, response);
            case "averageRatingByYear" -> handleMediaContentAverageRatingByYear(request, response); // Asynchronous request for anime and manga
            case "averageRatingByMonth" -> handleMediaContentAverageRatingByMonth(request, response); // Asynchronous request for anime and manga
            case "distribution" -> handleUsersDistribution(request, response); // Asynchronous request for user
            case "averageAppRatingByCriteria" -> handleUsersAverageAppRatingCriteria(request, response); // Asynchronous request for user
            case "averageAppRatingByAgeRange" -> handleUsersAverageAppRatingAgeRange(request, response); // Asynchronous request for user
            case "trendMediaContentByYear" ->  handleTrendMediaContentByYear(request, response); // Asynchronous request for anime and manga
            case "show_info" -> handleShowInfo(request, response);
            case "update_info" -> handleUpdateInfo(request,response);
            case "delete_media" -> handleDeleteMedia(request,response);
            case null, default -> handleLoadPage(request, response);
        }
    }

    public void handleGetAnimeDefaultAnalytics(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        //Create threads
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
                return mediaContentService.getTrendMediaContentByYear(2024, MediaContentType.ANIME);
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
            jsonResponse.set("bestAnimeCriteria", bestAnimeCriteriaJson);
            jsonResponse.set("trendAnimeByYear", trendAnimeByYearJson);
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
                return mediaContentService.getTrendMediaContentByYear(2024, MediaContentType.MANGA);
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
            jsonResponse.set("bestMangaCriteria", bestMangaCriteriaJson);
            jsonResponse.set("trendAnimeByYear", trendMangaByYearJson);
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


    public void handleGetUserDefaultAnalytics(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException, IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        //Create threads
        //Thread 1: getDistribution Map<String, Integer> getDistribution

        Future<Map<String, Integer>> distributionFuture = executorService.submit(() -> {
            try {
                return userService.getDistribution("location");
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Thread 2: averageAppRatingByCriteria Map<String, Double> averageAppRating
        Future<Map<String, Double>> averageAppRatingFuture = executorService.submit(() -> {
            try {
                return userService.averageAppRating("location");
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        });

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        try {
            Map<String, Integer> distribution = distributionFuture.get();
            Map<String, Double> averageAppRating = averageAppRatingFuture.get();
            jsonResponse.put("success", true);

            JsonNode distributionJson = objectMapper.valueToTree(distribution);
            JsonNode averageAppRatingJson = objectMapper.valueToTree(averageAppRating);
            jsonResponse.set("UserDistribution", distributionJson);
            jsonResponse.set("averageUserAppRating", averageAppRatingJson);
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

    //Asynchronous request
    private void handleBestCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String criteria = request.getParameter("criteria");
        String section = request.getParameter("section");
        int page = Integer.parseInt(request.getParameter("page"));

        if (section == null) {
            jsonResponse.put("error", "Section not specified");
        } else if (section.equals("anime") && !(criteria.equals("tags") || criteria.equals("genres") || criteria.equals("themes") || criteria.equals("demographics"))) {
            jsonResponse.put("error", "Criteria not supported");
        } else if (section.equals("manga") && !(criteria.equals("genres") || criteria.equals("themes") || criteria.equals("demographics"))) {
            jsonResponse.put("error", "Criteria not supported");
        } else {
            try {
                Map<String, Double> bestCriteria = mediaContentService.getBestCriteria(criteria, page, section.equals("manga") ? MediaContentType.MANGA : MediaContentType.ANIME);
                jsonResponse.put("success", true);
                JsonNode bestCriteriaJson = objectMapper.valueToTree(bestCriteria);
                jsonResponse.set("bestCriteria", bestCriteriaJson);
            } catch (BusinessException e) {
                jsonResponse.put("error", "An error occurred while processing the request");
            }
        }

        // Write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    //Asynchronous request
    private void handleMediaContentAverageRatingByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaContentId = request.getParameter("mediaContentId");
        int startYear = Integer.parseInt(request.getParameter("startYear"));
        int endYear = Integer.parseInt(request.getParameter("endYear"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Map<String, Double> averageRatingByYear = reviewService.getMediaContentRatingByYear(isManga ? MediaContentType.MANGA : MediaContentType.ANIME, mediaContentId, startYear, endYear);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("mediaContentId", mediaContentId);
            jsonResponse.addProperty("startYear", startYear);
            jsonResponse.addProperty("endYear", endYear);
            JsonElement ratingByYearJson = new Gson().toJsonTree(averageRatingByYear);
            jsonResponse.add("averageRatingByYear", ratingByYearJson);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Asynchronous request

    private void handleMediaContentAverageRatingByMonth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaContentId = request.getParameter("mediaContentId");
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Map<String, Double> averageRatingByMonth = reviewService.getMediaContentRatingByMonth(isManga ? MediaContentType.MANGA : MediaContentType.ANIME, mediaContentId, year);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("mediaContentId", mediaContentId);
            jsonResponse.addProperty("year", year);
            JsonElement ratingByMonthJson = new Gson().toJsonTree(averageRatingByMonth);
            jsonResponse.add("averageRatingByMonth", ratingByMonthJson);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Asynchronous request
    private void handleUsersDistribution(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String criteria = request.getParameter("criteria");
        UserService userService = ServiceLocator.getUserService();

        try {
            if (!(criteria.equals("gender") || criteria.equals("location") || criteria.equals("birthday") || criteria.equals("joined_on"))) {
                throw new IllegalArgumentException("Criteria not supported");
            }
            Map<String, Integer> distribution = userService.getDistribution(criteria);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("criteria", criteria);
            JsonObject distributionJson = new JsonObject();
            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                distributionJson.addProperty(entry.getKey(), entry.getValue());
            }
            jsonResponse.add("distribution", distributionJson);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Asynchronous request
    private void handleUsersAverageAppRatingCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String criteria = request.getParameter("criteria");
        UserService userService = ServiceLocator.getUserService();

        try {
            Map<String, Double> averageAppRatingByCriteria = userService.averageAppRating(criteria);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("criteria", criteria);
            JsonObject averageAppRatingByCriteriaJson = new JsonObject();
            for (Map.Entry<String, Double> entry : averageAppRatingByCriteria.entrySet()) {
                averageAppRatingByCriteriaJson.addProperty(entry.getKey(), entry.getValue());
            }
            jsonResponse.add("averageAppRatingByCriteria", averageAppRatingByCriteriaJson);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Asynchronous request
    private void handleUsersAverageAppRatingAgeRange(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();

        try {
            Map<String, Double> averageAppRatingByAgeRange = userService.averageAppRatingByAgeRange();

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("averageAppRatingByAgeRange", new Gson().toJsonTree(averageAppRatingByAgeRange));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Asynchronous request

    private void handleTrendMediaContentByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            Map<? extends MediaContentDTO, Integer> trendMediaContentByYear = mediaContentService.getTrendMediaContentByYear(year, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("trendMediaContentByYear", new Gson().toJsonTree(trendMediaContentByYear));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Asynchronous request
    private void handleTrendMediaContentByLikes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<? extends MediaContentDTO> trendMediaContentByLikes = mediaContentService.getMediaContentTrendByLikes(isManga ? MediaContentType.MANGA : MediaContentType.ANIME);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("trendMediaContentByLikes", new Gson().toJsonTree(trendMediaContentByLikes));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    //Handler for each type of task
    //Examples: analytics requests (add media content, remove media content, update media content and search will be done by Fey)


    private void handleShowInfo(HttpServletRequest request, HttpServletResponse response) {
        if ((boolean) request.getAttribute("isManga")) {
            try {
                Manga manga = (Manga) mediaContentService.getMediaContentById(request.getParameter("mediaId"), MediaContentType.MANGA);

                // Set the content type and write the JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                ObjectNode jsonResponse = objectMapper.createObjectNode();
                JsonNode mangaNode = objectMapper.valueToTree(manga);
                jsonResponse.set("manga", mangaNode);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse.toString());
            } catch (BusinessException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handleUpdateInfo(HttpServletRequest request, HttpServletResponse response){
        if ((boolean) request.getAttribute("isManga")) {
            try {
                // Retrieve updated manga information from the request parameters
                String mangaId = request.getParameter("mangaId");
                String title = request.getParameter("title");
                String author = request.getParameter("author");
                String url = request.getParameter("url");
                // You can retrieve other fields similarly

                // Create a Manga object with the updated information
                Manga manga = new Manga();
                manga.setTitle(title);
                manga.setImageUrl(url);
                // Set other fields as needed

                // Update manga information using the MediaContentService
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                mediaContentService.updateMediaContent(manga);

                // Send a success response back to the client
                response.setContentType("text/plain");
                response.getWriter().write("Manga information updated successfully.");
            } catch (BusinessException | IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    public void handleDeleteMedia(HttpServletRequest request, HttpServletResponse response){
        if ((boolean) request.getAttribute("isManga")){
            try{
                String mediaId = request.getParameter("mediaId");
                if (mediaId != null){

                    mediaContentService.deleteMediaContent(mediaId, MediaContentType.MANGA);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Manga deleted successfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Invalid manga ID");
                }
            }catch (BusinessException | IOException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    public void handleLoadPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String targetJSP = "/WEB-INF/jsp/manager.jsp";

        try {
            Map<String, Integer> distribution = userService.getDistribution("location");
            request.setAttribute("distribution", distribution);
            Map<String, Double> averageAppRating = userService.averageAppRating("location");
            request.setAttribute("averageAppRating", averageAppRating);
            request.setAttribute("page", "user");
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}
