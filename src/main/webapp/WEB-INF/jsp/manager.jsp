<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page import="static java.time.LocalDate.now" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Manager Page - Anime Analytics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css"/>

</head>

<body>
<c:set var="currentYear" value="<%= now().getYear() %>" />

<!-- navbar -->
<nav>
    <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
    <h1 id="welcome-message">Welcome ${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}</h1>
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
        <div class="logout" onclick="logout('auth')">
            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="sign-out-alt" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="logout-icon"><path data-v-04b245e6="" fill="currentColor" d="M497 273L329 441c-15 15-41 4.5-41-17v-96H152c-13.3 0-24-10.7-24-24v-96c0-13.3 10.7-24 24-24h136V88c0-21.4 25.9-32 41-17l168 168c9.3 9.4 9.3 24.6 0 34zM192 436v-40c0-6.6-5.4-12-12-12H96c-17.7 0-32-14.3-32-32V160c0-17.7 14.3-32 32-32h84c6.6 0 12-5.4 12-12V76c0-6.6-5.4-12-12-12H96c-53 0-96 43-96 96v192c0 53 43 96 96 96h84c6.6 0 12-5.4 12-12z" class=""></path></svg>
            <div class="logout-text">Log Out</div>
        </div>
        <a href="${pageContext.request.contextPath}/profile" class="small-pic">
            <img id="navbar-profile-picture" alt="profile bar" src="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}">
        </a>
    </div>
</nav>

