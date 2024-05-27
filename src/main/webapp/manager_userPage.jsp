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

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<div class="all-page">
    <!-- navbar -->
    <div id="side-navbar" class="button-container">
        <div><button id="user-button" class="options" onclick="changeSection(this)">Users</button></div>
        <div><button id="manga-button" class="options" onclick="changeSection(this)">Manga</button></div>
        <div><button id=anime-button" class="options" onclick="changeSection(this)">Anime</button></div>
    </div>



    <!-- user statistics -->
    <div id="user-page" class="page">
        <div id="user-analytics">
            <h1>USER ANALYTICS</h1>
            <div class="analytic-box" id="user-distribution-analytics">
                <label for="distributionType">Select Distribution Type:</label>
                <select id="distributionType" onchange="distribution(this.value)">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                    <option value="birthday">Birthday</option>
                    <option value="joined-on">Joined On</option>
                </select>

                <div>
                    <!-- Chart -->
                </div>
            </div>

            <div class="analytic-box" id="average-app-rate-by-age-range">
                <p>Average App Rate By Age Range</p>
                <canvas id="ageRangeChart" width="500" height="400"></canvas>
            </div>

            <div class="analytic-box" id="average-app-rating-by-criteria">
                <label for="criteria">Select Distribution Type:</label>
                <select id="criteria" onchange="distribution(this.value)">
                    <option value="gender">Gender</option>
                    <option value="location">Location</option>
                </select>

                <div>
                    <!-- Chart -->
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function distribution(criteria){
        const inputData ={
            action: "distribution",
            criteria: criteria
        };
        $.post("${pageContext.request.contextPath}/manager",inputData, function (data){
            console.log(data);
        });
    }

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

    function loadPage(action) {
        $.post("${pageContext.request.contextPath}/manager", {action: action}, function (data) {
            console.log(data);
        });
    }
</script>
</body>
</html>
