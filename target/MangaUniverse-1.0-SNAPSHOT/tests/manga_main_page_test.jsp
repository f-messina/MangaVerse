<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 12:31
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>MAIN PAGE</title>
</head>
<body>
<button onclick="window.location.href='mainPage'">Home</button>
<form action="mainPage" method="post">
    <input type="hidden" name="action" value="search">
    <input type="hidden" name="type" value="manga">
    <label for="search">Title:</label>
    <input type="search" id="search" name="searchTerm" placeholder="Title">
    <input type="submit" value="SEARCH">
</form>
<form action="mainPage" method="post">
    <input type="hidden" name="action" value="search">
    <input type="hidden" name="type" value="manga">
    <label>Genres:</label><br/>
    <form>
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
        <input type="submit" value="SEARCH">
    </form>
</form>

<section>
    <h1>Total results: ${requestScope.mediaContentPage.getTotalCount()}</h1>
    <c:forEach items="${requestScope.mediaContentPage.getEntries()}" var="manga">
        <article>
            <h2>${manga.getTitle()}</h2>
            <img src="${manga.getImageUrl()}" alt="No image">
            <p>${manga.getAverageRating()}</p>
            <p>${manga.getStartDate()}</p>
            <p>${manga.getEndDate()}</p>
        </article>
    </c:forEach>
</section>
<!-- Navigation bar -->
<div>
    <form action="mainPage" method="post">
        <input type="hidden" name="action" value="changePage">
        <input type="hidden" name="type" value="manga">

        <c:if test="${requestScope.page > 1}">
            <button type="submit" class="navigation-button" name="page" value="${requestScope.page - 1}">Previous Page</button>
        </c:if>
        <c:if test="${requestScope.page < requestScope.mediaContentPage.getTotalPages()}">
            <button type="submit" class="navigation-button" name="page" value="${requestScope.page + 1}">Next Page</button>
        </c:if>
    </form>
</div>

<script>
    function toggleRadio(element) {
        if(element.classList.contains("active")) {
            element.checked = false;
        }
        element.classList.toggle("active");
        let radios = document.getElementsByName(element.name);
        radios.forEach(radio => {
            if (radio !== element) {
                radio.classList.remove("active");
            }
        });
    }
</script>
</body>
</html>
