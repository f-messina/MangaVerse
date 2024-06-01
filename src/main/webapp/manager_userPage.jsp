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
    <title>Manager Page - User Analytics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<div class="all-page">
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
