<%@ page import="it.unipi.lsmsd.fnf.model.registeredUser.User" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.SecurityUtils" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="it.unipi.lsmsd.fnf.model.PersonalList" %>
<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 18.01.2024
  Time: 01:03
  To change this template use File | Settings | File Templates.
--%>
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
    <link rel="stylesheet" href="css/userProfilePage.css"/>
    <title>Profile Page</title>
</head>
<body>

<%
    User authUser = SecurityUtils.getAuthenticatedUser(request);
%>
<nav>
    <a href="#"><img src="images/logo-with-initial.png" alt="logo" /></a>
    <h1>Profile Page</h1>
    <div class="nav-items">
        <a href="main--registered-user.jsp" class="anime">Anime</a>
        <a href="main--registered-user.jsp" class="manga">Manga</a>
        <a id="logout">Logout</a>
        <a href="#" class="small-pic"><img alt="profile bar" src="images/account-icon.png"> <i class="fa-solid fa-chevron-down" style="color: #000000"> </i></a>
    </div>
</nav>

<section class="user-info">
    <%
        String username = authUser.getUsername();
        String description = authUser.getDescription();
        String gender = authUser.getGender();
        String location = authUser.getLocation();
        LocalDate birthday = authUser.getBirthday();
        String picture = authUser.getProfilePicUrl();
    %>

    <div class="profile" id="profile">
        <div class="profile-img">
            <img alt="profile image" src="images/user-icon.png">
        </div>
        <button class="edit-prof" id="enable-edit" onclick="enableEdit()">Edit Profile</button>
        <span class="edit-prof" id="edit-prof-options" style="display: none">
            <form class="edit-actions" id="edit-prof-form" method="post" action="${pageContext.request.contextPath}/profile" style="display: flex">
                <button class="edit-actions" id="confirm-button" onclick="handleConfirm()" name="action" value="update-info">Confirm</button>
                <div class="vertical-line"></div>
                <button class="edit-actions" id="cancel-button" onclick="handleCancel()">Cancel</button>
            </form>
        </span>
    </div>

    <div id="user-info" class="texts">
        <h1 class="info">Username:
            <p id="username" class="editable" contenteditable="false"><%= authUser.getUsername()%></p>
            <span class="error" id="username-error" style="display: none"></span>
        </h1>
        <h1 class="info">Description:

            <% if (description != null && !description.isEmpty()) { %>
            <p id="description" class="editable" contenteditable="false"> <%= authUser.getDescription()%></p>
            <% } else { %>
            <p id="description" class="editable" contenteditable="false">No description</p>
            <% } %>
        </h1>
        <h1 class="info">Email: <p id="email"><%= authUser.getEmail()%></p></h1>
        <h1 class="info">Gender:
            <div>
                <% if (gender.equals("Prefer not to say")) { %>
                <p id="gender" class="editable" contenteditable="false"><%= authUser.getGender()%></p>
                <% } else { %>
                <p id="gender" class="editable" contenteditable="false"> </p>
                <% } %>
                <div class="dropdown-content" id="genderDropdown"></div>

            </div>
        </h1>
        <h1 class="info">Birthday:
            <p id="birthday" class="editable"><span id="birthday-year"><%= birthday.getYear()%></span> - <span id="birthday-month"><%= String.format("%02d", birthday.getMonthValue())%></span> - <span id="birthday-day"><%= String.format("%02d", birthday.getDayOfMonth())%></span></p>
            <span class="error" id="date-error" style="display: none">the date</span>
        </h1>
        <h1 class="info">Country:
            <div>
                <p id="country" class="editable" contenteditable="false"><%= authUser.getLocation() %></p>
                <div class="dropdown-content" id="countryDropdown"></div>
            </div>
            <span class="error" id="country-error" style="display: none">the date</span>
        </h1>
        <h1 class="info">Joined On: <p id="joined-on"><%= authUser.getJoinedDate()%></p></h1>
    </div>
</section>

<section class="lists-section">
    <h1>My Lists</h1>
    <div class="lists">
        <div>
            <div class="name">
                <p>FRA SEI STUPENDO</p>
            </div>
            <div class="list-items">
            </div>
        </div>
    </div>
</section>

<script src="js/profile.js">
</script>

</body>

</html>
