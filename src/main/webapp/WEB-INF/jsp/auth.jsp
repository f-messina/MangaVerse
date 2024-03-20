<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 10:02
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="it.unipi.lsmsd.fnf.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth_test.css">
    <script src="${pageContext.request.contextPath}/js/auth_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/country_dropdown.js" defer></script>

    <title>AUTH PAGE</title>
</head>
<body>
<div class="nav-bar">
    <nav>
        <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
        <div class="nav-items">
            <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
        </div>
    </nav>
</div>


<div class="options">
    <section class="sign-up">
        <div class="container mt-5">
            <div class="signup-content">
                <div class="signup-form">
                    <h2 class="form-title">Sign up</h2>
                    <form action="${pageContext.request.contextPath}/auth" method="post" class="register-form" id="register-form">
                        <input type="hidden" name="action" value="signup"/>
                        <div class="form-group">
                            <label for="username"><i class="zmdi zmdi-account material-icons-name"></i></label>
                            <input class="type-input" type="text" name="username" id="username" placeholder="Username" oninput="validateUsername()" required/>
                            <span id="username-error" style="color: red"><c:out value="${requestScope['usernameError']}" /></span>
                        </div>
                        <div class="form-group">
                            <label for="email"><i class="zmdi zmdi-email"></i></label>
                            <input class="type-input" type="email" name="email" id="email" placeholder="Your Email" required/>
                            <span id="email-error" style="color: red"><c:out value="${requestScope['emailError']}" /></span>
                        </div>
                        <div class="form-group">
                            <label for="password"><i class="zmdi zmdi-lock"></i></label>
                            <input class="type-input" type="password" name="password" id="password" placeholder="Password" oninput="validatePassword()" required/>
                            <span id="pwd-error" style="color: red"></span>
                        </div>
                        <div class="form-group">
                            <label for="re-pass"><i class="zmdi zmdi-lock-outline"></i></label>
                            <input class="type-input" type="password" name="re-pass" id="re-pass" placeholder="Repeat your password" oninput="validatePassword()" required/>
                            <span id="re_pwd-error" style="color: red"></span>
                        </div>
                        <div class="form-group">
                            <label for="fullname"><i class="zmdi zmdi-lock-outline"></i></label>
                            <input class="type-input" type="text" name="fullname" id="fullname" placeholder="Full Name (Optional)"/>
                        </div>
                        <div class="form-group">
                            <label for="gender"><i class="zmdi zmdi-lock-outline"></i></label>
                            <select class="type-input" id="gender" name="gender">
                                <option value="male">Male</option>
                                <option value="female">Female</option>
                                <option value="non_binary">Not Binary</option>
                                <option value="unknown">I prefer not to answer</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="birthdate"><i class="zmdi zmdi-lock-outline"></i></label>
                            <input class="type-input" type="date" name="birthdate" id="birthdate" placeholder="Birthdate"/>
                        </div>
                        <div class="form-group">
                            <label for="country"><i class="zmdi zmdi-lock-outline"></i></label>
                            <input class="type-input" type="text" name="country" id="country" placeholder="Country (Optional)" oninput="validateCountry()"/>
                            <div class="dropdown-content" id="country-dropdown" onclick="validateCountry()"></div>
                            <span id="country-error" style="color: red"></span>
                        </div>
                        <div class="form-group form-button">
                            <input class="type-input form-submit" type="submit" name="signup" id="signup" value="Register"/>
                        </div>
                    </form>
                    <button class="open-button" onclick="openForm()">Already have an account. Log In</button>
                </div>
            </div>
        </div>
    </section>

    <section class="log-in" id="myForm">
        <div class="container mt-5">
            <div class="login-content">
                <div class="login-form">
                    <h2 class="form-title">Log in</h2>
                    <form action="${pageContext.request.contextPath}/auth" method="post" class="login-form" id="login-form">
                        <input type="hidden" name="action" value="login"/>
                        <div class="form-group">
                            <label for="email_login"><i class="zmdi zmdi-account material-icons-name"></i></label>
                            <input class="type-input" type="email" name="email" id="email_login" placeholder="Email" required/>
                            <span id="email-auth-error" style="color: red"><c:out value="${requestScope['emailLoginError']}" /> </span>
                        </div>
                        <div class="form-group">
                            <label for="password_login"><i class="zmdi zmdi-lock"></i></label>
                            <input class="type-input" type="password" name="password" id="password_login" placeholder="Password" required/>
                            <span id="password-auth-error" style="color: red"><c:out value="${requestScope['passwordLoginError']}" /> </span>
                        </div>
                        <div class="form-group">
                            <input class="type-input form-submit" type="submit" name="login" id="login" value="Log in"/>
                        </div>
                        <button type="button" class="btn cancel" onclick="closeForm()">Close</button>
                    </form>
                </div>
            </div>
        </div>
    </section>
</div>

    <c:if test="${not empty sessionScope[Constants.AUTHENTICATED_USER_KEY]}">
        <h1>Welcome, ${sessionScope[Constants.AUTHENTICATED_USER_KEY].getUsername()}!</h1>
    </c:if>

<script>
    function openForm() {
        document.getElementById("myForm").style.display = "block";
    }

    function closeForm() {
        document.getElementById("myForm").style.display = "none";
    }
</script>
</body>
</html>
