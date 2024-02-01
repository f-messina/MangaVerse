<%@ page import="it.unipi.lsmsd.fnf.model.registeredUser.User" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.SecurityUtils" %>
<%@ page import="java.util.logging.Logger" %><%--
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
        <a href="profile.jsp">Profile</a>
        <a href="#" class="search"><i class="fa-solid fa-magnifying-glass"></i></a>
        <a href="#" class="small-pic"><img src="images/user%20icon%20-%20Kopya%20-%20Kopya.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
    </div>
</nav>



<section class="main-page-unreg">

    <div class="filtering" >
        <div class="search-title-div">
            <p class="search-title">Search Manga </p>
        </div>

        <div class="manga-filter">
            <input type="text" placeholder="Name">
            <a href="#" class="arama"><i class="fa-solid fa-magnifying-glass"></i></a>
        </div>
        <button onclick="myFunction()" class="more-filtering">See Detailed Filtering</button>
        <div id="myDIV">
            <div class="manga-filter">
                <input type="text" placeholder="Author">
            </div>

            <div class="genre-filtering">
                <p class="genre-title">Genre</p>
                <div class="two-genres">
                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Girls Love</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Sci-Fi</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Supernatural</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Comedy</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Award Winning</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Mystery</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Romance</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Horror</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Suspense</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Fantasy</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Gourmet</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Erotica</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Avant Garde</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Slice of Life</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Action</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Boys Love</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Adventure</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Drama</p>
                        </div>
                    </div>

                    <div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Ecchi</p>
                        </div>
                        <div class="genre">
                            <label class="container">
                                <input type="checkbox" name="checkbox"  class="green-checkbox">
                                <span class="checkmark green" ></span>
                            </label>
                            <label class="container">
                                <input type="checkbox" name="checkbox" class="red-checkbox">
                                <span class="checkmark red"></span>
                            </label>
                            <p>Sports</p>
                        </div>
                    </div>

                </div>
                <hr>
                <div class="filter-type">
                    <label class="radio-container">
                        <input type="radio" name="option" value="option1">
                        <span class="checkmark"></span>
                        Show all the manga with all the selected genres
                    </label>

                    <label class="radio-container">
                        <input type="radio" name="option" value="option2">
                        <span class="checkmark"></span>
                        Show all the manga with at least one selected genre
                    </label>
                </div>

            </div>


            <div class="genre-filtering">
                <p class="genre-title">Type</p>
                <div class="two-genres">
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Manga</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Manhwa</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Light Novel</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Novel</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Doujinshi</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Manhua</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty"  ></span>
                        </label>
                        <p>One Shot</p>
                    </div>
                </div>
            </div>


            <div class="genre-filtering">
                <p class="genre-title">Publishing Status</p>
                <div class="two-genres">
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Currently Publishing</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Discontinued</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Finished</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>On Hiatus</p>
                    </div>
                </div>
            </div>

            <div class="genre-filtering">
                <p class="genre-title">Year of Publication</p>
                <div class="year-filtering">
                    <label for="startYear"></label>
                    <input type="number" id="startYear" placeholder="Enter start year">

                    <label for="endYear"></label>
                    <input type="number" id="endYear" placeholder="Enter end year">
                </div>
            </div>

            <div class="detailed-search-button">
                <a href="#" class="arama"><i class="fa-solid fa-magnifying-glass"></i></a>
            </div>
        </div>


    </div>


    <div class="filtering" >
        <div class="search-title-div">
            <p class="search-title">Search Anime </p>
        </div>

        <div class="manga-filter">
            <input type="text" placeholder="Name">
            <a href="#" class="arama"><i class="fa-solid fa-magnifying-glass"></i></a>
        </div>
        <button onclick="myFunction2()" class="more-filtering">See Detailed Filtering</button>
        <div id="myDIV2">

            <div class="genre-filtering">
                <p class="genre-title">Type</p>
                <div class="two-genres">
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Movie</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Ona</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>TV</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Ova</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Special</p>
                    </div>
                </div>
            </div>


            <div class="genre-filtering">
                <p class="genre-title">Publishing Status</p>
                <div class="two-genres">
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Upcoming</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Finished</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox">
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Unknown</p>
                    </div>
                    <div class="genre">
                        <label class="container">
                            <input type="checkbox" name="checkbox" class="genre-checkbox" >
                            <span class="checkmark empty" ></span>
                        </label>
                        <p>Ongoing</p>
                    </div>
                </div>
            </div>


            <div class="genre-filtering">
                <p class="genre-title">Year of Publication</p>
                <div class="year-filtering">
                    <div class="custom-select" style="width:200px;">
                        <select>
                            <option value="0">Start Season</option>
                            <option value="1">Summer</option>
                            <option value="2">Fall</option>
                            <option value="2">Winter</option>
                            <option value="2">Spring</option>
                        </select>
                    </div>

                    <label for="startYear"></label>
                    <input type="number" id="startYear" placeholder="Enter start year">

                    <div class="custom-select" style="width:200px;">
                        <select>
                            <option value="0">End Season</option>
                            <option value="1">Summer</option>
                            <option value="2">Fall</option>
                            <option value="2">Winter</option>
                            <option value="2">Spring</option>
                        </select>
                    </div>

                    <label for="endYear"></label>
                    <input type="number" id="endYear" placeholder="Enter end year">
                </div>
            </div>
            <div class="detailed-search-button">
                <a href="#" class="arama"><i class="fa-solid fa-magnifying-glass"></i></a>
            </div>
        </div>

    </div>






