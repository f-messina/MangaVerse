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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css"/>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script type="text/javascript">
        function searchAnime(query) {
            if (query.length === 0) {
                $("#searchResults").empty();
                return;
            }

            $.ajax({
                url: "searchAnime.jsp",
                type: "GET",
                data: { query: query },
                dataType: "json",
                success: function(animeList) {
                    displayDropdown(animeList);
                },
                error: function(xhr, status, error) {
                    console.error("Error:", error);
                }
            });
        }

        function displayDropdown(animeList) {
            $("#searchResults").empty();
            animeList.forEach(function(anime) {
                $("#searchResults").append("<div onclick='selectAnime(\"" + anime.title + "\")'>" + anime.title + "</div>");
            });
        }

        function selectAnime(title) {
            $("#searchInput").val(title);
            // Further actions like searching the database with the selected anime title can be added here
        }
    </script>
</head>
<body>
    <div id="page">
        <div id="side-navbar">
            <div><a class="options" href="">Manga</a></div>
            <div><a class="options" href="">Anime</a></div>
            <div><a class="options" href="">Users</a></div>
        </div>

        <div id="analytics">
            <h1>ANIME ANALYTICS</h1>
            <div class="analytic-box" id="tag-analytics">
                <p class="analytic-title">Average Rate of Tags</p>
            </div>

            <div class="analytic-box" id="search-name">
                <input type="text" id="searchInput"  placeholder="Search for anime...">
                <div id="searchResults"></div>
                <%--Solve the search bar here to show the result as heep writing--%>
            </div>

            <div class="analytic-box" id="rate-of-months">
                <p class="analytic-title">Average Rate of Months in a Specific Year</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="monthlyRatesChart" width="500" height="400"></canvas>
                    </div>

                    <%
                        // Assuming you have some data containing rates for each month
                        // You can replace this with your actual data
                        double[] rates = {5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8, 6.7};
                        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    %>
                    <div class="select">
                        <label for="year">Select Year:</label>
                        <input type="text" id="year" name="year">
                        <button onclick="selectYear()">Select</button>
                    </div>
                </div>
            </div>

            <div class="analytic-box" id="rate-of-years">
                <p class="analytic-title">Average Rate in Years Ranger</p>
                <div class="diagram-parameter">
                    <div>
                        <canvas id="yearlyRatesChart" width="500" height="400"></canvas>
                    </div>
                    <%
                        // Assuming you have some data containing rates for each year within the given range
                        // You can replace this with your actual data
                        double[] rates2 = {5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8, 6.7,5.6, 6.2, 7.8, 8.5, 7.3, 8.1, 9.2, 8.6, 7.9, 6.4, 5.8};
                        int startYear = 2000;
                        int endYear = 2022;
                    %>

                    <div class="select">
                        <label for="startYear">Select Starting Year:</label>
                        <input type="number" id="startYear" name="startYear" min="2000" max="2100" value="">
                        <label for="endYear">Select Ending Year:</label>
                        <input type="number" id="endYear" name="endYear" min="2000" max="2100" value="">
                        <button onclick="updateChart()">Select</button>
                    </div>
                </div>
            </div>

        </div>
    </div>
    <script>
        var ctxMonthly = document.getElementById('monthlyRatesChart').getContext('2d');
        var myMonthlyChart;

        function selectYear() {
            var selectedYear = document.getElementById('year').value;
            // Here you can use the selectedYear to fetch data for that year and update the chart accordingly
            console.log("Selected year:", selectedYear);
            updateMonthlyChart(selectedYear);
        }

        function updateMonthlyChart(selectedYear) {
            // Assuming you have a function to fetch data for the selected year
            // You need to implement this function according to your data source
            var yearData = fetchDataForYear(selectedYear);

            // Update chart with new data
            myMonthlyChart.data.datasets[0].data = yearData;
            myMonthlyChart.update();
        }

        document.addEventListener('DOMContentLoaded', function() {
            myMonthlyChart = new Chart(ctxMonthly, {
                type: 'bar',
                data: {
                    labels: [<% for (int i = 0; i < months.length; i++) { %>"<%= months[i] %>",<% } %>],
                    datasets: [{
                        label: 'Monthly Rates',
                        data: [<% for (int i = 0; i < rates.length; i++) { %><%= rates[i] %>,<% } %>],
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
            });
        });
    </script>
    <script>
        var ctxYearly = document.getElementById('yearlyRatesChart').getContext('2d');
        var myYearlyChart;

        function updateChart() {
            var startYear = document.getElementById('startYear').value;
            var endYear = document.getElementById('endYear').value;
            // Here you can use the startYear and endYear to fetch data for that year range and update the chart accordingly
            console.log("Selected start year:", startYear);
            console.log("Selected end year:", endYear);
            fetchDataAndUpdateChart(startYear, endYear);
        }

        function fetchDataAndUpdateChart(startYear, endYear) {
            // Assuming you have a function to fetch data for the given year range
            // You need to implement this function according to your data source
            var yearRangeData = fetchDataForYearRange(startYear, endYear);

            // Update chart with new data
            myYearlyChart.data.labels = yearRangeData.years;
            myYearlyChart.data.datasets[0].data = yearRangeData.rates;
            myYearlyChart.update();
        }

        document.addEventListener('DOMContentLoaded', function() {
            myYearlyChart = new Chart(ctxYearly, {
                type: 'bar', // Change to bar chart
                data: {
                    labels: [<% for (int i = startYear; i <= endYear; i++) { %>"<%= i %>",<% } %>],
                    datasets: [{
                        label: 'Yearly Rates',
                        data: [<% for (int i = 0; i < rates2.length; i++) { %><%= rates2[i] %>,<% } %>],
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
            });
        });
    </script>
</body>
</html>