<div class="all-page">
    <!-- navbar -->
    <div class="navbar-container">
        <div id="side-navbar" class="button-container">
            <button id="user-button" class="options active">USER</button>
            <button id="manga-button" class="options">MANGA</button>
            <button id="anime-button" class="options">ANIME</button>
        </div>
    </div>

    <!-- user statistics -->
    <div id="user-page" class="page selected">
        <h1>User Analytics</h1>
        <div class="analytic-box" id="user-distribution">
            <div class="analytic-title">
                <label for="user-distribution-type">Select Distribution Type:</label>
                <select id="user-distribution-type">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="birthday">Birthday</option>
                    <option value="joined_on">Joined On</option>
                </select>
            </div>
            <canvas id="user-distribution-chart"></canvas>
        </div>

        <div class="analytic-box" id="user-criteria-rating">
            <div class="analytic-title">
                <label for="user-rating-criteria">Average App Rating by:</label>
                <select id="user-rating-criteria">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="age">Age</option>
                </select>
            </div>
            <canvas id="app-rating-chart"></canvas>
        </div>
    </div>

    <!-- manga statistics -->
    <div id="manga-page" class="page">
        <h1>Manga Analytics</h1>

        <div id="manga-trend" class="analytic-box">
            <div class="analytic-title">
                <label for="manga-trend-year">Anime Trend on: </label>
                <input type="number" id="manga-trend-year" name="year" min="1900" max="${currentYear}">
            </div>
            <div id="manga-trend-list" class="media-list"></div>
        </div>

        <div class="analytic-box" id="manga-average-rating">
            <div class="analytic-title">
                <label for="manga-analytics-type">Average Rating by:</label>
                <select id="manga-analytics-type">
                    <option value="genres">Genres</option>
                    <option value="themes">Themes</option>
                    <option value="demographics">Demographics</option>
                    <option value="authors">Authors</option>
                    <option value="serializations">Serializations</option>
                </select>
            </div>
            <canvas id="manga-criteria-rating-chart"></canvas>
        </div>

        <div id="manga-search-section" class="analytic-box">
            <div class="media-list-section">
                <div class="analytic-title">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="manga-search">Title:</label>
                        <input type="search" id="manga-search" name="searchTerm" placeholder="Enter Anime Title">
                    </div>
                </div>
            </div>

            <div id="manga-list" class="media-list"></div>
        </div>

        <div id="single-manga-analytics" class="analytic-box" style="display: none;">
            <h1 id="manga-selected"></h1>
            <div class="analytic-title">
                <label class="analytic-title" for="manga-period-selection">Average Rating by: </label>
                <select id="manga-period-selection">
                    <option value="month">MONTH</option>
                    <option value="year">YEAR</option>
                </select>
            </div>
            <div id="manga-year-form" class="diagram-parameter">
                <div class="select active">
                    <label for="manga-year">Select Year:</label>
                    <input type="number" id="manga-year" name="year" min="1900" max="${currentYear}">
                </div>
            </div>
            <div id="manga-year-range-form" class="diagram-parameter">
                <div class="select">
                    <label for="manga-start-year">Start Year:</label>
                    <input type="number" id="manga-start-year" name="startYear" min="1900" max="${currentYear}">
                    <label for="manga-end-year">End Year:</label>
                    <input type="number" id="manga-end-year" name="endYear" min="1900" max="${currentYear}">
                </div>
            </div>
            <canvas id="manga-chart-month" class="avg-rating-media active"></canvas>
            <canvas id="manga-chart-year" class="avg-rating-media"></canvas>
        </div>
    </div>

    <!-- anime statistics -->
    <div id="anime-page" class="page">
        <h1>Anime Analytics</h1>

        <div id="anime-trend" class="analytic-box">
            <div class="analytic-title">
                <label for="anime-trend-year">Anime Trend on: </label>
                <input type="number" id="anime-trend-year" name="year" min="1900" max="${currentYear}">
            </div>
            <div id="anime-trend-list" class="media-list"></div>
        </div>

        <div id="anime-average-rating" class="analytic-box">
            <div class="analytic-title">
                <label for="anime-analytics-type">Select Analytics Type:</label>
                <select id="anime-analytics-type">
                    <option value="tags">Tags</option>
                    <option value="producers">Producers</option>
                    <option value="studios">Studios</option>
                </select>
            </div>
            <canvas id="anime-criteria-rating-chart"></canvas>
        </div>

        <div id="anime-search-section" class="analytic-box">
            <div class="media-list-section">
                <div class="analytic-title">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="anime-search">Title:</label>
                        <input type="search" id="anime-search" name="searchTerm" placeholder="Enter Anime Title">
                    </div>
                </div>
            </div>

            <div id="anime-list" class="media-list"></div>
        </div>

        <div id="single-anime-analytics" class="analytic-box" style="display: none;">
            <h1 id="anime-selected"></h1>
            <div class="analytic-title">
                <label class="analytic-title" for="anime-period-selection">Average Rating by: </label>
                <select id="anime-period-selection">
                    <option value="month">MONTH</option>
                    <option value="year">YEAR</option>
                </select>
            </div>
            <div id="anime-year-form" class="diagram-parameter">
                <div class="select active">
                    <label for="anime-year">Select Year:</label>
                    <input type="number" id="anime-year" name="year" min="1900" max="${currentYear}">
                </div>
            </div>
            <div id="anime-year-range-form" class="diagram-parameter">
                <div class="select">
                    <label for="anime-start-year">Start Year:</label>
                    <input type="number" id="anime-start-year" name="startYear" min="1900" max="${currentYear}">
                    <label for="anime-end-year">End Year:</label>
                    <input type="number" id="anime-end-year" name="endYear" min="1900" max="${currentYear}">
                </div>
            </div>
            <canvas id="anime-chart-month" class="avg-rating-media active"></canvas>
            <canvas id="anime-chart-year" class="avg-rating-media"></canvas>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script>
    const contextPath = '${pageContext.request.contextPath}';
    const animeDefaultImage = contextPath + '/images/anime-image-default.png';
    const mangaDefaultImage = contextPath + '/images/manga-image-default.png';
    let mangaSectionAccessed = false;
    let animeSectionAccessed = false;
    let animeSelectedId;
    let mangaSelectedId;

    function fetchData(inputData, chartId, chartType) {
        $.post(contextPath + "/manager", inputData, function(data) {
            if (data.success) {
                let max;
                const labels = Object.keys(data.results);
                const values = Object.values(data.results);
                console.log(data);
                // Destroy the existing chart
                const existingChart = Chart.getChart(chartId);
                if (existingChart) {
                    existingChart.destroy();
                }

                if (chartType === 'bar' && chartId === 'app-rating-chart') {
                    max = 5;
                } else if (chartType === 'bar' && chartId === 'user-distribution-chart') {
                    max = null;
                } else {
                    max = 10;
                }

                createChart(chartId, labels, values, chartType, max);
            }
        });
    }

    function createChart(chartId, labels, values, chartType, max) {
        const ctx = $('#' + chartId)[0].getContext('2d');

        new Chart(ctx, {
            type: chartType,
            data: {
                labels: labels,
                datasets: [{
                    label: chartType === 'bar' ? 'Average Rating' : 'Distribution',
                    data: values,
                    backgroundColor: chartType === 'bar' ? '#36A2EB' : ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0'],
                }]
            },
            options: {
                y: {
                    duration: 1000,
                    easing: 'easeOutQuad',
                    from: chartType === 'bar' ? 0 : 10, // Start from 0 for bar chart, or from the maximum value for other types
                    loop: false
                },
                responsive: true,
                plugins: {
                    legend: {
                        position: "top"
                    },
                },
                scales: chartType !== 'pie' ? {
                    y: {
                        beginAtZero: true,
                        max: max
                    }
                } : {}
            }
        });
    }

    function search(type, searchTerm = '') {
        const inputData = {
            title: searchTerm,
            action: 'searchByTitle'
        }
        $.post(contextPath + "/" + type, inputData, function(data) {
            if (data.success) {
                const mediaList = type === 'anime' ? $('#anime-list') : $('#manga-list');
                mediaList.empty(); // Clear previous search results
                data.results.entries.forEach(function(entry) {
                    const mediaItem = $('<div class="media-item"></div>');
                    const image = $('<img src="' + entry.imageUrl + '" alt="' + entry.title + '" class="media-image">')
                        .on("error", () =>  setDefaultCover(image, type));
                    const title = $('<div class="media-title">' + entry.title + '</div>').click(function() {
                        type === 'anime' ? animeSelectedId = entry.id : mangaSelectedId = entry.id;
                        $('#' + type + '-selected').text("Selected " + type + ": " + entry.title);
                        const inputData = {
                            mediaId: entry.id,
                            action: 'getAverageRatingByMonth',
                            type: type,
                            year: new Date().getFullYear()
                        }
                        fetchData(inputData, type + '-chart-month', 'line');
                        $('#single-' + type + '-analytics').show();
                    });
                    const link = $('<a>').attr('href', contextPath + '/' + type + '?mediaId=' + entry.id).text('View Details');
                    mediaItem.append(image, title, link);
                    mediaList.append(mediaItem);
                });
            } else if (data.noResults) {
                const mediaList = type === 'anime' ? $('#anime-list') : $('#manga-list');
                mediaList.empty(); // Clear previous search results
                mediaList.append('<div class="no-results">No results found</div>');
            } else {
                console.log('Failed to search');
            }
        }).fail(function() {
            console.log('Failed to search');
        });
    }

    function setDefaultCover(image, type) {
        image.off("error");
        image.attr("src", type === "anime" ? animeDefaultImage : mangaDefaultImage);
    }

    function resetPage() {
        $("select").prop('selectedIndex', 0);
        $("input").each(function() {
            if ($(this).attr("name") === "startYear") {
                $(this).val(new Date().getFullYear() - 1);
            } else if ($(this).attr("type") === "search") {
                $(this).val("");
            } else {
                $(this).val(new Date().getFullYear());
            }
        });
    }

    function getDefaultAnalytics(type) {
        const action = type === 'anime' ? 'getAnimeDefaultAnalytics' : 'getMangaDefaultAnalytics';
        $.post(contextPath + "/manager", {action: action}, function(data) {
            if (data.success) {
                const chartId = type === 'anime' ? 'anime-criteria-rating-chart' : 'manga-criteria-rating-chart';
                const labels = Object.keys(data.bestCriteria);
                const values = Object.values(data.bestCriteria);
                createChart(chartId, labels, values, 'bar', 10);
                if (type === 'anime')
                    animeSectionAccessed = true;
                else
                    mangaSectionAccessed = true;
            }
        });
    }

    function getTrendMediaContent(type) {
        const inputData = {
            year: $("#" + type + "-trend-year").val(),
            action: 'getTrendMediaContentByYear',
            type: type
        }
        $.post(contextPath + "/manager", inputData, function(data) {
            console.log(data);
            if (data.success) {
                const mediaList = $("#" + type + "-trend-list");
                mediaList.empty(); // Clear previous search results

                Object.entries(data.results).forEach(function([key, value]) {
                    console.log(key);
                    // Parse the key (which is a JSON string representing MediaContentDTO) and value (which is the integer)
                    const entry = JSON.parse(key)

                    const mediaItem = $('<div>').addClass('media-item');
                    const imgLink = $('<a>').attr('href', contextPath + '/' + type + '?mediaId=' + entry.id);
                    const image = $('<img src="' + entry.imageUrl + '" alt="' + entry.title + '" class="media-image">')
                        .on("error", () => setDefaultCover(image, type));
                    imgLink.append(image);
                    const title = $('<a>').attr('href', contextPath + '/' + type + '?mediaId=' + entry.id).addClass("media-title").text(entry.title);
                    const link = $('<p>').text(value); // Set the integer value in the paragraph
                    mediaItem.append(imgLink, title, link);
                    mediaList.append(mediaItem);
                });
            } else {
                console.log('Failed to get trend');
            }
        }).fail(function() {
            console.log('Failed to get trend');
        });
    }

    function showPage(pageId, button) {
        $('.page').removeClass('selected');
        $(pageId).addClass('selected');
        $('.options').removeClass('active');
        $(button).addClass('active');
    }

    $(document).ready(function() {
        resetPage();
        // Extract labels and values from the distribution map
        const distributionLabels = [];
        const distributionData = [];
        <c:forEach var="entry" items="${distribution}">
        distributionLabels.push('<c:out value="${entry.key}"/>');
        distributionData.push(<c:out value="${entry.value}"/>);
        </c:forEach>
        createChart('user-distribution-chart', distributionLabels, distributionData, 'pie');

        // Extract labels and values from the average app rating map
        const averageAppRatingLabels = [];
        const averageAppRatingData = [];
        <c:forEach var="entry" items="${averageAppRating}">
        averageAppRatingLabels.push('<c:out value="${entry.key}"/>');
        averageAppRatingData.push(<c:out value="${entry.value}"/>);
        </c:forEach>
        createChart('app-rating-chart', averageAppRatingLabels, averageAppRatingData, 'bar', 5);

        $('#user-distribution-type').change(function () {
            const criteria = $('#user-distribution-type').val();
            let chartType = 'bar';

            const inputData = {
                criteria: criteria,
                action: 'getDistribution'
            }
            if (criteria === 'gender') {
                chartType = 'pie';
            }
            fetchData(inputData, 'user-distribution-chart', chartType);
        });

        $('#user-rating-criteria').change(function () {
            const inputData = {
                criteria: $('#user-rating-criteria').val(),
                action: 'getAverageAppRatingByCriteria'
            }
            fetchData(inputData, 'app-rating-chart', 'bar');
        });

        $('#manga-analytics-type').change(function () {
            const inputData = {
                criteria: $('#manga-analytics-type').val(),
                action: 'getBestCriteria',
                type: 'manga',
                page: 1
            }
            fetchData(inputData, 'manga-criteria-rating-chart', 'bar');
        });

        $('#anime-analytics-type').change(function () {
            const inputData = {
                criteria: $('#anime-analytics-type').val(),
                action: 'getBestCriteria',
                type: 'anime',
                page: 1
            }
            fetchData(inputData, 'anime-criteria-rating-chart', 'bar');
        });

        // page triggers

        $('#user-button').click(function () {
            showPage('#user-page', $(this));
        });

        $('#manga-button').click(function () {
            if (!mangaSectionAccessed) {
                getDefaultAnalytics('manga');
                getTrendMediaContent('manga');
                search('manga');
            }
            showPage('#manga-page', $(this));
        });

        $('#anime-button').click(function () {
            if (!animeSectionAccessed) {
                getDefaultAnalytics('anime');
                getTrendMediaContent('anime');
                search('anime');
            }
            showPage('#anime-page', $(this));
        });


        // search triggers
        mediaTypes = ['manga', 'anime'];
        mediaTypes.forEach(function(type) {
            const yearInput = $('#' + type + '-year');
            const startYearInput = $('#' + type + '-start-year');
            const endYearInput = $('#' + type + '-end-year');
            const trendYearInput = $("#" + type + "-trend-year");

            $('#' + type + '-search').on('input', function () {
                const searchTerm = $('#' + type + '-search').val();
                search(type, searchTerm);
            });

            yearInput.on('input', function () {
                if (yearInput.val() < 1900 || yearInput.val() > new Date().getFullYear()) {
                    return;
                }
                const inputData = {
                    mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                    action: 'getAverageRatingByMonth',
                    type: type,
                    year: yearInput.val()
                }
                fetchData(inputData, type + '-chart-month', 'line');
            });

            startYearInput.on('input', function () {
                const startYear = startYearInput.val();
                let endYear = endYearInput.val();
                if (startYear < 1900 || startYear > new Date().getFullYear()) {
                    return;
                }
                if (startYear > endYear) {
                    $('#' + type + '-end-year').val(startYear);
                    endYear = startYear;
                }
                const inputData = {
                    mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                    action: 'getAverageRatingByYear',
                    type: type,
                    startYear: startYear,
                    endYear: endYear
                }
                fetchData(inputData, type + '-chart-year', 'line');
            });

            endYearInput.on('input', function () {
                let startYear = startYearInput.val();
                const endYear = endYearInput.val();
                if (endYear < 1900 || endYear > new Date().getFullYear()) {
                    return;
                }
                if (endYear < startYear) {
                    $('#' + type + '-start-year').val(endYear);
                    startYear = endYear;
                }

                const inputData = {
                    mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                    action: 'getAverageRatingByYear',
                    type: type,
                    startYear: startYear,
                    endYear: endYear
                }
                fetchData(inputData, type + '-chart-year', 'line');
            });

            function isCanvasPopulated(canvasId) {
                const canvas = $('#' + canvasId)[0];
                const offscreen = new OffscreenCanvas($(canvas).prop('width'), $(canvas).prop('height'));
                const ctx = offscreen.getContext('2d');
                ctx.drawImage(canvas, 0, 0);
                return ctx.getImageData(0, 0, canvas.width, canvas.height).data.some(alpha => alpha !== 0);
            }

            $('#' + type + '-period-selection').change(function () {
                const period = $('#' + type + '-period-selection').val();

                $("#single-" + type + "-analytics").find(".select").toggleClass("active");
                const inputData = {
                    mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                    action: 'getAverageRatingBy' + period.charAt(0).toUpperCase() + period.slice(1),
                    type: type,
                }
                if (period === 'year') {
                    inputData.startYear = startYearInput.val();
                    inputData.endYear = endYearInput.val();
                } else {
                    inputData.year = yearInput.val();
                }
                $('#' + type + '-chart-year').toggleClass("active");
                $('#' + type + '-chart-month').toggleClass("active");

                if (!isCanvasPopulated(type + '-chart-' + period)) {
                    fetchData(inputData, type + '-chart-' + period, 'line');
                }
            });

            trendYearInput.on('input', function() {
                if (trendYearInput.val() < 1900 || trendYearInput.val() > new Date().getFullYear()) {
                    return;
                }
                getTrendMediaContent(type);
            });
        });
    });
</script>
</body>
</html>