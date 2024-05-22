const signUpButton = $("#signup");
const overlay = $("#overlay");

function startsWithCaseInsensitive(str, prefix) {
    return str.toUpperCase().indexOf(prefix.toUpperCase()) === 0;
}

function validateUsername() {
    signUpButton.prop("disabled", true);

    const username = $("#username").val();
    const usernameError = $("#username-error");

    if (username.length < 3)
        usernameError.text("The username must contain at least 3 characters");
    else if (username.length > 16)
        usernameError.text("The username must contain at most 16 characters");
    else if (!/^[a-zA-Z0-9_-]+$/.test(username))
        usernameError.text("The username must contain only letters, digits, \"-\" or \"_\"");
    else
        usernameError.text("");
}

function validateCountry() {
    signUpButton.prop("disabled", false);

    const country = $("#country").val();
    const countryError = $("#country-error");

    if (country !== "" && !countryOptions.includes(country)) {
        countryError.text("Select a valid country from the dropdown or leave it empty.");
        signUpButton.prop("disabled", true);
    } else {
        countryError.text("");
    }
}

function validatePassword() {
    signUpButton.prop("disabled", false);

    const password = $("#password").val();
    const rePass = $("#re-pass").val();
    const pwdError = $("#pwd-error");
    const rePwdError = $("#re_pwd-error");

    const minLength = 8;

    if (password.length < minLength) {
        pwdError.text(`Password should be at least ${minLength} characters long.`);
        signUpButton.prop("disabled", true);
        return;
    }
    if (!/[A-Z]/.test(password)) {
        pwdError.text("Password should contain at least one uppercase letter.");
        signUpButton.prop("disabled", true);
        return;
    }
    if (!/[a-z]/.test(password)) {
        pwdError.text("Password should contain at least one lowercase letter.");
        signUpButton.prop("disabled", true);
        return;
    }
    if (!/\d/.test(password)) {
        pwdError.text("Password should contain at least one digit.");
        signUpButton.prop("disabled", true);
        return;
    }
    if (!/[!@#$%^&*()_+{}\[\]:;<>,.?~\\/-]/.test(password)) {
        pwdError.text("Password should contain at least one special character.");
        signUpButton.prop("disabled", true);
        return;
    }

    pwdError.text("");

    if (password !== rePass) {
        rePwdError.text("Passwords do not match.");
        signUpButton.prop("disabled", true);
        return;
    }

    rePwdError.text("");
}

function showSignUpForm() {
    overlay.show();
    $("body").css("overflow-y", "hidden");
    const signupDiv = $("#signupPopup");
    signupDiv.find("input:not(#action):not(#signup)").val("");
    $("#gender").val("unknown");
    signupDiv.show();
    overlay.click(hideEditForm);

}

function hideSignUpForm() {
    overlay.hide();
    $("body").css("overflow-y", "auto");
    $("#signupPopup").hide();
}

signUpButton.click(function(event) {
    event.preventDefault();
    const signupForm = $("#register-form");
    $.post(signupForm.attr("action"), signupForm.serialize(), function(data) {
        if (data.success) {
            window.location.href = data.redirect;
        }

        $("#username-error").text(data.usernameError || "");
        $("#email-error").text(data.emailError || "");
        $("#general-error").text(data.generalError || "");
    }).fail(function() {
        $("#general-error").text("An error occurred. Please try again later.");
    });
});