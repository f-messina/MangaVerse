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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
            case "averageRating" -> handleUserAverageRating(request, response);
            case "averageRatingByYear" -> handleMediaContentAverageRatingByYear(request, response);
            case "averageRatingByMonth" -> handleMediaContentAverageRatingByMonth(request, response);
            case "distribution" -> handleUsersDistribution(request, response);
            case "usersAverageAge" -> handleUsersAverageAge(request, response);
            case "averageAppRatingByCriteria" -> handleUsersAverageAppRatingCriteria(request, response);
            case "averageAppRatingByAgeRange" -> handleUsersAverageAppRatingAgeRange(request, response);
            case "trendMediaContentByYear" ->  handleTrendMediaContentByYear(request, response);
            case "trendGenresByYear" -> handleTrendGenresByYear(request, response);
            case "trendMediaContentByLikes" -> handleTrendMediaContentByLikes(request, response);
            case "trendGenres" -> handleTrendGenres(request, response);

            case "show_info" -> handleShowInfo(request, response);
            case "update_info" -> handleUpdateInfo(request,response);
            case "delete_media" -> handleDeleteMedia(request,response);

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
                /*defaultActions.add(() -> {

                    try {
                        handleBestCriteria(request, response);
                    } catch (ServletException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                defaultActions.add(() -> {
                    try {
                        handleUsersAverageAppRatingCriteria(request, response);
                    } catch (ServletException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                defaultActions.add(() -> {
                    try {
                        handleTrendMediaContentByLikes(request, response);
                    } catch (ServletException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });*/
            }
        }
        /*for (Runnable actionToRun : defaultActions) {
            actionToRun.run();
        }*/

    }



    public String managerDefaultPageAnime(HttpServletRequest request, HttpServletResponse response) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        //Create threads
        //Thread 1: getBestCriteria: tags for anime, genres for manga Map<String, Double> getBestCriteria
        Future<Map<String, Double>> bestAnimeCriteriaFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getBestAnimeCriteria("tags", 1);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
            });


        //Thread 2: getTrendMediaContentByYear with year = 2021 List<? extends MediaContentDTO> getTrendMediaContentByYear
        Future<List<? extends MediaContentDTO>> trendAnimeByYearFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getTrendMediaContentByYear(2021, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Tread 3: getTrendMediaContentGenreByYear with year = 2021 List<String> getMediaContentGenresTrendByYear
        Future<List<String>> trendAnimeGenresByYearFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getMediaContentGenresTrendByYear(2021, MediaContentType.ANIME);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Tread 4: getTrendMediaContentByLikes List<? extends MediaContentDTO> getMediaContentTrendByLikes
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
            List<? extends MediaContentDTO> trendAnimeByYear = trendAnimeByYearFuture.get();
            List<String> trendAnimeGenresByYear = trendAnimeGenresByYearFuture.get();
            List<? extends MediaContentDTO> trendAnimeByLikes = trendAnimeByLikesFuture.get();


            Gson gson = new Gson();
            // String bestAnimeCriteriaJson = gson.toJson(bestAnimeCriteria);
            String bestAnimeCriteriaJson = gson.toJson(bestAnimeCriteria);
            String trendAnimeByYearJson = gson.toJson(trendAnimeByYear);
            String trendAnimeGenresByYearJson = gson.toJson(trendAnimeGenresByYear);
            String trendAnimeByLikesJson = gson.toJson(trendAnimeByLikes);
            // request.setAttribute("bestAnimeCriteria", bestAnimeCriteriaJson);
            request.setAttribute("bestAnimeCriteria", bestAnimeCriteriaJson);
            request.setAttribute("trendAnimeByYear", trendAnimeByYearJson);
            request.setAttribute("trendAnimeGenresByYear", trendAnimeGenresByYearJson);
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
                return mediaContentService.getBestMangaCriteria("genres", 1);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        //Thread 2: getTrendMediaContentByYear with year = 2021 List<? extends MediaContentDTO> getTrendMediaContentByYear

        Future<List<? extends MediaContentDTO>> trendMangaByYearFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getTrendMediaContentByYear(2021, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });
        //Tread 3: getTrendMediaContentGenreByYear with year = 2021 List<String> getMediaContentGenresTrendByYear

        Future<List<String>> trendMangaGenresByYearFuture = executorService.submit(() -> {
            try {
                MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
                return mediaContentService.getMediaContentGenresTrendByYear(2021, MediaContentType.MANGA);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });
        //Tread 4: getTrendMediaContentByLikes List<? extends MediaContentDTO> getMediaContentTrendByLikes

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
            List<? extends MediaContentDTO> trendMangaByYear = trendMangaByYearFuture.get();
            List<String> trendMangaGenresByYear = trendMangaGenresByYearFuture.get();
            List<? extends MediaContentDTO> trendMangaByLikes = trendMangaByLikesFuture.get();


            Gson gson = new Gson();
            // String bestAnimeCriteriaJson = gson.toJson(bestAnimeCriteria);
            String bestMangaCriteriaJson = gson.toJson(bestMangaCriteria);
            String trendMangaByYearJson = gson.toJson(trendMangaByYear);
            String trendMangaGenresByYearJson = gson.toJson(trendMangaGenresByYear);
            String trendMangaByLikesJson = gson.toJson(trendMangaByLikes);
            //request.setAttribute("bestAnimeCriteria", bestAnimeCriteriaJson);
            request.setAttribute("bestMangaCriteria", bestMangaCriteriaJson);
            request.setAttribute("trendMangaByYear", trendMangaByYearJson);
            request.setAttribute("trendMangaGenresByYear", trendMangaGenresByYearJson);
            request.setAttribute("trendMangaByLikes", trendMangaByLikesJson);



        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Shut down the ExecutorService
        executorService.shutdown();


        return "managerDefaultPageMediaContent";
    }


    public String managerDefaultPageUser(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        //Create threads
        //Thread 1: getAverageRating Double averageRatingUser
        Future<Double> averageRatingUserFuture = executorService.submit(() -> {
            try {
                ReviewService reviewService = ServiceLocator.getReviewService();
                return reviewService.getUserAverageRating("userId");
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });
        //Thread 2: getAverageUserAge Double averageAgeUsers
        Future<Double> averageAgeUsersFuture = executorService.submit(() -> {
            try {
                UserService userService = ServiceLocator.getUserService();
                return userService.averageAgeUsers();
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });
        //Thread 3: getDistribution Map<String, Integer> getDistribution
        Future<Map<String, Integer>> distributionFuture = executorService.submit(() -> {
            try {
                UserService userService = ServiceLocator.getUserService();
                return userService.getDistribution("location");
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }

        });

        Gson gson = new Gson();

        Double averageRatingUserResult = averageRatingUserFuture.get();
        Double averageAgeUsersResult = averageAgeUsersFuture.get();
        Map<String, Integer> distributionResult = distributionFuture.get();

        String averageRatingUserJson = gson.toJson(averageRatingUserResult);
        String averageAgeUsersJson = gson.toJson(averageAgeUsersResult);
        String distributionJson = gson.toJson(distributionResult);

        request.setAttribute("averageRatingUser", averageRatingUserJson);
        request.setAttribute("averageAgeUsers", averageAgeUsersJson);
        request.setAttribute("distribution", distributionJson);


        // Shut down the ExecutorService
        executorService.shutdown();

        return "managerDefaultPageUser";
    }


    //Requests for the analytics
    //MongoDB analytics
    //Get best criteria (anime and manga)
    /*private void handleBestCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String criteria = request.getParameter("criteria");
        int page = Integer.parseInt(request.getParameter("page"));

        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        switch (criteria) {
            case "tags" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestAnimeCriteria("tags", page);
                    request.setAttribute("bestCriteria", bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "genres" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestMangaCriteria("genres", page);
                    request.setAttribute("bestCriteria", bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "themes" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestMangaCriteria("themes", page);
                    request.setAttribute("bestCriteria", bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "demographics" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestMangaCriteria("demographics", page);
                    request.setAttribute("bestCriteria", bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);


    }*/

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
                bestCriteria = mediaContentService.getBestAnimeCriteria(criteria, page);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                if (!(criteria.equals("genres") || criteria.equals("themes") || criteria.equals("demographics"))) {
                    throw new IllegalArgumentException("Criteria not supported");
                }
                bestCriteria = mediaContentService.getBestMangaCriteria(criteria, page);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }

        String jsonResponse = new Gson().toJson(bestCriteria);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }


    //Average rating of specific user?(reviews)
    /*private void handleUserAverageRating (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        ReviewService reviewService = ServiceLocator.getReviewService();
        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        try {
            Double averageRating = reviewService.averageRatingUser (userId);
            request.setAttribute("userId", userId);
            request.setAttribute("averageRating", averageRating);


        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }*/
    //Asynchronous request
    private void handleUserAverageRating(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Double averageRating = reviewService.getUserAverageRating(userId);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("userId", userId);
            jsonResponse.addProperty("averageRating", averageRating);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }


    //Get media content rating by year(reviews)
    /*private void handleMediaContentAverageRatingByYear (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaContentId = request.getParameter("mediaContentId");
        int startYear = Integer.parseInt(request.getParameter("startYear"));
        int endYear = Integer.parseInt(request.getParameter("endYear"));

        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Map<String, Double> averageRatingByYear = reviewService.ratingMediaContentByYear(isManga? MediaContentType.MANGA : MediaContentType.ANIME, mediaContentId, startYear, endYear);
            request.setAttribute("averageRatingByYear", averageRatingByYear);


        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }*/

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


    //Get media content rating by month(reviews)
    /*private void handleMediaContentAverageRatingByMonth (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaContentId = request.getParameter("mediaContentId");
        int year = Integer.parseInt(request.getParameter("year"));

        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Map<String, Double> averageRatingByMonth = reviewService.ratingMediaContentByMonth(isManga? MediaContentType.MANGA : MediaContentType.ANIME, mediaContentId, year);
            request.setAttribute("averageRatingByMonth", averageRatingByMonth);


        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }*/

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



    //Get distribution (users)
    /*private void handleUsersDistribution (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String criteria = request.getParameter("criteria");

        UserService userService = ServiceLocator.getUserService();

        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        switch (criteria) {
            case "gender" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("gender");
                    request.setAttribute("distribution", distribution);

                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "location" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("location");
                    request.setAttribute("distribution", distribution);


                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "birthday" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("birthday");
                    request.setAttribute("distribution", distribution);


                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "joined_on" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("joined_on");
                    request.setAttribute("distribution", distribution);


                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }*/

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


    //Average age of users (users)
    /*private void handleUsersAverageAge (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();

        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        try {
            Double usersAverageAge = userService.averageAgeUsers();
            request.setAttribute("usersAverageAge", usersAverageAge);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }*/

    //Asynchronous request
    private void handleUsersAverageAge(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();

        try {
            Double usersAverageAge = userService.averageAgeUsers();

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("usersAverageAge", usersAverageAge);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }


    //Average app rating through criteria (users)
    /*private void handleUsersAverageAppRatingCriteria (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String criteria = request.getParameter("criteria");

        UserService userService = ServiceLocator.getUserService();

        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        if (criteria.equals("location")) {
            try {
                Map<String, Double> averageAppRatingByCriteria = userService.averageAppRating("location");
                request.setAttribute("averageAppRatingByCriteria", averageAppRatingByCriteria);

            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else if (criteria.equals("gender")) {
            try {
                Map<String, Double> averageAppRatingByCriteria = userService.averageAppRating("gender");
                request.setAttribute("averageAppRatingByCriteria", averageAppRatingByCriteria);

            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }*/

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


    //Average app rating through age range (users)

    /*private void handleUsersAverageAppRatingAgeRange (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();

        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        try {
            Map<String, Double> averageAppRatingByAgeRange = userService.averageAppRatingByAgeRange();
            request.setAttribute("averageAppRatingByAgeRange", averageAppRatingByAgeRange);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }*/

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



    //Neo4J analytics
    //List<AnimeDTO> getTrendMediaContentByYear(int year)
    /*private void handleTrendMediaContentByYear (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<? extends MediaContentDTO> trendMediaContentByYear = mediaContentService.getTrendMediaContentByYear(year, isManga? MediaContentType.MANGA : MediaContentType.ANIME);
            request.setAttribute("trendMediaContentByYear", trendMediaContentByYear);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);


    }*/

    //Asynchronous request

    private void handleTrendMediaContentByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<? extends MediaContentDTO> trendMediaContentByYear = mediaContentService.getTrendMediaContentByYear(year, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("trendMediaContentByYear", new Gson().toJsonTree(trendMediaContentByYear));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }


    //List<String> getMediaContentGenresTrendByYear
    /*private void handleTrendGenresByYear (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<String> trendGenresByYear = mediaContentService.getMediaContentGenresTrendByYear(year, isManga? MediaContentType.MANGA : MediaContentType.ANIME);
            request.setAttribute("trendGenresByYear", trendGenresByYear);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }*/
    //Asynchronous request
    private void handleTrendGenresByYear(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int year = Integer.parseInt(request.getParameter("year"));
        boolean isManga = (boolean) request.getAttribute("isManga");

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<String> trendGenresByYear = mediaContentService.getMediaContentGenresTrendByYear(year, isManga ? MediaContentType.MANGA : MediaContentType.ANIME);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("trendGenresByYear", new Gson().toJsonTree(trendGenresByYear));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse.toString());

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }



    //List<AnimeDTO> getMediaContentTrendByLikes()
    /*private void handleTrendMediaContentByLikes (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<? extends MediaContentDTO> trendMediaContentByLikes = mediaContentService.getMediaContentTrendByLikes(isManga? MediaContentType.MANGA : MediaContentType.ANIME);
            request.setAttribute("trendMediaContentByLikes", trendMediaContentByLikes);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }*/
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


    //List<String> getMediaContentGenresTrend()
    /*private void handleTrendGenres (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        String targetJSP = isManga ? "/WEB-INF/jsp/manga_manager.jsp" : "/WEB-INF/jsp/anime_manager.jsp";

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<String> trendGenres = mediaContentService.getMediaContentGenresTrend(isManga? MediaContentType.MANGA : MediaContentType.ANIME);
            request.setAttribute("trendGenres", trendGenres);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);
    }*/

    private void handleTrendGenres(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isManga = (boolean) request.getAttribute("isManga");
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<String> trendGenres = mediaContentService.getMediaContentGenresTrend(isManga ? MediaContentType.MANGA : MediaContentType.ANIME);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("trendGenres", new Gson().toJsonTree(trendGenres));

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

                    mediaContentService.removeMediaContent(mediaId, MediaContentType.MANGA);
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

}
