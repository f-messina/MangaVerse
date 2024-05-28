<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 12:31
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>MAIN PAGE</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com%22%3E/" crossorigin />
    <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin />
    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"
            integrity="sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA=="
            crossorigin="anonymous"
            referrerpolicy="no-referrer"
    />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main-registered-user.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user_list.css"/>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/main_page_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/navbar.js" defer></script>
</head>
<body>
    <c:set var="isLogged" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />

    <!-- Welcome section -->
    <section id="welcome-section" class="section-home">
        <!-- Home container -->
        <div class="home-container">
            <div class="home-wrapper">
                <div class="active-page"><span id="mangaLink">Manga</span></div>
                <div class="welcome">
                    <div class="welcome-text">
                        <p>Welcome to</p>
                    </div>
                    <img src="${pageContext.request.contextPath}/images/logo-with-name.png" alt="middle" />
                </div>
                <div class="selection-page-link"><a href="${pageContext.request.contextPath}/mainPage/anime" id="animeLink">Anime</a></div>
            </div>
        </div>

        <!-- Down arrow -->
        <div class="down-arrow">
            <span data-scroll="list" id="down-arrow">
                <i class="fa-solid fa-chevron-down"> </i>
            </span>
        </div>
    </section>

    <!-- navbar -->
    <nav class="nav-bar" id="navbar">
        <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
        <div class="up-arrow">
                <span data-scroll="list" id="up-arrow">
                    <i class="fa-solid fa-chevron-up" style="color: #000000"></i>
                </span>
        </div>
        <c:if test="${isLogged}">
            <h1 id="welcome-message">Welcome ${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}</h1>
        </c:if>
        <div class="nav-items">
            <div class="search-box">
                <button id="user-search-button" class="btn-search"><i class="fa fa-search"></i></button>
                <label for="user-search"></label>
                <input id="user-search" type="text" class="input-search" placeholder="Search user...">
                <div id="user-search-section" class="user-list-section users-results">
                    <div id="user-search-results"></div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
            <c:choose>
                <c:when test="${isLogged}">
                    <form action="${pageContext.request.contextPath}/auth" method="post">
                        <input type="hidden" name="action" value="logout">
                        <input type="hidden" name="targetServlet" value="mainPage">
                        <button type="submit" class="logout">Log Out</button>
                    </form>
                    <a href="${pageContext.request.contextPath}/profile" class="small-pic">
                        <img id="navbar-profile-picture" alt="profile bar" src="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}">
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/auth">Log In</a>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>

    <!-- Main section -->
    <section id="main-section" class="scrollable">
        <div id="search-div" class="write-search">
            <form id="searchForm" action="${pageContext.request.contextPath}/mainPage/manga" method="post">
                <input type="hidden" name="action" value="search">
                <label class="filter-name" for="search">Title:</label>
                <input type="search" id="search" name="searchTerm" placeholder="Title">
                <input class="search" type="submit" value="SEARCH">
            </form>

            <button onclick="toggleFiltersDisplay()" class="more-filtering">See Detailed Filtering</button>

            <div id="filtersFormContainer" class="filtering">
                <form id="filterForm" action="${pageContext.request.contextPath}/mainPage/manga" method="post">
                    <input type="hidden" name="action" value="search">

                    <div class="title-ope">
                        <%-- This are the radios for the genres --%>
                        <label class="filter-name">Genres:</label><br/>
                        <div class="operator" style="background-color: #ecebeb; padding: 5px">
                            <label >Include:</label>
                            <input class="gap" type="radio" name="genreOperator" checked value="and">all
                            <input class="gap" type="radio" name="genreOperator" value="or">any
                        </div>
                    </div>

                    <div class="all">
                        <c:forEach items="${requestScope.mangaGenres}" var="genre">
                            <div class="one">
                                <input type = "radio" name="${genre}" style="color: green" onclick="toggleRadio(this)" value="select">
                                <input type = "radio" name="${genre}" style="color: red" onclick="toggleRadio(this)" value="avoid">
                                <label>${genre}</label>
                            </div>
                        </c:forEach>
                    </div>

                    <label class="filter-name">Type:</label>
                    <%-- This are the checkboxes for the types --%>
                    <div class="all">

                        <c:forEach var="entry" items="${requestScope.mangaTypes}">
                            <div class="one">
                                <input type="checkbox" id="${entry.name()}" name="mangaTypes" value="${entry.name()}">
                                <label for="${entry.name()}">${entry.toString()}</label>
                            </div>
                        </c:forEach>
                    </div>

                    <%-- This are the checkboxes for the demographics --%>
                    <label class="filter-name">Demographics:</label>
                    <div class="all">
                        <c:forEach var="entry" items="${requestScope.mangaDemographics}">
                            <c:if test="${entry.name() != 'UNKNOWN'}">
                                <div  class="one">
                                    <input type="checkbox" id="${entry.name()}" name="mangaDemographics" value="${entry.name()}">
                                    <label for="${entry.name()}">${entry.toString()}</label>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>

                    <label class="filter-name">Publishing status:</label>
                    <%-- This are the checkboxes for the status --%>
                    <div class="all">
                        <c:forEach var="entry" items="${requestScope.mangaStatus}">
                            <div  class="one">
                                <input type="checkbox" id="${entry.name()}" name="status" value="${entry.name()}">
                                <label for="${entry.name()}">${entry.toString()}</label>
                            </div>
                        </c:forEach>
                    </div>

                    <label class="filter-name">Rating:</label>
                    <%-- This are the range inputs for the min and max score --%>
                    <div class="rating">
                        <div class="range-slider container">
                            <span class="output outputOne"></span>
                            <span class="output outputTwo"></span>
                            <span class="full-range"></span>
                            <span class="incl-range"></span>
                            <input name="minScore" value="0" min="0" max="10" step="0.1" type="range">
                            <input name="maxScore" value="10" min="0" max="10" step="0.1" type="range">
                        </div>
                    </div>

                    <%-- This are the range inputs for the min and max start date --%>
                    <div>
                        <label  class="filter-name" for="startDate">Start Date:</label>
                        <input class="date" type="date" id="startDate" name="startDate">
                        <br/>
                        <label  class="filter-name" for="endDate">End Date:</label>
                        <input class="date" type="date" id="endDate" name="endDate">
                    </div>

                    <div>
                        <label  class="filter-name" for="orderBy">Order By:</label>
                        <select class="order" name="orderBy" id="orderBy">
                            <option value="title 1">Title enc</option>
                            <option value="title -1">Title dec</option>
                            <option value="average_rating 1">Average Rating enc</option>
                            <option value="average_rating -1">Average Rating dec</option>
                            <option value="start_date 1">Start Date enc</option>
                            <option value="start_date -1">Start Date dec</option>
                        </select>
                    </div>
                    <input class="search" type="submit" value="SEARCH">
                </form>
            </div>
        </div>

        <section id="resultsSection">
            <h1 id="totalResults"></h1>
            <div id="orderSelection"></div>
            <div id="mediaContentContainer"></div>
            <div id="pageSelection"></div>
        </section>

    </section>

    <script>
        const mediaType = "manga";
        const mediaDetailHRef = "${pageContext.request.contextPath}/manga?mediaId=";
        const authenticatedUser = ${isLogged};
        const contextPath = "${pageContext.request.contextPath}";
        const authURI = "${pageContext.request.contextPath}/auth";
        const servletURI = "${pageContext.request.contextPath}/mainPage/manga";
        const mangaDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_MANGA}";
        const animeDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_ANIME}";
    </script>
</body>
</html>
