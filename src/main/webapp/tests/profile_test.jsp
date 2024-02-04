<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page import="it.unipi.lsmsd.fnf.model.enums.Gender" %><%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 15:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="../css/profile_test.css">
    <script src="../js/profile_test.js" defer></script>
    <title>PROFILE</title>
</head>
<body>
<section class="profile">
    <div class="profile-img">
        <img alt="profile image" src="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}">
    </div>
    <div id="user-info" class="texts">
        <form id="profile-form" method="post" action="<%= request.getContextPath() %>/profile">
            <input type="hidden" name="action" value="update-info">
            <div class="form-group">
                <label for="username"><i class="zmdi zmdi-account material-icons-name"></i></label>
                <input type="text" class="editable" name="username" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}" id="username" placeholder="Username" required disabled/>
                <span id="username-error" style="color: red"><c:out value="${requestScope['usernameError']}" /></span>
            </div>
            <div class="form-group">
                <label for="email"><i class="zmdi zmdi-email"></i></label>
                <input type="email" name="email" id="email" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getEmail()}" placeholder="Your Email" required disabled/>
            </div>
            <div class="form-group">
                <label for="fullname"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="text" class="editable" name="fullname" id="fullname" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getFullname()}" placeholder="Full Name (Optional)" disabled/>
            </div>
            <div class="form-group">
                <label for="description"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="text" class="editable" name="description" id="description" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getDescription()}" placeholder="Description (Optional)" disabled/>
            </div>
            <div class="form-group">
                <label for="gender"><i class="zmdi zmdi-lock-outline"></i></label>
                <select id="gender" class="editable" name="gender" disabled>
                    <c:set var="selectedGender" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getGender()}" />
                    <c:forEach var="gender" items="${Gender.values()}">
                        <option value="${gender.name()}" <c:if test="${gender.name() eq selectedGender}">selected</c:if>>${gender.toString()}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label for="birthdate"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="date" class="editable" name="birthdate" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getBirthday()}" id="birthdate" placeholder="Birthdate" disabled/>
            </div>
            <div class="form-group">
                <label for="country"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="text" name="country" class="editable" id="country" placeholder="Country (Optional)" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getLocation()}" oninput="ValidateForm()" disabled/>
                <div class="dropdown-content" id="country-dropdown" onclick="ValidateForm()"></div>
                <span id="country-error" style="color: red"></span>
            </div>
            <div class="form-group">
                <label for="joined-date"><i class="zmdi zmdi-lock"></i></label>
                <input type="text" name="joined-date" id="joined-date" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getJoinedDate()}" placeholder="Joined Date" disabled/>
            </div>
            <div class="form-group">
                <button type="submit" id="edit-button" class="submit-btn">Modify</button>
                <button type="button" id="confirm-button" class="submit-btn" style="display: none">Confirm</button>
            </div>
        </form>
    </div>
</section>
</body>
</html>
