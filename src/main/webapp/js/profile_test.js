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

    button.disabled = false;
    validateCountry();

    function validateCountry() {
        const country = document.getElementById("country").value;
        const country_error = document.getElementById("country-error");
        if (country !== "" && !options.includes(country)) {
            country_error.innerText = "Select a valid country from the dropdown or leave it empty.";
            button.disabled = true;
        } else {
            country_error.innerText = "";
        }
    }
}

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
