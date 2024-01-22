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
    <link rel="stylesheet" href="css/userProfilePage.css"/>
    <title>Profile Page</title>
</head>
<body>
<nav>
    <a href="#"><img src="images/logo%20with%20initial-8.png" alt="logo" /></a>
    <h1>Profile Page</h1>
    <div class="nav-items">
        <a href="#" class="anime">Anime</a>
        <a href="#" class="manga">Manga</a>
        <a href="#">Profile</a>
        <a href="#" class="search"><i class="fa-solid fa-magnifying-glass"></i></a>
        <a href="#" class="small-pic"><img src="images/user%20icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
    </div>
</nav>

<section class="user-info">
    <div class="profile">
        <div class="profile-img">
            <img src="images/user%20icon%20-%20Kopya%20-%20Kopya.png">
        </div>
        <button class="edit-prof">Edit Profile</button>
    </div>

    <div class="texts">
        <h1 class="info">User Name: <p>Xinil</p></h1>
        <h1 class="info">Description: <p>Liberating the mind from stress with the power of anime zen.</p></h1>
        <h1 class="info">Email: <p>xdavis@exapmle.com</p></h1>
        <h1 class="info">Gender: <p>Male</p></h1>
        <h1 class="info">Birthday: <p>1985-03-04</p></h1>
        <h1 class="info">Location: <p>California</p></h1>
        <h1 class="info">Joined On: <p>2004-11-05</p></h1>
    </div>
</section>

<section class="lists-section">
    <h1>My Lists</h1>
    <div class="lists">
        <div>
            <div class="img-name">
                <div><img src="https://cdn.myanimelist.net/images/anime/6/85893.jpg"></div>
                <p>Sci-Fi anime with musical scene to suggest to my son</p>
            </div>
            <div class="list-items">
                    <ol>
                        <li>Item 1</li>
                        <li>Item 2</li>
                        <li>Item 3</li>
                        <li>Item 4</li>
                    </ol>
            </div>
        </div>

        <div>
            <div class="img-name">
                <div><img src="https://cdn.myanimelist.net/images/anime/1108/105484.jpg"></div>
                <p>Anime with mystery to collect</p>
            </div>
            <div class="list-items">
                <ol>
                    <li>Item 1</li>
                    <li>Item 2</li>
                    <li>Item 3</li>
                    <li>Item 4</li>
                    <li>Item 5</li>
                </ol>
            </div>
        </div>

        <div>
            <div class="img-name">
                <div><img src="https://cdn.myanimelist.net/images/anime/1718/124900.jpg"></div>
                <p>Shoujo manga with heroic stories to read with my brother</p>
            </div>
            <div class="list-items">
                <ol>
                    <li>Item 1</li>
                    <li>Item 2</li>
                    <li>Item 3</li>
                    <li>Item 4</li>
                </ol>
            </div>
        </div>


    </div>
</section>


</body>

</html>
