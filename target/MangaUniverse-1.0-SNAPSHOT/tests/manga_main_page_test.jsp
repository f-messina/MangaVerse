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
    <script src="${pageContext.request.contextPath}/js/range_input.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/main_page_test.js" defer></script>
</head>
<body>
<form id="searchForm" action="${pageContext.request.contextPath}/mainPage/manga" method="post">
    <input type="hidden" name="action" value="search">
    <label for="search">Title:</label>
    <input type="search" id="search" name="searchTerm" placeholder="Title">
    <input type="submit" value="SEARCH">
</form>
<form id="filterForm" action="${pageContext.request.contextPath}/mainPage/manga" method="post" style="width: 10rem">
    <input type="hidden" name="action" value="search">

    <%-- This are the radios for the genres --%>
    <label>Genres:</label><br/>
    <c:forEach items="${requestScope.mangaGenres}" var="genre">
    <div>
        <input type="radio" name="${genre}" style="color: green" onclick="toggleRadio(this)" value="select">
        <input type="radio" name="${genre}" style="color: red" onclick="toggleRadio(this)" value="avoid">
        <label>${genre}</label>
    </div>
    </c:forEach>
    <div>
        <label>Operator:</label>
        <input type="radio" name="genreOperator" checked value="and">and
        <input type="radio" name="genreOperator" value="or">or
    </div>

    <%-- This are the checkboxes for the types --%>
    <div>
        <label>Type:</label>
        <c:forEach var="entry" items="${requestScope.mangaTypes}">
        <div>
            <input type="checkbox" id="${entry.name()}" name="mangaTypes" value="${entry.name()}">
            <label for="${entry.name()}">${entry.toString()}</label>
        </div>
        </c:forEach>
    </div>

    <%-- This are the checkboxes for the demographics --%>
    <div>
        <label>Demographics:</label>
        <c:forEach var="entry" items="${requestScope.mangaDemographics}">
        <c:if test="${entry.name() != 'UNKNOWN'}">
        <div>
            <input type="checkbox" id="${entry.name()}" name="mangaDemographics" value="${entry.name()}">
            <label for="${entry.name()}">${entry.toString()}</label>
        </div>
        </c:if>
        </c:forEach>
    </div>

    <%-- This are the checkboxes for the status --%>
    <div>
        <label>Publishing status:</label>
        <c:forEach var="entry" items="${requestScope.mangaStatus}">
        <div>
            <input type="checkbox" id="${entry.name()}" name="status" value="${entry.name()}">
            <label for="${entry.name()}">${entry.toString()}</label>
        </div>
        </c:forEach>
    </div>

    <%-- This are the range inputs for the min and max score --%>
    <div>
        <label>Rating:</label>
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
        <label for="startDate">Start Date:</label>
        <input type="date" id="startDate" name="startDate">
        <br/>
        <label for="endDate">End Date:</label>
        <input type="date" id="endDate" name="endDate">
    </div>

    <div>
        <label for="orderBy">Order By:</label>
        <select name="orderBy" id="orderBy">
            <option value="title 1">Title enc</option>
            <option value="title -1">Title dec</option>
            <option value="average_rating 1">Average Rating enc</option>
            <option value="average_rating -1">Average Rating dec</option>
            <option value="start_date 1">Start Date enc</option>
            <option value="start_date -1">Start Date dec</option>
        </select>
    </div>
    <input type="submit" value="SEARCH">
</form>

<section id="resultsSection"></section>

<!-- page bar -->
<div>
    <form action="${pageContext.request.contextPath}/mainPage/manga" method="post">
        <input type="hidden" name="action" value="sortAndPaginate">

        <c:if test="${requestScope.page > 1}">
            <button type="submit" class="navigation-button" name="page" value="${requestScope.page - 1}">Previous Page</button>
        </c:if>
        <c:if test="${requestScope.page < requestScope.mediaContentPage.getTotalPages()}">
            <button type="submit" class="navigation-button" name="page" value="${requestScope.page + 1}">Next Page</button>
        </c:if>
    </form>
</div>


<c:set var="authenticatedUser" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />
<c:set var="lists" value="${authenticatedUser ? sessionScope[Constants.AUTHENTICATED_USER_KEY].getLists() : null}" />
<script>

    const authenticatedUser = ${authenticatedUser};
    const servletURI = "${pageContext.request.contextPath}/mainPage/manga";
    const lists = [];
    <c:forEach items="${lists}" var="list">
        lists.push(["${list.getId()}", "${list.getName()}"]);
    </c:forEach>
</script>
</body>
</html>