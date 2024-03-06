<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 8.02.2024
  Time: 15:07
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>MANGA</title>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/media_content_test.css">
    <script src="${pageContext.request.contextPath}/js/media_content_test.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/logout.js" defer></script>
</head>
<body>
    <div class="nav-bar">
            <nav>
                <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
                <h1>Profile Page</h1>
                <div class="nav-items">
                    <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
                    <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
                    <c:choose>
                        <c:when test="${empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
                            <a href="${pageContext.request.contextPath}/login">Log In</a>
                            <a href="${pageContext.request.contextPath}/signup">Sign Up</a>
                        </c:when>
                        <c:otherwise>
                            <a id="logoutBtn" href="${pageContext.request.contextPath}/manga?mediaId=${requestScope.manga.id}">Logout</a>
                            <a href="${pageContext.request.contextPath}/profile">Profile</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </nav>
    </div>

    <div class="info">
        <section class="mediaContent-info">
            <div class="mediaContent-img">
                <img alt="profile image" src=${empty requestScope.media.imageUrl ? '${pageContext.request.contextPath}/images/logo-with-name.png' : requestScope.media.imageUrl}>
            </div>
            <form id="mediaContent-form" method="post" action="${pageContext.request.contextPath}/manga" autocomplete="off" class="forms">
                <div id="mediaContent-info" class="two-div">
                    <div class="texts">
                        <div class="form-group">
                            <p class="info-name">Title:</p>
                            <div class="info-box"><p id="title"><c:out value="${empty requestScope.media.title ? 'N/A' : requestScope.media.title}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Title Japanese:</p>
                            <div class="info-box"><p id="title-japanese"><c:out value="${empty requestScope.media.titleJapanese ? 'N/A' : requestScope.media.titleJapanese}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Publishing Status:</p>
                            <div class="info-box"><p id="status"><c:out value="${empty requestScope.media.status ? 'N/A' : requestScope.media.status}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Genres:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.genres}">
                                    <div class="info-box"><p>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="genres">
                                    <c:forEach var="genre" items="${requestScope.media.genres}">
                                        <c:out value="${genre}"/>
                                    </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Themes:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.themes}">
                                    <div class="info-box"><p>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="themes">
                                    <c:forEach var="theme" items="${requestScope.media.themes}">
                                        <c:out value="${theme}"/>
                                    </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Demographics:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.demographics}">
                                    <div class="info-box"><p id=>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="demographics">
                                    <c:forEach var="demographic" items="${requestScope.media.demographics}">
                                        <c:out value="${demographic}"/>
                                    </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Authors:</p>
                            <c:forEach var="author" items="${requestScope.media.authors}">
                                Name: <div class="info-box"><p id="authors">Name: <c:out value="${author.name}"/>
                                Role: <c:out value="${author.role}"/></p></div>
                            </c:forEach>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Serialization:</p>
                            <div class="info-box"><p id="serialization"><c:out value="${empty requestScope.media.serializations ? 'N/A' : requestScope.media.serializations}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Start Date:</p>
                            <div class="info-box"><p id="start-date"><c:out value="${empty requestScope.media.startDate ? 'N/A' : requestScope.media.startDate}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">End Date:</p>
                            <div class="info-box"><p id="end-date"><c:out value="${empty requestScope.media.endDate ? 'N/A' : requestScope.media.endDate}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Average Rating:</p>
                            <div class="info-box"><p id="average-rating"><c:out value="${empty requestScope.media.averageRating ? 'N/A' : requestScope.media.averageRating}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Volumes:</p>
                            <div class="info-box"><p id="volumes"><c:out value="${empty requestScope.media.volumes ? 'N/A' : requestScope.media.volumes}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Chapters:</p>
                            <div class="info-box"><p id="chapters"><c:out value="${empty requestScope.media.chapters ? 'N/A' : requestScope.media.chapters}"/></p></div>
                        </div>
                    </div>
                    <div class="texts">
                        <div class="form-group">
                            <p class="info-name">Synopsis:</p>
                            <div class="info-box"><p id="synopsis"><c:out value="${empty requestScope.media.synopsis ? 'N/A' : requestScope.media.synopsis}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Background:</p>
                            <div class="info-box"><p id="background"><c:out value="${empty requestScope.media.background ? 'N/A' : requestScope.media.background}"/></p></div>
                        </div>
                    </div>
                </div>
            </form>
        </section>
    </div>
</body>
</html>
