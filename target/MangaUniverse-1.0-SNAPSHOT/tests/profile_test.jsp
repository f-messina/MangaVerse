<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 02/02/2024
  Time: 15:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>PROFILE</title>
</head>
<body>
<section class="profile">
    <div class="profile-img">
        <img alt="profile image" src="images/user-icon.png">
    </div>
    <div id="user-info" class="texts">
        <form id="profile-form" method="post" action="#">
            <input type="hidden" name="action" value="update">
            <div class="form-group">
                <label for="username"><i class="zmdi zmdi-account material-icons-name"></i></label>
                <input type="text" class="editable" name="username" id="username" placeholder="Username" required disabled/>
                <span id="username-error" style="color: red"><c:out value="${requestScope['usernameError']}" /></span>
            </div>
            <div class="form-group">
                <label for="email"><i class="zmdi zmdi-email"></i></label>
                <input type="email" name="email" id="email" placeholder="Your Email" required disabled/>
            </div>
            <div class="form-group">
                <label for="fullname"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="text" class="editable" name="fullname" id="fullname" placeholder="Full Name (Optional)" disabled/>
            </div>
            <div class="form-group">
                <label for="gender"><i class="zmdi zmdi-lock-outline"></i></label>
                <select id="gender" class="editable" name="gender" disabled>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="not_binary">Not Binary</option>
                    <option value="unknown">I prefer not to answer</option>
                </select>
            </div>
            <div class="form-group">
                <label for="birthdate"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="date" class="editable" name="birthdate" id="birthdate" placeholder="Birthdate" disabled/>
            </div>
            <div class="form-group">
                <label for="country"><i class="zmdi zmdi-lock-outline"></i></label>
                <input type="text" name="country" class="editable" id="country" placeholder="Country (Optional)" oninput="ValidateForm()" disabled/>
                <div class="dropdown-content" id="country-dropdown" onclick="ValidateForm()"></div>
                <span id="country-error" style="color: red"></span>
            </div>
            <div class="form-group">
                <label for="joined-date"><i class="zmdi zmdi-lock"></i></label>
                <input type="text" name="joined-date" id="joined-date" placeholder="Joined Date" disabled/>
            </div>
            <div class="form-group">
                <button type="submit" id="edit-button" class="submit-btn">Modify</button>
                <button type="button" id="confirm-button" class="submit-btn" style="display: none">Confirm</button>
            </div>
        </form>
    </div>
</section>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        const form = document.getElementById("profile-form");
        const inputs = form.getElementsByClassName("editable");
        const editButton = document.getElementById("edit-button");
        const confirmButton = document.getElementById("confirm-button");
        let originalValues = {};

        editButton.addEventListener("click", function(event) {
            event.preventDefault();

            if (editButton.innerText === "Modify") {
                // Enable inputs and store original values
                originalValues = storeOriginalValues(inputs);
                enableInputs(inputs);
                showButton(confirmButton);
                editButton.innerText = "Cancel";
            } else {
                // Disable inputs and restore original values
                disableInputs(inputs);
                hideButton(confirmButton);
                restoreOriginalValues(inputs, originalValues);
                editButton.innerText = "Modify";
            }
        });

        confirmButton.addEventListener("click", function(event) {
            // You can add any additional logic or validation here before submitting the form
            form.submit();
        });

        function enableInputs(inputs) {
            for (let i = 0; i < inputs.length; i++) {
                inputs[i].disabled = false;
            }
        }

        function disableInputs(inputs) {
            for (let i = 0; i < inputs.length; i++) {
                inputs[i].disabled = true;
            }
        }

        function showButton(button) {
            button.style.display = "block";
        }

        function hideButton(button) {
            button.style.display = "none";
        }

        function storeOriginalValues(inputs) {
            const values = {};
            for (let i = 0; i < inputs.length; i++) {
                values[inputs[i].name] = inputs[i].value;
            }
            return values;
        }

        function restoreOriginalValues(inputs, originalValues) {
            for (let i = 0; i < inputs.length; i++) {
                const name = inputs[i].name;
                if (originalValues.hasOwnProperty(name)) {
                    inputs[i].value = originalValues[name];
                }
            }
        }
    });
</script>
</body>
</html>
