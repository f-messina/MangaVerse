<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth_test.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/website.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css"/>

    <title>AUTH PAGE</title>
</head>

<body>
    <!-- navbar -->
    <div class="nav-bar">
        <nav>
            <a href="${pageContext.request.contextPath}/mainPage"><img src="${pageContext.request.contextPath}/images/logo-with-initial.png" alt="logo" /></a>
            <div class="nav-items">
                <a href="${pageContext.request.contextPath}/mainPage/manga" class="manga">Manga</a>
                <a href="${pageContext.request.contextPath}/mainPage/anime" class="anime">Anime</a>
            </div>
        </nav>
    </div>

    <div id="overlay" class="overlay"></div>

    <!-- Sign up -->
    <div id="signupPopup" class="myAlert container" style="max-width: 600px">
        <div class="myAlertBody">
            <h2 class="pb-3">Sign up</h2>
            <form action="${pageContext.request.contextPath}/auth" method="post" id="register-form" style="width: 70%">
                <input type="hidden" name="action" id="action" value="signup"/>
                <div>
                    <label for="username">Username</label>
                    <input class="form-control" type="text" name="username" id="username" placeholder="Username" oninput="validateUsername()" required/>
                    <span id="username-error" class="error"></span>
                </div>
                <div>
                    <label for="email">Email</label>
                    <input class="form-control" type="email" name="email" id="email" placeholder="Your Email" required/>
                    <span id="email-error" class="error"></span>
                </div>
                <div>
                    <label for="password">Password</label>
                    <input class="form-control" type="password" name="password" id="password" placeholder="Password" oninput="validatePassword()" required/>
                    <span id="pwd-error" class="error"></span>
                </div>
                <div>
                    <label for="re-pass">Repeat Password</label>
                    <input class="form-control" type="password" name="re-pass" id="re-pass" placeholder="Repeat your password" oninput="validatePassword()" required/>
                    <span id="re_pwd-error" class="error"></span>
                </div>
                <div class="pb-3">
                    <label for="fullname">Full Name (Optional)</label>
                    <input class="form-control" type="text" name="fullname" id="fullname" placeholder="Full Name (Optional)"/>
                </div>
                <div id="gender-div" class="dropdown-content pb-3">
                    <label for="gender">Gender</label>
                    <input type="text" class="form-control" id="gender" name="gender" readonly>
                    <div class="dropdown-options">
                        <a class="option" data-value="male">Male</a>
                        <a class="option" data-value="female">Female</a>
                        <a class="option" data-value="non_binary">Non Binary</a>
                        <a class="option" data-value="unknown">I prefer not to answer</a>
                    </div>
                </div>
                <div class="pb-3">
                    <label for="birthdate">Birthdate</label>
                    <input class="form-control" type="date" name="birthdate" id="birthdate"/>
                </div>
                <div>
                    <label for="country">Country (Optional)</label>
                    <input class="form-control" type="text" name="country" id="country" placeholder="Country (Optional)" oninput="validateCountry()"/>
                    <div class="dropdown-content" id="country-dropdown" onclick="validateCountry()"></div>
                    <span id="country-error" class="error"></span>
                </div>
                <div class="py-3 text-center">
                    <button class="btn btn-secondary" onclick="hideSignUpForm()">Close</button>
                    <button id="signup" class="btn btn-primary">Sign Up</button>
                </div>
            </form>
            <span id="general-error" class="error"></span>
        </div>
    </div>

    <!-- Log in -->
    <div class="container login-container d-flex justify-content-center align-items-center">
        <div class="login-content text-center">
            <h1 class="pb-3">Log in</h1>
            <span id="auth-error" class="error"><c:out value="${requestScope['authError']}" /></span>
            <form action="${pageContext.request.contextPath}/auth" method="post" id="login-form">
                <input type="hidden" name="action" value="login"/>
                <div class="pb-3">
                    <label for="email_login"></label>
                    <input class="form-control form-control-lg " type="email" name="email" id="email_login" placeholder="Email" required/>
                </div>
                <div class="pb-3">
                    <label for="password_login"></label>
                    <input class="form-control form-control-lg" type="password" name="password" id="password_login" placeholder="Password" required/>
                </div>
                <div>
                    <input class="btn btn-primary btn-block" type="submit" name="login" id="login" value="Log In"/>
                </div>
            </form>
            <p id="show-sign-up-button" class="btn" onclick="showSignUpForm()">Don't have an account? Sign up</p>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/auth_test.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/country_dropdown.js" defer></script>
</body>
</html>
