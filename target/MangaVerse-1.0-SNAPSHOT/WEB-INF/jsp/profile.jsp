<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 15:46
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="it.unipi.lsmsd.fnf.model.enums.Gender" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="preconnect" href="https://fonts.googleapis.com%22%3E/" crossorigin />
    <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin />
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile_test.css">
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/profile_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/country_dropdown.js" defer></script>
    <title>PROFILE</title>
</head>
<body>
    <nav>
        <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
        <h1>Profile Page</h1>
        <div class="nav-items">
            <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
            <form action="${pageContext.request.contextPath}/auth" method="post">
                <input type="hidden" name="action" value="logout">
                <input type="hidden" name="targetServlet" value="/auth">
                <button type="submit" class="logout">Log Out</button>
            </form>
            <a href="#" class="small-pic"><img alt="profile bar" src="${pageContext.request.contextPath}/images/account-icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
        </div>
    </nav>

    <c:set var="userInfo" value="${requestScope['userInfo']}" />
    <section class="profile">
        <div class="profile-img">
            <img alt="profile image" src="${userInfo.getProfilePicUrl()}">
        </div>
        <div id="user-info" class="texts">
            <form id="profile-form" method="post" action="${pageContext.request.contextPath}/profile" autocomplete="off" class="forms">
                <input type="hidden" name="action" value="update-info">
                <div class="form-group">
                    <label for="username"><i class="zmdi zmdi-account material-icons-name"></i></label>
                    <input type="text" class="editable info-box" name="username" value="${userInfo.getUsername()}" id="username" placeholder="Username" oninput="validateUsername()" required disabled/>
                    <span id="username-error" style="color: red"><c:out value="${requestScope['usernameError']}" /></span>
                </div>
                <div class="form-group">
                    <label for="email"><i class="zmdi zmdi-email"></i></label>
                    <input type="email" class="info-box" name="email" id="email" value="${userInfo.getEmail()}" placeholder="Your Email" required disabled/>
                </div>
                <div class="form-group">
                    <label for="fullname"><i class="zmdi zmdi-lock-outline"></i></label>
                    <input type="text" class="editable info-box" name="fullname" id="fullname" value="${userInfo.getFullname()}" placeholder="Full Name (Optional)" disabled/>
                </div>
                <div class="form-group">
                    <label for="description"><i class="zmdi zmdi-lock-outline"></i></label>
                    <input type="text" class="editable info-box" name="description" id="description" value="${userInfo.getDescription()}" placeholder="Description (Optional)" disabled/>
                </div>
                <div class="form-group">
                    <label for="gender"><i class="zmdi zmdi-lock-outline"></i></label>
                    <select id="gender" class="editable info-box" name="gender" disabled>
                        <c:set var="selectedGender" value="${userInfo.getGender()}" />
                        <c:forEach var="gender" items="${Gender.values()}">
                            <option value="${gender.name()}" <c:if test="${gender.name() eq selectedGender}">selected</c:if>>${gender.toString()}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="birthdate"><i class="zmdi zmdi-lock-outline"></i></label>
                    <input type="date" class="editable info-box" name="birthdate" value="${userInfo.getBirthday()}" id="birthdate" placeholder="Birthdate" disabled/>
                </div>
                <div class="form-group">
                    <label for="country"><i class="zmdi zmdi-lock-outline"></i></label>
                    <input type="text" name="country" class="editable info-box" id="country" placeholder="Country (Optional)" value="${userInfo.getLocation()}" oninput="validateCountry()" disabled/>
                    <div class="dropdown-content" id="country-dropdown" onclick="validateCountry()"></div>
                    <span id="country-error" style="color: red"></span>
                </div>
                <div class="form-group">
                    <label for="joined-date"><i class="zmdi zmdi-lock"></i></label>
                    <input type="text" class="info-box" name="joined-date" id="joined-date" value="${userInfo.getJoinedDate()}" placeholder="Joined Date" disabled/>
                </div>
                <div class="form-group">
                    <button type="submit" id="edit-button" class="submit-btn">Modify</button>
                    <button type="button" id="confirm-button" class="submit-btn" style="display: none">Confirm</button>
                </div>
            </form>
        </div>
    </section>

    <section class="likes">
        <h1>Likes</h1>
        <div class="likes-container">
            <div class="likes-box">
                <h2>Manga</h2>
                <ul class="manga-anime-list" id="manga-list">
                    <c:forEach var="manga" items="${requestScope.likedManga}">
                        <li>
                            <a class="manga-anime" href="${pageContext.request.contextPath}/manga?mediaId=${manga.getId()}"><c:out value="${manga.getTitle()}"/></a>
                            <img class="list-img" src="${manga.getImageUrl()}" alt="${fn:escapeXml(manga.getTitle())} image" />
                        </li>
                    </c:forEach>
                    <c:if test="${empty requestScope.likedManga}">
                        <li>No liked manga found</li>
                    </c:if>
                </ul>
            </div>
            <div class="likes-box">
                <h2>Anime</h2>
                <ul class="manga-anime-list" id="anime-list">
                    <c:forEach var="anime" items="${requestScope.likedAnime}">
                        <li>
                            <a class="manga-anime" href="${pageContext.request.contextPath}/anime?mediaId=${anime.getId()}"><c:out value="${anime.getTitle()}"/></a>
                            <img class="list-img" src="${anime.getImageUrl()}" alt="${fn:escapeXml(anime.getTitle())} image" />
                        </li>
                    </c:forEach>
                    <c:if test="${empty requestScope.likedAnime}">
                        <li>No liked anime found</li>
                    </c:if>
                </ul>
            </div>
        </div>
    </section>

    <section class="reviews">
        <h1>Reviews</h1>
        <c:set var="reviews" value="${requestScope.reviews}" />
        <c:choose>
            <c:when test="${not empty reviews}">
                <c:forEach var="review" items="${reviews.getEntries()}">
                    <div class="review ">
                        <h2>
                            <c:out value="${review.getMediaContent().getTitle()}" />
                        </h2>
                        <p>
                            <c:out value="${review.getDate().toString()}" />
                            <br/>
                            <c:choose>
                                <c:when test="${not empty review.comment}">
                                    <c:out value="${review.comment}" />
                                </c:when>
                                <c:otherwise>
                                    No comment.
                                </c:otherwise>
                            </c:choose>
                            <br/>
                            <c:choose>
                                <c:when test="${not empty review.rating}">
                                    <c:out value="${review.rating}" />
                                </c:when>
                                <c:otherwise>
                                    No rating.
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </c:forEach>
                <button id="show-hide-button" onclick="toggleReviews()">Show All Reviews</button>
            </c:when>
            <c:otherwise>
                <h2>No reviews found</h2>
            </c:otherwise>
        </c:choose>
    </section>
</body>
</html>
