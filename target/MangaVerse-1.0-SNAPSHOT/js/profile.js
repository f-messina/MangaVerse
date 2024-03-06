initGenderDropdown();

const observer = new MutationObserver(() => {
    displayErrors();
});
const originalValues = {};
function enableEdit() {
    const descriptionElements = document.getElementsByClassName("editable");
    const config = {characterData: true, subtree: true, attributes: true};
    for (let element of descriptionElements) {
        switch (element.id) {
            case "username":
                originalValues[element.id] = element.innerText;
                observer.observe(element, config);
                handleEditable(element, null, 16);
                break;
            case "birthday":
                const numbers = element.getElementsByTagName("span");
                for (let number of numbers) {
                    observer.observe(number, config);
                    number.addEventListener("check", () => displayErrors("date"));
                    let size = (number === numbers[0]) ? 4 : 2;
                    originalValues[number.id] = number.innerText;
                    handleEditable(number, isNumericKey, size);
                }
                break;
            case "country":
                observer.observe(element.parentElement, config);
                originalValues[element.id] = element.innerText;
                handleEditable(element);
                break;
            case "gender":
                observer.observe(element.parentElement, config);
                originalValues[element.id] = element.innerText;
                handleEditable(element);
                break;
            default:
                originalValues[element.id] = element.innerText;
                handleEditable(element, null, 200);
        }
    }

    // Hide the "Edit Profile" button
    const editProfileButton = document.getElementById("enable-edit");
    editProfileButton.style.display = "none";

    const change = document.getElementById("edit-prof-options");
    change.style.display = "block";
}

function handleEditable(element, validationFunction = null, size = 0) {
    element.contentEditable = "true";
    element.classList.add("highlighted");
    element.addEventListener("keydown", (event) => handleKeyDown(event, validationFunction, size));
}

function disableEditable(element, validationFunction = null, size = 0) {
    element.contentEditable = "false";
    element.classList.remove("highlighted");
    element.removeEventListener("keydown", (event) => handleKeyDown(event, validationFunction, size));
}

function handleKeyDown(event, validationFunction = null, size = 0) {
    if (event.key === "Enter") {
        event.preventDefault();
        const target = event.target;
        target.classList.add("disabled-hover");
        target.blur();
        target.addEventListener("transitionend", function () {
            // Remove the class after the transition ends
            target.classList.remove("disabled-hover");
            target.removeEventListener("transitionend", arguments.callee);
        });
    }
    if (validationFunction && !validationFunction(event) && event.key !== "Backspace") {
        event.preventDefault();
    }
    const maxsize = size && event.target.innerText.length >= size;
    if (maxsize && event.key !== "Backspace") {
        console.log("Max size reached");
        event.preventDefault();
    }
}

function cleanup() {
    // Show the "Edit Profile" button
    const editProfileButton = document.querySelector(".edit-prof");
    editProfileButton.style.display = "block";
    // Remove the Confirm and Cancel buttons from the DOM
    const change = document.getElementById("edit-prof-options");
    change.style.display = "none";
}

function isNumericKey(event) {
    return /\d/.test(event.key);
}

function validateDate() {
    const birthdayElement = document.getElementById("birthday");
    const numbers = birthdayElement.getElementsByTagName("span");
    const dateError = document.getElementById("date-error");

    if (numbers.length === 3) {
        const year = numbers[0].innerText;
        const month = numbers[1].innerText;
        const day = numbers[2].innerText;

        if (year.length === 4 && month.length === 2 && day.length === 2) {
            const thirteenYearsAgo = new Date();
            thirteenYearsAgo.setFullYear(thirteenYearsAgo.getFullYear() - 13);
            const birthday = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
            if (birthday > thirteenYearsAgo) {
                dateError.innerText = "You must be at least 13 years old";
                console.log("You must be at least 13 years old");
            }
            return birthday <= thirteenYearsAgo;
        } else {
            dateError.innerText = "The date must be in the format YYYY-MM-DD";
            console.log("The date must be in the format YYYY-MM-DD");
        }
    } else {
        dateError.innerText = "The date must be in the format YYYY-MM-DD";
        console.log("The date must be in the format YYYY-MM-DD");
    }

    return false;
}

function validateUsername() {
    const username = document.getElementById("username").innerText;
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
    }
    return username.length >= 3 && username.length <= 16 && username.match(/^[a-zA-Z0-9_-]+$/);
}

