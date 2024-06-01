<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 28/02/2024
  Time: 14:15
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
    <script src="${pageContext.request.contextPath}/js/index.js" defer></script>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_page.css"/>
    <link
            rel="stylesheet"
            href="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.css"
    />
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/main_page_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/anime_main_page_test.js" defer></script>
</head>

<body>
    <!-- Welcome section -->
    <section id="welcome-section" class="section-home">
        <!-- Home container -->
        <div class="home-container">
            <div class="home-wrapper">
                <div class="selection-page-link"><a href="${pageContext.request.contextPath}/mainPage/manga" id="mangaLink">Manga</a></div>
                <div class="welcome">
                    <div class="welcome-text">
                        <p>Welcome to</p>
                    </div>
                    <img src="${pageContext.request.contextPath}/images/logo-with-name.png" alt="middle" />
                </div>
                <div class="active-page"><span id="animeLink">Anime</span></div>
            </div>
        </div>

        <!-- Down arrow -->
        <div class="down-arrow">
            <span data-scroll="list" id="down-arrow">
                <i class="fa-solid fa-chevron-down"></i>
            </span>
        </div>
    </section>

    <span id="scroll-point"></span>
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
                            <input type="hidden" name="targetServlet" value="mainPage/anime">
                            <button type="submit" class="logout">Log Out</button>
                        </form>
                        <a href="${pageContext.request.contextPath}/profile" class="small-pic"><img alt="profile bar" src="${pageContext.request.contextPath}/${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}"></a>
                    </c:otherwise>
                </c:choose>
            </div>
        </nav>
    </div>
    <div class="write-search">
        <form id="searchForm" action="${pageContext.request.contextPath}/mainPage/anime" method="post">
            <input type="hidden" name="action" value="search">
            <label for="search"  class="filter-name">Title:</label>
            <input type="search" id="search" name="searchTerm" placeholder="Title">
            <input  class="search" type="submit" value="SEARCH">
        </form>
        <button onclick="toggleFiltersDisplay()" class="show-filters-button" id="toggleFilterButton" >See Detailed Filtering</button>
    </div>


    <div id="filtersFormContainer">
        <form id="filterForm" action="${pageContext.request.contextPath}/mainPage/anime" method="post" >
            <input type="hidden" name="action" value="search">


            <div class="title-ope">
                <label  class="filter-name">Genres:</label><br/>
                <div class="operator" style="background-color: #ecebeb; padding: 5px">
                    <label >Operator:</label>
                    <input type="radio" name="genreOperator" checked value="and">and
                    <input type="radio" name="genreOperator" value="or">or
                </div>
            </div>
            <div class="all">
                <%-- This are the radios for the tags --%>
                <c:forEach items="${requestScope.animeTags}" var="tag">
                    <div>
                        <input class="green" type="radio" name="${tag}" onclick="toggleRadio(this)" value="select">
                        <input class="red" type="radio" name="${tag}" onclick="toggleRadio(this)" value="avoid">
                        <label>${tag}</label>
                    </div>
                </c:forEach>
            </div>

            <label  class="filter-name">Type:</label>
            <%-- This are the checkboxes for the types --%>
            <div>
                <div class="all">
                    <c:forEach var="entry" items="${requestScope.animeTypes}">
                        <c:if test="${entry.name() != 'UNKNOWN'}">
                            <div>
                                <input type="checkbox" id="${entry.name()}" name="animeTypes" value="${entry.name()}">
                                <label for="${entry.name()}">${entry.toString()}</label>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </div>

            <label class="filter-name">Publishing status:</label>
            <%-- This are the checkboxes for the status --%>
            <div class="all">
                <c:forEach var="entry" items="${requestScope.animeStatus}">
                    <div>
                        <input type="checkbox" id="${entry.name()}" name="status" value="${entry.name()}">
                        <label for="${entry.name()}">${entry.toString()}</label>
                    </div>
                </c:forEach>
            </div>
            <%-- This are the range inputs for the min and max score --%>
            <div>
                <label class="filter-name">Rating:</label>
                <div class="range-slider container order">
                    <span class="output outputOne"></span>
                    <span class="output outputTwo"></span>
                    <span class="full-range"></span>
                    <span class="incl-range"></span>
                    <input name="minScore" value="0" min="0" max="10" step="0.1" type="range">
                    <input name="maxScore" value="10" min="0" max="10" step="0.1" type="range">
                </div>
            </div>

            <div>
                <label>
                    <input type="checkbox" id="yearRangeCheckbox"> Choose Year Range
                </label>
            </div>

            <%-- This is the selection of an anime season --%>
            <div id="singleYearDiv">
                <label  class="filter-name" for="season">Season:</label>
                <select name="season" id="season">
                    <option value="WINTER">Winter</option>
                    <option value="SPRING">Spring</option>
                    <option value="SUMMER">Summer</option>
                    <option value="FALL">Fall</option>
                </select>
                <br/>
                <label  class="filter-name" for="year">Year:</label>
                <input type="number" id="year" name="year" step="1">
            </div>

            <%-- This are the range inputs for the min and max start year --%>
            <div id="yearRangeDiv" class="year-range">
                <label for="minYear" class="filter-name">Start Year:</label>
                <input type="number" id="minYear" name="minYear" step="1" >
                <br/>
                <label for="maxYear" class="filter-name">End Year:</label>
                <input type="number" id="maxYear" name="maxYear" step="1">
            </div>

            <div>
                <label for="orderBy" class="filter-name">Order By:</label>
                <select  class="order" name="orderBy" id="orderBy">
                    <option value="title 1">Title enc</option>
                    <option value="title -1">Title dec</option>
                    <option value="average_rating 1">Average Rating enc</option>
                    <option value="average_rating -1">Average Rating dec</option>
                    <option value="anime_season.year 1">Year enc</option>
                    <option value="anime_season.year -1">Year dec</option>
                </select>
            </div>

            <input class="search" type="submit" value="SEARCH">
        </form>

    </div>


    <section id="resultsSection"></section>

    <!-- page bar -->
    <div id="changePage">
        <form action="${pageContext.request.contextPath}/mainPage/anime" method="post">
            <input type="hidden" name="action" value="sortAndPaginate">

            <c:if test="${requestScope.page > 1}">
                <button type="submit" class="navigation-button" name="page" value="${requestScope.page - 1}">Previous Page</button>
            </c:if>
            <c:if test="${requestScope.page < requestScope.mediaContentPage.getTotalPages()}">
                <button type="submit" class="navigation-button" name="page" value="${requestScope.page + 1}">Next Page</button>
            </c:if>
        </form>
    </div>

    <div id="spaceElement"></div>

    <script>
        <c:set var="authenticatedUser" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />

        let mediaDetailHRef = "${pageContext.request.contextPath}/anime?mediaId=";
        let authenticatedUser = ${authenticatedUser};
        const authURI = "${pageContext.request.contextPath}/auth";
        let servletURI = "${pageContext.request.contextPath}/mainPage/anime";

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
