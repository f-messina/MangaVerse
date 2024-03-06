// Helper function to check if a string starts with another string in a case-insensitive manner
function startsWithCaseInsensitive(str, prefix) {
    return str.toUpperCase().indexOf(prefix) === 0;
}

function validateUsername() {
    const button = document.getElementById("signup");
    button.disabled = true;

    const username = document.getElementById("username").value;
    const usernameError = document.getElementById("username-error");

    if (username.length < 3) {
        usernameError.innerText = "The username must contain at least 3 characters";
        console.log("The username must contain at least 3 characters");
    } else if (username.length > 16) {
        usernameError.innerText = "The username must contain at most 16 characters";
        console.log("The username must contain at most 16 characters");
    } else if (!username.match(/^[a-zA-Z0-9_-]+$/)) {
        usernameError.innerText = "The username must contain only letters, digits, \"-\" or \"_\"";
        console.log("The username must contain only letters,digits,\"-\" or \"_\"");
    } else {
        usernameError.innerText = "";
        button.disabled = false;
    }
}

function validateCountry() {
    const button = document.getElementById("signup");
    button.disabled = false;

    const country = document.getElementById("country").value;
    const country_error = document.getElementById("country-error");

    if (country !== "" && !options.includes(country)) {
        country_error.innerText = "Select a valid country from the dropdown or leave it empty.";
        button.disabled = true;
    } else {
        country_error.innerText = "";
    }
}

function validatePassword() {
    const button = document.getElementById("signup");
    button.disabled = false;

    const password = document.getElementById("password").value;
    const re_pass = document.getElementById("re-pass").value;
    const pwd_error = document.getElementById("pwd-error");
    const re_pwd_error = document.getElementById("re_pwd-error");

    const minLength = 8;
    // Check for minimum length
    if (password.length < minLength) {
        pwd_error.innerText = `Password should be at least ${minLength} characters long.`;
        button.disabled = true;
        return;
    }
    // Check for at least one uppercase letter
    if (!/[A-Z]/.test(password)) {
        pwd_error.innerText = "Password should contain at least one uppercase letter.";
        button.disabled = true;
        return;
    }
    // Check for at least one lowercase letter
    if (!/[a-z]/.test(password)) {
        pwd_error.innerText = "Password should contain at least one lowercase letter.";
        button.disabled = true;
        return;
    }
    // Check for at least one digit
    if (!/\d/.test(password)) {
        pwd_error.innerText = "Password should contain at least one digit.";
        button.disabled = true;
        return;
    }
    // Check for at least one special character
    if (!/[!@#$%^&*()_+{}\[\]:;<>,.?~\\/-]/.test(password)) {
        pwd_error.innerText = "Password should contain at least one special character.";
        button.disabled = true;
        return;
    }
    // All checks passed, password is valid
    pwd_error.innerText = "";

    // Check if passwords match
    if (password !== re_pass) {
        re_pwd_error.innerText = "Passwords do not match.";
        button.disabled = true;
        return;
    }
    // All checks passed, passwords match
    re_pwd_error.innerText = "";
}
