<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 15:46
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="it.unipi.lsmsd.fnf.model.enums.Gender" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="preconnect" href="https://fonts.googleapis.com%22%3E/" crossorigin />
    <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile_test.css">
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/profile_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/country_dropdown.js" defer></script>
    <title>PROFILE</title>
</head>
<body>

    <c:set var="userInfo" value="${requestScope['userInfo']}" />
    <!-- navbar -->
    <nav>
        <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
        <h1>Welcome ${userInfo.getUsername()}</h1>
        <div class="nav-items">
            <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
            <form action="${pageContext.request.contextPath}/auth" method="post">
                <input type="hidden" name="action" value="logout">
                <input type="hidden" name="targetServlet" value="auth">
                <button type="submit" class="logout">Log Out</button>
            </form>
            <a href="#" class="small-pic"><img alt="profile bar" src="${pageContext.request.contextPath}/images/account-icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
        </div>
    </nav>

    <div id="overlay" class="overlay"></div>

    <!-- profile info -->
    <header>
        <div class="container-px">
            <div class="profile-px">

                <div class="profile-image-px">
                    <img src="${userInfo.getProfilePicUrl()}" alt="profile picture">
                </div>

                <div class="profile-user-settings-px">
                    <h1 class="profile-user-name-px">${userInfo.getUsername()}</h1>
                    <button class="btn-px profile-edit-btn-px" onclick="showEditForm()">Edit Profile</button>
                </div>

                <div class="profile-stats-px">
                    <ul>
                        <li id="show-followers">
                            <span class="profile-stat-count-px">
                                ${empty userInfo.getFollowers() ? 0 : userInfo.getFollowers()}
                            </span> followers
                        </li>
                        <li id="show-followings">
                            <span class="profile-stat-count-px">
                                ${empty userInfo.getFollowed() ? 0 : userInfo.getFollowed()}
                            </span> following
                        </li>
                    </ul>
                </div>

                <div class="profile-bio-px">
                    <c:if test="${not empty userInfo.getFullname()}">
                        <p><span class="profile-real-name-px">${userInfo.getFullname()}</span></p>
                    </c:if>
                    <c:if test="${not empty userInfo.getDescription()}">
                        <p>${userInfo.getDescription()}</p>
                    </c:if>
                    <c:if test="${userInfo.getGender() ne Gender.UNKNOWN}">
                        <p>${userInfo.getGender().toString()}</p>
                    </c:if>
                    <c:if test="${not empty userInfo.getLocation()}">
                        <p>${userInfo.getLocation()}</p>
                    </c:if>
                    <c:if test="${not empty userInfo.getBirthday()}">
                        <p>${userInfo.getBirthday()}</p>
                    </c:if>
                </div>
            </div>
        </div>
    </header>

    <div id="editPopup" class="edit-container myAlert">
        <div class="row myAlertBody">
            <div class="col-xl-4">
                <!-- Profile picture card-->
                <div class="card mb-xl-0">
                    <div class="card-header">Profile Picture</div>
                    <div class="card-body text-center">
                        <!-- Profile picture image-->
                        <img class="img-account-profile" src="${userInfo.getProfilePicUrl()}" alt="">
                        <!-- Profile picture help block-->
                        <div class="edit-label">JPG or PNG no larger than 5 MB</div>
                        <!-- Profile picture upload button-->
                        <button class="btn btn-primary" type="button">Upload new image</button>
                    </div>
                </div>
            </div>
            <div class="col-xl-8">
                <!-- Account details card-->
                <div class="card">
                    <div class="card-header">Account Details</div>
                    <div class="card-body">
                        <form id="edit-profile-form" method="post" action="${pageContext.request.contextPath}/profile" autocomplete="off">
                            <input type="hidden" name="action" value="edit-profile">
                            <input type="hidden" name="picture" value=${userInfo.getProfilePicUrl()}>
                            <!-- Form Group (username)-->
                            <div class="gx-3">
                                <label class="edit-label" for="username">Username (how your name will appear to other users on the site)</label>
                                <input class="form-control" id="username" name="username" type="text" placeholder="Enter your username" value="${userInfo.getUsername()}" oninput="validateUsername()">
                                <span id="username-error" style="color: red"></span>
                            </div>
                            <!-- Form Row-->
                            <div class="row">
                                <!-- Form Group (Email)-->
                                <div class="col-md-6 gx-3">
                                    <label class="edit-label" for="email">Email (non-editable)</label>
                                    <input class="form-control" id="email" name="email" type="email" value="${userInfo.getEmail()}" disabled>
                                </div>
                                <!-- Form Group (Password)-->
                                <div class="col-md-6 gx-3">
                                    <label class="edit-label" for="password">Password (non-editable)</label>
                                    <input class="form-control" id="password" name="password" type="password"  value="${empty userInfo.getPassword() ? "" : userInfo.getPassword()}" disabled>
                                </div>
                            </div>
                            <!-- Form Row-->
                            <div class="row">
                                <!-- Form Group (Fullname)-->
                                <div class="col-md-6 gx-3">
                                    <label class="edit-label" for="fullname">Full Name (optional)</label>
                                    <input class="form-control" id="fullname" name="fullname" type="text" placeholder="Enter your fullname" value="${empty userInfo.getFullname() ? "" : userInfo.getFullname()}">
                                </div>
                                <!-- Form Group (Gender)-->
                                <div class="col-md-6 gx-3">
                                    <label class="edit-label" for="gender">Gender</label>
                                    <select class="form-control" id="gender" name="gender">
                                        <c:set var="selectedGender" value="${userInfo.getGender()}" />
                                        <c:forEach var="gender" items="${Gender.values()}">
                                        <option class="gender-option" value="${gender.name()}" <c:if test="${gender.name() eq selectedGender}">selected</c:if>>${gender.toString()}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <!-- Form Row        -->
                            <div class="row">
                                <!-- Form Group (Country)-->
                                <div class="col-md-6 gx-3">
                                    <label class="edit-label" for="country">Country (optional)</label>
                                    <input class="form-control" id="country" name="country" type="text" placeholder="Enter your country" value="${empty userInfo.getLocation() ? "" : userInfo.getLocation()}" oninput="validateCountry()">
                                    <div class="dropdown-content" id="country-dropdown" onclick="validateCountry()"></div>
                                    <span id="country-error" style="color: red"></span>
                                </div>
                                <!-- Form Group (Birthday)-->
                                <div class="col-md-6 gx-3">
                                    <label class="edit-label" for="birthdate">Birthday (optional)</label>
                                    <input class="form-control" type="date" value="${userInfo.getBirthday()}" id="birthdate" name="birthdate" placeholder="Enter your Birthday">
                                </div>
                            </div>
                            <!-- Form Group (Description)-->
                            <div class="textarea-group gx-3">
                                <label class="edit-label" for="description">Description (optional - maximum 300 characters)</label>
                                <textarea class="form-control textarea" id="description" name="description" rows="3" maxlength="300" placeholder="Enter a description">${empty userInfo.getDescription() ? "" : userInfo.getDescription()}</textarea>
                            </div>
                            <!-- cancel and save changes buttons-->
                            <button class="btn btn-secondary" onclick="hideEditForm()" type="button">Cancel</button>
                            <button class="btn btn-primary" type="button" id="edit-button">Save changes</button>
                            <span id="general-error" style="color: red;"></span>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- followers -->
    <div id="followers" class="myAlert user-list-section">
        <div  id="followersBody" class="myAlertBody">
            <p class="user-list-name">Followers</p>
            <!-- search bar -->
            <div class="d-flex align-items-center">
                <label for="follower-search"></label>
                <input type="text" class="form-control me-2" id="follower-search" required
                       placeholder="Search..." />
            </div>

            <!-- followers list -->
            <div id="followers-list" class="user-list"></div>
        </div>
    </div>

    <!-- followings -->
    <div id="followings" class="myAlert user-list-section">
        <div  id="followingsBody" class="myAlertBody">
            <p for="following-search" class="user-list-name">Following</p>

            <!-- search bar -->
            <div class="d-flex align-items-center">
                <label for="following-search"></label>
                <input type="text" class="form-control me-2" id="following-search" required
                       placeholder="Search..." />
            </div>

            <!-- followers list -->
            <div id="followings-list" class="user-list"></div>
        </div>
    </div>

    <section id="like-and-reviews">

        <div class="button-container">
            <div class="selection-buttons">
                <button id="manga-button" onclick="changeSection(this)">Manga Like</button>
                <button id="anime-button" onclick="changeSection(this)">Anime Like</button>
                <button id="reviews-button" onclick="changeSection(this)">Reviews</button>
            </div>
            <hr class="horizontal-line">
        </div>

        <div id="manga-like">
            <div id="manga-list" class="project-boxes jsGridView">

            </div>
        </div>

        <div id="anime-like">
            <div id="anime-list" class="project-boxes jsGridView">

            </div>
        </div>

        <div id="reviews">
            <div id="reviews-list" class="review-boxes">

            </div>
        </div>
    </section>

