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
    $.post(contextPath+"/profile", inputData, function(data) {
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
            } else if (data.notFoundError && searchValue) {
                followersList.empty();
                followersList.append("<p>No results found.</p>");
            } else if (data.notFoundError) {
                followersList.empty();
                followersList.append("<p>No Followers found.</p>");
            }
        }
    ).fail(function() {
        followersList.empty();
        followersList.append("<p>An error occurred. Please try again later.</p>");
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
    $.post(contextPath+"/profile", inputData, function(data) {
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
            } else if (data.notFoundError && searchValue) {
                followingsList.empty();
                followingsList.append("<p>No results found.</p>");
            } else if (data.notFoundError) {
                followingsList.empty();
                followingsList.append("<p>No Followers found.</p>");
            }
        }
    ).fail(function() {
        console.log("fail");
        followingsList.empty();
        followingsList.append("<p>An error occurred. Please try again later.</p>");
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
    console.log("clicked");
    event.preventDefault();
    const editForm = $("#edit-profile-form");
    $.post(editForm.attr("action"), editForm.serialize(), function(data) {
        if (data.success) {
            editProfileDiv.hide();
            overlay.hide();
        }
        console.log(data);
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
