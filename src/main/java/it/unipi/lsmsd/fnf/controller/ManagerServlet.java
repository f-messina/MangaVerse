package it.unipi.lsmsd.fnf.controller;


import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
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

@WebServlet(urlPatterns = {"/manager"})
public class ManagerServlet extends HttpServlet {


    //DoGet and DoPost methods as the other servlets
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    //Process request method to execute task based on the type

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String action = request.getParameter("action");
        //String targetJSP;
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

        List<Runnable> defaultActions = new ArrayList<>();


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
            case null, default -> {
                defaultActions.add(() -> {
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
                });
            }
        }
        for (Runnable actionToRun : defaultActions) {
            actionToRun.run();
        }

    }

    //Requests for the analytics
    //MongoDB analytics
    //Get best criteria (anime and manga)
    private void handleBestCriteria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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


    }

    //Average rating of specific user?(reviews)
    private void handleUserAverageRating (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    }

    //Get media content rating by year(reviews)
    private void handleMediaContentAverageRatingByYear (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    }

    //Get media content rating by month(reviews)
    private void handleMediaContentAverageRatingByMonth (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    }



    //Get distribution (users)
    private void handleUsersDistribution (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    }

    //Average age of users (users)
    private void handleUsersAverageAge (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();

        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        try {
            Double usersAverageAge = userService.averageAgeUsers();
            request.setAttribute("usersAverageAge", usersAverageAge);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }

    //Average app rating through criteria (users)
    private void handleUsersAverageAppRatingCriteria (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    }

    //Average app rating through age range (users)

    private void handleUsersAverageAppRatingAgeRange (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = ServiceLocator.getUserService();

        String targetJSP = "/WEB-INF/jsp/user_manager.jsp";

        try {
            Map<String, Double> averageAppRatingByAgeRange = userService.averageAppRatingByAgeRange();
            request.setAttribute("averageAppRatingByAgeRange", averageAppRatingByAgeRange);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher(targetJSP).forward(request, response);

    }


    //Neo4J analytics
    //List<AnimeDTO> getTrendMediaContentByYear(int year)
    private void handleTrendMediaContentByYear (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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


    }

    //List<String> getMediaContentGenresTrendByYear
    private void handleTrendGenresByYear (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    }


    //List<AnimeDTO> getMediaContentTrendByLikes()
    private void handleTrendMediaContentByLikes (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    }

    //List<String> getMediaContentGenresTrend()
    private void handleTrendGenres (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    }

    //Handler for each type of task
    //Examples: analytics requests (add media content, remove media content, update media content and search will be done by Fey)


}
