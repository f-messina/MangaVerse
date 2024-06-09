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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/media_content_test.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css"/>
    <title>ANIME</title>
</head>
<body>
<c:set var="isLogged" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" /> <!-- check if the user is logged in -->
<c:set var="isManager" value="${isLogged and sessionScope[Constants.AUTHENTICATED_USER_KEY].getType().equals(UserType.USER)}" />

    <!-- navbar -->
    <nav class="nav-bar" id="navbar">
        <div id="logo" class="clickable"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></div>
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
            <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
            <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            <c:choose>
                <c:when test="${isLogged}">
                    <div class="logout" onclick="logout('mainPage')">
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="sign-out-alt" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="logout-icon"><path data-v-04b245e6="" fill="currentColor" d="M497 273L329 441c-15 15-41 4.5-41-17v-96H152c-13.3 0-24-10.7-24-24v-96c0-13.3 10.7-24 24-24h136V88c0-21.4 25.9-32 41-17l168 168c9.3 9.4 9.3 24.6 0 34zM192 436v-40c0-6.6-5.4-12-12-12H96c-17.7 0-32-14.3-32-32V160c0-17.7 14.3-32 32-32h84c6.6 0 12-5.4 12-12V76c0-6.6-5.4-12-12-12H96c-53 0-96 43-96 96v192c0 53 43 96 96 96h84c6.6 0 12-5.4 12-12z" class=""></path></svg>
                        <div class="logout-text">Log Out</div>
                    </div>
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

    <div class="page-content">
        <div class="media media-page-unscoped media-anime">
            <div class="header-wrap">
                <div class="banner">
                    <div class="shadow"></div>
                </div>
                <div class="header"><!---->
                    <div class="container" style="min-height: 250px;">
                        <div class="cover-wrap overlap-banner">
                            <div class="cover-wrap-inner" style="position: static;">
                                <img src="${requestScope.media.imageUrl}" alt="cover" class="cover">
                                <c:if test="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
                                <div class="actions">
                                    <div id="like-button" class="favourite <c:if test="${requestScope.isLiked}"> isFavourite </c:if>">
                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="heart" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-heart fa-w-16">
                                            <path fill="currentColor" d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z" class=""></path>
                                        </svg> <!---->
                                    </div>
                                </div>
                                </c:if>
                            </div>
                        </div>
                        <div class="content">
                            <h1>${requestScope.media.title}</h1>
                            <p class="description"><c:out value="${empty requestScope.media.synopsis ? '' : requestScope.media.synopsis}"/></p>
                            <div class="nav">
                                <div class="link router-link-exact-active router-link-active">Reviews</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="main-div container">
                <div class="sidebar">
                    <div class="data">
                        <div class="data-set"><div class="type">Format</div> <div  class="value">TV<!----></div></div>
                        <div class="data-set"><div  class="type">Status</div> <div  class="value">Releasing</div></div>
                        <div class="data-set"><div  class="type">Season</div> <div class="value">Spring 2024</div></div>
                        <div class="data-set"><div  class="type">Episodes</div> <div  class="value">54</div></div>
                        <div class="el-tooltip data-set" aria-describedby="el-tooltip-4537" tabindex="0"><div  class="type">Average Score</div> <div  class="value">80%</div></div>
                        <div class="data-set"><div  class="type">Likes</div> <div  class="value">2752</div></div>
                        <div class="data-set"><div  class="type">Studios</div> <div  class="value">ufotable</div></div>
                        <div class="data-set"><div  class="type">Producers</div> <div  class="value">Aniplex</div></div>
                        <div class="data-set data-list"><div  class="type">Tags</div> <div  class="value"><span ><a  href="/search/anime/Action" class="">Action</a><br ></span><span ><a  href="/search/anime/Adventure" class="">Adventure</a><br ></span><span ><a  href="/search/anime/Drama" class="">Drama</a><br ></span><span ><a  href="/search/anime/Fantasy" class="">Fantasy</a><br ></span><span ><a  href="/search/anime/Supernatural" class="">Supernatural</a><!----></span></div></div>
                    </div>
                    <div class="review-button">
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="edit" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" class="icon svg-inline--fa fa-edit fa-w-18">
                            <path fill="currentColor" d="M402.6 83.2l90.2 90.2c3.8 3.8 3.8 10 0 13.8L274.4 405.6l-92.8 10.3c-12.4 1.4-22.9-9.1-21.5-21.5l10.3-92.8L388.8 83.2c3.8-3.8 10-3.8 13.8 0zm162-22.9l-48.8-48.8c-15.2-15.2-39.9-15.2-55.2 0l-35.4 35.4c-3.8 3.8-3.8 10 0 13.8l90.2 90.2c3.8 3.8 10 3.8 13.8 0l35.4-35.4c15.2-15.3 15.2-40 0-55.2zM384 346.2V448H64V128h229.8c3.2 0 6.2-1.3 8.5-3.5l40-40c7.6-7.6 2.2-20.5-8.5-20.5H48C21.5 64 0 85.5 0 112v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V306.2c0-10.7-12.9-16-20.5-8.5l-40 40c-2.2 2.3-3.5 5.3-3.5 8.5z" class=""></path>
                        </svg>
                        <span>Write Review</span>
                    </div>
                    <div class="review-button delete-review">
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="edit" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" class="icon svg-inline--fa fa-edit fa-w-18">
                            <g transform="translate(-30, 80)">
                                <path fill="currentColor" d="M87.748,388.784c0.461,11.01,9.521,19.699,20.539,19.699h191.911c11.018,0,20.078-8.689,20.539-19.699l13.705-289.316H74.043L87.748,388.784z M247.655,171.329c0-4.61,3.738-8.349,8.35-8.349h13.355c4.609,0,8.35,3.738,8.35,8.349v165.293c0,4.611-3.738,8.349-8.35,8.349h-13.355c-4.61,0-8.35-3.736-8.35-8.349V171.329z M189.216,171.329c0-4.61,3.738-8.349,8.349-8.349h13.355c4.609,0,8.349,3.738,8.349,8.349v165.293c0,4.611-3.737,8.349-8.349,8.349h-13.355c-4.61,0-8.349-3.736-8.349-8.349V171.329L189.216,171.329z M130.775,171.329c0-4.61,3.738-8.349,8.349-8.349h13.356c4.61,0,8.349,3.738,8.349,8.349v165.293c0,4.611-3.738,8.349-8.349,8.349h-13.356c-4.61,0-8.349-3.736-8.349-8.349V171.329z"></path>
                                <path fill="currentColor" d="M343.567,21.043h-88.535V4.305c0-2.377-1.927-4.305-4.305-4.305h-92.971c-2.377,0-4.304,1.928-4.304,4.305v16.737H64.916c-7.125,0-12.9,5.776-12.9,12.901V74.47h304.451V33.944C356.467,26.819,350.692,21.043,343.567,21.043z"></path>
                            </g>
                        </svg>
                        <span>Delete Review</span>
                    </div>
                </div>
                <div class="review-boxes">
                <c:choose>
                    <c:when test="${not empty requestScope.reviews}">
                        <c:forEach var="review" items="${requestScope.reviews.getEntries()}">
                        <div class="review-box">
                            <div class="review-row">
                                <a href="/MangaVerse/profile?userId=${review.user.id}" class="review-media-title">${review.user.username}</a>
                                <p class="review-rating">${empty review.rating ? 'N/A' : review.rating}</p>
                            </div>
                            <p class="review-comment">${empty review.comment ? 'N/A' : review.comment}</p>
                            <p class="review-date">${review.date}</p>
                        </div>
                    </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="review">
                            <p>No reviews yet</p>
                        </div>
                    </c:otherwise>
                </c:choose>
                </div>
                <div id="reviews">
                    <div class="review-list">
                        <c:choose>
                            <c:when test="${not empty requestScope.reviews}">
                                <c:forEach var="review" items="${requestScope.media.getEntries()}">
                                    <div class="review">
                                        <p class="username-review">${review.user.username}</p>
                                        <div class="inside-review">
                                            <div>
                                                <img src="${review.user.profilePicUrl}" alt="profile image" />
                                            </div>
                                            <div class="review-text">
                                                <p>${review.date}</p>
                                                <p>${review.comment}</p>
                                                <p>${review.rating}</p>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="review">
                                    <p>No reviews yet</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <section class="info">
        <div class="mediaContent-info">
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
        </div>
    </section>

    <section id="edit-reviews">
        <c:if test="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
            <div id="review-form-container" class="review-form">
                <c:set var="found" value="false"/>
                <c:forEach var="review" items="${requestScope.reviews.getEntries()}">
                    <c:if test="${review.user.id == sessionScope[Constants.AUTHENTICATED_USER_KEY].getId()}">
                        <c:set var="found" value="true"/>
                        <button id="enable-modify-review" type="submit">Modify</button>
                        <button id="delete-review" type="submit">Delete</button>
                        <div id="modify-review-container" class="popup-container hidden">
                            <div class="list-popup">
                                <div class="popup-content">
                                    <form id="modify-review-form" method="post" action="${pageContext.request.contextPath}/manga" autocomplete="off" class="forms">
                                        <input type="hidden" name="mediaId" value="${requestScope.media.id}">
                                        <input type="hidden" name="action" value="addReview">
                                        <input type="hidden" name="reviewId" value="${review.id}">
                                        <div class="form-group">
                                            <label>Change the review:</label>
                                            <textarea id="modify-comment" name="comment" rows="4" cols="50" placeholder="Write a comment" >${review.comment}</textarea>
                                            <input id="modify-rating" type="number" name="rating" step="1" min="0" max = "10" placeholder="Rating" value="${review.rating}">
                                        </div>
                                        <button id="submit-modify-review" type="submit">Send</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
                <c:if test="${found == false}">
                    <button id="enable-add-review" type="submit">Write a comment</button>
                    <div id="add-review-container" class="popup-container hidden">
                        <div class="list-popup">
                            <div class="popup-content">
                                <form id="add-review-form" method="post" action="${pageContext.request.contextPath}/manga" autocomplete="off" class="forms">
                                    <input type="hidden" name="mediaId" value="${requestScope.media.id}">
                                    <input type="hidden" name="action" value="addReview">
                                    <div class="form-group">
                                        <label>Change the review:</label>
                                        <textarea id="add-comment" name="comment" rows="4" cols="50" placeholder="Write a comment"></textarea>
                                        <input id="add-rating" type="number" name="rating" step="1" min="0" max = "10" placeholder="Rating">
                                    </div>
                                    <button id="submit-add-review" type="submit">Send</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </c:if>
    </section>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/media_content_test.js" defer></script>
    <script>
        const mediaId = "${requestScope.media.id}";
        const contextPath = "${pageContext.request.contextPath}";
        const mediaType = "anime";
    </script>
</body>
</html>
