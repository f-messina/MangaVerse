const overlay = $("#overlay");

/////////////////
/// EDIT FORM ///
/////////////////

const editProfileDiv = $("#editPopup");
const editButton = $("#edit-button");
const deleteButton = $("#delete-button");

// Validation functions for the edit form //

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

function validatePictureUrl() {
    const pictureUrl = $("#profile-picture-url");
    const pictureMessage = $("#check-image-message");
    const pictureForm = $("#picture");
    const preview = $("#profile-picture");

    const img = new Image();

    // Check if the picture URL has not changed
    if (pictureUrl.val() === profile.picture ||
        (pictureUrl.val() === "" && profile.picture === userDefaultImage)) {
        pictureMessage.text("No changes made.");
        pictureMessage.css("color", "red");
        return;
    }

    // If the picture URL is empty, use the default image
    if (pictureUrl.val() === "") {
        pictureMessage.text("Default image selected, save to apply changes.");
        pictureMessage.css("color", "green");
        preview.attr("src", userDefaultImage);
        pictureForm.val("");
        return;
    }

    // Validate the image URL and display a preview
    img.onload = function() {
        pictureMessage.text("URL valid, save to apply changes.");
        pictureMessage.css("color", "green");
        preview.attr("src", pictureUrl.val());
        pictureForm.val(pictureUrl.val());
    };

    // If the image URL is invalid, display an error message
    img.onerror = function() {
        pictureMessage.text("Invalid URL. Please enter a valid image URL.");
        pictureMessage.css("color", "red");
    };

    img.src = pictureUrl.val();
}

$(".password-toggle-icon").click(function() {
    const passwordField = $("#password");
    const togglePasswordIcon = $(".password-toggle-icon .fa");
    if (passwordField.attr("type") === "password") {
        passwordField.attr("type", "text");
        togglePasswordIcon.removeClass("fa-eye");
        togglePasswordIcon.addClass("fa-eye-slash");
    } else {
        passwordField.attr("type", "password");
        togglePasswordIcon.removeClass("fa-eye-slash");
        togglePasswordIcon.addClass("fa-eye");
    }
});

