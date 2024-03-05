document.addEventListener("DOMContentLoaded", function() {
    const showAllBtns = document.querySelectorAll(".show-all-btn");

    showAllBtns.forEach(btn => {
        const list = btn.previousElementSibling;
        const listItems = list.querySelectorAll("li");
        let isHidden = true;

        hideRowsExceptFirst(list);

        btn.addEventListener("click", function() {
            if (isHidden) {
                listItems.forEach(item => {
                    item.style.display = "block";
                });
                btn.textContent = "Hide Items";
                isHidden = false;
            } else {
                hideRowsExceptFirst(list);
                btn.textContent = "Show All Items";
                isHidden = true;
            }
        });

        function hideRowsExceptFirst(list) {
            const listItems = list.querySelectorAll("li");
            for (let i = 6; i < listItems.length; i++) {
                listItems[i].style.display = "none";
            }
        }
    });

    const reviews = document.querySelectorAll(".review");
    let isHidden = true;

    hideReviewsExceptFirst(reviews);

    document.getElementById("show-hide-button").addEventListener("click", function() {
        if (isHidden) {
            reviews.forEach(review => {
                review.style.display = "block";
            });
            document.getElementById("show-hide-button").textContent = "Hide All Reviews";
            isHidden = false;
        } else {
            hideReviewsExceptFirst(reviews);
            document.getElementById("show-hide-button").textContent = "Show All Reviews";
            isHidden = true;
        }
    });

    function hideReviewsExceptFirst(reviews) {
        for (let i = 6; i < reviews.length; i++) {
            reviews[i].style.display = "none";
        }
    }
});

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
