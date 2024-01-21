<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 18.01.2024
  Time: 01:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com%22%3E/
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
    <link rel="stylesheet" href="css/main-registered-user.css"/>
    <link
            rel="stylesheet"
            href="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.css"
    />
    <title>Main Page</title>
</head>
<body>
<nav>
    <a href="#"><img src="images/logo%20with%20initial-8.png" alt="logo" /></a>
    <h1>Welcome Aokaado!</h1>
    <div class="nav-items">
        <a href="#" class="anime">Anime</a>
        <a href="#" class="manga">Manga</a>
        <a href="#">Profile</a>
        <a href="#" class="search"><i class="fa-solid fa-magnifying-glass"></i></a>
        <a href="#" class="small-pic"><img src="images/user%20icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
    </div>
</nav>

<section class="main-page-unreg">
    <div class="filtering">
        <div class="search-title-div">
            <p class="search-title">Search Manga </p>
            <a href="#" class="arama"><i class="fa-solid fa-magnifying-glass"></i></a>
        </div>

        <div class="manga-filter">
            <input type="text" placeholder="Name">
            <input type="text" placeholder="Genre">
            <input type="text" placeholder="Author">
            <input type="text" placeholder="Type">
            <input type="text" placeholder="Year of Publication">
            <input type="text" placeholder="Publishing Status">
        </div>
    </div>

    <div class="filtering">
        <div class="search-title-div">
            <p class="search-title">Search Anime </p>
            <a href="#" class="arama"><i class="fa-solid fa-magnifying-glass"></i></a>
        </div>

        <div class="anime-filter">
            <input type="text" placeholder="Name">
            <input type="text" placeholder="Type">
            <input type="text" placeholder="Tags">
            <input type="text" placeholder="Year of Publication">
            <input type="text" placeholder="Publishing Status">
        </div>
    </div>
</section>

<section>
  

</section>

<script src="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.js"></script>
<script src="js/index2.js"></script>
</body>

</html>
