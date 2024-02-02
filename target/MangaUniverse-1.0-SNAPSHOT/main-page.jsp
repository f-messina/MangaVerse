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
    <link rel="stylesheet" href="css/index.css"/>
    <title>MangaVerse</title>
</head>
<body>
<nav>
    <a href="#"><img src="images/logo-with-initial.png.png" alt="logo" /></a>
    <div class="nav-items">
        <a href="#" class="anime">Anime</a>
        <a href="#" class="manga">Manga</a>
        <a href="auth.jsp">Sign Up</a>
        <a href="auth.jsp">Log In</a>
    </div>
</nav>

<section id="home" class="section-home">
    <div class="home-container">
        <div class="home-wrapper">
            <div class="anime"><a href="#">Anime</a></div>
            <div class="welcome">
                <div class="welcome-text">
                    <p>Welcome to</p>
                </div>
                <img src="images/logo-with-name.png" alt="middle" />
            </div>
            <div class="manga"><a href="#">Manga</a></div>
        </div>
    </div>
    <div class="down-arrow">
        <a data-scroll="list" href="#end-page">
            <i class="fa-solid fa-chevron-down" style="color: #000000"> </i>
        </a>
    </div>
</section>

<section id="list" class="main-page-unreg" >


    <div class="examples">
        <div class="example-title">
            <p>Manga of the Week</p>
        </div>
        <div class="items">
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/1/157897l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Berserk</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">One Piece</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/3/179882l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">JoJo no Kimyou na Bouken Part 7: Steel Ball Run</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/3/266834l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Oyasumi Punpun</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/3/243675l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Fullmetal Alchemist</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/1/259070l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Vagabond</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
        </div>
        <a href="#" class="show-all">Show All Manga</a>




        <div class="example-title">
            <p>Anime of the Week</p>
        </div>
        <div class="items">
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/anime/1948/136272.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Ai wo Taberu</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg"></div>
                <div class="items-text">
                    <p class="items-name">!NVADE SHOW!</p>
                    <p class="items-score">Score: 6.8</p>

                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/anime/1671/127574.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Bungaku Shoujo" Kyou no Oyatsu: Hatsukoi</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/anime/6/26770.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Bungaku Shoujo" Memoire</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/anime/8/81162.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Bungaku Shoujo" Movie</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
            <div class="items-content">
                <div class="items-img-box"><img src="https://cdn.myanimelist.net/images/anime/1168/93236.jpg"></div>
                <div class="items-text">
                    <p class="items-name">Calpis" Hakkou Monogatari</p>
                    <p class="items-score">Score: 6.8</p>
                </div>
            </div>
        </div>
        <a href="#" class="show-all">Show All Anime</a>
    </div>
    <a id="end-page"></a>
</section>

<script src="js/index.js"></script>
</body>

</html>