function validateCountry() {
    const countryElement = document.getElementById("country");
    const countryError = document.getElementById("country-error");
    const country = countryElement.innerText;
    console.log(country);
    if (!(options.includes(country))) {
        countryError.innerText = "Select a country from the list";
        console.log("Select a country from the list");
    }
    return options.includes(country);
}

function displayErrors() {
    const usernameError = document.getElementById("username-error");
    const dateError = document.getElementById("date-error");
    const countryError = document.getElementById("country-error");
    const confirmButton = document.getElementById("confirm-button");
    if (!validateUsername()) {
        usernameError.style.display = "block";
        confirmButton.disabled = true;
    } else {
        usernameError.style.display = "none";
        confirmButton.disabled = false;
    }
    if (!validateDate()) {
        dateError.style.display = "block";
        confirmButton.disabled = true;
    } else {
        dateError.style.display = "none";
        confirmButton.disabled = false;
    }
    if (!validateCountry()) {
        countryError.style.display = "block";
        confirmButton.disabled = true;
    } else {
        countryError.style.display = "none";
        confirmButton.disabled = false;
    }
}

function handleConfirm() {
    if (!validateDate() || !validateUsername()) {
        return;
    }
    observer.disconnect();
    updateValues(false);
    console.log("Changes confirmed");

    const form = document.getElementById("edit-prof-form");
    const keyValues = {
        "username" : document.getElementById("username").innerText,
        "description" : document.getElementById("description").innerText,
        "birthday" : document.getElementById("birthday").innerText,
        "country" : document.getElementById("country").innerText,
        "gender": document.getElementById("gender").innerText
    };
    // Loop through key-value pairs and create input elements
    for (let key in keyValues) {
        if (keyValues.hasOwnProperty(key)) {
            let input = document.createElement('input');
            input.type = 'hidden'; // Set the input type to hidden
            input.name = key;      // Set the input name to the key
            input.value = keyValues[key]; // Set the input value to the corresponding value
            // Append the input element to the form
            form.appendChild(input);
        }
    }
    cleanup();
}

function handleCancel() {
    observer.disconnect();
    updateValues(true);
    console.log("Changes canceled");
    cleanup();
}

function updateValues(isCancel) {
    const descriptionElements = document.getElementsByClassName("editable");
    for (let element of descriptionElements) {
        switch (element.id) {
            case "username":
                element.innerHTML = isCancel ? originalValues[element.id] : element.innerText;
                disableEditable(element, null, 16);
                break;
            case "birthday":
                const numbers = element.getElementsByTagName("span");
                for (let number of numbers) {
                    const observer = new MutationObserver(() => {
                        displayErrors();
                    });
                    const config = { characterData: true, subtree: true };
                    observer.observe(element, config);
                    number.removeEventListener("check", () => displayErrors("date"));
                    number.innerHTML = isCancel ? originalValues[number.id] : number.innerText;
                    let size = (number === numbers[0]) ? 4 : 2;
                    disableEditable(number, isNumericKey, size);
                }
                break;
            case "country":
                element.innerHTML = isCancel ? originalValues[element.id] : element.innerText;
                disableEditable(element);
                break;
            case "gender":
                element.innerHTML = isCancel ? originalValues[element.id] : element.innerText;
                disableEditable(element);
                break;
            default:
                element.innerHTML = isCancel ? originalValues[element.id] : element.innerText;
                disableEditable(element, null, 200);
        }
    }
}

function initGenderDropdown() {
    const input = document.getElementById("gender");
    const dropdown = document.getElementById("genderDropdown");
    const genderOptions = [
        "Male", "Female", "Non Binary", "Prefer not to say"
    ]
    input.addEventListener("click", function () {
        dropdown.innerHTML = "";
        for (let i = 0; i < genderOptions.length; i++) {
            let optionElement = document.createElement("p");
            optionElement.innerHTML = genderOptions[i];
            optionElement.addEventListener("click", function () {
                input.innerHTML = this.innerHTML;
                dropdown.style.display = "none";
            });
            dropdown.appendChild(optionElement);
        }
        dropdown.style.display = "block";
    }
    );

    window.addEventListener("click", function (event) {
        if (!event.target.matches("#gender") && !event.target.matches('.dropdown-content a')) {
            dropdown.style.display = "none";
        }
    });
}