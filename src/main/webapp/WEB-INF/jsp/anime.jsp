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
    <title>ANIME</title>
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
                        <form action="${pageContext.request.contextPath}/auth" method="post">
                            <input type="hidden" name="action" value="logout">
                            <input type="hidden" name="targetServlet" value="anime?mediaId=${requestScope.anime.id}">
                            <button type="submit" class="logout">Log Out</button>
                        </form>
                        <a href="${pageContext.request.contextPath}/profile" class="small-pic"><img alt="profile bar" src="${pageContext.request.contextPath}/${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}"></a>
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
            <form id="mediaContent-form" method="post" action="${pageContext.request.contextPath}/anime" autocomplete="off" class="forms">
                <div id="mediaContent-info" class="two-div">
                    <div class="texts">
                        <div class="form-group">
                            <p class="info-name">Title:</p>
                            <div class="info-box"><p id="title"><c:out value="${empty requestScope.media.title ? 'N/A' : requestScope.media.title}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Type:</p>
                            <div class="info-box"><p id="type"><c:out value="${empty requestScope.media.type ? 'N/A' : requestScope.media.type}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Episode Count:</p>
                            <div class="info-box"><p id="episode-count"><c:out value="${empty requestScope.media.episodeCount ? 'N/A' : requestScope.media.episodeCount}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Publishing Status:</p>
                            <div class="info-box"><p  id="publishing-status"><c:out value="${empty requestScope.media.status ? 'N/A' : requestScope.media.status}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Tags:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.tags}">
                                    <div class="info-box"><p>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="tags">
                                        <c:forEach var="tag" items="${requestScope.media.tags}">
                                            <c:out value="${tag}"/>
                                        </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Studios:</p>
                            <div class="info-box"><p id="studio"><c:out value="${empty requestScope.media.studios ? 'N/A' : requestScope.media.studios}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Season:</p>
                            <div class="info-box"><p id="season"><c:out value="${empty requestScope.media.season ? 'N/A' : requestScope.media.season}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Average Rating:</p>
                            <div class="info-box"><p id="average-rating"><c:out value="${empty requestScope.media.averageRating ? 'N/A' : requestScope.media.averageRating}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Year:</p>
                            <div class="info-box"><p id="year"><c:out value="${empty requestScope.media.year ? 'N/A' : requestScope.media.year}"/></p></div>
                        </div>
                    </div>
                    <div class="texts">
                        <div class="form-group">
                            <p class="info-name">Synopsis:</p>
                            <div class="info-box"><p id="synopsis"><c:out value="${empty requestScope.media.synopsis ? 'N/A' : requestScope.media.synopsis}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Producers:</p>
                            <div class="info-box"><p  id="producers"><c:out value="${empty requestScope.media.producers ? 'N/A' : requestScope.media.producers}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Related Anime:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.relatedAnime}">
                                    <div class="info-box"><p>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="related-anime">
                                        <c:forEach var="relatedAnime" items="${requestScope.media.relatedAnime}">
                                            <c:out value="${relatedAnime}"/>
                                        </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </form>
        </section>
    </div>
</body>
</html>
