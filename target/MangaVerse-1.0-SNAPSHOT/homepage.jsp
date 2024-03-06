<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 18.01.2024
  Time: 01:03
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css"/>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/index.js" defer></script>
    <title>MangaVerse</title>
</head>
<body>
    <section id="home" class="section-home">
        <div class="home-container">
            <div class="home-wrapper">
                <div class="anime"><a href="${pageContext.request.contextPath}/mainPage/anime" id="animeLink">Anime</a></div>
                <div class="welcome">
                    <div class="welcome-text">
                        <p>Welcome to</p>
                    </div>
                    <img src="${pageContext.request.contextPath}/images/logo-with-name.png" alt="middle" />
                </div>
                <div class="manga"><a href="${pageContext.request.contextPath}/mainPage/manga" id="mangaLink">Manga</a></div>
            </div>
        </div>
        <div class="down-arrow">
            <a data-scroll="list" id="down-arrow">
                <i class="fa-solid fa-chevron-down" style="color: #000000"> </i>
            </a>
        </div>
    </section>
</body>
</html>
