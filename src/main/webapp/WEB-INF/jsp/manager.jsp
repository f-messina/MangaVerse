<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8"%>

<html>
<head>
    <title>Manager Page - Anime Analytics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>

<body>
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
            <canvas id="user-distribution-chart" height="700" width="1400"></canvas>
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
            <canvas id="app-rating-chart" height="700" width="1400"></canvas>
        </div>
    </div>

    <!-- manga statistics -->
    <div id="manga-page" class="page">
        <h1>Manga Analytics</h1>
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
            <canvas id="manga-criteria-rating-chart" height="700" width="1400"></canvas>
        </div>

        <div id="single-manga-analytics" class="analytic-box">
            <div id="manga-search-section" class="media-list-section">
                <div class="analytic-title">
                    <div id="manga-search-body">
                        <div class="d-flex align-items-center">
                            <label class="filter-name" for="manga-search">Title:</label>
                            <input type="search" id="manga-search" name="searchTerm" placeholder="Enter Manga Title">
                        </div>
                    </div>
                </div>
            </div>

            <div id="manga-list" class="media-list"></div>

            <div class="media-popup">
                <div id="manga-selected"></div>
                <div class="analytic-box">
                    <div class="analytic-title">
                        <label class="analytic-title" for="manga-period-selection">Average Rating by: </label>
                        <select id="manga-period-selection">
                            <option value="month">MONTH</option>
                            <option value="year">YEAR</option>
                        </select>
                    </div>
                    <div class="diagram-parameter">
                        <div class="select">
                            <label for="manga-year">Select Year:</label>
                            <input type="text" id="manga-year" name="year">
                        </div>
                    </div>
                    <div class="diagram-parameter">
                        <div class="select">
                            <label for="manga-start-year">Start Year:</label>
                            <input type="number" id="manga-start-year" name="startYear" min="2000" max="2100">
                            <label for="manga-end-year">End Year:</label>
                            <input type="number" id="manga-end-year" name="endYear" min="2000" max="2100">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- anime statistics -->
    <div id="anime-page" class="page">
        <h1>Anime Analytics</h1>
        <div id="anime-average-rating" class="analytic-box">
            <div class="analytic-title">
                <label for="anime-analytics-type">Select Analytics Type:</label>
                <select id="anime-analytics-type">
                    <option value="tags">Tags</option>
                    <option value="producers">Producers</option>
                    <option value="studios">Studios</option>
                </select>
            </div>
            <canvas id="anime-criteria-rating-chart" height="700" width="1400"></canvas>
        </div>

        <div id="single-anime-analytics" class="analytic-box">
            <div id="anime-search-section" class="media-list-section">
                <div class="analytic-title">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="anime-search">Title:</label>
                        <input type="search" id="anime-search" name="searchTerm" placeholder="Enter Anime Title">
                    </div>
                </div>
            </div>

            <div id="anime-list" class="media-list"></div>

            <div class="media-popup">
                <div id="anime-selected"></div>
                <div class="analytic-box">
                    <div class="analytic-title">
                        <label class="analytic-title" for="anime-period-selection">Average Rating by: </label>
                        <select id="anime-period-selection">
                            <option value="month">MONTH</option>
                            <option value="year">YEAR</option>
                        </select>
                    </div>
                    <div class="diagram-parameter">
                        <div class="select">
                            <label for="anime-year">Select Year:</label>
                            <input type="text" id="anime-year" name="year">
                        </div>
                    </div>
                    <div class="diagram-parameter">
                        <div class="select">
                            <label for="anime-start-year">Start Year:</label>
                            <input type="number" id="anime-start-year" name="startYear" min="2000" max="2100">
                            <label for="anime-end-year">End Year:</label>
                            <input type="number" id="anime-end-year" name="endYear" min="2000" max="2100">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    const contextPath = '${pageContext.request.contextPath}';
    const animeDefaultImage = contextPath + '/images/anime-image-default.png';
    const mangaDefaultImage = contextPath + '/images/manga-image-default.png';
    let mangaSectionAccessed = false;
    let animeSectionAccessed = false;
    let animeTotalPages;
    let mangaTotalPages;

    function fetchData(inputData, chartId, chartType) {
        $.post(contextPath + "/manager", inputData, function(data) {
            if (data.success) {
                let max;
                const labels = Object.keys(data.results);
                const values = Object.values(data.results);

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
                responsive: false,
                plugins: {
                    legend: {
                        position: chartType === 'bar' ? 'top' : "right"
                    },
                },
                scales: chartType === 'bar' ? {
                    y: {
                        beginAtZero: true,
                        max: max
                    }
                } : {}
            }
        });
    }

    function search(searchTerm, type) {
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
                    const title = $('<div class="media-title">' + entry.title + '</div>');
                    mediaItem.append(image, title);
                    mediaList.append(mediaItem);
                });
                if (type === 'anime')
                    animeTotalPages = data.results.totalPages;
                else
                    mangaTotalPages = data.results.totalPages;
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
        $("input").val("");
        $("select").prop('selectedIndex', 0);
    }

    function getDefaultAnalytics(type) {
        const action = type === 'anime' ? 'getAnimeDefaultAnalytics' : 'getMangaDefaultAnalytics';
        $.post(contextPath + "/manager", {action: action}, function(data) {
            if (data.success) {
                const chartId = type === 'anime' ? 'anime-criteria-rating-chart' : 'manga-criteria-rating-chart';
                const labels = Object.keys(data.bestCriteria);
                const values = Object.values(data.bestCriteria);
                createChart(chartId, labels, values, 'bar');
                if (type === 'anime')
                    animeSectionAccessed = true;
                else
                    mangaSectionAccessed = true;
            }
        });

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
        createChart('app-rating-chart', averageAppRatingLabels, averageAppRatingData, 'bar');

        $('#user-distribution-type').change(function () {
            const criteria = $('#user-distribution-type').val();
            let chartType = 'pie';
            const inputData = {
                criteria: criteria,
                action: 'getDistribution'
            }
            if (criteria === 'birthday' || criteria === 'joined_on') {
                chartType = 'bar';
            }
            fetchData(inputData, 'user-distribution-chart', chartType);
        });

        $('#user-rating-criteria').change(function () {
            const inputData = {
                criteria: $('#user-rating-criteria').val(),
                action: 'getAverageAppRatingByCriteria'
            }
            fetchData(inputData, 'app-rating-chart', 'bar',);
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

        function showPage(pageId, button) {
            $('.page').removeClass('selected');
            $(pageId).addClass('selected');
            $('.options').removeClass('active');
            $(button).addClass('active');
        }

        // page triggers

        $('#user-button').click(function () {
            showPage('#user-page', $(this));
        });

        $('#manga-button').click(function () {
            if (!mangaSectionAccessed)
                getDefaultAnalytics('manga');
            showPage('#manga-page', $(this));
        });

        $('#anime-button').click(function () {
            if (!animeSectionAccessed) {
                getDefaultAnalytics('anime');
            }
            showPage('#anime-page', $(this));
        });


        // search triggers

        $('#manga-search').on('input', function () {
            const searchTerm = $('#manga-search').val();
            search(searchTerm, 'manga');
        });

        $('#anime-search').on('input', function () {
            const searchTerm = $('#anime-search').val();
            search(searchTerm, 'anime');
        });
    });
</script>
</body>
</html>