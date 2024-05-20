// Helper function to check if a string starts with another string in a case-insensitive manner
function startsWithCaseInsensitive(str, prefix) {
    return str.toUpperCase().indexOf(prefix) === 0;
}

function validateCountry() {
    const button = document.getElementById("confirm-button");
    button.disabled = false;

    const country = document.getElementById("country").value;
    const country_error = document.getElementById("country-error");

    if (country !== "" && !countryOptions.includes(country)) {
        country_error.innerText = "Select a valid country from the dropdown or leave it empty.";
        button.disabled = true;
    } else {
        country_error.innerText = "";
    }
}

function validateUsername() {
    const button = document.getElementById("confirm-button");
    button.disabled = false;

    const username = document.getElementById("username").value;
    const username_error = document.getElementById("username-error");

    const regex = /^[a-zA-Z0-9_\-]+$/;
    if (username.length < 4 || username.length > 15 || !regex.test(username)) {
        username_error.innerText = "Username must be 4-15 characters long and contain only letters, numbers, hyphens, and underscores.";
        button.disabled = true;
    } else {
        username_error.innerText = "";
    }
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

function showEditForm() {
    $("#overlay").show();
    $("body").css("overflow-y", "hidden");
    const signupDiv = $("#editPopup");
    signupDiv.find("input:not(#action):not(#edit)").val("");
    $("#gender").val("unknown");
    signupDiv.show();
}

function hideEditForm() {
    $("#overlay").hide();
    $("body").css("overflow-y", "auto");
    $("#editPopup").hide();
}
