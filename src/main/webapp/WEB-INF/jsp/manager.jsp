<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 13.03.2024
  Time: 16:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>



<html>
<head>
    <title>Manager Page - Anime Analytics</title>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script type="text/javascript">



        //function updateGenreChart() {
            // Implement updating genre chart here
           // console.log("Updating genre chart");
        //}

        //function updateThemeChart() {
            // Implement updating theme chart here
        //  console.log("Updating theme chart");
        //}

        //function updateDemographicChart() {
            // Implement updating demographic chart here
        //  console.log("Updating demographic chart");
        ////}

        //function updateAuthorChart() {
            // Implement updating author chart here
        //  console.log("Updating author chart");
        //}
    </script>
</head>
<body>
<div class="all-page">
    <!-- navbar -->
    <div id="side-navbar" class="button-container">
        <div><button id="user-button" class="options" onclick="changeSection(this)">Users</button></div>
        <div><button id="manga-button" class="options" onclick="changeSection(this)">Manga</button></div>
        <div><button id=anime-button" class="options" onclick="changeSection(this)">Anime</button></div>
    </div>

    <!-- manga statistics -->
    <div id="manga-page" class="page">
        <div id="manga-analytics">
            <h1>MANGA ANALYTICS</h1>
            <div class="analytic-box" id="manga-tag-analytics">
                <%--genre - theme - demographic -author  --%>
                <label for="analyticsType">Select Analytics Type:</label>
                <select id="analyticsType" onchange="getBestCriteria(this.value, 'manga', 1)">
                    <option value="genres">Genre</option>
                    <option value="themes">Theme</option>
                    <option value="demographics">Demographic</option>
                </select>
                    <div>
                        <canvas id="mangaCriteriaChart"> </canvas>
                    </div>

            </div>

            <div id="manga-search-name" class="media-list-section analytic-box">
                <div  id="mangaBody">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="manga-search">Title:</label>
                        <input type="search" id="manga-search" name="searchTerm" placeholder="Title" oninput="getMediaContent('manga')">
                    </div>
                    <!-- manga list -->
                    <div id="manga-list" class="media-list"></div>
                    <!-- manga selected -->
                    <div id="manga-selected"></div>
                </div>
            </div>

            <section id="manga-resultsSection"></section>
            <div id="mangaInfo"></div>

            <div class="analytic-box" id="manga-rate-of-months">
                <p class="analytic-title">Average Rate of Months in a Specific Year</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="manga-monthlyRatesChart" width="500" height="400"></canvas>
                        <p id="not-found-monthly-rate-manga"> </p>
                    </div>

                  <%
                        // Assuming you have some data containing rates for each month
                        // You can replace this with your actual data
                        double[] rates = {5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8, 6.7};
                        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    %>
                    <div class="select">
                        <label for="manga-year">Select Year:</label>
                        <input type="text" id="manga-year" name="year">
                        <button id="select-button" onclick="selectYear('manga')">Select</button>
                    </div>
                </div>
            </div>

            <div class="analytic-box" id="manga-rate-of-years">
                <p class="analytic-title">Average Rate in Years Ranger</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="manga-yearlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <%
                        // Assuming you have some data containing rates for each year within the given range
                        // You can replace this with your actual data
                        double[] rates2 = {5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8, 6.7,5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8};
                        int startYear = 2000;
                        int endYear = 2022;
                    %>
                    <div class="select">
                        <label for="manga-startYear">Select Starting Year:</label>
                        <input type="number" id="manga-startYear" name="startYear" min="2000" max="2100" value="">
                        <label for="manga-endYear">Select Ending Year:</label>
                        <input type="number" id="manga-endYear" name="endYear" min="2000" max="2100" value="">
                        <button onclick="selectYearRange('manga')">Select</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- anime statistics -->
    <div id="anime-page" class="page">
        <div id="anime-analytics">
            <h1>ANIME ANALYTICS</h1>
            <div class="analytic-box" id="anime-tag-analytics">
                <label for="analyticsType2">Select Analytics Type:</label>
                <select id="analyticsType2" onchange="getBestCriteria(this.value, 'anime', 1)">
                    <option value="tags">Tags</option>
                    <option value="genres">Genres</option>
                    <option value="themes">Themes</option>
                    <option value="demographics">Demographics</option>
                </select>
                    <div>
                        <canvas id="animeCriteriaChart"> </canvas>
                    </div>
            </div>


            <div id="anime-search-name" class="media-list-section analytic-box">
                <div  id="animeBody">
                    <div class="d-flex align-items-center">
                        <label class="filter-name" for="anime-search">Title:</label>
                        <input type="search" id="anime-search" name="searchTerm" placeholder="Title" oninput="getMediaContent('anime')">
                    </div>
                    <!-- anime list -->
                    <div id="anime-list" class="media-list"></div>
                    <!-- anime selected -->
                    <div id="anime-selected"></div>
                </div>
            </div>

            <div class="analytic-box" id="anime-rate-of-months">
                <p class="anime-analytic-title">Average Rate of Months in a Specific Year</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="anime-monthlyRatesChart" width="500" height="400"></canvas>

                    </div>

                    <%
                        // Assuming you have some data containing rates for each month
                        // You can replace this with your actual data
                        double[] animeRates = {5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8, 6.7};
                        String[] animeMonths = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    %>
                    <div class="select">
                        <label for="anime-year">Select Year:</label>
                        <input type="text" id="anime-year" name="year">
                        <button id="select-button2" onclick="selectYear('anime')">Select</button>
                    </div>
                </div>
            </div>

            <div class="analytic-box" id="anime-rate-of-years">
                <p class="analytic-title">Average Rate in Years Ranger</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="anime-yearlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <%
                        // Assuming you have some data containing rates for each year within the given range
                        // You can replace this with your actual data
                        double[] animeRates2 = {5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8, 6.7,5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8};
                        int animeStartYear = 2000;
                        int animeEndYear = 2022;
                    %>

                    <div class="select">
                        <label for="anime-startYear">Select Starting Year:</label>
                        <input type="number" id="anime-startYear" name="startYear" min="2000" max="2100" value="">
                        <label for="anime-endYear">Select Ending Year:</label>
                        <input type="number" id="anime-endYear" name="endYear" min="2000" max="2100" value="">
                        <button onclick="selectYearRange('anime')">Select</button>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <!-- user statistics -->
    <div id="user-page" class="page">
        <div id="user-analytics">
            <h1>USER ANALYTICS</h1>
            <div class="analytic-box2" id="user-distribution-analytics">
                <label for="distributionType">Select Distribution Type:</label>
                <select id="distributionType" onchange="distribution(this.value)">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="birthday">Birthday</option>
                    <option value="joined-on">Joined On</option>
                </select>

                <div>
                    <canvas id="myChart"></canvas>
                </div>
            </div>

            <div class="analytic-box2" id="average-app-rate-by-age-range">
                <p>Average App Rate By Age Range</p>
                <canvas id="ageRangeChart" width="500" height="400"></canvas>
            </div>

            <div class="analytic-box2" id="average-app-rating-by-criteria">
                <label for="criteria">Average App Rating by Criteria</label>
                <select id="criteria" onchange="averageAppRatingByCriteria(this.value)">
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



