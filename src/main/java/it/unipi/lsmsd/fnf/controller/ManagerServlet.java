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
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.time.Year;


@WebServlet(urlPatterns = {"/manager", "/manager/manga", "/manager/anime", "/manager/user" })
public class ManagerServlet extends HttpServlet {

    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();


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

    @FunctionalInterface
    interface PageHandler {
        void handle(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException;
    }

    //Process request method to execute task based on the type

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ExecutionException, InterruptedException {
        String action = request.getParameter("action");
        //String targetJSP;
        if (request.getServletPath().equals("/manager")) {
            response.sendRedirect("/manager/user");
        }
        request.setAttribute("isManga",  request.getServletPath().equals("/manager/manga"));
        request.setAttribute("isAnime", request.getServletPath().equals("/manager/anime"));
        request.setAttribute("isUser", request.getServletPath().equals("/manager/user"));

        /*if ((boolean) request.getAttribute("isManga")) {
            targetJSP = "/WEB-INF/jsp/manga_manager.jsp";
        } else if ((boolean) request.getAttribute("isAnime")) {
            targetJSP = "/WEB-INF/jsp/anime_manager.jsp";
        } else if ((boolean) request.getAttribute("isUser")) {
            targetJSP = "/WEB-INF/jsp/user_manager.jsp";
        } else {
            response.sendRedirect("manager/manga");
            return;
        }*/

        PageHandler defaultAction = null;
        String servletPath = request.getServletPath();
        defaultAction = switch (servletPath) {
            case "/manager/anime" -> this::managerDefaultPageAnime;
            case "/manager/manga" -> this::managerDefaultPageManga;
            case "/manager/user" -> this::managerDefaultPageUser;
            default -> defaultAction;
        };
        List<PageHandler> defaultActions = new ArrayList<>();
        if (defaultAction != null) {
            defaultActions.add(defaultAction);
        }

        switch (action) {
            case "bestCriteria" -> handleBestCriteria(request, response);
            case "averageRatingByYear" -> handleMediaContentAverageRatingByYear(request, response);
            case "averageRatingByMonth" -> handleMediaContentAverageRatingByMonth(request, response);
            case "distribution" -> handleUsersDistribution(request, response);
            case "averageAppRatingByCriteria" -> handleUsersAverageAppRatingCriteria(request, response);
            case "averageAppRatingByAgeRange" -> handleUsersAverageAppRatingAgeRange(request, response);
            case "trendMediaContentByYear" ->  handleTrendMediaContentByYear(request, response);
            case "trendMediaContentByLikes" -> handleTrendMediaContentByLikes(request, response);
            case "show_info" -> {
                try {
                    handleShowInfo(request, response);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "update_info" -> handleUpdateInfo(request, response);
            case "delete_media" -> handleDeleteMedia(request, response);

            case null, default -> {
                for (PageHandler actionToRun : defaultActions) {
                    actionToRun.handle(request, response);
                }
                // TODO: when the manager access to the page the servlet
                //  must return using threads all the analytics about user/anime/manga
                //  and then the page will be updated with the data.
                //  For the anime and manga we have getBestCriteria, getTrendMediaContentByYear,
                //  getTrendMediaContentGenreByYear, getTrendMediaContentByLikes.
                //  For the user we have getAverageRating, getAverageUserAge, getDistribution.
                //  Check per manga, anime e user
            }
        }

    }



    public String managerDefaultPageAnime(HttpServletRequest request, HttpServletResponse response) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        //Create threads
        //Thread 1: getBestCriteria: tags for anime, genres for manga Map<String, Double> getBestCriteria
        Future<Map<String, Double>> bestAnimeCriteriaFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getBestCriteria("tags", 1, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
            });


        //Thread 2: getTrendMediaContentByYear with year = 2021 List<? extends MediaContentDTO> getTrendMediaContentByYear
        Future<Map<? extends MediaContentDTO, Integer>> trendAnimeByYearFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getTrendMediaContentByYear(2021, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Tread 3: getTrendMediaContentByLikes List<? extends MediaContentDTO> getMediaContentTrendByLikes
        Future<List<? extends MediaContentDTO>> trendAnimeByLikesFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getMediaContentTrendByLikes(MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });


        try {
            Map<String, Double> bestAnimeCriteria = bestAnimeCriteriaFuture.get();
            Map<? extends MediaContentDTO, Integer> trendAnimeByYear = trendAnimeByYearFuture.get();
            List<? extends MediaContentDTO> trendAnimeByLikes = trendAnimeByLikesFuture.get();


            Gson gson = new Gson();
            // String bestAnimeCriteriaJson = gson.toJson(bestAnimeCriteria);
            String bestAnimeCriteriaJson = gson.toJson(bestAnimeCriteria);
            String trendAnimeByYearJson = gson.toJson(trendAnimeByYear);
            String trendAnimeByLikesJson = gson.toJson(trendAnimeByLikes);
            // request.setAttribute("bestAnimeCriteria", bestAnimeCriteriaJson);
            request.setAttribute("bestAnimeCriteria", bestAnimeCriteriaJson);
            request.setAttribute("trendAnimeByYear", trendAnimeByYearJson);
            request.setAttribute("trendAnimeByLikes", trendAnimeByLikesJson);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Shut down the ExecutorService
        executorService.shutdown();


        return "managerDefaultPageAnime";
    }

    public String managerDefaultPageManga(HttpServletRequest request, HttpServletResponse response) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        //Create threads
        //Thread 1: getBestCriteria: tags for anime, genres for manga Map<String, Double> getBestCriteria
        Future<Map<String, Double>> bestMangaCriteriaFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getBestCriteria("genres", 1, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Thread 2: getTrendMediaContentByYear with year = 2021 List<? extends MediaContentDTO> getTrendMediaContentByYear

        Future<Map<? extends MediaContentDTO, Integer>> trendMangaByYearFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getTrendMediaContentByYear(2021, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Tread 3: getTrendMediaContentByLikes List<? extends MediaContentDTO> getMediaContentTrendByLikes

        Future<List<? extends MediaContentDTO>> trendMangaByLikesFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getMediaContentTrendByLikes(MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        try {
            Map<String, Double> bestMangaCriteria = bestMangaCriteriaFuture.get();
            Map<? extends MediaContentDTO, Integer> trendMangaByYear = trendMangaByYearFuture.get();
            List<? extends MediaContentDTO> trendMangaByLikes = trendMangaByLikesFuture.get();


            Gson gson = new Gson();
            // String bestAnimeCriteriaJson = gson.toJson(bestAnimeCriteria);
            String bestMangaCriteriaJson = gson.toJson(bestMangaCriteria);
            String trendMangaByYearJson = gson.toJson(trendMangaByYear);
            String trendMangaByLikesJson = gson.toJson(trendMangaByLikes);
            //request.setAttribute("bestAnimeCriteria", bestAnimeCriteriaJson);
            request.setAttribute("bestMangaCriteria", bestMangaCriteriaJson);
            request.setAttribute("trendMangaByYear", trendMangaByYearJson);
            request.setAttribute("trendMangaByLikes", trendMangaByLikesJson);



        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Shut down the ExecutorService
        executorService.shutdown();


        return "managerDefaultPageMediaContent";
    }


    public String managerDefaultPageUser(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        //Create threads
        //Thread 1: getDistribution Map<String, Integer> getDistribution

        Future<Map<String, Integer>> distributionFuture = executorService.submit(() -> {
            try {
                UserService userService = ServiceLocator.getUserService();
                return userService.getDistribution("location");
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        Gson gson = new Gson();

        Map<String, Integer> distributionResult = distributionFuture.get();

        String distributionJson = gson.toJson(distributionResult);

        request.setAttribute("distribution", distributionJson);


        // Shut down the ExecutorService
        executorService.shutdown();

        return "managerDefaultPageUser";
    }

    //Asynchronous request
    private void handleBestCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String criteria = request.getParameter("criteria");
        int page = Integer.parseInt(request.getParameter("page"));

        boolean isManga = (boolean) request.getAttribute("isManga");
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        if (!(criteria.equals("tags") || criteria.equals("genres") || criteria.equals("themes") || criteria.equals("demographics"))) {
            throw new IllegalArgumentException("Criteria not supported");
        }

        Map<String, Double> bestCriteria;
        if (!isManga) {
            try {
                if (!(criteria.equals("tags"))) {
                    throw new IllegalArgumentException("Criteria not supported");
                }
                bestCriteria = mediaContentService.getBestCriteria(criteria, page, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                if (!(criteria.equals("genres") || criteria.equals("themes") || criteria.equals("demographics"))) {
                    throw new IllegalArgumentException("Criteria not supported");
                }
                bestCriteria = mediaContentService.getBestCriteria(criteria, page, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }

        String jsonResponse = new Gson().toJson(bestCriteria);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    //Asynchronous request
    private void handleMediaContentAverageRatingByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaContentId = request.getParameter("mediaContentId");
        int startYear = Integer.parseInt(request.getParameter("startYear"));
        int endYear = Integer.parseInt(request.getParameter("endYear"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        int currentYear = Year.now().getValue();


        ReviewService reviewService = ServiceLocator.getReviewService();
        Map<String, Double> averageRatingByYear;
        //if start year or end year is greater than current year, throw exception
        if (startYear > endYear || startYear < 0 || startYear > currentYear || endYear > currentYear) {
            throw new IllegalArgumentException("Year must be valid");
        }

        //if media content is manga, get average rating by year for manga, else for anime
        if(isManga){
            try {
                // Is it better to also add the page?
                averageRatingByYear = reviewService.getMediaContentRatingByYear(
                        MediaContentType.MANGA,
                        mediaContentId,
                        startYear,
                        endYear
                );


            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                averageRatingByYear = reviewService.getMediaContentRatingByYear(MediaContentType.ANIME, mediaContentId, startYear, endYear);

            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
        String jsonResponse = new Gson().toJson(averageRatingByYear);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    //Asynchronous request

    private void handleMediaContentAverageRatingByMonth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaContentId = request.getParameter("mediaContentId");
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        int currentYear = Year.now().getValue();

        ReviewService reviewService = ServiceLocator.getReviewService();
        Map<String, Double> averageRatingByMonth;

        if (year < 0 || year > currentYear) {
            throw new IllegalArgumentException("Year must be valid");
        }

        //if media content is manga, get average rating by month for manga, else for anime
        if(isManga){
            try {
                // Is it better to also add the page?
                averageRatingByMonth = reviewService.getMediaContentRatingByMonth(
                        MediaContentType.MANGA,
                        mediaContentId,
                        year
                );


            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                averageRatingByMonth = reviewService.getMediaContentRatingByMonth(MediaContentType.ANIME, mediaContentId, year);

            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }

        String jsonResponse = new Gson().toJson(averageRatingByMonth);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);

    }

    //Asynchronous request
    private void handleUsersDistribution(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String criteria = request.getParameter("criteria");
        UserService userService = ServiceLocator.getUserService();

        Map<String, Integer> distribution;

        if (!(criteria.equals("gender") || criteria.equals("location") || criteria.equals("birthday") || criteria.equals("joined_on"))) {
            throw new IllegalArgumentException("Criteria not supported");
        }
        try {

            distribution = userService.getDistribution(criteria);

            } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        String jsonResponse = new Gson().toJson(distribution);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);

    }

    //Asynchronous request
    private void handleUsersAverageAppRatingCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String criteria = request.getParameter("criteria");
        UserService userService = ServiceLocator.getUserService();

        Map<String, Double> averageAppRatingByCriteria;
        if (!(criteria.equals("gender") || criteria.equals("location"))) {
            throw new IllegalArgumentException("Criteria not supported");
        }
        try {
            averageAppRatingByCriteria = userService.averageAppRating(criteria);

            } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        String jsonResponse = new Gson().toJson(averageAppRatingByCriteria);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    //Asynchronous request
    private void handleUsersAverageAppRatingAgeRange(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();
        Map<String, Double> averageAppRatingByAgeRange;
        try {
            averageAppRatingByAgeRange = userService.averageAppRatingByAgeRange();

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        String jsonResponse = new Gson().toJson(averageAppRatingByAgeRange);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    //Asynchronous request

    private void handleTrendMediaContentByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");
        int currentYear = Year.now().getValue();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        //Is it better to put just MediaContentDTO instead of ? extends MediaContentDTO?
        Map<? extends MediaContentDTO, Integer> trendMediaContentByYear;
        if (year < 0 || year > currentYear) {
            throw new IllegalArgumentException("Year must be valid");
        }
        //If media content is manga, get trend media content by year for manga, else for anime
        if(isManga){
            try {
                trendMediaContentByYear = mediaContentService.getTrendMediaContentByYear(year, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                trendMediaContentByYear = mediaContentService.getTrendMediaContentByYear(year, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
        String jsonResponse = new Gson().toJson(trendMediaContentByYear);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);

    }

    //Asynchronous request
    private void handleTrendMediaContentByLikes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        //Is it better to put just MediaContentDTO instead of ? extends MediaContentDTO?
        List<? extends MediaContentDTO> trendMediaContentByLikes;
        //If media content is manga, get trend media content by likes for manga, else for anime
        if(isManga){
            try {
                trendMediaContentByLikes = mediaContentService.getMediaContentTrendByLikes(MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                trendMediaContentByLikes = mediaContentService.getMediaContentTrendByLikes(MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
        String jsonResponse = new Gson().toJson(trendMediaContentByLikes);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    //Handler for each type of task
    //Examples: analytics requests (add media content, remove media content, update media content and search will be done by Fey)


    private void handleShowInfo(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        if (isManga) {
            try {
                String mediaId = request.getParameter("mediaId");
                Manga manga = (Manga) mediaContentService.getMediaContentById(mediaId, MediaContentType.MANGA);

                String jsonResponse = new Gson().toJson(manga);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
                } catch (BusinessException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                // Handle anime information
                String mediaId = request.getParameter("mediaId");
                Anime anime = (Anime) mediaContentService.getMediaContentById(mediaId, MediaContentType.ANIME);

                String jsonResponse = new Gson().toJson(anime);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
            } catch (BusinessException | IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void handleUpdateInfo(HttpServletRequest request, HttpServletResponse response){
        boolean isManga = (boolean) request.getAttribute("isManga");
        if (isManga) {
            try {
                //Which fields should we be able to update?
                // Retrieve updated manga information from the request parameters
                String mangaId = request.getParameter("mangaId");
                String title = request.getParameter("title");
                //Why doesn't this work?
                List<String> genres = Collections.singletonList(request.getParameter("genres"));
                List<String> themes = Collections.singletonList(request.getParameter("themes"));
                String url = request.getParameter("url");
                String serialization = request.getParameter("serialization");
                String background = request.getParameter("background");
                String titleEnglish = request.getParameter("titleEnglish");
                String titleJapanese = request.getParameter("titleJapanese");
                LocalDate startDate = LocalDate.parse(request.getParameter("startDate"));
                LocalDate endDate = LocalDate.parse(request.getParameter("endDate"));
                Integer volumes = Integer.parseInt(request.getParameter("volumes"));
                Integer chapters = Integer.parseInt(request.getParameter("chapters"));
                String synopsis = request.getParameter("synopsis");
                // You can retrieve other fields similarly

                // Create a Manga object with the updated information
                Manga manga = new Manga();
                manga.setId(mangaId);
                manga.setTitle(title);
                manga.setImageUrl(url);
                manga.setGenres(genres);
                manga.setThemes(themes);
                manga.setSerializations(serialization);
                manga.setBackground(background);
                manga.setTitleEnglish(titleEnglish);
                manga.setTitleJapanese(titleJapanese);
                manga.setStartDate(startDate);
                manga.setEndDate(endDate);
                manga.setVolumes(volumes);
                manga.setChapters(chapters);
                manga.setSynopsis(synopsis);
                // Set other fields as needed

                //To update the author, we need to retrieve the author from the database
                String authorId = request.getParameter("authorId");

                List<MangaAuthor> authors = new ArrayList<>();

                if(authorId != null && !authorId.isEmpty()){
                    String [] authorNames = authorId.split(",");

                    for(String authorName : authorNames){
                        MangaAuthor author = new MangaAuthor();
                        author.setName(authorName.trim());
                        authors.add(author);
                    }
                }

                // Update manga information using the MediaContentService
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                mediaContentService.updateMediaContent(manga);

                // Send a success response back to the client
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Manga information updated successfully.");
            } catch (BusinessException | IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }
        else {
            try {
                // Retrieve updated anime information from the request parameters
                String animeId = request.getParameter("animeId");
                String title = request.getParameter("title");
                List<String> tags = Collections.singletonList(request.getParameter("genres"));
                String url = request.getParameter("url");
                Integer year = Integer.parseInt(request.getParameter("year"));
                String season = request.getParameter("season");
                Integer episodeCount = Integer.parseInt(request.getParameter("episodeCount"));
                List<String> relatedAnime = Collections.singletonList(request.getParameter("relatedAnime"));
                String producers = request.getParameter("producers");
                String studios = request.getParameter("studios");
                String synopsis = request.getParameter("synopsis");

                // You can retrieve other fields similarly

                // Create an Anime object with the updated information
                Anime anime = new Anime();
                anime.setId(animeId);
                anime.setTitle(title);
                anime.setTags(tags);
                anime.setImageUrl(url);
                anime.setYear(year);
                anime.setSeason(season);
                anime.setEpisodeCount(episodeCount);
                anime.setRelatedAnime(relatedAnime);
                anime.setProducers(producers);
                anime.setStudios(studios);
                anime.setSynopsis(synopsis);
                // Set other fields as needed

                // Update anime information using the MediaContentService
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                mediaContentService.updateMediaContent(anime);

                // Send a success response back to the client
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Anime information updated successfully.");
            } catch (BusinessException | IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    public void handleDeleteMedia(HttpServletRequest request, HttpServletResponse response){
        boolean isManga = (boolean) request.getAttribute("isManga");

        if (isManga){
            try{
                String mediaId = request.getParameter("mediaId");
                if (mediaId != null && !mediaId.isEmpty()){

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
        else {
            try{
                String mediaId = request.getParameter("mediaId");
                if (mediaId != null && !mediaId.isEmpty()){

                    mediaContentService.deleteMediaContent(mediaId, MediaContentType.ANIME);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Anime deleted successfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Invalid anime ID");
                }
            }catch (BusinessException | IOException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

}