<script>
    const contextPath = "${pageContext.request.contextPath}";
    const userId = "${userInfo.getId()}";
    const mangaDefaultImage = "${Constants.DEFAULT_COVER_MANGA}";

    function changeSection(button) {
        const section = button.id.split("-")[0];
        const animeSection = $("#anime-like");
        const mangaSection = $("#manga-like");
        const reviewsSection = $("#reviews");
        if (section === "reviews") {
            animeSection.hide();
            mangaSection.hide();
            reviewsSection.show();
            if (reviewsSection.children().first()) {
                fetchData("getReviews");
            }
        } else {
            reviewsSection.hide();
            if (section === "anime") {
                animeSection.show();
                mangaSection.hide();
                if (animeSection.children().first()) {
                    fetchData("getAnimeLikes");
                }
            } else {
                mangaSection.show();
                animeSection.hide();
                if (mangaSection.children().first()) {
                    fetchData("getMangaLikes");
                }
            }
        }
    }

    function fetchData(action, page = 0) {
        const inputData = {
            action: action,
            userId: userId,
            page: page
        };
        $.post(contextPath + "/profile", inputData, function (data) {
            if (action === "getReviews") {
                showReviews(data);
            } else {
                showLikes(data, action);
            }
        }).fail(function (xhr) {
            console.error("Profile data fetch failed: " + xhr.responseText);
        });
    }

    function showLikes(data, action) {
        const likeList = action === "getAnimeLikes" ? $("#anime-list") : $("#manga-list");
        likeList.empty();

        if (data.mediaLikes === undefined) {
            likeList.append($("<p>").text("No likes found"));
            return;
        }

        data.mediaLikes.forEach(media => {
            console.log(media);
            const mediaWrapper = $("<div>").addClass("project-box-wrapper");
            const mediaBox = $("<div>").addClass("project-box");

            const picture = $("<img>").attr("src", media.imageUrl).attr("alt", media.title)
                .addClass("box-image")
                .on("error", function () {
                    defaultMangaCover(this)
                });
            const title = $("<a>").attr("href", contextPath + "/" + (action === "getAnimeLikes" ? "anime" : "manga") + "?mediaId=" + media.id)
                .addClass("box-title")
                .text(media.title);
            mediaBox.append(picture, title);
            mediaWrapper.append(mediaBox);
            likeList.append(mediaWrapper);
        });
    }

    function defaultMangaCover(image) {
        image.onerror = null;
        image.src = mangaDefaultImage;
    }

    function showReviews(data) {
        const reviews = $("#reviews-list")
        reviews.empty();

        if (data.reviews === undefined) {
            reviews.append($("<p>").text("No reviews found"));
            return;
        }

        data.reviews.entries.forEach(review => {
            const type = (review.mediaContent.season === undefined) ? "manga" : "anime";

            const title = $("<a>").attr("href", contextPath + "/" + type + "?mediaId=" + review.mediaContent.id)
                .addClass("review-media-title")
                .text(review.mediaContent.title);
            const rating = $("<p>").addClass("review-rating")
                .text(review.rating === null ? "No rating" : "Rating: " + review.rating);
            const firstRow = $("<div>").addClass("review-row").append(title, rating);

            const comment = $("<p>")
                .addClass("review-comment")
                .text(review.comment === null ? "No comment" :review.comment);

            const date = $("<p>")
                .addClass("review-date")
                .text("Date: " + review.date);

            const reviewBox = $("<div>").addClass("review-box")
                .append(firstRow, comment, date);
            reviews.append(reviewBox);
        });
    }
</script>
</body>
</html>