<script>
    let startYear= null;
    let endYear = null;
    function averageRatingInYearRange(mediaId, startYear, endYear, section){
        let mediaCtxYearly = null;
        if(section === 'manga'){
            mediaCtxYearly = document.getElementById('manga-yearlyRatesChart').getContext('2d');
        }else if(section === 'anime'){
            mediaCtxYearly = document.getElementById('anime-yearlyRatesChart').getContext('2d');
        }
        let myYearlyChart;

        const inputData={
            action: "averageRatingByYear",
            mediaContentId: mediaId,
            startYear: startYear,
            endYear: endYear,
            section: section
        };
        $.post("${pageContext.request.contextPath}/manager", inputData, function(data){
            console.log(data)
            if (data.success){
                const years = Object.keys(data.averageRatingByYear);
                const rates = Object.values(data.averageRatingByYear);

                if(!myYearlyChart){
                    myYearlyChart = new Chart(mediaCtxYearly,{
                        type: 'bar', // Change to bar chart
                        data: {
                            labels: years,
                            datasets: [{
                                label: 'Yearly Rates',
                                data: rates,
                                backgroundColor: 'rgba(54, 162, 235, 0.2)', // Adjust color as needed
                                borderColor: 'rgba(54, 162, 235, 1)', // Adjust color as needed
                                borderWidth: 1
                            }]
                        },
                        options: {
                            scales: {
                                yAxes: [{
                                    ticks: {
                                        beginAtZero: true,
                                        suggestedMax: 10 // Set max value for y-axis
                                    }
                                }]
                            }
                        }
                    })
                }else{
                    myYearlyChart.data.labels = years;
                    myYearlyChart.data.datasets[0].data = rates;
                    myYearlyChart.update();
                }
            }else if(data.not_found){
                const notFound = document.getElementById('not-found-yearly-rate-manga');
                notFound.textContent = data.not_found;
            }
        }).fail(function(){
            console.error('Failed to fetch data from server.');
        });
    }

    $(document).on('click', '#manga-list .media', function (){
        selectedMediaId = $(this).data('id');
        if(startYear && endYear){
            averageRatingInYearRange(selectedMediaId,startYear,endYear,'manga');
        }
    });
    $(document).on('click', '#anime-list .media', function (){
        selectedMediaId = $(this).data('id');
        if(startYear && endYear){
            averageRatingInYearRange(selectedMediaId,startYear,endYear,'anime');
        }
    });

    function selectYearRange(type){
        if (type === "manga"){
            startYear = document.getElementById('manga-startYear').value;
            endYear = document.getElementById('manga-endYear').value;
        }else if(type === "anime"){
            startYear = document.getElementById('anime-startYear').value;
            endYear = document.getElementById('anime-endYear').value;
        }console.log("Selected range: ", startYear, "- ", endYear);
        if(selectedMediaId && startYear && endYear){
            averageRatingInYearRange(selectedMediaId,startYear,endYear,type);
        }
    }
