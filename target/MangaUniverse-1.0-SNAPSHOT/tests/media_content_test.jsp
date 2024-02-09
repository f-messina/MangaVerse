<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page import="it.unipi.lsmsd.fnf.model.enums.Gender" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 8.02.2024
  Time: 15:07
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Media Content</title>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/media_content_test.css">
    <script src="${pageContext.request.contextPath}/js/media_content_test.js" defer></script>
</head>
<body>

    <div class="nav-bar">
        <c:if test="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
            <nav>
                <a href="#"><img src="../images/logo-with-initial.png" alt="logo" /></a>
                <h1>Profile Page</h1>
                <div class="nav-items">
                    <a href="main--registered-user.jsp" class="anime">Anime</a>
                    <a href="main--registered-user.jsp" class="manga">Manga</a>
                    <a id="logout">Logout</a>
                    <a href="#" class="small-pic"><img alt="profile bar" src="images/account-icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
                </div>
            </nav>
        </c:if>
        <c:if test="${empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
            <nav>
                <a href="#"><img src="../images/logo-with-initial.png" alt="logo" /></a>
                <div class="nav-items">
                    <a href="#" class="anime">Anime</a>
                    <a href="#" class="manga">Manga</a>
                    <a href="#">Sign Up</a>
                    <a href="#">Log In</a>
                </div>
            </nav>
        </c:if>
    </div>

    <div class="info">
        <%--getManga()}--%>
        <section class="mediaContent-info">
            <div class="mediaContent-img">
                <img alt="profile image" src="https://cdn.myanimelist.net/images/anime/1930/122178.jpg">
            </div>
            <form id="mediaContent-form" method="post" action="${pageContext.request.contextPath}/mediaContent" autocomplete="off" class="forms">
                <div id="mediaContent-info" class="two-div">
                        <div class="texts">
                            <div class="form-group">
                                <p class="info-name">Title:</p>
                                <div class="info-box"><p id="title">Berserk<c:out value="${requestScope[manga].getTitle()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Title Japanese:</p>
                                <div class="info-box"><p id="title-japanese">ベルセルク<c:out value="${requestScope[manga].getTitleJapanese()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Publishing Status:</p>
                                <div class="info-box"><p id="status">Currently Publishing<c:out value="${requestScope[manga].getStatus()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Genres:</p>
                                <div class="info-box"><p  id="genres">Action, Adventure, Drama<c:out value="${requestScope[manga].getGenres()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Themes:</p>
                                <div class="info-box"><p  id="themes">Military, Psychological<c:out value="${requestScope[manga].getThemes()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Demographics:</p>
                                <div class="info-box"><p  id="demographics">Seinen<c:out value="${requestScope[manga].getDemographics()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Authors:</p>
                                <div class="info-box"><p id="authors">Kentarou Miura, Studio Gaga<c:out value="${requestScope[manga].getAuthors()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Serialization:</p>
                                <div class="info-box"><p id="serialization">Young Animal<c:out value="${requestScope[manga].getSerialization()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Start Date:</p>
                                <div class="info-box"><p id="start-date">1989-08-25<c:out value="${requestScope[manga].getStartDate()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">End Date:</p>
                                <div class="info-box"><p id="end-date">Continues<c:out value="${requestScope[manga].getEndDate()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Average Rating:</p>
                                <div class="info-box"><p id="average-rating">1.67<c:out value="${requestScope[manga].getAverageRating()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Volumes:</p>
                                <div class="info-box"><p id="volumes">24<c:out value="${requestScope[manga].getVolumes()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Chapter:</p>
                                <div class="info-box"><p id="chapters">96<c:out value="${requestScope[manga].getChapters()}"/></p></div>
                            </div>
                        </div>

                        <div class="texts">
                            <div class="form-group">
                                <p class="info-name">Synopsis:</p>
                                <div class="info-box"><p id="synopsis">Guts, a former mercenary now known as the "Black Swordsman," is out for revenge. After a tumultuous childhood, he finally finds someone he respects and believes he can trust, only to have everything fall apart when this person takes away everything important to Guts for the purpose of fulfilling his own desires. Now marked for death, Guts becomes condemned to a fate in which he is relentlessly pursued by demonic beings.

                                    Setting out on a dreadful quest riddled with misfortune, Guts, armed with a massive sword and monstrous strength, will let nothing stop him, not even death itself, until he is finally able to take the head of the one who stripped him—and his loved one—of their humanity.

                                    [Written by MAL Rewrite]

                                    Included one-shot:
                                    Volume 14: Berserk: The Prototype<c:out value="${requestScope[manga].getSynopsis()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Background:</p>
                                <div class="info-box"><p id="background">Berserk won the Award for Excellence at the sixth installment of Tezuka Osamu Cultural Prize in 2002. The series has over 50 million copies
                                    in print worldwide and has been published in English by Dark Horse since November 4, 2003. It is
                                    also published in Italy, Germany, Spain, France, Brazil, South Korea, Hong Kong, Taiwan, Thailand,
                                    Poland, México and Turkey. In May 2021, the author Kentaro Miura suddenly died at the age of 54.
                                    Chapter 364 of Berserk was published posthumously on September 10, 2021. Miura would often share
                                    details about the series' story with his childhood friend and fellow mangaka Kouji Mori. Berserk
                                    resumed on June 24, 2022, with Studio Gaga handling the art and Kouji Mori's supervision.l
                                    <c:out value="${requestScope[manga].getBackground()}"/></p></div>
                            </div>
                        </div>
                </div>
            </form>
        </section>

            <%--getAnime()}--%>
            <section class="mediaContent-info">
                <div class="mediaContent-img">
                    <img alt="profile image" src="https://cdn.myanimelist.net/images/anime/1948/136272.jpg">
                </div>
                <form id="mediaContent-form" method="post" action="${pageContext.request.contextPath}/mediaContent" autocomplete="off" class="forms">
                    <div id="mediaContent-info" class="two-div">
                        <div class="texts">
                            <div class="form-group">
                                <p class="info-name">Title:</p>
                                <div class="info-box"><p id="title">Ai wo Taberu<c:out value="${requestScope[anime].getTitle()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Type:</p>
                                <div class="info-box"><p id="type">Movie<c:out value="${requestScope[anime].getType()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Episode Count:</p>
                                <div class="info-box"><p id="episode-count">3<c:out value="${requestScope[anime].getEpisodeCount()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Publishing Status:</p>
                                <div class="info-box"><p  id="publishing-status">Finished<c:out value="${requestScope[anime].getPublishingStatus()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Tags:</p>
                                <div class="info-box"><p  id="tags">Fmaily Friendly, Fantasy, Frogs, Kids<c:out value="${requestScope[anime].getTags()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Studio:</p>
                                <div class="info-box"><p id="studio">Production I.G<c:out value="${requestScope[anime].getStudio()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Season:</p>
                                <div class="info-box"><p id="season">Winter<c:out value="${requestScope[anime].getSeason()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Average Rating:</p>
                                <div class="info-box"><p id="average-rating">6.8<c:out value="${requestScope[anime].getAverageRating()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Year:</p>
                                <div class="info-box"><p id="year">2009<c:out value="${requestScope[anime].getYear()}"/></p></div>
                            </div>

                        </div>

                        <div class="texts">
                            <div class="form-group">
                                <p class="info-name">Synopsis:</p>
                                <div class="info-box"><p id="synopsis">Short episode bundled with the limited edition release of "Bungaku Shoujo" Minarai no, Shoushin .<c:out value="${requestScope[anime].getSynopsis()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Producers:</p>
                                <div class="info-box"><p  id="producers">Lantis, Pony Canyon, Enterbrain, Kadokawa Contents Gate, T.O Entertainment<c:out value="${requestScope[anime].getProducers()}"/></p></div>
                            </div>
                            <div class="form-group">
                                <p class="info-name">Related Anime:</p>
                                <div class="info-box"><p id="related-anime">"Bungaku Shoujo" Movie, "Bungaku Shoujo" Memoire<c:out value="${requestScope[anime].getRelatedAnime()}"/></p></div>
                            </div>
                        </div>
                    </div>
                </form>
            </section>
    </div>
</body>
</html>
