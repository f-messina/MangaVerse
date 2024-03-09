<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 8.02.2024
  Time: 15:07
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>MANGA</title>
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
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/media_content_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/logout.js" defer></script>
</head>
<body>
    <div class="nav-bar">
            <nav>
                <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
                <h1>Profile Page</h1>
                <div class="nav-items">
                    <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
                    <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
                    <c:choose>
                        <c:when test="${empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
                            <a href="${pageContext.request.contextPath}/login">Log In</a>
                            <a href="${pageContext.request.contextPath}/signup">Sign Up</a>
                        </c:when>
                        <c:otherwise>
                            <a id="logoutBtn" href="${pageContext.request.contextPath}/manga?mediaId=${requestScope.manga.id}">Logout</a>
                            <a href="${pageContext.request.contextPath}/profile">Profile</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </nav>
    </div>

    <section class="info">
        <div class="mediaContent-info">
            <div class="mediaContent-img">
                <img alt="profile image" src=${empty requestScope.media.imageUrl ? '${pageContext.request.contextPath}/images/logo-with-name.png' : requestScope.media.imageUrl}>
            </div>
            <form id="mediaContent-form" method="post" action="${pageContext.request.contextPath}/manga" autocomplete="off" class="forms">
                <div id="mediaContent-info" class="two-div">
                    <div class="texts">
                        <div class="form-group">
                            <p class="info-name">Title:</p>
                            <div class="info-box"><p id="title"><c:out value="${empty requestScope.media.title ? 'N/A' : requestScope.media.title}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Title Japanese:</p>
                            <div class="info-box"><p id="title-japanese"><c:out value="${empty requestScope.media.titleJapanese ? 'N/A' : requestScope.media.titleJapanese}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Publishing Status:</p>
                            <div class="info-box"><p id="status"><c:out value="${empty requestScope.media.status ? 'N/A' : requestScope.media.status}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Genres:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.genres}">
                                    <div class="info-box"><p>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="genres">
                                    <c:forEach var="genre" items="${requestScope.media.genres}">
                                        <c:out value="${genre}"/>
                                    </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Themes:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.themes}">
                                    <div class="info-box"><p>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="themes">
                                    <c:forEach var="theme" items="${requestScope.media.themes}">
                                        <c:out value="${theme}"/>
                                    </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Demographics:</p>
                            <c:choose>
                                <c:when test="${empty requestScope.media.demographics}">
                                    <div class="info-box"><p id=>N/A</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="info-box"><p id="demographics">
                                    <c:forEach var="demographic" items="${requestScope.media.demographics}">
                                        <c:out value="${demographic}"/>
                                    </c:forEach>
                                    </p></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Authors:</p>
                            <c:forEach var="author" items="${requestScope.media.authors}">
                                Name: <div class="info-box"><p id="authors">Name: <c:out value="${author.name}"/>
                                Role: <c:out value="${author.role}"/></p></div>
                            </c:forEach>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Serialization:</p>
                            <div class="info-box"><p id="serialization"><c:out value="${empty requestScope.media.serializations ? 'N/A' : requestScope.media.serializations}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Start Date:</p>
                            <div class="info-box"><p id="start-date"><c:out value="${empty requestScope.media.startDate ? 'N/A' : requestScope.media.startDate}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">End Date:</p>
                            <div class="info-box"><p id="end-date"><c:out value="${empty requestScope.media.endDate ? 'N/A' : requestScope.media.endDate}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Average Rating:</p>
                            <div class="info-box"><p id="average-rating"><c:out value="${empty requestScope.media.averageRating ? 'N/A' : requestScope.media.averageRating}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Volumes:</p>
                            <div class="info-box"><p id="volumes"><c:out value="${empty requestScope.media.volumes ? 'N/A' : requestScope.media.volumes}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Chapters:</p>
                            <div class="info-box"><p id="chapters"><c:out value="${empty requestScope.media.chapters ? 'N/A' : requestScope.media.chapters}"/></p></div>
                        </div>
                    </div>
                    <div class="texts">
                        <div class="form-group">
                            <p class="info-name">Synopsis:</p>
                            <div class="info-box"><p id="synopsis"><c:out value="${empty requestScope.media.synopsis ? 'N/A' : requestScope.media.synopsis}"/></p></div>
                        </div>
                        <div class="form-group">
                            <p class="info-name">Background:</p>
                            <div class="info-box"><p id="background"><c:out value="${empty requestScope.media.background ? 'N/A' : requestScope.media.background}"/></p></div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </section>
    <section id="reviews">
        <c:if test="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
            <div id="review-form-container" class="review-form">
                <c:set var="found" value="false"/>
                <c:forEach var="review" items="${requestScope.reviews}">
                    <c:if test="${review.user.id == sessionScope[Constants.AUTHENTICATED_USER_KEY].getId()}">
                        <c:set var="found" value="true"/>
                        <button id="enable-modify-review" type="submit">Modify</button>
                        <button id="delete-review" type="submit">Delete</button>
                        <div id="modify-review-container" class="popup-container hidden">
                            <div class="list-popup">
                                <div class="popup-content">
                                    <form id="modify-review-form" method="post" action="${pageContext.request.contextPath}/manga" autocomplete="off" class="forms">
                                        <input type="hidden" name="mediaId" value="${requestScope.media.id}">
                                        <input type="hidden" name="action" value="addReview">
                                        <input type="hidden" name="reviewId" value="${review.id}">
                                        <div class="form-group">
                                            <label>Change the review:</label>
                                            <textarea id="modify-comment" name="comment" rows="4" cols="50" placeholder="Write a comment" >${review.comment}</textarea>
                                            <input id="modify-rating" type="number" name="rating" step="1" min="0" max = "10" placeholder="Rating" value="${review.rating}">
                                        </div>
                                        <button id="submit-modify-review" type="submit">Send</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
                <c:if test="${found == false}">
                    <button id="enable-add-review" type="submit">Send</button>
                    <div id="add-review-container" class="popup-container hidden">
                        <div class="list-popup">
                            <div class="popup-content">
                                <form id="add-review-form" method="post" action="${pageContext.request.contextPath}/manga" autocomplete="off" class="forms">
                                    <input type="hidden" name="mediaId" value="${requestScope.media.id}">
                                    <input type="hidden" name="action" value="addReview">
                                    <div class="form-group">
                                        <label>Change the review:</label>
                                        <textarea id="add-comment" name="comment" rows="4" cols="50" placeholder="Write a comment"></textarea>
                                        <input id="add-rating" type="number" name="rating" step="1" min="0" max = "10" placeholder="Rating">
                                    </div>
                                    <button id="submit-add-review" type="submit">Send</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </c:if>

        <div class="review-list">
            <h2>Reviews</h2>
            <c:choose>
                <c:when test="${not empty requestScope.reviews}">
                    <c:forEach var="review" items="${requestScope.reviews}">
                        <div class="review">
                            <p>${review.user.username}</p>
                            <img src="${review.user.profilePicUrl}" alt="profile image" />
                            <p>${review.date}</p>
                            <p>${review.comment}</p>
                            <p>${review.rating}</p>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="review">
                        <p>No reviews yet</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <script>
        <c:set var="mediaId" value="${requestScope.media.id}"/>
        <c:set var="mediaTitle" value="${requestScope.media.title}"/>
        const servletURI = "${pageContext.request.contextPath}/manga";
        const mediaId = "<c:out value="${mediaId}"/>";
        const mediaTitle = "<c:out value="${mediaTitle}"/>";

        const popupContainer = $('.popup-container');
        popupContainer.on('click', (e) => {
            if (e.target === popupContainer[0]) {
                // Your code to handle the click on the popupContainer
                popupContainer.addClass('hidden');
            }
        });

        const enableAddReview = $('#enable-add-review');
        const addReviewContainer = $('#add-review-container');
        enableAddReview.on('click', () => {
            addReviewContainer.removeClass('hidden');
        });
        const enableModifyReview = $('#enable-modify-review');
        const modifyReviewContainer = $('#modify-review-container');
        enableModifyReview.on('click', () => {
            modifyReviewContainer.removeClass('hidden');
        });

        $(document).ready(function() {
            // Function to submit form asynchronously
            function submitFormAsync(formId) {
                // Get form data
                const formData = $('#' + formId).serialize();

                // Make an AJAX request using $.post
                $.post('${pageContext.request.contextPath}/manga', formData, function(response) {
                    // Handle the success response
                    console.log('Form submitted successfully:', response);
                    // You can update the page or perform other actions here
                    $('.popup-container').addClass('hidden');
                })
                    .fail(function(error) {
                        // Handle the error response
                        console.error('Error submitting form:', error);
                    });
            }

            // Event handler for modify-review-form
            $('#modify-review-form').on('submit', function(event) {
                event.preventDefault();
                submitFormAsync('modify-review-form');
            });

            // Event handler for add-review-form
            $('#add-review-form').on('submit', function(event) {
                event.preventDefault();
                submitFormAsync('add-review-form');
            });

            // Add other event handlers as needed for different forms
        });
    </script>
</body>
</html>
