<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page import="static java.time.LocalDate.now" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user_list.css"/>

    <title>Analytics</title>
</head>

<body>
<c:set var="currentYear" value="<%= now().getYear() %>" />

<!-- navbar -->
<nav>
    <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
    <h1 id="welcome-message">Welcome ${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}</h1>
    <div class="nav-items">
        <div class="search-box">
            <button id="user-search-button" class="btn-search"><i class="fa fa-search"></i></button>
            <label for="user-search"></label>
            <input id="user-search" type="text" class="input-search" placeholder="Search user...">
            <div id="user-search-section" class="user-list-section users-results">
                <div id="user-search-results"></div>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
        <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
        <div class="logout" onclick="logout('auth')">
            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="sign-out-alt" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="logout-icon"><path data-v-04b245e6="" fill="currentColor" d="M497 273L329 441c-15 15-41 4.5-41-17v-96H152c-13.3 0-24-10.7-24-24v-96c0-13.3 10.7-24 24-24h136V88c0-21.4 25.9-32 41-17l168 168c9.3 9.4 9.3 24.6 0 34zM192 436v-40c0-6.6-5.4-12-12-12H96c-17.7 0-32-14.3-32-32V160c0-17.7 14.3-32 32-32h84c6.6 0 12-5.4 12-12V76c0-6.6-5.4-12-12-12H96c-53 0-96 43-96 96v192c0 53 43 96 96 96h84c6.6 0 12-5.4 12-12z" class=""></path></svg>
            <div class="logout-text">Log Out</div>
        </div>
        <a href="${pageContext.request.contextPath}/profile" class="small-pic">
            <img id="navbar-profile-picture" alt="profile bar" src="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}">
        </a>
    </div>
</nav>