</section>


<div class="suggestions">

    <div class="suggestions-div" >
        <p class="suggestion-title">Manga Suggestions</p>
        <swiper-container class="mySwiper" pagination="true" pagination-clickable="true" space-between="30"
                          slides-per-view="4">
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/1/157897l.jpg"></div>
                    <a href="#"><p>Berserk</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg"></div>
                    <a href="#"><p>One Piece</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/1/259070l.jpg"></div>
                    <a href="#"><p>Vagabond</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/3/179882l.jpg"></div>
                    <a href="#"><p>JoJo no Kimyou na Bouken Part 7: Steel Ball Run</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/3/266834l.jpg"></div>
                    <a href="#"><p>Oyasumi Punpun</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/3/243675l.jpg"></div>
                    <a href="#"><p>Fullmetal Alchemist</p></a>
                </div>
            </swiper-slide>

        </swiper-container>
    </div>








    <div class="suggestions-div">
        <p class="suggestion-title">Anime Suggestions</p>
        <swiper-container class="mySwiper" pagination="true" pagination-clickable="true" space-between="30"
                          slides-per-view="4">
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/anime/1948/136272.jpg"></div>
                    <a href="#"><p>Ai wo Taberu</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg"></div>
                    <a href="#"><p>!NVADE SHOW!</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/anime/1671/127574.jpg"></div>
                    <a href="#"><p>Bungaku Shoujo" Kyou no Oyatsu: Hatsukoi</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/anime/6/26770.jpg"></div>
                    <a href="#"><p>Bungaku Shoujo" Memoire</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/anime/8/81162.jpg"></div>
                    <a href="#"><p>Bungaku Shoujo" Movie</p></a>
                </div>
            </swiper-slide>
            <swiper-slide>
                <div class="slider-item">
                    <div class="slider-icons">
                        <a href="#"><i class="fa-solid fa-plus"></i></a>
                        <a href="#"><i class="fa-regular fa-heart"></i></a>
                    </div>
                    <div class="slider-img"><img src="https://cdn.myanimelist.net/images/anime/1168/93236.jpg"></div>
                    <a href="#"><p>Calpis" Hakkou Monogatari</p></a>
                </div>
            </swiper-slide>

        </swiper-container>
    </div>


</div>


<section class="friend-activity">
    <p class="activity-title">Friends Activity</p>
    <div class="one-line">
        <div class="activity-label">
            <img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg">
            <div class="texts">
                <div class="texts-who">
                    <div class="user-name">
                        <img src="images/user%20icon%20-%20Kopya%20-%20Kopya.png">
                        <p>Crystal</p>
                    </div>
                    <p class="time">2 hours ago</p>
                </div>
                <p class="activity-info">Made comment on One Piece </p>
            </div>
        </div>

        <div class="activity-label">
            <img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg">
            <div class="texts">
                <div class="texts-who">
                    <div class="user-name">
                        <img src="images/user%20icon%20-%20Kopya%20-%20Kopya.png">
                        <p>Crystal</p>
                    </div>
                    <p class="time">2 hours ago</p>
                </div>
                <p class="activity-info">Made comment on One Piece </p>
            </div>
        </div>


    </div>



    <div class="one-line">
        <div class="activity-label">
            <img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg">
            <div class="texts">
                <div class="texts-who">
                    <div class="user-name">
                        <img src="images/user%20icon%20-%20Kopya%20-%20Kopya.png">
                        <p>Crystal</p>
                    </div>
                    <p class="time">2 hours ago</p>
                </div>
                <p class="activity-info">Made comment on One Piece </p>
            </div>
        </div>

        <div class="activity-label">
            <img src="https://cdn.myanimelist.net/images/manga/2/253146l.jpg">
            <div class="texts">
                <div class="texts-who">
                    <div class="user-name">
                        <img src="images/user%20icon%20-%20Kopya%20-%20Kopya.png">
                        <p>Crystal</p>
                    </div>
                    <p class="time">2 hours ago</p>
                </div>
                <p class="activity-info">Made comment on One Piece </p>
            </div>
        </div>


    </div>


</section>


