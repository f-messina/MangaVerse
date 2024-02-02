<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 26.01.2024
  Time: 15:56
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Sign Up - Log In</title>
    <link rel="stylesheet" href="css/signUp-logIn.css"/>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com%22%3E/" crossorigin />
    <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin />
    <link
            href="https://fonts.googleapis.com/css2?family=Fira+Sans+Condensed:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&family=Roboto:wght@300;400&display=swap"
            rel="stylesheet"
    />

</head>
<body>

<div class="log-sign">
    <div class="sign-up">
        <div class="form_wrapper">
            <div class="form_container">
                <div class="title_container">
                    <h2>Create an Account</h2>
                </div>
                <div class="row clearfix">
                    <div class="">
                        <form action="auth" method="post">
                            <!-- Add a hidden input field for the action -->
                            <input type="hidden" name="action" value="signup">

                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-envelope"></i></span>
                                <input type="username" name="username" placeholder="Username" required/>
                            </div>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-envelope"></i></span>
                                <input type="fullname" name="fullname" placeholder="Full Name"/>
                            </div>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-envelope"></i></span>
                                <input type="email" name="email" placeholder="Email" required />
                            </div>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <input type="password" name="password" placeholder="Password" required />
                            </div>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <input type="password" name="password" placeholder="Re-type Password" required />
                            </div>
                            <div class="input_field "> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <input type="text" class="search-input" placeholder="Country" id="input_country" oninput="filterOptions()">
                                <select multiple size="5" name="location" id="mySelect">
                                    <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                    <option value="Afghanistan">Afghanistan</option>
                                    <option value="Aland Islands">Aland Islands</option>
                                </select>
                                <div class="select_arrow"></div>
                            </div>
                            <div class="input_field">
                                <span><i aria-hidden="true" class="fa fa-calendar"></i></span>
                                <input type="date" name="birthday" placeholder="Choose your birthday"  />
                            </div>

                            <div class="input_field select_option"> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <select name = "gender" required>
                                    <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                    <option value="" disabled selected>Gender</option>
                                    <option value = "Female">Female</option>
                                    <option value = "Male">Male</option>
                                    <option value = "Non-Binary">Non Binary</option>
                                    <option value = "">Prefer not to answer</option>
                                </select>
                                <div class="select_arrow"></div>
                            </div>


                            <input class="button" type="submit" value="Register" />
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>



    <div class="log-in">
        <div class="form_wrapper">
            <div class="form_container">
                <div class="title_container">
                    <h2>Log In</h2>
                </div>
                <div class="row clearfix">
                    <div class="">
                        <form action="auth" method="post">
                            <input type="hidden" name="action" value="login">

                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-envelope"></i></span>
                                <input type="email" name="email" placeholder="Email" required />
                            </div>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <input type="password" name="password" placeholder="Password" required />
                            </div>

                            <input class="button" type="submit" value="Log In" />
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function filterOptions() {
        const input = document.querySelector('.search-input').value.toUpperCase();
        const select = document.getElementById('mySelect');
        const options = select.getElementsByTagName('option');

        for (let i = 0; i < options.length; i++) {
            const textValue = options[i].textContent || options[i].innerText;
            const startsWithInput = textValue.toUpperCase().startsWith(input);
            options[i].style.display = startsWithInput ? "" : "none";
        }
    }

    const select = document.getElementById('mySelect');
    const input = document.getElementById('input_country');

    select.addEventListener('change', function() {
        input.value = select.value;
    });
</script>
</body>
</html>