<div class="all-page">
    <!-- navbar -->
    <div class="navbar-container">
        <div id="side-navbar" class="button-container">
            <button id="user-button" class="options active">USER</button>
            <button id="manga-button" class="options">MANGA</button>
            <button id="anime-button" class="options">ANIME</button>
        </div>
    </div>

    <!-- user statistics -->
    <div id="user-page" class="page selected">
        <h1>User Analytics</h1>
        <div class="analytic-box" id="user-distribution">
            <div class="analytic-title">
                <label for="user-distribution-type">Select Distribution Type:</label>
                <select id="user-distribution-type">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="birthday">Birthday</option>
                    <option value="joined_on">Joined On</option>
                </select>
            </div>
            <canvas id="user-distribution-chart" class="small"></canvas>
        </div>

        <div class="analytic-box medium" id="user-criteria-rating">
            <div class="analytic-title">
                <label for="user-rating-criteria">Average App Rating by:</label>
                <select id="user-rating-criteria">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="age">Age</option>
                </select>
            </div>
            <canvas id="app-rating-chart"></canvas>
        </div>
    </div>

    <!-- manga statistics -->
    <div id="manga-page" class="page">
        <h1>Manga Analytics</h1>

        <div id="manga-trend" class="analytic-box">
            <div class="analytic-title">
                <label for="manga-trend-year">Anime Trend on: </label>
                <input type="number" id="manga-trend-year" name="year" min="1900" max="${currentYear}">
            </div>
            <div id="manga-trend-list" class="media-list"></div>
        </div>

        <div class="analytic-box" id="manga-average-rating">
            <div class="analytic-title">
                <label for="manga-analytics-type">Average Rating by:</label>
                <select id="manga-analytics-type">
                    <option value="genres">Genres</option>
                    <option value="themes">Themes</option>
                    <option value="demographics">Demographics</option>
                    <option value="authors">Authors</option>
                    <option value="serializations">Serializations</option>
                </select>
            </div>
            <canvas id="manga-criteria-rating-chart"></canvas>
        </div>

        <div id="manga-search-section" class="analytic-box">
            <div class="media-list-section">
                <div class="analytic-title">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="manga-search">Title:</label>
                        <input type="search" id="manga-search" name="searchTerm" placeholder="Enter Anime Title">
                    </div>
                </div>
            </div>

            <div id="manga-list" class="media-list"></div>
        </div>

        <div id="single-manga-analytics" class="analytic-box large" style="display: none;">
            <h1 id="manga-selected"></h1>
            <div class="analytic-title">
                <label class="analytic-title" for="manga-period-selection">Average Rating by: </label>
                <select id="manga-period-selection">
                    <option value="month">MONTH</option>
                    <option value="year">YEAR</option>
                </select>
            </div>
            <div id="manga-year-form" class="diagram-parameter active">
                <div class="select">
                    <label for="manga-year">Select Year:</label>
                    <input type="number" id="manga-year" name="year" min="1900" max="${currentYear}">
                </div>
            </div>
            <div id="manga-year-range-form" class="diagram-parameter">
                <div class="select">
                    <label for="manga-start-year">Start Year:</label>
                    <input type="number" id="manga-start-year" name="startYear" min="1900" max="${currentYear}">
                    <label for="manga-end-year">End Year:</label>
                    <input type="number" id="manga-end-year" name="endYear" min="1900" max="${currentYear}">
                </div>
            </div>
            <canvas id="manga-chart-month" class="avg-rating-media active"></canvas>
            <canvas id="manga-chart-year" class="avg-rating-media"></canvas>
        </div>
    </div>

    <!-- anime statistics -->
    <div id="anime-page" class="page">
        <h1>Anime Analytics</h1>

        <div id="anime-trend" class="analytic-box">
            <div class="analytic-title">
                <label for="anime-trend-year">Anime Trend on: </label>
                <input type="number" id="anime-trend-year" name="year" min="1900" max="${currentYear}">
            </div>
            <div id="anime-trend-list" class="media-list"></div>
        </div>

        <div id="anime-average-rating" class="analytic-box">
            <div class="analytic-title">
                <label for="anime-analytics-type">Select Analytics Type:</label>
                <select id="anime-analytics-type">
                    <option value="tags">Tags</option>
                    <option value="producers">Producers</option>
                    <option value="studios">Studios</option>
                </select>
            </div>
            <canvas id="anime-criteria-rating-chart"></canvas>
        </div>

        <div id="anime-search-section" class="analytic-box">
            <div class="media-list-section">
                <div class="analytic-title">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="anime-search">Title:</label>
                        <input type="search" id="anime-search" name="searchTerm" placeholder="Enter Anime Title">
                    </div>
                </div>
            </div>

            <div id="anime-list" class="media-list"></div>
        </div>

        <div id="single-anime-analytics" class="analytic-box large" style="display: none;">
            <h1 id="anime-selected"></h1>
            <div class="analytic-title">
                <label class="analytic-title" for="anime-period-selection">Average Rating by: </label>
                <select id="anime-period-selection">
                    <option value="month">MONTH</option>
                    <option value="year">YEAR</option>
                </select>
            </div>
            <div id="anime-year-form" class="diagram-parameter active">
                <div class="select">
                    <label for="anime-year">Select Year:</label>
                    <input type="number" id="anime-year" name="year" min="1900" max="${currentYear}">
                </div>
            </div>
            <div id="anime-year-range-form" class="diagram-parameter">
                <div class="select">
                    <label for="anime-start-year">Start Year:</label>
                    <input type="number" id="anime-start-year" name="startYear" min="1900" max="${currentYear}">
                    <label for="anime-end-year">End Year:</label>
                    <input type="number" id="anime-end-year" name="endYear" min="1900" max="${currentYear}">
                </div>
            </div>
            <canvas id="anime-chart-month" class="avg-rating-media active"></canvas>
            <canvas id="anime-chart-year" class="avg-rating-media"></canvas>

        </div>
    </div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js" defer></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js" defer></script>
<script src="${pageContext.request.contextPath}/js/navbar.js" defer></script>
<script src="${pageContext.request.contextPath}/js/manager.js" defer></script>
<script>
    const contextPath = '${pageContext.request.contextPath}';
    const mangaDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_MANGA}";
    const animeDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_ANIME}";
    const distributionLabels = [];
    const distributionData = [];
    <c:forEach var="entry" items="${distribution}">
        distributionLabels.push('<c:out value="${entry.key}"/>');
        distributionData.push(<c:out value="${entry.value}"/>);
    </c:forEach>
    const averageAppRatingLabels = [];
    const averageAppRatingData = [];
    <c:forEach var="entry" items="${averageAppRating}">
        averageAppRatingLabels.push('<c:out value="${entry.key}"/>');
        averageAppRatingData.push(<c:out value="${entry.value}"/>);
    </c:forEach>
</script>
</body>
</html>