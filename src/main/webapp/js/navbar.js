const userSearchButton = $("#user-search-button");
const userSearch = $("#user-search");
const usersList = $("#user-search-results");
const userSearchSection = $("#user-search-section");

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
                const img = $("<img src='" + user.profilePicUrl + "'>").addClass("user-pic")
                    .on("error", () => setDefaultProfilePicture($(this)));
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
    usersList.empty();
    getUsers(null);
    if (!userSearch.hasClass("active")) {
        userSearch.val("");
    }
    userSearch.addClass("active");
    userSearchSection.show();

    $("body").on("click.hideUserResults", function(event) {
        if (!$(event.target).closest(userSearch).length &&
            !$(event.target).closest(usersList).length &&
            !$(event.target).closest(userSearchButton).length) {
            userSearch.removeClass("active");
            userSearchSection.hide();
            $("body").off("click.hideUserResults");
        }
    });
});

function setDefaultProfilePicture(image) {
    image.off("error");
    image.attr("src", userDefaultImage);
}

$(document).ready(function() {
    const profilePicture = $("#navbar-profile-picture");

    profilePicture.on("error", () => setDefaultProfilePicture($(this)));
});

function logout(targetServlet) {
    $.post(contextPath+"/auth", {action: "logout"}, function() {
        window.location.href = contextPath + "/" + targetServlet;
    });
}