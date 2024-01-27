<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 26.01.2024
  Time: 15:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Sign Up - Log In</title>
    <link rel="stylesheet" href="css/signUp-logIn.css"/>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com%22%3E/
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
                        <form>
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
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <input type="location" name="location" placeholder="Location" />
                            </div>
                            <div class="input_field">
                                <span><i aria-hidden="true" class="fa fa-calendar"></i></span>
                                <input type="date" name="birthday" placeholder="Choose your birthday"  />
                            </div>

                            <div class="input_field select_option">
                                <select>
                                    <option>Gender</option>
                                    <option>Female</option>
                                    <option>Male</option>
                                    <option>Non Binary</option>
                                    <option>Prefer not to answer</option>
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
                        <form>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-envelope"></i></span>
                                <input type="email" name="email2" placeholder="Email" required />
                            </div>
                            <div class="input_field"> <span><i aria-hidden="true" class="fa fa-lock"></i></span>
                                <input type="password" name="password2" placeholder="Password" required />
                            </div>

                            <input class="button" type="submit" value="Log In" />
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
