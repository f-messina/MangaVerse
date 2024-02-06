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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile_test.css">
    <script src="${pageContext.request.contextPath}/js/profile_test.js" defer></script>
    <title>PROFILE</title>
</head>

<body>
<section class="profile">
    <div class="profile-img">
        <img alt="profile image" src="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getProfilePicUrl()}">
    </div>
    <div id="user-info" class="texts">
        <form id="profile-form" method="post" action="${pageContext.request.contextPath}/profile" autocomplete="off">
            <input type="hidden" name="action" value="update-info">
            <div class="form-group">
                <label for="username"><i class="zmdi zmdi-account material-icons-name"></i></label>
                <input type="text" class="editable" name="username" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}" id="username" placeholder="Username" oninput="validateUsername()" required disabled/>
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
                <input type="text" name="country" class="editable" id="country" placeholder="Country (Optional)" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getLocation()}" oninput="validateCountry()" disabled/>
                <div class="dropdown-content" id="country-dropdown" onclick="validateCountry()"></div>
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
<section class="lists">
    <h1>Lists</h1>
    <form method="post" action="${pageContext.request.contextPath}/profile">
        <input type="hidden" name="action" value="add-list">
        <label for="listName">Add a new list</label>
        <input type="text" name="listName" id="listName" placeholder="List Name" required/>
        <button type="submit">Add List</button>
    </form>
    <c:set var="lists" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getLists()}" />
    <c:choose>
        <c:when test="${not empty lists}">
            <c:forEach var="list" items="${lists}">
                <div class="list">
                    <div style="display: flex; align-items: center;">
                        <h2>
                            <c:out value="${list.getName()}" />
                        </h2>

                        <form method="post" action="${pageContext.request.contextPath}/profile">
                            <input type="hidden" name="action" value="delete-list">
                            <input type="hidden" name="listIdToRemove" value="${list.getId()}">
                            <button type="submit">Remove</button>
                        </form>
                    </div>
                    <ul>
                        <c:if test="${not empty list.getManga()}">
                            <c:forEach var="manga" items="${list.getManga()}">
                                <li>
                                    <form method="post" action="${pageContext.request.contextPath}/profile">
                                        <input type="hidden" name="action" value="delete-item">
                                        <input type="hidden" name="listId" value="${list.getId()}">
                                        <input type="hidden" name="mangaIdToRemove" value="${manga.getId()}">

                                        <div style="display: flex; align-items: center;">
                                            <div style="margin-right: 10px;">
                                                <a><c:out value="${manga.getTitle()}"/></a>
                                            </div>

                                            <div style="margin-right: 10px;">
                                                <button type="submit">Remove</button>
                                            </div>
                                        </div>
                                        <br/>
                                        <img src="${manga.getImageUrl()}" alt="${fn:escapeXml(manga.getTitle())} image" />
                                        <br/><br/><br/>
                                    </form>
                                </li>
                            </c:forEach>
                        </c:if>
                        <c:if test="${not empty list.getAnime()}">
                            <c:forEach var="anime" items="${list.getAnime()}">
                                <li>
                                    <form method="post" action="${pageContext.request.contextPath}/profile">
                                        <input type="hidden" name="action" value="delete-item">
                                        <input type="hidden" name="listId" value="${list.getId()}">
                                        <input type="hidden" name="animeIdToRemove" value="${anime.getId()}">

                                        <div style="display: flex; align-items: center;">
                                            <div style="margin-right: 10px;">
                                                <a><c:out value="${anime.getTitle()}"/></a>
                                            </div>

                                            <div style="margin-right: 10px;">
                                                <button type="submit">Remove</button>
                                            </div>
                                        </div>
                                        <br/>
                                        <img src="${anime.getImageUrl()}" alt="${fn:escapeXml(anime.getTitle())} image" />
                                        <br/><br/><br/>
                                    </form>
                                </li>
                            </c:forEach>
                        </c:if>
                        <c:if test="${empty list.getManga() and empty list.getAnime()}">
                            <li>No items found</li>
                        </c:if>
                    </ul>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <h2>No lists found</h2>
        </c:otherwise>
    </c:choose>
</section>
<section class="reviews">
    <h1>Reviews</h1>
    <c:set var="reviews" value="${sessionScope[Constants.AUTHENTICATED_USER_KEY].getReviews()}" />
    <c:choose>
        <c:when test="${not empty reviews}">
            <c:forEach var="review" items="${reviews}">
                <div class="review">
                    <h2>
                        <c:out value="${review.getMediaContent().getTitle()}" />
                    </h2>
                    <p>
                        <c:out value="${review.getDate().toString()}" />
                        <br/>
                        <c:choose>
                            <c:when test="${not empty review.comment}">
                                <c:out value="${review.comment}" />
                            </c:when>
                            <c:otherwise>
                                No comment.
                            </c:otherwise>
                        </c:choose>
                        <br/>
                        <c:choose>
                            <c:when test="${not empty review.rating}">
                                <c:out value="${review.rating}" />
                            </c:when>
                            <c:otherwise>
                                No rating.
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <h2>No reviews found</h2>
        </c:otherwise>
    </c:choose>
</section>

</body>
</html>
