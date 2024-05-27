const overlay = $("#overlay");
const editProfileDiv = $("#editPopup");
const editButton = $("#edit-button");

const followers = $("#followers");
const followersList = $("#followers-list");
const showFollowerButton = $("#show-followers");
const followerSearch = $("#follower-search");

const followings = $("#followings");
const followingsList = $("#followings-list");
const showFollowingButton = $("#show-followings");
const followingSearch = $("#following-search");

const userSearchButton = $("#user-search-button");
const userSearch = $("#user-search");
const usersList = $("#user-search-results");
const userSearchSection = $("#user-search-section");

// Helper function to check if a string starts with another string in a case-insensitive manner
function startsWithCaseInsensitive(str, prefix) {
    return str.toUpperCase().indexOf(prefix) === 0;
}

function validateCountry() {
    const button = document.getElementById("edit-button");
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
    const button = document.getElementById("edit-button");
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
    overlay.show();
    $("body").css("overflow-y", "hidden");
    editProfileDiv.css("display", "flex");
    overlay.click(hideEditForm);
    editProfileDiv.click(function(event) {
        const target = $(event.target);
        if (target.is(editProfileDiv.children().first()) ||
            target.is(editProfileDiv) ||
            target.is(overlay)) {
            hideEditForm();
        }
    });
}

function hideEditForm() {
    overlay.hide();
    $("body").css("overflow-y", "auto");
    editProfileDiv.hide();
}

function showFollowers(){
    overlay.show();
    $("body").css("overflow-y", "hidden");
    getFollowers();
    followers.show();
    overlay.click(hideFollowers);
}

function getFollowers(searchValue) {
    const inputData = {action: "getFollowers", userId: userId};
    if (searchValue) {
        inputData.searchValue = searchValue;
    }
    const errorMessage = $("<p>").addClass("user");

    $.post(contextPath+"/user", inputData, function(data) {
        if (data.success) {
            followersList.empty();
            for (let i = 0; i < data.followers.length; i++) {
                const follower = data.followers[i];
                const followerDiv = $("<div>").addClass("user");
                // Create the image element and set the source
                const img = $("<img src='" + follower.profilePicUrl + "'>").addClass("user-pic");
                followerDiv.append(img);

                // Create the paragraph element and set the text
                const p = $("<p>").addClass("user-username").text(follower.username);
                followerDiv.append(p);
                followersList.append(followerDiv);
            }
        } else if (data.notFoundError) {
            followersList.empty();
            followersList.append(errorMessage.text("No followers found."));
        }
    }).fail(function() {
        followersList.empty();
        followersList.append(errorMessage.text("An error occurred. Please try again later."));
    });
}

function hideFollowers(){
    overlay.hide();
    $("body").css("overflow-y", "auto");
    followers.hide();
}

showFollowerButton.click(() => {
        showFollowers();
    }
);

function showFollowings(){
    overlay.show();
    $("body").css("overflow-y", "hidden");
    getFollowings();
    followings.show();
    overlay.click(hideFollowings);
}

function getFollowings(searchValue) {
    const inputData = {action: "getFollowings", userId: userId};
    if (searchValue) {
        inputData.searchValue = searchValue;
    }
    const errorMessage = $("<p>").addClass("user");

    $.post(contextPath+"/user", inputData, function(data) {
        if (data.success) {
                followingsList.empty();
                for (let i = 0; i < data.followings.length; i++) {
                    const following = data.followings[i];
                    const followingDiv = $("<div>").addClass("user");
                    // Create the image element and set the source
                    const img = $("<img src='" + following.profilePicUrl + "'>").addClass("user-pic");
                    followingDiv.append(img);

                    // Create the paragraph element and set the text
                    const p = $("<p>").addClass("user-username").text(following.username);
                    followingDiv.append(p);
                    followingsList.append(followingDiv);
                }
            } else if (data.notFoundError) {
                followingsList.empty();
                followingsList.append(errorMessage.text("No followings found."));
            }
        }
    ).fail(function() {
        followingsList.empty();
        followingsList.append(errorMessage.text("An error occurred. Please try again later."));
    });
}

function hideFollowings(){
    overlay.hide();
    $("body").css("overflow-y", "auto");
    followings.hide();
}

showFollowingButton.click(() => {
        showFollowings();
    }
);

editButton.click(function(event) {
    event.preventDefault();
    const editForm = $("#edit-profile-form");
    $.post(editForm.attr("action"), editForm.serialize(), function(data) {
        if (data.success) {
            editProfileDiv.hide();
            overlay.hide();
        }
        $("#username-error").text(data.usernameError || "");
        $("#general-error").text(data.generalError || "");
    }).fail(function() {
        $("#general-error").text("An error occurred. Please try again later.");
    });
});

followerSearch.on("input", function() {
    getFollowers(followerSearch.val());
});

followingSearch.on("input", function() {
    getFollowings(followingSearch.val());
});

function getUsers(searchValue) {
    const inputData = {action: "getUsers"};
    if (searchValue) {
        inputData.searchValue = searchValue;
    }
    const errorMessage = $("<p>").addClass("user");

    $.post(contextPath+"/user", inputData, function(data) {
        if (data.success) {
            usersList.empty();
            if (data.users.length === 0) {
                usersList.append(errorMessage.text("No users found."));
                return;
            }
            for (let i = 0; i < data.users.length; i++) {
                const user = data.users[i];
                const userDiv = $("<a>").addClass("user").attr("href", contextPath + "/profile?userId=" + user.id);
                // Create the image element and set the source
                const img = $("<img src='" + user.profilePicUrl + "'>").addClass("user-pic");
                userDiv.append(img);

                // Create the paragraph element and set the text
                const p = $("<p>").addClass("user-username").text(user.username);
                userDiv.append(p);
                usersList.append(userDiv);
            }
        } else if (data.notFoundError) {
            usersList.empty();
            usersList.append(errorMessage.text("No users found."));
        }
    }).fail(function() {
        usersList.empty();
        usersList.append(errorMessage.text("An error occurred. Please try again later."));
    });
}
userSearch.on("input", function() {
    getUsers(userSearch.val());
});

userSearchButton.click(() => {
    if (userSearch.val() === "" && usersList.children().length === 0) {
        usersList.empty();
        getUsers(null);
    }
    userSearch.addClass("active");
    userSearchSection.show();

    $("body").on("click.hideUserResults", function(event) {
        console.log(event.target);
        if (!$(event.target).closest(userSearch).length &&
            !$(event.target).closest(usersList).length &&
            !$(event.target).closest(userSearchButton).length) {
            userSearch.removeClass("active");
            userSearchSection.hide();
            $("body").off("click.hideUserResults");
        }
    });
});
