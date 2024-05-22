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
                        <li>
                            <span class="profile-stat-count-px">
                                ${empty userInfo.getFollowers() ? 0 : userInfo.getFollowers()}
                            </span> followers
                        </li>
                        <li>
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
                    <c:if test="${not empty userInfo.getGender()}">
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
    <div id="followers" class="myAlert">
        <div class="myAlertBody">

            <!-- search bar -->
            <div class="d-flex align-items-center">
                <label for="follower-name" class="form-label">Followers</label>
                <input type="text" class="form-control me-2" id="follower-name" required
                       placeholder="Search..." />
            </div>

            <!-- followers list -->
            <div class="followers-list"></div>
        </div>
    </div>
    <section id="like-and-reviews">
        <div class="button-container">
            <button id="anime-button" onclick="fetchData('getAnimeLikes')">Anime Like</button>
            <button id="manga-button" onclick="fetchData('getMangaLikes')">Manga Like</button>
            <button id="reviews-button" onclick="fetchData('getReviews')">Reviews</button>
        </div>
        <div id="anime-like">
            //asynchronous data will be loaded here

        </div>

        <div id="manga-like">

        </div>

        <div id="reviews">

        </div>



    </section>
<script>
    const pageContext = "${pageContext.request.contextPath}";
    function fetchData(action) {
        $.post(pageContext + "/profile", {action: action}, function (response) {

        }).fail(function (xhr) {
            console.error(xhr.responseText);
        });
    }

    </script>
</body>
</html>
