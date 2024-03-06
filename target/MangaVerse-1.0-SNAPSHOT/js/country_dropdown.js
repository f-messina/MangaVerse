// Array of state country options
const countryOptions = ["Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua & Deps", "Argentina",
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

    // Remove existing country options in the dropdown
    dropdown.innerHTML = "";

    // If input is empty, do not filter countryOptions
    if (filter === "") {
        dropdown.style.display = "none";
        return;
    }

    for (let i = 0; i < countryOptions.length; i++) {
        if (startsWithCaseInsensitive(countryOptions[i], filter)) {
            let optionElement = document.createElement("a");
            optionElement.href = "#";
            optionElement.textContent = countryOptions[i];
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
    // Show all country options when the dropdown is clicked
    dropdown.innerHTML = "";

    for (let i = 0; i < countryOptions.length; i++) {
        let optionElement = document.createElement("a");
        optionElement.href = "#";
        optionElement.textContent = countryOptions[i];
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