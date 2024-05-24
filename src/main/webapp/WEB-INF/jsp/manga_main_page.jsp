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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css"/>
    <script src="${pageContext.request.contextPath}/js/index.js" defer></script>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main-registered-user.css"/>
    <link
            rel="stylesheet"
            href="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.css"
    />
    <script src="${pageContext.request.contextPath}/js/range_input.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/main_page_test.js" defer></script>
</head>
<body>
    <section id="home" class="section-home">
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
        <div class="down-arrow">
            <span data-scroll="list" id="down-arrow">
                <i class="fa-solid fa-chevron-down"> </i>
            </span>
        </div>
    </section>
    <span id="scroll-point"></span>

    <c:set var="userInfo" value="${requestScope['userInfo']}" />
    <div class="nav-bar" id="navbar">
        <nav>
            <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
            <div class="up-arrow">
                <span data-scroll="list" id="up-arrow">
                    <i class="fa-solid fa-chevron-up" style="color: #000000"></i>
                </span>
            </div>

            <div class="nav-items">
                <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
                <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
                <c:choose>
                    <c:when test="${empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
                        <a href="${pageContext.request.contextPath}/auth">Log In</a>
                        <a href="${pageContext.request.contextPath}/auth">Sign Up</a>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/auth" method="post">
                            <input type="hidden" name="action" value="logout">
                            <input type="hidden" name="targetServlet" value="mainPage/manga">
                            <button type="submit" class="logout">Log Out</button>
                        </form>
                        <a href="${pageContext.request.contextPath}/profile" class="small-pic"><img alt="profile bar" src="${pageContext.request.contextPath}/${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}"></a>
                    </c:otherwise>
                </c:choose>
            </div>
        </nav>
    </div>
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

    <div id="spaceElement"></div>

<script>
    <c:set var="authenticatedUser" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />

    const mediaDetailHRef = "${pageContext.request.contextPath}/manga?mediaId=";
    const authenticatedUser = ${authenticatedUser};
    const authURI = "${pageContext.request.contextPath}/auth";
    const servletURI = "${pageContext.request.contextPath}/mainPage/manga";

    document.addEventListener("DOMContentLoaded", function () {
        let navBar = document.querySelector(".nav-bar");
        let sectionHome = document.getElementById("home");

        const searchForm = document.getElementById("searchForm");

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    // Element is out of the viewport
                    navBar.classList.remove("fixed-nav");
                    searchForm.style.marginTop = "20px";
                } else {
                    // Element is in the viewport
                    navBar.classList.add("fixed-nav");
                    searchForm.style.marginTop = "120px";
                }
            });
        });

        // Start observing changes in the sectionHome
        observer.observe(sectionHome);
    });

    // Function to add space at the end of the page if it's shorter than 2 times the height of #home
    function addSpaceIfNeeded() {
        const homeDivHeight = document.getElementById('home').offsetHeight;
        const pageHeight = Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight,
            document.documentElement.scrollHeight, document.documentElement.offsetHeight);
        if (pageHeight < 2 * homeDivHeight) {
            const spaceElement = document.getElementById('spaceElement');
            spaceElement.style.height = 2 * homeDivHeight - pageHeight + 'px';
        }
    }
    // Call the function when the page has finished loading
    window.addEventListener('load', addSpaceIfNeeded);

    document.getElementById('down-arrow').addEventListener('click', function() {
        const targetElement = document.getElementById('navbar');
        const home = document.getElementById('home');
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
        setTimeout(() => {
            home.style.display = 'none';
        }, 500);
        document.documentElement.style.overflow = 'auto';
    });


    document.getElementById('up-arrow').addEventListener('click', function() {
        const home = document.getElementById('home');
        const scrollPoint = document.getElementById('scroll-point');

        home.style.display = 'flex';
        document.documentElement.style.overflow = 'hidden';
        console.log(scrollPoint.offsetTop);
        window.scrollTo({ top: scrollPoint.offsetTop, behavior: 'instant' });
        home.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
    });
</script>
    <script>
        function toggleFiltersDisplay() {
            const filtersFormContainer = document.getElementById('filtersFormContainer');
            filtersFormContainer.style.display = filtersFormContainer.style.display === 'none' ? 'block' : 'none';


        }
    </script>
</body>
</html>