<script src="https://cdn.jsdelivr.net/npm/swiper@11/swiper-element-bundle.min.js"></script><script src="js/index2.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        // Get all genre containers
        var genreContainers = document.querySelectorAll('.genre');

        // Add click event listeners to each genre container
        genreContainers.forEach(function (container) {
            var greenCheckbox = container.querySelector('.green-checkbox');
            var redCheckbox = container.querySelector('.red-checkbox');

            // Add event listener to green checkbox
            greenCheckbox.addEventListener('change', function () {
                if (greenCheckbox.checked) {
                    // If green checkbox is checked, uncheck red checkbox
                    redCheckbox.checked = false;
                }
            });

            // Add event listener to red checkbox
            redCheckbox.addEventListener('change', function () {
                if (redCheckbox.checked) {
                    // If red checkbox is checked, uncheck green checkbox
                    greenCheckbox.checked = false;
                }
            });
        });
    });
</script>

<script>
    const startYearInput = document.getElementById('startYear');
    const endYearInput = document.getElementById('endYear');
    const yearList = document.getElementById('yearList');
    const yearItems = document.querySelectorAll('.year-item');

    function filterByYear() {
        const startYear = parseInt(startYearInput.value);
        const endYear = parseInt(endYearInput.value);

        yearItems.forEach(item => {
            const itemYear = parseInt(item.getAttribute('data-year'));
            if (isNaN(startYear) || isNaN(endYear) || (itemYear >= startYear && itemYear <= endYear)) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    }

    startYearInput.addEventListener('input', filterByYear);
    endYearInput.addEventListener('input', filterByYear);

    // Initial filtering
    filterByYear();
</script>

<script>
    function myFunction() {
        var x = document.getElementById("myDIV");
        if (x.style.display === "none") {
            x.style.display = "block";
        } else {
            x.style.display = "none";
        }
    }
</script>


<script>
    function myFunction2() {
        var x = document.getElementById("myDIV2");
        if (x.style.display === "none") {
            x.style.display = "block";
        } else {
            x.style.display = "none";
        }
    }
</script>

<script>
    var x, i, j, l, ll, selElmnt, a, b, c;
    /*look for any elements with the class "custom-select":*/
    x = document.getElementsByClassName("custom-select");
    l = x.length;
    for (i = 0; i < l; i++) {
        selElmnt = x[i].getElementsByTagName("select")[0];
        ll = selElmnt.length;
        /*for each element, create a new DIV that will act as the selected item:*/
        a = document.createElement("DIV");
        a.setAttribute("class", "select-selected");
        a.innerHTML = selElmnt.options[selElmnt.selectedIndex].innerHTML;
        x[i].appendChild(a);
        /*for each element, create a new DIV that will contain the option list:*/
        b = document.createElement("DIV");
        b.setAttribute("class", "select-items select-hide");
        for (j = 1; j < ll; j++) {
            /*for each option in the original select element,
            create a new DIV that will act as an option item:*/
            c = document.createElement("DIV");
            c.innerHTML = selElmnt.options[j].innerHTML;
            c.addEventListener("click", function(e) {
                /*when an item is clicked, update the original select box,
                and the selected item:*/
                var y, i, k, s, h, sl, yl;
                s = this.parentNode.parentNode.getElementsByTagName("select")[0];
                sl = s.length;
                h = this.parentNode.previousSibling;
                for (i = 0; i < sl; i++) {
                    if (s.options[i].innerHTML == this.innerHTML) {
                        s.selectedIndex = i;
                        h.innerHTML = this.innerHTML;
                        y = this.parentNode.getElementsByClassName("same-as-selected");
                        yl = y.length;
                        for (k = 0; k < yl; k++) {
                            y[k].removeAttribute("class");
                        }
                        this.setAttribute("class", "same-as-selected");
                        break;
                    }
                }
                h.click();
            });
            b.appendChild(c);
        }
        x[i].appendChild(b);
        a.addEventListener("click", function(e) {
            /*when the select box is clicked, close any other select boxes,
            and open/close the current select box:*/
            e.stopPropagation();
            closeAllSelect(this);
            this.nextSibling.classList.toggle("select-hide");
            this.classList.toggle("select-arrow-active");
        });
    }
    function closeAllSelect(elmnt) {
        /*a function that will close all select boxes in the document,
        except the current select box:*/
        var x, y, i, xl, yl, arrNo = [];
        x = document.getElementsByClassName("select-items");
        y = document.getElementsByClassName("select-selected");
        xl = x.length;
        yl = y.length;
        for (i = 0; i < yl; i++) {
            if (elmnt == y[i]) {
                arrNo.push(i)
            } else {
                y[i].classList.remove("select-arrow-active");
            }
        }
        for (i = 0; i < xl; i++) {
            if (arrNo.indexOf(i)) {
                x[i].classList.add("select-hide");
            }
        }
    }
    /*if the user clicks anywhere outside the select box,
    then close all select boxes:*/
    document.addEventListener("click", closeAllSelect);


</script>
</body>

</html>
