<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 12:31
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>MAIN PAGE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/range_input.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_page_test.css">
    <link
            href="https://fonts.googleapis.com/css2?family=Fira+Sans+Condensed:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&family=Roboto:wght@300;400&display=swap"
            rel="stylesheet"
    />
    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"
            integrity="sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA=="
            crossorigin="anonymous"
            referrerpolicy="no-referrer"
    />
    <link rel="stylesheet" href="../css/main-registered-user.css"/>
    <link
            rel="stylesheet"
            href="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.css"
    />
    <script src="${pageContext.request.contextPath}/js/range_input.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/main_page_test.js" defer></script>
</head>
<body>

    <nav>
        <a href="#"><img src="../images/logo-with-initial.png" alt="logo" /></a>
        <div class="nav-items">
            <a href="#" class="anime">Anime</a>
            <a href="#" class="manga">Manga</a>
            <a href="../profile.jsp">Profile</a>
            <a href="#" class="small-pic"><img alt = "user icon" src = "../images/user-icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
        </div>
    </nav>

    <button style="margin-top: 100px; padding: 8px" class="search" onclick="window.location.href='mainPage'">Home</button>
    <div class="write-search">
        <form id="searchForm" action="${pageContext.request.contextPath}/mainPage/manga" method="post">
            <input type="hidden" name="action" value="search">
            <label class="filter-name" for="search">Title:</label>
            <input type="search" id="search" name="searchTerm" placeholder="Title">
            <input class="search" type="submit" value="SEARCH">
        </form>
        <button onclick="toggleFiltersDisplay()" class="more-filtering">See Detailed Filtering</button>
    </div>

    <div id="filtersFormContainer">
        <form id="filterForm" action="${pageContext.request.contextPath}/mainPage/manga" method="post">
            <input type="hidden" name="action" value="search">

            <div class="title-ope">
                <%-- This are the radios for the genres --%>
                <label class="filter-name">Genres:</label><br/>
                <div class="operator" style="background-color: #ecebeb; padding: 5px">
                    <label >Operator:</label>
                    <input class="gap" type="radio" name="genreOperator" checked value="and">and
                    <input class="gap" type="radio" name="genreOperator" value="or">or
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

    <section id="resultsSection"></section>

    <!-- page bar -->
    <section id="changePage">
        <form action="${pageContext.request.contextPath}/mainPage/manga" method="post">
            <input type="hidden" name="action" value="sortAndPaginate">

            <c:if test="${requestScope.page > 1}">
                <button type="submit" class="navigation-button" name="page" value="${requestScope.page - 1}">Previous Page</button>
            </c:if>
            <c:if test="${requestScope.page < requestScope.mediaContentPage.getTotalPages()}">
                <button type="submit" class="navigation-button" name="page" value="${requestScope.page + 1}">Next Page</button>
            </c:if>
        </form>
    </section>

<script>
    <c:set var="authenticatedUser" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />
    <c:set var="lists" value="${authenticatedUser ? sessionScope[Constants.AUTHENTICATED_USER_KEY].getLists() : null}" />

    const mediaDetailHRef = "${pageContext.request.contextPath}/manga?mediaId=";
    const authenticatedUser = ${authenticatedUser};
    const servletURI = "${pageContext.request.contextPath}/mainPage/manga";
    const lists = [];
    <c:forEach items="${lists}" var="list">
    lists.push(["${list.getId()}", "${list.getName()}"]);
    </c:forEach>
</script>
</body>
</html>
