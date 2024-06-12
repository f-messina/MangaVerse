<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.format.FormatStyle" %>
<%@ page import="it.unipi.lsmsd.fnf.model.enums.UserType" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<c:set var="isLogged" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />
<c:set var="isManager" value="${isLogged and not sessionScope[Constants.AUTHENTICATED_USER_KEY].getType().equals(UserType.USER)}" />
<c:set var="media" value="${requestScope.media}"/>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/media_content.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user_list.css"/>
    <title><c:out value="${media.title}"/></title>
</head>
<body>

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
                <div class="logout" onclick="logout('manga?mediaId=${media.id}')">
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
            <div class="header landing-section"><!---->
                <div class="container" style="min-height: 250px;">
                    <div class="cover-wrap overlap-banner">
                        <div class="cover-wrap-inner" style="position: static;">
                            <img src="${media.imageUrl}" alt="cover" class="cover">
                            <c:if test="${isLogged and not isManager}">
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
                        <h1>${media.title}</h1>
                        <p class="description"><c:out value="${empty media.synopsis ? '' : media.synopsis}"/></p>
                        <div class="nav">
                            <div id="detail-section-button" class="link router-link-exact-active router-link-active">Details</div>
                            <div id="review-section-button" class="link">Reviews</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="main-div container">
            <div class="sidebar">
                <div class="data">
                    <c:if test="${not empty media.type}"><div class="data-set"><div class="type">Format</div> <div class="value"><c:out value="${media.type}"/></div></div></c:if>
                    <c:if test="${not empty media.status}"><div class="data-set"><div  class="type">Status</div> <div class="value"><c:out value="${media.status}"/></div></div></c:if>
                    <c:if test="${not empty media.volumes}"><div class="data-set"><div  class="type">Volumes</div> <div class="value"><c:out value="${media.volumes}"/></div></div></c:if>
                    <c:if test="${not empty media.chapters}"><div class="data-set"><div  class="type">Chapters</div> <div class="value"><c:out value="${media.chapters}"/></div></div></c:if>
                    <c:if test="${not empty media.startDate}"><div class="data-set"><div  class="type">Start Date</div> <div class="value"><c:out value="${media.startDate}"/></div></div></c:if>
                    <c:if test="${not empty media.endDate}"><div class="data-set"><div  class="type">End Date</div> <div class="value"><c:out value="${media.endDate}"/></div></div></c:if>
                    <c:if test="${not empty media.demographics}"><div class="data-set data-list"><div  class="type">Demographics</div> <div class="value"><c:forEach var="demographic" items="${media.demographics}" varStatus="status"><div class="tag"><c:out value="${demographic}"/></div><c:if test="${not status.last}"><br /></c:if></c:forEach></div></div></c:if>
                    <c:if test="${not empty media.authors}"><div class="data-set data-list"><div  class="type">Authors</div> <div class="value"><c:forEach var="author" items="${media.authors}" varStatus="status"><div class="tag"><c:out value="${author.name}"/> (<c:out value="${author.role}"/>)</div><c:if test="${not status.last}"><br /></c:if></c:forEach></div></div></c:if>
                    <c:if test="${not empty media.serializations}"><div class="data-set"><div  class="type">Serializations</div> <div class="value"><c:out value="${media.serializations}"/></div></div></c:if>
                    <c:if test="${not empty media.averageRating}"><div class="data-set"><div class="type">Average Score</div> <div class="value"><c:out value="${media.averageRating}"/></div></div></c:if>
                    <c:if test="${not empty media.likes}"><div class="data-set"><div  class="type">Likes</div> <div class="value"><c:out value="${media.likes}"/></div></div></c:if>
                    <c:if test="${not empty media.titleEnglish}"><div class="data-set"><div  class="type">English Title</div> <div class="value"><c:out value="${media.titleEnglish}"/></div></div></c:if>
                    <c:if test="${not empty media.titleJapanese}"><div class="data-set"><div  class="type">Japanese Title</div> <div class="value"><c:out value="${media.titleJapanese}"/></div></div></c:if>
                    <c:if test="${not empty media.genres}"><div class="data-set data-list"><div  class="type">Genres</div> <div class="value"><c:forEach var="genre" items="${media.genres}" varStatus="status"><div class="tag"><c:out value="${genre}"/></div><c:if test="${not status.last}"><br /></c:if></c:forEach></div></div></c:if>
                    <c:if test="${not empty media.themes}"><div class="data-set data-list"><div  class="type">Themes</div> <div class="value"><c:forEach var="theme" items="${media.themes}" varStatus="status"><div class="tag"><c:out value="${theme}"/></div><c:if test="${not status.last}"><br /></c:if></c:forEach></div></div></c:if>
                </div>
                <c:if test="${isLogged and not isManager}">
                    <c:choose>
                        <c:when test="${empty requestScope.userReview}">
                            <div class="review-container">
                                <div class="review-button toggle-form">
                                    <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="edit" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" class="icon svg-inline--fa fa-edit fa-w-18">
                                        <path fill="currentColor" d="M402.6 83.2l90.2 90.2c3.8 3.8 3.8 10 0 13.8L274.4 405.6l-92.8 10.3c-12.4 1.4-22.9-9.1-21.5-21.5l10.3-92.8L388.8 83.2c3.8-3.8 10-3.8 13.8 0zm162-22.9l-48.8-48.8c-15.2-15.2-39.9-15.2-55.2 0l-35.4 35.4c-3.8 3.8-3.8 10 0 13.8l90.2 90.2c3.8 3.8 10 3.8 13.8 0l35.4-35.4c15.2-15.3 15.2-40 0-55.2zM384 346.2V448H64V128h229.8c3.2 0 6.2-1.3 8.5-3.5l40-40c7.6-7.6 2.2-20.5-8.5-20.5H48C21.5 64 0 85.5 0 112v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V306.2c0-10.7-12.9-16-20.5-8.5l-40 40c-2.2 2.3-3.5 5.3-3.5 8.5z" class=""></path>
                                    </svg>
                                    <span>Write Review</span>
                                </div>
                                <div id="create-review" class="review-form">
                                    <div class="rating-container">
                                        <label class="review-label" for="add-rating">Score</label>
                                        <div class="rating-wrapper">
                                            <button type="button" class="minus-button">
                                                <svg  xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <path d="M5 12h14"></path>
                                                </svg>
                                            </button>
                                            <input id="add-rating" class="input-rating" type="text">
                                            <button type="button" class="plus-button">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <path d="M5 12h14"></path>
                                                    <path d="M12 5v14"></path>
                                                </svg>
                                            </button>
                                        </div>
                                    </div>
                                    <div class="user-comment">
                                        <label for="add-comment"></label>
                                        <div class="comment-input">
                                            <textarea id="add-comment" rows="4" cols="50" maxlength="300" placeholder="Write your review here..."></textarea>
                                        </div>
                                    </div>
                                    <div id="send-new-review" class="review-button send">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon svg-inline--fa fa-edit fa-w-18" viewBox="0 0 20 20">
                                            <path fill-rule="evenodd" clip-rule="evenodd" d="M18.2978 5.68315C17.9189 5.75055 17.3817 5.92686 16.5215 6.21358L10.0305 8.37724C9.20312 8.65304 8.61936 8.84795 8.19945 9.01179C7.99178 9.09282 7.84715 9.15754 7.74658 9.2106C7.66139 9.25554 7.63077 9.2803 7.62895 9.28156C7.22309 9.67446 7.22309 10.3255 7.62895 10.7184C7.63077 10.7197 7.66139 10.7444 7.74658 10.7894C7.84715 10.8424 7.99178 10.9072 8.19945 10.9882C8.61936 11.152 9.20312 11.3469 10.0305 11.6227C10.0495 11.6291 10.0683 11.6353 10.087 11.6415C10.3604 11.7325 10.6004 11.8123 10.8214 11.9292C11.3539 12.2108 11.7892 12.6461 12.0708 13.1786C12.1877 13.3996 12.2675 13.6396 12.3585 13.913C12.3647 13.9317 12.3709 13.9505 12.3773 13.9695C12.6531 14.7969 12.848 15.3806 13.0118 15.8005C13.0928 16.0082 13.1576 16.1528 13.2106 16.2534C13.2556 16.3386 13.2803 16.3692 13.2816 16.371C13.6745 16.7769 14.3255 16.7769 14.7184 16.371C14.7197 16.3692 14.7444 16.3386 14.7894 16.2534C14.8424 16.1528 14.9072 16.0082 14.9882 15.8005C15.152 15.3806 15.3469 14.7969 15.6227 13.9695L17.7864 7.4785C18.0731 6.61832 18.2494 6.0811 18.3168 5.70219C18.3182 5.6943 18.3196 5.68663 18.3208 5.67916C18.3134 5.68042 18.3057 5.68175 18.2978 5.68315ZM18.5568 5.66004C18.5566 5.66022 18.5533 5.65995 18.5475 5.65868C18.5541 5.65922 18.557 5.65985 18.5568 5.66004ZM18.3413 5.45245C18.34 5.44671 18.3398 5.44343 18.34 5.44322C18.3401 5.44302 18.3408 5.44588 18.3413 5.45245ZM17.9475 3.71406C18.4985 3.61605 19.253 3.58686 19.8331 4.16691C20.4131 4.74697 20.3839 5.50148 20.2859 6.05247C20.1896 6.5939 19.9632 7.27302 19.7077 8.03931L19.6838 8.11095L17.5201 14.6019L17.5107 14.6301C17.2464 15.423 17.0358 16.0549 16.8514 16.5275C16.6781 16.9717 16.4726 17.4321 16.1631 17.7541C14.9827 18.9825 13.0173 18.9825 11.8369 17.7541C11.5274 17.4321 11.3219 16.9717 11.1486 16.5275C10.9642 16.055 10.7536 15.423 10.4893 14.6303L10.4799 14.6019C10.3595 14.2407 10.3324 14.1694 10.3029 14.1136C10.209 13.9361 10.0639 13.791 9.88637 13.6971C9.83055 13.6676 9.75926 13.6405 9.39806 13.5201L9.36973 13.5107C8.57694 13.2464 7.94503 13.0358 7.47249 12.8514C7.0283 12.6781 6.56794 12.4726 6.24588 12.1631C5.01744 10.9826 5.01744 9.01733 6.24588 7.83686C6.56794 7.52738 7.0283 7.32189 7.47249 7.14859C7.94505 6.96421 8.57699 6.75356 9.36981 6.48929L9.39806 6.47988L15.889 4.31622C15.913 4.30823 15.9369 4.30027 15.9607 4.29234C16.727 4.03683 17.4061 3.81038 17.9475 3.71406Z" fill="currentColor"></path>
                                        </svg>
                                        <span>Send</span>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="review-container">
                                <div class="review-button toggle-form">
                                    <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="edit" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" class="icon svg-inline--fa fa-edit fa-w-18">
                                        <path fill="currentColor" d="M402.6 83.2l90.2 90.2c3.8 3.8 3.8 10 0 13.8L274.4 405.6l-92.8 10.3c-12.4 1.4-22.9-9.1-21.5-21.5l10.3-92.8L388.8 83.2c3.8-3.8 10-3.8 13.8 0zm162-22.9l-48.8-48.8c-15.2-15.2-39.9-15.2-55.2 0l-35.4 35.4c-3.8 3.8-3.8 10 0 13.8l90.2 90.2c3.8 3.8 10 3.8 13.8 0l35.4-35.4c15.2-15.3 15.2-40 0-55.2zM384 346.2V448H64V128h229.8c3.2 0 6.2-1.3 8.5-3.5l40-40c7.6-7.6 2.2-20.5-8.5-20.5H48C21.5 64 0 85.5 0 112v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V306.2c0-10.7-12.9-16-20.5-8.5l-40 40c-2.2 2.3-3.5 5.3-3.5 8.5z" class=""></path>
                                    </svg>
                                    <span>Edit Review</span>
                                </div>
                                <div id="update-review" class="review-form">
                                    <div class="rating-container">
                                        <label class="review-label" for="update-rating">Score</label>
                                        <div class="rating-wrapper">
                                            <button type="button" class="minus-button">
                                                <svg  xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <path d="M5 12h14"></path>
                                                </svg>
                                            </button>
                                            <input id="update-rating" class="input-rating" type="text" value="${requestScope.userReview.rating}">
                                            <button type="button" class="plus-button">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <path d="M5 12h14"></path>
                                                    <path d="M12 5v14"></path>
                                                </svg>
                                            </button>
                                        </div>
                                    </div>
                                    <div class="user-comment">
                                        <label for="update-comment"></label>
                                        <div class="comment-input">
                                            <textarea id="update-comment" rows="4" cols="50" maxlength="300" placeholder="Write your review here...">${requestScope.userReview.comment}</textarea>
                                        </div>
                                    </div>
                                    <div id="send-updated-review" class="review-button send">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon svg-inline--fa fa-edit fa-w-18" viewBox="0 0 20 20">
                                            <path fill-rule="evenodd" clip-rule="evenodd" d="M18.2978 5.68315C17.9189 5.75055 17.3817 5.92686 16.5215 6.21358L10.0305 8.37724C9.20312 8.65304 8.61936 8.84795 8.19945 9.01179C7.99178 9.09282 7.84715 9.15754 7.74658 9.2106C7.66139 9.25554 7.63077 9.2803 7.62895 9.28156C7.22309 9.67446 7.22309 10.3255 7.62895 10.7184C7.63077 10.7197 7.66139 10.7444 7.74658 10.7894C7.84715 10.8424 7.99178 10.9072 8.19945 10.9882C8.61936 11.152 9.20312 11.3469 10.0305 11.6227C10.0495 11.6291 10.0683 11.6353 10.087 11.6415C10.3604 11.7325 10.6004 11.8123 10.8214 11.9292C11.3539 12.2108 11.7892 12.6461 12.0708 13.1786C12.1877 13.3996 12.2675 13.6396 12.3585 13.913C12.3647 13.9317 12.3709 13.9505 12.3773 13.9695C12.6531 14.7969 12.848 15.3806 13.0118 15.8005C13.0928 16.0082 13.1576 16.1528 13.2106 16.2534C13.2556 16.3386 13.2803 16.3692 13.2816 16.371C13.6745 16.7769 14.3255 16.7769 14.7184 16.371C14.7197 16.3692 14.7444 16.3386 14.7894 16.2534C14.8424 16.1528 14.9072 16.0082 14.9882 15.8005C15.152 15.3806 15.3469 14.7969 15.6227 13.9695L17.7864 7.4785C18.0731 6.61832 18.2494 6.0811 18.3168 5.70219C18.3182 5.6943 18.3196 5.68663 18.3208 5.67916C18.3134 5.68042 18.3057 5.68175 18.2978 5.68315ZM18.5568 5.66004C18.5566 5.66022 18.5533 5.65995 18.5475 5.65868C18.5541 5.65922 18.557 5.65985 18.5568 5.66004ZM18.3413 5.45245C18.34 5.44671 18.3398 5.44343 18.34 5.44322C18.3401 5.44302 18.3408 5.44588 18.3413 5.45245ZM17.9475 3.71406C18.4985 3.61605 19.253 3.58686 19.8331 4.16691C20.4131 4.74697 20.3839 5.50148 20.2859 6.05247C20.1896 6.5939 19.9632 7.27302 19.7077 8.03931L19.6838 8.11095L17.5201 14.6019L17.5107 14.6301C17.2464 15.423 17.0358 16.0549 16.8514 16.5275C16.6781 16.9717 16.4726 17.4321 16.1631 17.7541C14.9827 18.9825 13.0173 18.9825 11.8369 17.7541C11.5274 17.4321 11.3219 16.9717 11.1486 16.5275C10.9642 16.055 10.7536 15.423 10.4893 14.6303L10.4799 14.6019C10.3595 14.2407 10.3324 14.1694 10.3029 14.1136C10.209 13.9361 10.0639 13.791 9.88637 13.6971C9.83055 13.6676 9.75926 13.6405 9.39806 13.5201L9.36973 13.5107C8.57694 13.2464 7.94503 13.0358 7.47249 12.8514C7.0283 12.6781 6.56794 12.4726 6.24588 12.1631C5.01744 10.9826 5.01744 9.01733 6.24588 7.83686C6.56794 7.52738 7.0283 7.32189 7.47249 7.14859C7.94505 6.96421 8.57699 6.75356 9.36981 6.48929L9.39806 6.47988L15.889 4.31622C15.913 4.30823 15.9369 4.30027 15.9607 4.29234C16.727 4.03683 17.4061 3.81038 17.9475 3.71406Z" fill="currentColor"></path>
                                        </svg>
                                        <span>Send</span>
                                    </div>
                                </div>
                            </div>
                            <div id="delete-review-button" class="review-button delete-review">
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="edit" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" class="icon svg-inline--fa fa-edit fa-w-18">
                                    <g transform="translate(-30, 80)">
                                        <path fill="currentColor" d="M87.748,388.784c0.461,11.01,9.521,19.699,20.539,19.699h191.911c11.018,0,20.078-8.689,20.539-19.699l13.705-289.316H74.043L87.748,388.784z M247.655,171.329c0-4.61,3.738-8.349,8.35-8.349h13.355c4.609,0,8.35,3.738,8.35,8.349v165.293c0,4.611-3.738,8.349-8.35,8.349h-13.355c-4.61,0-8.35-3.736-8.35-8.349V171.329z M189.216,171.329c0-4.61,3.738-8.349,8.349-8.349h13.355c4.609,0,8.349,3.738,8.349,8.349v165.293c0,4.611-3.737,8.349-8.349,8.349h-13.355c-4.61,0-8.349-3.736-8.349-8.349V171.329L189.216,171.329z M130.775,171.329c0-4.61,3.738-8.349,8.349-8.349h13.356c4.61,0,8.349,3.738,8.349,8.349v165.293c0,4.611-3.738,8.349-8.349,8.349h-13.356c-4.61,0-8.349-3.736-8.349-8.349V171.329z"></path>
                                        <path fill="currentColor" d="M343.567,21.043h-88.535V4.305c0-2.377-1.927-4.305-4.305-4.305h-92.971c-2.377,0-4.304,1.928-4.304,4.305v16.737H64.916c-7.125,0-12.9,5.776-12.9,12.901V74.47h304.451V33.944C356.467,26.819,350.692,21.043,343.567,21.043z"></path>
                                    </g>
                                </svg>
                                <span>Delete Review</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>

            <div class="content-section active details">
                <c:choose>
                    <c:when test="${not empty media.background}">
                        <div>
                            <h1>Background</h1>
                            <p><c:out value="${media.background}"/></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div>
                            <p class="text-center">No other information available</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="content-section">
                <div id="latest-reviews" class="review-boxes">
                    <c:choose>
                        <c:when test="${not empty media.latestReviews}">
                            <c:forEach var="review" items="${media.latestReviews}">
                                <div class="review-box">
                                    <div class="review-picture">
                                        <c:choose>
                                            <c:when test="${empty review.user.profilePicUrl}">
                                                <img src="${pageContext.request.contextPath}/images/account-icon.png" alt="profile image" />
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${review.user.profilePicUrl}" alt="profile image" />
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="review-info">
                                        <div class="review-row">
                                            <a href="/MangaVerse/profile?userId=${review.user.id}" class="review-media-title">${review.user.username}</a>
                                            <p class="review-rating">${empty review.rating ? 'N/A' : review.rating}</p>
                                        </div>
                                        <p class="review-comment">${empty review.comment ? 'N/A' : review.comment}</p>
                                        <p class="review-date">${review.date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="review">
                                <p class="text-center">No reviews yet</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div id="all-reviews" class="review-boxes"></div>
                <c:if test="${media.reviewIds.size() > 5}">
                    <div class="container-pagination" style="display: none">
                        <ul class="page pagination"></ul>
                    </div>
                    <div class="button-wrapper">
                        <div id="show-review-button" class="show-more-reviews">Show more</div>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
<script src="${pageContext.request.contextPath}/js/media_content.js" defer></script>
<script src="${pageContext.request.contextPath}/js/navbar.js" defer></script>
<script src="${pageContext.request.contextPath}/js/load_default_picture.js" defer></script>
<script>
    mediaType = "manga";
    const media = {
        id: "${media.id}",
        type: "manga",
        title: '${media.title}',
        latestReviewsIds: [],
        reviewIds: '${empty media.reviewIds ? [] : media.reviewIds}'.slice(1, -1).split(", ")
    }
    <c:forEach items="${media.latestReviews}" var="review">
    media.latestReviewsIds.push("${review.getId()}");
    </c:forEach>

    const contextPath = "${pageContext.request.contextPath}";
    const userDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_PROFILE_PICTURE}";
    const mangaDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_MANGA}";
    const animeDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_ANIME}";

    let userReview = {
        id: "${empty requestScope.userReview ? "" : requestScope.userReview.id}",
        rating: "${empty requestScope.userReview ? "" : requestScope.userReview.rating}",
        comment: "${empty requestScope.userReview ? "" : requestScope.userReview.comment}",
    }

</script>
</body>
</html>