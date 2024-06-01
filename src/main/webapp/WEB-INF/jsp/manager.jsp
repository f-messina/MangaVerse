<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Manager Page - Anime Analytics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>

<body>
<div class="all-page">
    <!-- Navbar -->
    <div id="side-navbar" class="button-container">
        <div><button id="user-button" class="options">Users</button></div>
        <div><button id="manga-button" class="options">Manga</button></div>
        <div><button id="anime-button" class="options">Anime</button></div>
    </div>

    <!-- Manga statistics -->
    <div id="manga-page" class="page">
        <div id="manga-analytics">
            <h1>MANGA ANALYTICS</h1>
            <!-- Manga tag analytics -->
            <div class="analytic-box" id="manga-tag-analytics">
                <label for="analyticsType">Select Analytics Type:</label>
                <select id="analyticsType">
                    <option value="genres">Genre</option>
                    <option value="themes">Theme</option>
                    <option value="demographics">Demographic</option>
                    <option value="authors">Author</option>
                    <option value="serializations">Serialization</option>
                </select>
                <div>
                    <canvas id="mangaCriteriaChart"></canvas>
                </div>
            </div>

            <!-- Manga search and list -->
            <div id="manga-search-name" class="media-list-section analytic-box">
                <div  id="mangaBody">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="manga-search">Title:</label>
                        <input type="search" id="manga-search" name="searchTerm" placeholder="Title">
                    </div>
                    <div id="manga-list" class="media-list"></div>
                    <div id="manga-selected"></div>
                </div>
            </div>

            <!-- Manga rate of months -->
            <section id="manga-resultsSection"></section>
            <div id="mangaInfo"></div>
            <div class="analytic-box" id="manga-rate-of-months">
                <p class="analytic-title">Average Rate of Months in a Specific Year</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="manga-monthlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <div class="select">
                        <label for="manga-year">Select Year:</label>
                        <input type="text" id="manga-year" name="year">
                        <button id="select-button">Select</button>
                    </div>
                </div>
            </div>

            <!-- Manga rate of years -->
            <div class="analytic-box" id="manga-rate-of-years">
                <p class="analytic-title">Average Rate in Years Ranger</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="manga-yearlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <div class="select">
                        <label for="manga-startYear">Select Starting Year:</label>
                        <input type="number" id="manga-startYear" name="startYear" min="2000" max="2100" value="">
                        <label for="manga-endYear">Select Ending Year:</label>
                        <input type="number" id="manga-endYear" name="endYear" min="2000" max="2100" value="">
                        <button id="select-button2">Select</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Anime statistics -->
    <div id="anime-page" class="page">
        <div id="anime-analytics">
            <h1>ANIME ANALYTICS</h1>
            <!-- Anime tag analytics -->
            <div class="analytic-box" id="anime-tag-analytics">
                <label for="analyticsType2">Select Analytics Type:</label>
                <select id="analyticsType2">
                    <option value="tags">Tags</option>
                    <option value="producers">Producers</option>
                    <option value="studios">Studios</option>
                </select>
                <div>
                    <canvas id="animeCriteriaChart"></canvas>
                </div>
            </div>

            <!-- Anime search and list -->
            <div id="anime-search-name" class="media-list-section analytic-box">
                <div  id="animeBody">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="anime-search">Title:</label>
                        <input type="search" id="anime-search" name="searchTerm" placeholder="Title">
                    </div>
                    <div id="anime-list" class="media-list"></div>
                    <div id="anime-selected"></div>
                </div>
            </div>

            <!-- Anime rate of months -->
            <div class="analytic-box" id="anime-rate-of-months">
                <p class="anime-analytic-title">Average Rate of Months in a Specific Year</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="anime-monthlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <div class="select">
                        <label for="anime-year">Select Year:</label>
                        <input type="text" id="anime-year" name="year">
                        <button id="select-button3">Select</button>
                    </div>
                </div>
            </div>

            <!-- Anime rate of years -->
            <div class="analytic-box" id="anime-rate-of-years">
                <p class="analytic-title">Average Rate in Years Ranger</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="anime-yearlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <div class="select">
                        <label for="anime-startYear">Select Starting Year:</label>
                        <input type="number" id="anime-startYear" name="startYear" min="2000" max="2100" value="">
                        <label for="anime-endYear">Select Ending Year:</label>
                        <input type="number" id="anime-endYear" name="endYear" min="2000" max="2100" value="">
                        <button id="select-button4">Select</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- user statistics -->
    <div id="user-page" class="page">
        <div id="user-analytics">
            <h1>USER ANALYTICS</h1>
            <!-- User distribution analytics -->
            <div class="analytic-box2" id="user-distribution-analytics">
                <label for="distributionType">Select Distribution Type:</label>
                <select id="distributionType">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="birthday">Birthday</option>
                    <option value="joined-on">Joined On</option>
                </select>
                <div>
                    <canvas id="myChart"></canvas>
                </div>
            </div>

            <!-- Average app rate by age range -->
            <div class="analytic-box2" id="average-app-rate-by-age-range">
                <p>Average App Rate By Age Range</p>
                <canvas id="ageRangeChart" width="500" height="400"></canvas>
            </div>

            <!-- Average app rating by criteria -->
            <div class="analytic-box2" id="average-app-rating-by-criteria">
                <label for="criteria">Average App Rating by Criteria</label>
                <select id="criteria">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                </select>
                <div>
                    <canvas id="myChart2"></canvas>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script>
    $(document).ready(function() {
        // Event listeners for changing sections
        $('.options').click(function() {
            var sectionId = $(this).attr('id').split('-')[0] + '-page';
            $('.page').hide();
            $('#' + sectionId).show();
        });

        // Event listener for selecting year range
        $('#manga-rate-of-years button').click(function() {
            selectYearRange('manga');
        });

        $('#anime-rate-of-years button').click(function() {
            selectYearRange('anime');
        });

        // Event listener for selecting year
        $('#manga-rate-of-months button').click(function() {
            selectYear('manga');
        });

        $('#anime-rate-of-months button').click(function() {
            selectYear('anime');
        });
    });

    // Function to select year range
    function selectYearRange(type) {
        const startYear = $('#'+type+'-startYear').val();
        const endYear = $('#'+type+'-endYear').val();
        console.log("Selected range: ", startYear, "-", endYear);
        if (selectedMediaId && startYear && endYear) {
            fetchAndDisplayYearlyRatings(selectedMediaId, startYear, endYear, type);
        }
    }

    // Function to select year
    function selectYear(type) {
        const selectedYear = $('#'+type+'-year').val();
        console.log("Selected year:", selectedYear);
        if (selectedMediaId && selectedYear) {
            averageRatingByMonth(selectedMediaId, selectedYear, type);
        }
    }

    // Function to handle AJAX errors
    function handleAjaxError(xhr, textStatus, errorThrown) {
        console.error('AJAX request failed:', errorThrown);
    }

    // Function to display error message
    function displayErrorMessage(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = message;
        }
    }

    // Function to fetch and display average ratings in a given year range
    function fetchAndDisplayYearlyRatings(mediaId, startYear, endYear, section) {
        // AJAX call to fetch data
        $.post("${pageContext.request.contextPath}/manager", {
            action: "averageRatingByYear",
            mediaContentId: mediaId,
            startYear: startYear,
            endYear: endYear,
            section: section
        })
            .done(function(data) {
                if (data.success) {
                    // Process received data and update chart
                    updateChartWithData(data.averageRatingByYear, section + '-yearlyRatesChart');
                } else if (data.not_found) {
                    displayErrorMessage('not-found-yearly-rate-' + section, data.not_found);
                }
            })
            .fail(handleAjaxError);
    }

    // Function to update chart with new data
    function updateChartWithData(data, chartId) {
        const ctx = document.getElementById(chartId).getContext('2d');
        const years = Object.keys(data);
        const rates = Object.values(data);

        const chartData = {
            labels: years,
            datasets: [{
                label: 'Yearly Rates',
                data: rates,
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        };

        const chartOptions = {
            scales: {
                y: {
                    beginAtZero: true,
                    suggestedMax: 10
                }
            }
        };

        new Chart(ctx, {
            type: 'bar',
            data: chartData,
            options: chartOptions
        });
    }

    // Event listener for selecting year range
    $(document).on('change', '.year-input', function() {
        const type = $(this).data('type');
        const startYear = $('#' + type + '-startYear').val();
        const endYear = $('#' + type + '-endYear').val();
        if (selectedMediaId && startYear && endYear) {
            fetchAndDisplayYearlyRatings(selectedMediaId, startYear, endYear, type);
        }
    });

    // Event listener for selecting media
    $(document).on('click', '.media', function() {
        selectedMediaId = $(this).data('id');
        const type = $(this).data('type');
        const year = $('#' + type + '-year').val();
        if (selectedMediaId && year) {
            averageRatingByMonth(selectedMediaId, year, type);
        }
    });

    // Function to fetch and display average ratings by month
    function averageRatingByMonth(mediaId, year, section) {
        // Similar to fetchAndDisplayYearlyRatings, implement this function
    }

    // Event listener for selecting year
    $(document).on('change', '.year-select', function() {
        const type = $(this).data('type');
        const year = $(this).val();
        if (selectedMediaId && year) {
            averageRatingByMonth(selectedMediaId, year, type);
        }
    });
</script>

</body>
</html>