</script>

<script>
    $(document).ready(function () {
        // Bind the searchForm submission to the performAsyncSearch function
        $("#searchForm").submit(function (event) {
            event.preventDefault(); // Prevent the default form submission
            performAsyncSearch("manga-searchForm", "manga-resultsSection");
        });
    });

    function performAsyncSearch(formId, containerId) {
        const form = $("#" + formId);
        const url = form.attr("action");
        const formData = form.serialize();

        $.post(url, formData, function (data) {
            const container = $("#" + containerId).empty();
            container.append(
                $("<h1>").text("Total results: " + data.mediaContentList.totalCount),
                $("<div>").attr("id", "manga-mediaContentContainer")
            );

            updateMediaContent(data, "manga-mediaContentContainer");
        }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
    }

    // Update media content in the specified container
    function updateMediaContent(data, containerId) {
        const mediaContentContainer = $("#" + containerId).empty();
        mediaContentContainer.append(data.mediaContentList.entries.map(media => createArticleElement(media)));
    }


    // Create HTML element for a media article
    function createArticleElement(media) {
        const articleElement = $("<article>").attr("id", media.id).append(
            $("<a>").text(media.title).attr("href", "#").on("click", function (event) {
                event.preventDefault(); // Prevent default link behavior
                // Make AJAX request to retrieve media information
                $.ajax({
                    url: "${pageContext.request.contextPath}/manager/manga",
                    method: "GET",
                    data: {mediaId: media.id, action: "show_info"}, // Pass media ID to servlet
                    success: function (response) {
                        // Handle successful response
                        console.log("Media Info:", response);
                        // You can handle the retrieved data as needed
                        createInfoDiv(response);
                    },
                    error: function (xhr, status, error) {
                        // Handle errors
                        console.error("Error:", error);
                    }
                });
            })
        );
        console.log(media.id + media.title);
        return articleElement;
    }

    function createInfoDiv(response) {
        // Clear previous content of mangaInfo div
        $("#mangaInfo").empty();

        // Create a div to contain the manga information
        const mangaInfoDiv = $("<div>");
        const mangaUpdatePopup = $("<form>").addClass("popup-container").addClass("hidden").attr("id", "manga-updatePopup");

        // Iterate through the response and append each property to the mangaInfoDiv
        Object.entries(response.manga).forEach(([key, value]) => {
            // If the value is null, set it to the string "null"
            if (value === null) {
                value = "null";
            }

            if (key === "authors") {
                let objectString = "";
                Object.entries(value).forEach(([authorsKey, authorsValue]) => {
                    Object.entries(authorsValue).forEach(([authorKey, authorValue]) => {
                        if (authorKey !== "id") {
                            objectString += authorKey + ": " + authorValue + ","
                        }
                    });
                });

                value = objectString.slice(0, -1);
                ;
            }
            // Append the key-value pair to mangaInfoDiv
            mangaInfoDiv.append("<p><strong>" + key + ":</strong>" + value + "</p>");
            mangaUpdatePopup.append("<label>" + key + ":</label>");
            mangaUpdatePopup.append($("<input>").attr("value", value))

        });

        // Add a delete button
        const deleteButton = $("<button>Delete</button>");
        deleteButton.on("click", function () {
            // Send asynchronous request to delete the manga
            $.ajax({
                url: "${pageContext.request.contextPath}/manager/manga",
                method: "POST",
                data: {mediaId: response.manga.id, action: "delete_media"},
                success: function () {
                    console.log("Manga deleted successfully");
                    $("#mangaInfo").empty();
                    $("#" + response.manga.id).remove();
                },
                error: function (xhr, status, error) {
                    // Handle error response
                    console.error("Error deleting manga:", error);
                }
            });
        });
        mangaInfoDiv.append(deleteButton);

        const updateButton = $("<button>Update<button>");
        updateButton.on("click", function () {
            $("#updatePopup").toggleClass("hidden");
        })
        updateButton.append(mangaUpdatePopup);
        mangaInfoDiv.append(updateButton);

        $("#mangaInfo").append(mangaInfoDiv);

        //add select button
        const selectButton = $("<button>Select</button>");
        selectButton.on("click", function () {
            // Send asynchronous request to select the manga
            selectMangaForAnalytics(media.id);
        });
        mangaInfoDiv.append(selectButton);
    }

   /* function selectMangaForAnalytics(mediaId) {
        // Get the selected manga's info
        $.ajax({
            url: "${pageContext.request.contextPath}/manager/manga",
            method: "GET",
            data: {mediaId: mediaId, action: "get_manga_info"},
            success: function (response) {
                // Handle successful response
                console.log("Selected Manga Info:", response);
                // You can store the selected manga info for later use
                // For now, let's assume you store it in a global variable
                selectedMangaInfo = response;
                // Now, prompt the user to enter the year for analytics
                var selectedYear = prompt("Enter year for analytics (YYYY):");
                if (selectedYear) {
                    // Update chart with the selected manga and year
                    updateMonthlyChart(selectedMangaInfo, selectedYear);
                }
            },
            error: function (xhr, status, error) {
                // Handle errors
                console.error("Error:", error);
            }
        });
    }*/
</script>
<script>

    const page = "${requestScope.page}";
    console.log(page);
    if (page === "manga") {
        document.getElementById("manga-button").classList.add("active");
        document.getElementById("manga-page").style.display = "flex";
    } else if (page === "anime") {
        document.getElementById("anime-button").classList.add("active");
        document.getElementById("anime-page").style.display = "flex";
    } else {
        document.getElementById("user-button").classList.add("active");
        document.getElementById("user-page").style.display = "flex";
    }

    function changeSection(button) {
        const section = button.id.split("-")[0];
        const animeSection = $("#anime-page");
        const mangaSection = $("#manga-page");
        const userSection = $("#user-page");
        $(".button-container button").removeClass("active");
        if (section === "user") {
            button.classList.add("active");
            animeSection.hide();
            mangaSection.hide();
            loadPage("getUserDefaultAnalytics");
            userSection.css("display", "flex");
        } else {
            userSection.hide();
            if (section === "anime") {
                button.classList.add("active");
                loadPage("getAnimeDefaultAnalytics");
                animeSection.css("display", "flex");
                mangaSection.hide();
            } else {
                button.classList.add("active");
                loadPage("getMangaDefaultAnalytics");
                mangaSection.css("display", "flex");
                animeSection.hide();
            }
        }
    }

    $(document).ready(function (){
        getBestCriteria('genres','manga',1);
    });
    $(document).ready(function (){
        getBestCriteria('tags','anime',1);
    });


    function getBestCriteria(criteria, section, page = 1) {
        const inputData = {
            action: "getBestCriteria",
            criteria: criteria,
            section: section,
            page: page
        };

        $.post("${pageContext.request.contextPath}/manager", inputData, function (data) {
            console.log(data);
            const canvasId = section === 'manga' ? 'mangaCriteriaChart' : 'animeCriteriaChart';
            showChart(criteria,data.bestCriteria, canvasId);
        },
        'json');
    }
    function showChart(criteria, bestCriteriaData, canvasId){

        const ctx = document.getElementById(canvasId).getContext('2d');
        const labels = Object.keys(bestCriteriaData);
        const values = Object.values(bestCriteriaData);

        const chartData = {
            labels: labels,
            datasets: [{
                label: `Best ${criteria}`,
                data: values,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        };

        const config = {
            type: 'bar',
            data: chartData,
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        };

        // Destroy existing chart instance if exists to avoid overlap
        if (window[canvasId + 'Instance']) {
            window[canvasId + 'Instance'].destroy();
        }

        window[canvasId + 'Instance'] = new Chart(ctx, config);
    }







    function loadPage(action) {
        $.post("${pageContext.request.contextPath}/manager", {action: action}, function (data) {
            console.log(data);
        });
    }


    // media content average rating by month chart

    let selectedMediaId =null;
    let selectedYear = null;

    function averageRatingByMonth(mediaId, year,section) {
        let mediaCtxMonthly = null ;
            if(section === 'manga'){
                mediaCtxMonthly = document.getElementById('manga-monthlyRatesChart').getContext('2d');
            }else if(section === 'anime'){
                mediaCtxMonthly = document.getElementById('anime-monthlyRatesChart').getContext('2d');
            }

        let MyMonthlyChart;


        const inputData ={
            action: "averageRatingByMonth",
            mediaContentId: mediaId,
            year: year,
            section: section
        };

        $.post("${pageContext.request.contextPath}/manager", inputData, function(data){
            console.log(data)
            if (data.success){
                const months = Object.keys(data.averageRatingByMonth);
                const rates = Object.values(data.averageRatingByMonth);


                if (!MyMonthlyChart) {
                    MyMonthlyChart = new Chart(mediaCtxMonthly, {
                        type: 'bar',
                        data: {
                            labels: months,
                            datasets: [{
                                label: 'Monthly Rates',
                                data: rates,
                                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                                borderColor: 'rgba(54, 162, 235, 1)',
                                borderWidth: 1
                            }]
                        },
                        options: {
                            scales: {
                                yAxes: [{
                                    ticks: {
                                        beginAtZero: true,
                                        suggestedMax: 10
                                    }
                                }]
                            }
                        }
                    });
                }
                else {
                    MyMonthlyChart.data.labels = months;
                    MyMonthlyChart.data.datasets[0].data = rates;
                    MyMonthlyChart.update();
                }
            }else if(data.not_found){
                const notFound = document.getElementById('not-found-monthly-rate-manga');
                notFound.textContent = data.not_found;
            }

        }).fail(function(){
                console.error('Failed to fetch data from server.');
            });
    }

    $(document).on('click', '#manga-list .media', function() {
        selectedMediaId = $(this).data('id');
        if (selectedYear) {
            averageRatingByMonth(selectedMediaId, selectedYear,'manga');
        }
    });
    $(document).on('click', '#anime-list .media', function() {
        selectedMediaId = $(this).data('id');
        if (selectedYear) {
            averageRatingByMonth(selectedMediaId, selectedYear,'anime');
        }
    });




   /* // Event listener for the "Select" button
    $(document).on('click', '#select-button', function() {
        selectedYear = document.getElementById('manga-year').value;
        if (selectedMediaId && selectedYear) {
            // Call the function to update the chart with selected manga and year
            averageRatingByMonth(selectedMediaId, selectedYear,'manga');
        } else {
            alert("Please select a media and enter a year.");
        }
    });
    // Event listener for the "Select" button
    $(document).on('click', '#select-button2', function() {
        selectedYear = document.getElementById('anime-year').value;

        if (selectedMediaId && selectedYear) {
            // Call the function to update the chart with selected anime and year
            averageRatingByMonth(selectedMediaId, selectedYear,'anime');
        } else {
            alert("Please select a media and enter a year.");
        }
    });*/

    function selectYear(type) {
        if (type === "manga"){
            selectedYear = document.getElementById('manga-year').value;
        }else if(type === "anime"){
            selectedYear = document.getElementById('anime-year').value;
        }
        console.log("Selected year:", selectedYear);
        if (selectedMediaId && selectedYear) {
            averageRatingByMonth(selectedMediaId,selectedYear,type)
        }
    }


    function getMediaContent(type){
        const mangaList = $("#manga-list");
        const animeList = $("#anime-list");
        let mediaTitle;
        if (type === "manga"){
            mediaTitle = $("#manga-search").val();
        }else if(type === "anime"){
            mediaTitle =$("#anime-search").val();
        }
        else{
            return;
        }
        const inputData = {
            action: "getMediaContentByTitle",
            type: type,
            mediaTitle: mediaTitle
        }
        $.post("${pageContext.request.contextPath}/"+ type, inputData, function (data) {
            mangaList.empty();
            if (data.success) {
                if (type === "manga"){
                    for (let i = 0; i < data.mangaList.length; i++) {
                        const manga = data.mangaList[i];
                        const mangaDiv = $("<div>").addClass("media").click(function() {
                            getMediaContentById(manga.id, type);
                        });

                        // Create the image element and set the source
                        const img = $("<img src='" + manga.imageUrl + "'>").addClass("media-pic");
                        mangaDiv.append(img);

                        // Create the paragraph element and set the text
                        const p = $("<p>").addClass("media-title").text(manga.title);
                        mangaDiv.append(p);
                        mangaList.append(mangaDiv);
                    }
                }else {
                    for (let i = 0; i < data.animeList.length; i++) {
                        const anime = data.animeList[i];
                        const animeDiv = $("<div>").addClass("media").click(function() {
                            getMediaContentById(anime.id, type);
                        });
                        // Create the image element and set the source
                        const img = $("<img src='" + anime.imageUrl + "'>").addClass("media-pic");
                        animeDiv.append(img);

                        // Create the paragraph element and set the text
                        const p = $("<p>").addClass("media-title").text(anime.title);
                        animeDiv.append(p);
                        animeList.append(animeDiv);
                    }
                }
            } else if (data.mediaSearchFailed) {
                mangaList.append($("<div>").text(data.mediaSearchFailed));
            }
        }).fail(function() {
            mangaList.empty();
            mangaList.append($("<div>").text("An error occurred. Please try again later."));
        });
    }

    function getMediaContentById(id,type){

        const manga = $("#manga-selected");
        const anime= $("#anime-selected");

        const inputData = {
            action: "getMediaContent",
            type: type,
            mediaId: id
        }
        console.log(inputData)
        $.post("${pageContext.request.contextPath}/"+ type, inputData, function (data) {
            manga.empty();
            if (data.success) {
                if (type === "manga"){
                    console.log(data)
                    selectedMediaId = data.manga.id;
                    const map = data.manga;
                    for (const key in map) {
                        if (map.hasOwnProperty(key)) {
                            const value = map[key];
                            const div = $("<div>");
                            div.text(value);
                            manga.append(div);
                        }
                    }
                }else {
                    selectedMediaId = data.anime.id;
                    const map = data.anime;
                    for (const key in map) {
                        if (map.hasOwnProperty(key)) {
                            const value = map[key];
                            const div = $("<div>");
                            div.text(value);
                            anime.append(div);
                        }
                    }
                }
            } else if (data.mediaSearchFailed) {
                manga.append($("<div>").text(data.mediaSearchFailed));
            }
        }).fail(function() {
            manga.empty();
            manga.append($("<div>").text("An error occurred. Please try again later."));
        });
    }


</script>


<script>
    $(document).ready(function(){
        // Append a canvas element for Chart.js
        //$("#user-distribution-analytics div").append('<canvas id="myChart"></canvas>');
        //$("#average-app-rating-by-criteria div").append('<canvas id="myChart2"></canvas>');

        // Load the default chart for gender
        distribution('gender', "user");
        averageAppRatingByCriteria('gender', "user");
    });



    function distribution(criteria, section){
        const inputData ={
            action: "distribution",
            criteria: criteria,
            section: section
        };
        $.post("${pageContext.request.contextPath}/manager",inputData, function (data){
                console.log(data);
                renderChart(criteria,data.distribution);
            },
            'json');
    }
    function renderChart(criteria, distributionData) {
        const ctx = document.getElementById('myChart').getContext('2d');
        const labels = Object.keys(distributionData);
        const values = Object.values(distributionData);

        const chartData = {
            labels: labels,
            datasets: [{
                label: `User Distribution by ${criteria}`,
                data: values,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        };

        const config = {
            type: 'bar',
            data: chartData,
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        };

        // Destroy existing chart instance if exists to avoid overlap
        if (window.myChartInstance) {
            window.myChartInstance.destroy();
        }

        window.myChartInstance = new Chart(ctx, config);
    }

    $(document).ready(function(){
        // Append a canvas element for Chart.js
        $(".analytic-box2 div").append('<canvas id="myChart"></canvas>');
    });

    function averageAppRatingByCriteria(criteria,section){
        const inputData ={
            action: "averageAppRatingByCriteria",
            criteria: criteria,
            section: section
        };
        $.post("${pageContext.request.contextPath}/manager",inputData, function (data){
                console.log(data);
                renderChart2(criteria,data.averageAppRatingByCriteria);
            },
            'json');
    }

    function renderChart2(criteria, averageAppRatingData) {
        const ctx = document.getElementById('myChart2').getContext('2d');
        const labels = Object.keys(averageAppRatingData);
        const values = Object.values(averageAppRatingData);

        const chartData = {
            labels: labels,
            datasets: [{
                label: `Average App Rating by ${criteria}`,
                data: values,
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 1
            }]
        };

        const config = {
            type: 'bar',
            data: chartData,
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        };

        // Destroy existing chart instance if exists to avoid overlap
        if (window.myChartInstance2) {
            window.myChartInstance2.destroy();
        }

        window.myChartInstance2 = new Chart(ctx, config);
    }

    $(document).ready(function(){
        // Append a canvas element for Chart.js
        $("#average-app-rating-by-criteria div").append('<canvas id="myChart2"></canvas>');
    });






    function averageAppRatingByAgeRange(){
        const inputData={
            action: "averageAppRatingByAgeRange"
        };
        $.post("${pageContext.request.contextPath}/manager",inputData, function (response){
            console.log(response);

            const data = response.averageAppRatingByAgeRange;

            // Parse the data into labels and ratings
            const labels = Object.keys(data);
            const ratings = Object.values(data);

            // Create the chart
            const ctx = document.getElementById('ageRangeChart').getContext('2d');
            const ageRangeChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Average App Rating',
                        data: ratings,
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Average Rating'
                            }
                        },
                        x: {
                            title: {
                                display: true,
                                text: 'Age Range'
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: true,
                            position: 'top'
                        }
                    }
                }
            });
        });
    }
    // Call the function to load and display the chart
    averageAppRatingByAgeRange();

</script>




</body>
</html>