function showEditForm() {
    resetForm();
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

function storeInitialValues() {
    profile.username = $("#username").val();
    profile.fullname = $("#fullname").val();
    profile.description = $("#description").val();
    profile.country = $("#country").val();
    profile.birthdate = $("#birthdate").val();
    profile.picture = $("#picture").val();
    profile.gender = $("#gender").val();
}

function resetForm() {
    $("#username").val(profile.username);
    $("#fullname").val(profile.fullname);
    $("#description").val(profile.description);
    $("#country").val(profile.country);
    $("#birthdate").val(profile.birthdate);
    if (profile.picture === "" || profile.picture === userDefaultImage) {
        $("#profile-picture-url").val("");
    } else {
        $("#profile-picture-url").val(profile.picture);
    }
    $("#profile-picture").attr("src", profile.picture === "" ? userDefaultImage : profile.picture);
    $("#gender").val(profile.gender);
    $("#check-image-message").text("");
    $("#username-error").text("");
    $("#country-error").text("");
    $("#general-error").text("");
    editButton.prop("disabled", false);
}

function setNewValues() {
    const bio = $("#bio").empty();
    const imageDisplayed = $("#profile-picture-display");
    const usernameDisplayed = $("#username-displayed");
    const picture = $("#picture");
    const gender = $("#gender");
    const welcomeMessage = $("#welcome-message");
    const usernameValue = $("#username");

    usernameDisplayed.text(usernameValue.val());
    const username = $("<span>").text($("#fullname").val()).addClass("profile-real-name-px");
    bio.append($("<p>").append(username));
    bio.append($("<p>").text($("#description").val()));
    if (gender.val() !== "UNKNOWN")
        bio.append($("<p>").text(gender.find('option:selected').text()));
    bio.append($("<p>").text($("#country").val()));
    bio.append($("<p>").text($("#birthdate").val()));
    if (picture.val() !== "" && picture.val() !== undefined) {
        imageDisplayed.attr("src", picture.val());
        $("#navbar-profile-picture").attr("src", picture.val());
    } else {
        imageDisplayed.attr("src", userDefaultImage);
        $("#navbar-profile-picture").attr("src", userDefaultImage);
    }
    welcomeMessage.text("Welcome " + usernameValue.val());

    storeInitialValues();
}

function getModifiedInfo() {
    // Initialize an empty object for inputData
    const inputData = {};

    // Get current form values
    const formValues = {
        username: $("#username").val(),
        fullname: $("#fullname").val(),
        description: $("#description").val(),
        country: $("#country").val(),
        birthdate: $("#birthdate").val(),
        picture: $("#picture").val(),
        gender: $("#gender").val()
    };

    // Loop through the keys in formValues
    for (const key in formValues) {
        if (formValues.hasOwnProperty(key)) {
            // Only add to inputData if the value is different from profile
            if (formValues[key] !== profile[key]) {
                inputData[key] = formValues[key];
            }
        }
    }

    inputData.action = "editProfile";
    inputData.reviewsIds = JSON.stringify(profile.reviewsIds)
    return inputData;
}

editButton.click(function() {
    const inputData = getModifiedInfo();
    if (Object.keys(inputData).length === 0) {
        $("#general-error").text("No changes made.");
        return;
    }

    $.post(contextPath + "/profile", inputData, function(data) {
        if (data.success) {
            editProfileDiv.hide();
            $("body").css("overflow-y", "auto");
            overlay.hide();
            setNewValues();
        }
        $("#username-error").text(data.usernameError || "");
        $("#general-error").text(data.generalError || "");
    }).fail(function() {
        $("#general-error").text("An error occurred. Please try again later.");
    });
});

deleteButton.click(function() {
    const inputData = {
        action: "deleteProfile",
        reviewsIds: JSON.stringify(profile.reviewsIds)
    };
    $.post(contextPath + "/profile", inputData, function(data) {
        if (data.success) {
            $.post(contextPath+"/auth", {action: "logout"}, function() {
                window.location.href = contextPath + "/auth";
            });
        }
    }).fail(function() {
        $("#general-error").text("An error occurred. Please try again later.");
    });
});

//////////////////////////////
/// FOLLOWERS & FOLLOWINGS ///
//////////////////////////////

const followers = $("#followers");
const followersList = $("#followers-list");
const showFollowerButton = $("#show-followers");
const followerSearch = $("#follower-search");

const followings = $("#followings");
const followingsList = $("#followings-list");
const showFollowingButton = $("#show-followings");
const followingSearch = $("#following-search");

showFollowerButton.click(() => showList("followers"));
showFollowingButton.click(() => showList("followings"));

function showList(type) {
    overlay.show();
    $("body").css("overflow-y", "hidden");

    const list = type === "followers" ? followers : followings;
    const searchInput = type === "followers" ? followerSearch : followingSearch;
    type === "followers" ? followerSearch.val("") : followingSearch.val("");

    getList(type);
    list.show();
    overlay.click(() => {
        overlay.hide();
        $("body").css("overflow-y", "auto");
        list.hide();
    });
}

followerSearch.on("input", () => getList("followers", followerSearch.val()));
followingSearch.on("input", () => getList("followings", followingSearch.val()));

function getList(type, searchValue) {
    const action = type === "followers" ? "getFollowers" : "getFollowings";
    const inputData = { action, userId: profile.userId };

    if (searchValue) inputData.searchValue = searchValue;

    $.post(`${contextPath}/user`, inputData, function (data) {
        const listElement = type === "followers" ? followersList : followingsList;
        listElement.empty();

        if (data.success) {
            const items = data[type];
            items.forEach(item => {
                const itemDiv = $(`<a href="${contextPath}/profile?userId=${item.id}">`).addClass("user");
                const img = $(`<img src="${item.profilePicUrl}">`).addClass("user-pic")
                    .on("error", () => setDefaultProfilePicture(img));
                const p = $("<p>").addClass("user-username").text(item.username);
                itemDiv.append(img, p);
                listElement.append(itemDiv);
            });
        } else if (data.notFoundError) {
            listElement.append($("<p>").addClass("user").text(`No ${type} found.`));
        } else {
            listElement.append($("<p>").addClass("user").text("An error occurred. Please try again later."));
        }
    }).fail(() => {
        const listElement = type === "followers" ? followersList : followingsList;
        listElement.empty().append($("<p>").addClass("user").text("An error occurred. Please try again later."));
    });
}

// Follow and unfollow functions
function follow() {
    changeFollowStatus("follow");
}

function unfollow() {
    changeFollowStatus("unfollow");
}

function changeFollowStatus(action) {
    const inputData = { action, userId: profile.userId };

    $.post(contextPath + '/user', inputData, function (data) {
        if (data.success) {
            const followButton = $(".profile-edit-btn-px");
            followButton.text(action === "follow" ? "Followed" : "Follow");
            followButton.attr("onclick", action === "follow" ? "unfollow()" : "follow()");
        }
    }).fail(xhr => console.error(`${action} failed: ${xhr.responseText}`));
}

//////////////////
/// APP RATING ///
//////////////////

let appRating = profile.appRating;

// Function to update rating
function setAppRating(n) {
    const stars = $(".star");
    const appRatingRequestMessage = $("#no-app-rating-message");
    if (appRating === n) return;
    $.post(contextPath + "/profile", { action: "rateApp", rating: n }, function(data) {
        if (data.success) {
            appRatingRequestMessage.text("Thanks for rating the app!");
            stars.removeClass("checked");

            for (let i = 0; i < n; i++) {
                if (i <= n)
                    stars.eq(i).addClass("checked");
            }
            appRating = n;
        }
    }).fail(() => console.error(`Rating failed`));
}

///////////////////////
/// LIKES & REVIEWS ///
///////////////////////

let mangaPage, totalMangaPages;
let animePage, totalAnimePages;
let reviewsPage, totalReviewsPages;

function changeSection(button) {
    const section = button.id.split("-")[0];
    $(".selection-buttons button").removeClass("active");
    button.classList.add("active");


    const sections = { anime: "#anime-like", manga: "#manga-like", reviews: "#reviews" };
    $.each(sections, (key, value) => $(value).toggle(key === section));

    if (section === "reviews" && $("#reviews-list").children().first().length === 0) {
        fetchData("getReviews");
    } else if (section === "anime" && $("#anime-list").children().first().length === 0) {
        fetchData("getAnimeLikes");
        if (profile.country !== "")
            fetchSuggestions("anime", "location", profile.country);
        if (profile.birthdate !== "")
            fetchSuggestions("anime", "birthday", profile.birthdate);
    } else if (section === "manga" && $("#manga-list").children().first().length === 0) {
        fetchData("getMangaLikes");
        if (profile.country !== "")
            fetchSuggestions("manga", "location",profile.country);
        if (profile.birthdate !== "")
            fetchSuggestions("manga", "birthday", profile.birthdate);
    }
}

function fetchData(action, page = 1) {
    const input = { "action": action, "page": page, "userId": profile.userId };
    let serverUrl = contextPath + "/user";
    if (action === "getReviews") {
        input["reviewsIds"] = JSON.stringify(profile.reviewsIds);
        serverUrl = contextPath + "/profile";
    }

    $.post(serverUrl, input, (data) => {
        if (action === "getReviews") {
            reviewsPage = page;
            showReviews(data);
        } else {
            action === "getAnimeLikes" ? animePage = page : mangaPage = page;
            showLikes(data, action);
        }
    }).fail((xhr) => console.error(`Profile data fetch failed: ${xhr.responseText}`));
}

function showLikes(data, action) {
    const isAnime = action === "getAnimeLikes";
    const likeList = isAnime ? $("#anime-list") : $("#manga-list");
    const pagination = isAnime ? $(".anime-pagination") : $(".manga-pagination");
    const defaultImage = isAnime ? animeDefaultImage : mangaDefaultImage;

    likeList.empty();
    pagination.empty();

    if (!data.mediaLikes || data.mediaLikes.totalPages === 0) {
        likeList.append($("<p>").addClass("no-results-error").text("No likes found"));
        isAnime ? totalAnimePages = 0 : totalMangaPages = 0;
        return;
    }

    isAnime ? totalAnimePages = data.mediaLikes.totalPages : totalMangaPages = data.mediaLikes.totalPages;
    updatePagination(isAnime ? "anime" : "manga");
    data.mediaLikes.entries.forEach(media => {
        const mediaWrapper = $("<div>").addClass("project-box-wrapper");
        const mediaBox = $("<div>").addClass("project-box");
        const picture = $("<img>").attr("src", media.imageUrl === null ? defaultImage: media.imageUrl).attr("alt", media.title)
            .addClass("box-image")
            .on("error", () => setDefaultCover(picture, isAnime ? "anime" : "manga"));
        const title = $("<a>").attr("href", contextPath + "/" + (isAnime ? "anime" : "manga") + "?mediaId=" + media.id)
            .addClass("box-title").text(media.title);

        mediaBox.append(picture, title);
        mediaWrapper.append(mediaBox);
        likeList.append(mediaWrapper);
    });
}

function showReviews(data) {
    const reviews = $("#reviews-list");
    const pagination = $(".review-pagination");

    reviews.empty();
    pagination.empty();

    if (!data.reviews || data.reviews.totalPages === 0) {
        totalReviewsPages = 0;
        reviews.append($("<p>").addClass("text-center no-results-error").text("No reviews found"));
        return;
    }

    totalReviewsPages = data.reviews.totalPages;
    updatePagination("reviews");

    data.reviews.entries.forEach(review => {
        const type = review.mediaContent.season === undefined ? "manga" : "anime";
        const title = $("<a>").attr("href", contextPath + "/" + type + "?mediaId=" + review.mediaContent.id)
            .addClass("review-media-title").text(review.mediaContent.title);
        const rating = $("<p>").addClass("review-rating")
            .text(review.rating === null ? "No rating" : `Rating: ${review.rating}`);
        const firstRow = $("<div>").addClass("review-row").append(title, rating);
        const comment = $("<p>").addClass("review-comment")
            .text(review.comment === null ? "No comment" : review.comment);
        const date = $("<p>").addClass("review-date").text(`Date: ${review.date}`);
        const reviewBox = $("<div>").addClass("review-box").append(firstRow, comment, date);

        reviews.append(reviewBox);
    });
}

function updatePagination(section) {
    let pagination, totalPages, currentPage, action;

    if (section === "anime") {
        pagination = $(".anime-pagination");
        totalPages = totalAnimePages;
        currentPage = animePage;
        action = "getAnimeLikes";
    } else if (section === "manga") {
        pagination = $(".manga-pagination");
        totalPages = totalMangaPages;
        currentPage = mangaPage;
        action = "getMangaLikes";
    } else {
        pagination = $(".review-pagination");
        totalPages = totalReviewsPages;
        currentPage = reviewsPage;
        action = "getReviews";
    }

    pagination.empty();
    if (totalPages === 1) return;

    const createPageButton = (pageNumber, isActive = false) => {
        const btn = $("<li>").addClass("page__numbers").text(pageNumber);
        if (isActive) btn.addClass("active");
        else btn.click(() => fetchData(action, pageNumber));
        return btn;
    };

    const createArrowButton = (direction, enabled, targetPage) => {
        const arrow = $("<li>").addClass("page__btn").html(`<span class="material-icons">chevron_${direction}</span>`);
        if (enabled) arrow.addClass("active").click(() => fetchData(action, targetPage));
        return arrow;
    };

    pagination.append(createArrowButton("left", currentPage > 1, currentPage - 1));

    if (totalPages < 10) {
        for (let i = 1; i <= totalPages; i++) {
            pagination.append(createPageButton(i, i === currentPage));
        }
    } else {
        if (currentPage < 6) {
            for (let i = 1; i <= 7; i++) {
                pagination.append(createPageButton(i, i === currentPage));
            }
            pagination.append($("<li>").addClass("page__dots").text("..."));
        } else if (currentPage > totalPages - 6) {
            pagination.append(createPageButton(1));
            pagination.append($("<li>").addClass("page__dots").text("..."));
            for (let i = totalPages - 6; i < totalPages; i++) {
                pagination.append(createPageButton(i, i === currentPage));
            }
        } else {
            pagination.append(createPageButton(1));
            pagination.append($("<li>").addClass("page__dots").text("..."));
            for (let i = currentPage - 2; i <= currentPage + 2; i++) {
                pagination.append(createPageButton(i, i === currentPage));
            }
            pagination.append($("<li>").addClass("page__dots").text("..."));
        }
        pagination.append(createPageButton(totalPages, currentPage === totalPages));
    }

    pagination.append(createArrowButton("right", currentPage < totalPages, currentPage + 1));
}

// Initialize the profile page with the manga section
$(document).ready(() => {
    changeSection(document.getElementById("manga-button"));
});


///////////////
//SUGGESTIONS//
///////////////

function fetchSuggestions(mediaContentType, criteria, value){
    if (criteria === "birthday"){
        value = new Date(value).getFullYear();
    }

    $.post(`${contextPath}/profile`,{
        action: "suggestedMediaContent",
        type: mediaContentType,
        criteria: criteria,
        value: value
    },function (data){
        if (data.success) {
            displaySuggestions(data.suggestedMediaContent, criteria, mediaContentType);
        }
    })
}
function displaySuggestions(suggestions, criteria, mediaContentType) {
    if (!suggestions || suggestions.length === 0) {
        return;
    }

    const suggestionsList = $("#" + mediaContentType + "-suggestions-lists")
    const text = criteria === "location" ? "in your country" : "with the same age";
    const suggestionTitle = $("<h2>").addClass("suggestion-title").text("Other users " + text + " also like:");
    const container = $("<div>").addClass("project-boxes jsGridView");
    suggestions.forEach(item => {
        const suggestionWrapper = $("<div>").addClass("project-box-wrapper");
        const itemDiv = $("<div>").addClass("project-box");
        const title = $("<a>").attr("href", contextPath + "/" + mediaContentType + "?mediaId=" + item.id)
            .addClass("box-title").text(item.title);

        itemDiv.append(title);
        suggestionWrapper.append(itemDiv);
        container.append(suggestionWrapper);
    });
    suggestionsList.append(suggestionTitle, container);
}

////////////////////
//USER SUGGESTIONS//
////////////////////

const suggestedUsers = $("#suggested-users");
const showSuggestedUsersButton = $("#show-suggested-users");

showSuggestedUsersButton.click(() => {
    if ($("#suggested-by-likes-list").is(":empty") && $("#suggested-by-followings-list").is(":empty")) {
        getSuggestedUsers();
    } else {
        overlay.show();
        $("body").css("overflow-y", "hidden");
        suggestedUsers.show();
    }
});

function showSuggestedUsers(suggestions, targetDiv) {
    overlay.show();
    $("body").css("overflow-y", "hidden");
    suggestedUsers.show();
    overlay.click(() => {
        overlay.hide();
        $("body").css("overflow-y", "auto");
        suggestedUsers.hide();
    });
    const container = $(targetDiv);
    container.empty();

    if (!suggestions || suggestions.length === 0) {
        container.append(
            $("<p>").addClass("no-results-error").text("No suggestions found")
        );
        return;
        }

    suggestions.forEach(item => {
            //const suggestionWrapper = $("<div>").addClass("project-box-wrapper");
            const itemDiv = $(`<a href="${contextPath}/profile?userId=${item.id}">`).addClass("user");
            const img = $(`<img src="${item.profilePicUrl}">`).addClass("user-pic")
                .on("error", () => setDefaultProfilePicture(img));
            const p = $("<p>").addClass("user-username").text(item.username);
            itemDiv.append(img, p);
            //suggestionWrapper.append(itemDiv)
            container.append(itemDiv);
        }
    );
    }

function getSuggestedUsers() {
    const suggestions = [
        { type: "likes", targetDiv: "#suggested-by-likes-list" },
        { type: "following", targetDiv: "#suggested-by-followings-list" }
    ];

    suggestions.forEach(suggestion => {
        $.post(`${contextPath}/profile`, {
            action: "suggestedUsers",
            userId: profile.userId,
            suggestionType: suggestion.type
        }, function (data) {
            if (data.success) {
                showSuggestedUsers(data.suggestedUsers, suggestion.targetDiv);
            } else if (data.notFoundError) {
                $(suggestion.targetDiv).append(
                    $("<p>").addClass("no-results-error").text("No suggestions found")
                );
            } else {
                $(suggestion.targetDiv).append($("<p>").addClass("error").text(data.error));
            }
        }).fail(function () {
            $(suggestion.targetDiv).append(
                $("<p>")
                    .addClass("error")
                    .text("An error occurred while fetching suggestions.")
            );
        });
    });
}


