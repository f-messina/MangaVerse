// Array of state options
const options = ["Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua & Deps", "Argentina",
    "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados",
    "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia Herzegovina", "Botswana", "Brazil",
    "Brunei", "Bulgaria", "Burkina", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde",
    "Central African Rep", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo", "Congo {Democratic Rep}",
    "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica",
    "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
    "Estonia", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana",
    "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hungary",
    "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland {Republic}", "Israel", "Italy", "Ivory Coast",
    "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea North", "Korea South", "Kosovo",
    "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",
    "Lithuania", "Luxembourg", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta",
    "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia",
    "Montenegro", "Morocco", "Mozambique", "Myanmar, {Burma}", "Namibia", "Nauru", "Nepal", "Netherlands",
    "New Zealand", "Nicaragua", "Niger", "Nigeria", "Norway", "Oman", "Pakistan", "Palau", "Panama",
    "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania",
    "Russian Federation", "Rwanda", "St Kitts & Nevis", "St Lucia", "Saint Vincent & the Grenadines", "Samoa",
    "San Marino", "Sao Tome & Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone",
    "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain",
    "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan",
    "Tanzania", "Thailand", "Togo", "Tonga", "Trinidad & Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu",
    "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan",
    "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"];

const input = document.getElementById("country");
const dropdown = document.getElementById("country-dropdown");

// Add an input event listener
input.addEventListener("input", function() {
    const filter = input.value.trim().toUpperCase();

    // Remove existing options in the dropdown
    dropdown.innerHTML = "";

    // If input is empty, do not filter options
    if (filter === "") {
        dropdown.style.display = "none";
        return;
    }

    for (let i = 0; i < options.length; i++) {
        if (startsWithCaseInsensitive(options[i], filter)) {
            let optionElement = document.createElement("a");
            optionElement.href = "#";
            optionElement.textContent = options[i];
            optionElement.addEventListener("click", function() {
                input.value = this.textContent;
                dropdown.style.display = "none";
            });

            dropdown.appendChild(optionElement);
        }
    }

    // Display the dropdown
    if (dropdown.children.length > 0) {
        dropdown.style.display = "block";
    } else {
        dropdown.style.display = "none";
    }
});

// Add a click event listener for the dropdown
dropdown.addEventListener("click", function() {
    // Show all options when the dropdown is clicked
    dropdown.innerHTML = "";

    for (let i = 0; i < options.length; i++) {
        let optionElement = document.createElement("a");
        optionElement.href = "#";
        optionElement.textContent = options[i];
        optionElement.addEventListener("click", function() {
            input.value = this.textContent;
            dropdown.style.display = "none";
        });

        dropdown.appendChild(optionElement);
    }

    dropdown.style.display = "block";
});

// Close the dropdown when clicking elsewhere on the page
window.addEventListener("click", function(event) {
    if (!event.target.matches('#myInput') && !event.target.matches('.dropdown-content a')) {
        dropdown.style.display = "none";
    }
});

// Helper function to check if a string starts with another string in a case-insensitive manner
function startsWithCaseInsensitive(str, prefix) {
    return str.toUpperCase().indexOf(prefix) === 0;
}

function ValidateForm() {
    const button = document.getElementById("signup");
    const country = document.getElementById("country").value;
    const country_error = document.getElementById("country-error");

    button.disabled = false;
    validatePassword();
    validateCountry();

    function validateCountry() {
        if (country !== "" && !options.includes(country)) {
            country_error.innerText = "Select a valid country from the dropdown or leave it empty.";
            button.disabled = true;
        } else {
            country_error.innerText = "";
        }
    }

    function validatePassword() {
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
}