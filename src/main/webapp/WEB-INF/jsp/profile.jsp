<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="it.unipi.lsmsd.fnf.model.enums.Gender" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user_list.css">

    <title>PROFILE</title>
</head>
<body>
    <c:set var="isLogged" value="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}" />
    <c:set var="userInfo" value="${requestScope['userInfo']}" />
    <c:set var="isLoggedPageOwner" value="${isLogged and sessionScope[Constants.AUTHENTICATED_USER_KEY].getId() eq userInfo.id}" />
    <c:set var="isFollowed" value="${requestScope['isFollowed']}" />

    <!-- navbar -->
    <nav>
        <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
        <c:if test="${isLogged}">
            <h1 id="welcome-message">Welcome ${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}</h1>
        </c:if>
        <div class="nav-items">
            <div class="search-box">
                <button id="user-search-button" class="btn-search"><i class="fa fa-search"></i></button>
                <label for="user-search"></label>
                <input id="user-search" type="text" class="input-search" placeholder="Search user...">
                <div id="user-search-section" class="user-list-section users-results">
                    <div id="user-search-results"></div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
            <c:choose>
                <c:when test="${isLogged}">
                    <div class="logout" onclick="logout('auth')">
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="sign-out-alt" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="logout-icon"><path data-v-04b245e6="" fill="currentColor" d="M497 273L329 441c-15 15-41 4.5-41-17v-96H152c-13.3 0-24-10.7-24-24v-96c0-13.3 10.7-24 24-24h136V88c0-21.4 25.9-32 41-17l168 168c9.3 9.4 9.3 24.6 0 34zM192 436v-40c0-6.6-5.4-12-12-12H96c-17.7 0-32-14.3-32-32V160c0-17.7 14.3-32 32-32h84c6.6 0 12-5.4 12-12V76c0-6.6-5.4-12-12-12H96c-53 0-96 43-96 96v192c0 53 43 96 96 96h84c6.6 0 12-5.4 12-12z" class=""></path></svg>
                        <div class="logout-text">Log Out</div>
                    </div>
                    <a href="${pageContext.request.contextPath}/profile" class="small-pic">
                        <img id="navbar-profile-picture" alt="profile bar" src="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}">
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/auth">Log In</a>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>

    <div id="overlay" class="overlay"></div>

    <section class="profile">
        <!-- profile info -->
        <div class="container-px">
            <div class="profile-px">

                <div class="profile-image-px">
                    <img id="profile-picture-display" src="${userInfo.getProfilePicUrl()}" alt="profile picture">
                </div>

                <div class="profile-user-settings-px">
                    <h1 id="username-displayed" class="profile-user-name-px">${userInfo.getUsername()}</h1>
                    <c:if test="${isLogged}">
                        <c:choose>
                            <c:when test="${isLoggedPageOwner}">
                                <button class="btn-px profile-edit-btn-px" onclick="showEditForm()">Edit Profile</button>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${isFollowed}">
                                        <button class="btn-px profile-edit-btn-px" onclick="unfollow()">Followed</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn-px profile-edit-btn-px" onclick="follow()">Follow</button>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
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

                <div id="bio" class="profile-bio-px">
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

        <c:if test="${isLogged and isLoggedPageOwner}">
        <div id="editPopup" class="edit-container myAlert">
            <div class="row myAlertBody">
                <div class="col-xl-4">
                    <!-- Profile picture card-->
                    <div class="card mb-xl-0">
                        <div class="card-header">Profile Picture</div>
                        <div class="card-body text-center">
                            <!-- Profile picture image-->
                            <img id="profile-picture" class="img-account-profile" src="${userInfo.getProfilePicUrl()}" alt="">
                            <!-- Profile picture help block-->
                            <div class="gx-3">
                                <label class="edit-label align-left" for="profile-picture-url">Profile Picture URL</label>
                                <input class="form-control" id="profile-picture-url" name="profilePicture" type="text" placeholder="Enter the picture URL (Optional)">
                                <p class="edit-label check-value" id="check-image-message"></p>
                                <button class="btn btn-primary" type="button" onclick="validatePictureUrl()">Check URL</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-xl-8">
                    <!-- Account details card-->
                    <div class="card">
                        <div class="card-header">Account Details</div>
                        <div class="card-body">
                            <div id="edit-profile-form">
                                <input type="hidden" name="action" value="edit-profile">
                                <input type="hidden" id="picture" name="picture" value="${userInfo.getProfilePicUrl()}">
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
                                    <textarea class="form-control textarea-ph-font" id="description" name="description" rows="3" maxlength="300" placeholder="Enter a description">${empty userInfo.getDescription() ? "" : userInfo.getDescription()}</textarea>
                                </div>
                                <!-- cancel and save changes buttons-->
                                <button class="btn btn-secondary" onclick="hideEditForm()" type="button">Cancel</button>
                                <button class="btn btn-primary" type="button" id="edit-button">Save changes</button>
                                <span id="general-error" style="color: red;"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        </c:if>

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
                <div id="followers-list"></div>
            </div>
        </div>

        <!-- followings -->
        <div id="followings" class="myAlert user-list-section">
            <div id="followingsBody" class="myAlertBody">
                <p class="user-list-name">Following</p>

                <!-- search bar -->
                <div class="d-flex align-items-center">
                    <label for="following-search"></label>
                    <input type="text" class="form-control me-2" id="following-search" required
                           placeholder="Search..." />
                </div>

                <!-- followers list -->
                <div id="followings-list"></div>
            </div>
        </div>
    </section>

    <c:if test="${isLogged and isLoggedPageOwner}">
        <section class="app-rating-container">
            <div class="app-rating-form">
                <br />
                <c:choose>
                    <c:when test="${empty userInfo.appRating}">
                        <h1 id="no-app-rating-message">Would you like to add your score to the website?</h1>
                    </c:when>
                    <c:otherwise>
                        <h1 id="app-rating-title">App rating:</h1>
                    </c:otherwise>
                </c:choose>
                <div class="stars">
                    <c:forEach var="i" begin="1" end="5">
                        <c:choose>
                            <c:when test="${empty userInfo.appRating or i > userInfo.appRating}">
                                <span onclick="setAppRating(${i})" class="star">★</span>
                            </c:when>
                            <c:otherwise>
                                <span onclick="setAppRating(${i})" class="star checked">★</span>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
            </div>
        </section>
    </c:if>

    <section class="profile-content">
        <div class="button-container">
            <div class="selection-buttons">
                <button id="manga-button" onclick="changeSection(this)">Manga Like</button>
                <button id="anime-button" onclick="changeSection(this)">Anime Like</button>
                <c:if test="${isLogged and sessionScope[Constants.AUTHENTICATED_USER_KEY].getId() eq userInfo.id}">
                    <button id="reviews-button" onclick="changeSection(this)">Reviews</button>
                </c:if>
            </div>
            <hr class="horizontal-line">
        </div>

        <div id="manga-like">
            <c:if test="${isLogged and isLoggedPageOwner}">
                <div class="suggestions-lists">
                    <p class="suggestion-title">Suggested Manga By Location</p>
                    <div id="manga-suggested-by-location" class="project-boxes jsGridView"></div>

                    <p class="suggestion-title">Suggested Manga By Birthday</p>
                    <div id="manga-suggested-by-birthday" class="project-boxes jsGridView"></div>
                </div>
            </c:if>

                <!--check if the page is the registered user(suggestions only if your are in your profile) check will be done in the js
                take the css of manga-list -->
            <div class="container">
                <ul class="page manga-pagination">
                </ul>
            </div>
            <div id="manga-list" class="project-boxes jsGridView"></div>
            <div class="container">
                <ul class="page manga-pagination">
                </ul>
            </div>
        </div>

        <div id="anime-like">
            <c:if test="${isLogged and isLoggedPageOwner}">
                <div class="suggestions-lists">
                    <p class="suggestion-title">Suggested Anime By Location</p>
                    <div id="anime-suggested-by-location" class="project-boxes jsGridView"></div>

                    <p class="suggestion-title">Suggested Anime By Birthday</p>
                    <div id="anime-suggested-by-birthday" class="project-boxes jsGridView"></div>
                </div>
            </c:if>

                <!--check if the page is the registered user(suggestions only if your are in your profile) check will be done in the js
                take the css of manga-list -->
            <div class="container">
                <ul class="page anime-pagination">
                </ul>
            </div>
            <div id="anime-list" class="project-boxes jsGridView"></div>
            <div class="container">
                <ul class="page anime-pagination">
                </ul>
            </div>
        </div>

        <c:if test="${isLogged and sessionScope[Constants.AUTHENTICATED_USER_KEY].getId() eq userInfo.id}">
            <div id="reviews">
                <div class="container">
                    <ul class="page review-pagination">
                    </ul>
                </div>
                <div id="reviews-list" class="review-boxes"></div>
                <div class="container">
                    <ul class="page review-pagination">
                    </ul>
                </div>
            </div>
        </c:if>
    </section>

    <div class="footer"></div>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/profile.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/country_dropdown.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/navbar.js" defer></script>
    <script>
        const contextPath = "${pageContext.request.contextPath}";
        const userId = "${userInfo.getId()}";
        const mangaDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_MANGA}";
        const animeDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_COVER_ANIME}";
        const userDefaultImage = "${pageContext.request.contextPath}/${Constants.DEFAULT_PROFILE_PICTURE}";
        let profile = {
            username: "${userInfo.getUsername()}",
            fullname: "${empty userInfo.getFullname() ? "" : userInfo.getFullname()}",
            description: "${empty userInfo.getDescription() ? "" : userInfo.getDescription()}",
            country: "${empty userInfo.getLocation() ? "" : userInfo.getLocation()}",
            birthdate: "${empty userInfo.getBirthday() ? "" : userInfo.getBirthday()}",
            picture: "${userInfo.getProfilePicUrl()}",
            gender: "${userInfo.getGender().name()}",
            appRating: parseInt("${empty userInfo.appRating ? "" : userInfo.appRating}"),
            reviewIds: "${empty userInfo.reviewIds ? "" : userInfo.reviewIds}".slice(1, -1).split(", ")
        }
    </script>
</body>
</html>
