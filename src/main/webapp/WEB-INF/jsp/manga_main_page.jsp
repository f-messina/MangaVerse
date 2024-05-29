<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 12:31
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page import="static java.time.LocalDate.now" %>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_page.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user_list.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/filters.css"/>
</head>
<body>
    <!-- JSP variables -->
    <c:set var="isLogged" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" /> <!-- check if the user is logged in -->
    <c:set var="currentYear" value="<%= now().getYear() %>" />

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
        <div class="container">
            <!-- filters div -->
            <div class="filters-wrap primary-filters">

                <!-- primary filters (shown) -->
                <div class="filters">

                    <!-- title -->
                    <div class="filter filter-select">
                        <div class="filter-name">Search</div>
                        <div class="search-wrap">
                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="search" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="icon left svg-inline--fa fa-search fa-w-16">
                                <path fill="currentColor" d="M505 442.7L405.3 343c-4.5-4.5-10.6-7-17-7H372c27.6-35.3 44-79.7 44-128C416 93.1 322.9 0 208 0S0 93.1 0 208s93.1 208 208 208c48.3 0 92.7-16.4 128-44v16.3c0 6.4 2.5 12.5 7 17l99.7 99.7c9.4 9.4 24.6 9.4 33.9 0l28.3-28.3c9.4-9.4 9.4-24.6.1-34zM208 336c-70.7 0-128-57.2-128-128 0-70.7 57.2-128 128-128 70.7 0 128 57.2 128 128 0 70.7-57.2 128-128 128z" class=""></path>
                            </svg>
                            <label>
                                <input id="title-filter" type="search" autocomplete="off" class="search">
                            </label>
                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                                <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                            </svg>
                        </div>
                    </div>

                    <!-- genres -->
                    <div id="genres-filter" class="filter filter-select" type="genre">
                        <div class="filter-name-wrap">
                            <div class="filter-name">Genres</div>
                            <div class="toggle-button-genre">
                                <div id="button-3" class="button r">
                                    <input id="genre-checkbox-type" class="checkbox" type="checkbox" value="and">
                                    <div class="knobs"></div>
                                    <div class="layer"></div>
                                </div>
                            </div>
                        </div>
                        <div class="select-wrap multi-choice" name="genre">
                            <div class="select">
                                <div class="value-wrap">
                                    <div class="placeholder">Any</div>
                                    <label>
                                        <input type="search" autocomplete="off" class="filter">
                                    </label>
                                </div>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                                    <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                                </svg>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                                    <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                                </svg>
                            </div>
                            <div class="options">
                                <div class="scroll-wrap">
                                    <div class="option-group">
                                        <c:forEach items="${requestScope.mangaGenres}" var="genre">
                                            <div class="option">
                                                <div class="label">
                                                    <div class="name" value="${genre}">${genre}</div>
                                                    <div class="selected-icon circle">
                                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="select" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16" >
                                                            <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                                        </svg>
                                                    </div>
                                                    <div class="selected-icon circle avoid">
                                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="avoid" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="svg-inline--fa fa-check fa-w-16">
                                                            <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                                                        </svg>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- year -->
                    <div class="filter filter-select">
                        <div class="filter-name">Year</div>
                        <div class="select-wrap" name="year">
                            <div class="select">
                                <div class="value-wrap">
                                    <div class="placeholder">Any</div>
                                    <label>
                                        <input type="search" autocomplete="off" class="filter">
                                    </label>
                                </div>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                                    <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                                </svg>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                                    <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                                </svg>
                            </div>
                            <div class="options">
                                <div class="scroll-wrap">
                                    <div class="option-group">
                                        <c:forEach var="year" begin="1930" end="${currentYear}">
                                            <div class="option">
                                                <div class="label">
                                                    <div class="name" value="${year}">${year}</div>
                                                    <div class="selected-icon circle">
                                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                            <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                                        </svg>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- demographics -->
                    <div class="filter filter-select">
                        <div class="filter-name">Demographics</div>
                        <div class="select-wrap" name="demographics">
                            <div class="select">
                                <div class="value-wrap">
                                    <div class="placeholder">Any</div>
                                    <div class="filter"></div>
                                </div>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                                    <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                                </svg>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                                    <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                                </svg>
                            </div>
                            <div class="options">
                                <div class="scroll-wrap">
                                    <div class="option-group">
                                        <c:forEach var="entry" items="${requestScope.mangaDemographics}">
                                            <c:if test="${entry.name() != 'UNKNOWN'}">
                                                <div class="option">
                                                    <div class="label">
                                                        <div class="name" value="${entry.name()}">${entry.toString()}</div>
                                                        <div class="selected-icon circle">
                                                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                                <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                                            </svg>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </c:forEach>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- publication status -->
                    <div class="filter filter-select">
                        <div class="filter-name">Publication Status</div>
                        <div class="select-wrap" name="status">
                            <div class="select">
                                <div class="value-wrap">
                                    <div class="placeholder">Any</div>
                                    <label>
                                        <input type="search" autocomplete="off" class="filter">
                                    </label>
                                </div>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                                    <path fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                                </svg>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                                    <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                                </svg>
                            </div>
                            <div class="options">
                                <div class="scroll-wrap">
                                    <div class="option-group">
                                        <c:forEach var="entry" items="${requestScope.mangaStatus}">
                                            <div class="option">
                                                <div class="label">
                                                    <div class="name" value="${entry.name()}">${entry.toString()}</div>
                                                    <div class="selected-icon circle">
                                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                            <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                                        </svg>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- extra filters (hidden)-->
                <div class="extra-filters-wrap">

                    <!-- extra filters button -->
                    <div id="extra-filters-button" class="open-btn active">
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="sliders-h" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="icon svg-inline--fa fa-sliders-h fa-w-16">
                            <path fill="currentColor" d="M496 384H160v-16c0-8.8-7.2-16-16-16h-32c-8.8 0-16 7.2-16 16v16H16c-8.8 0-16 7.2-16 16v32c0 8.8 7.2 16 16 16h80v16c0 8.8 7.2 16 16 16h32c8.8 0 16-7.2 16-16v-16h336c8.8 0 16-7.2 16-16v-32c0-8.8-7.2-16-16-16zm0-160h-80v-16c0-8.8-7.2-16-16-16h-32c-8.8 0-16 7.2-16 16v16H16c-8.8 0-16 7.2-16 16v32c0 8.8 7.2 16 16 16h336v16c0 8.8 7.2 16 16 16h32c8.8 0 16-7.2 16-16v-16h80c8.8 0 16-7.2 16-16v-32c0-8.8-7.2-16-16-16zm0-160H288V48c0-8.8-7.2-16-16-16h-32c-8.8 0-16 7.2-16 16v16H16C7.2 64 0 71.2 0 80v32c0 8.8 7.2 16 16 16h208v16c0 8.8 7.2 16 16 16h32c8.8 0 16-7.2 16-16v-16h208c8.8 0 16-7.2 16-16V80c0-8.8-7.2-16-16-16z" class=""></path>
                        </svg>
                    </div>

                    <!-- extra filters popup -->
                    <div id="extra-filters" class="dropdown anime">
                        <div class="filters-wrap">

                            <!-- format -->
                            <div class="filter filter-select">
                                <div class="filter-name">Format</div>
                                <div class="select-wrap multi-choice" name="format">
                                    <div class="select">
                                        <div class="value-wrap">
                                            <div class="placeholder">Any</div>
                                            <label>
                                                <input type="search" autocomplete="off" class="filter">
                                            </label>
                                        </div>
                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                                            <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                                        </svg>
                                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                                            <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                                        </svg>
                                    </div>
                                    <div class="options">
                                        <div class="scroll-wrap">
                                            <div class="option-group">
                                                <c:forEach var="entry" items="${requestScope.mangaTypes}">
                                                    <div class="option">
                                                        <div class="label">
                                                            <div class="name" value="${entry.name()}">${entry.toString()}</div>
                                                            <div class="selected-icon circle">
                                                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                                    <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                                                </svg>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- year range -->
                            <div class="filter">
                                <div class="range-wrap" name="year-range" style="--handle-0-position: 0px; --handle-1-position: 171px; --active-region-width: 171px; --min-val: 1930; --max-val: ${currentYear};">
                                    <div class="header header-filters">
                                        <div class="label">year range</div>
                                    </div>
                                    <div class="range">
                                        <div class="rail">
                                            <div value="1930" class="handle handle-0"></div>
                                            <div class="active-region"></div>
                                            <div value="${currentYear}" class="handle handle-1"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- rating range -->
                            <div class="filter">
                                <div class="range-wrap" name="rating-range" style="--handle-0-position: 0px; --handle-1-position: 171px; --active-region-width: 171px; --min-val: 0; --max-val: 10;">
                                    <div class="header header-filters">
                                        <div class="label">rating range</div>
                                    </div>
                                    <div class="range">
                                        <div class="rail">
                                            <div value="0" class="handle handle-0"></div>
                                            <div class="active-region"></div>
                                            <div value="10" class="handle handle-1"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- results -->
            <div class="results"></div>

            <div class="container-pagination">
                <ul class="page pagination">
                </ul>
            </div>
        </div>


        <!--
        <section id="resultsSection">
            <h1 id="totalResults"></h1>
            <div id="orderSelection"></div>
            <div id="media-list" class="project-boxes jsGridView"></div>
            <div id="pageSelection"></div>
        </section> -->

    </section>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/main_page_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/navbar.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/filters.js" defer></script>
    <script>
        const mediaType = "manga";
        const authenticatedUser = ${isLogged};
        const contextPath = "${pageContext.request.contextPath}";
        const authURI = "${pageContext.request.contextPath}/auth";
        const servletURI = "${pageContext.request.contextPath}/mainPage/manga";
        const mangaDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_MANGA}";
        const animeDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_ANIME}";
    </script>
</body>
</html>
