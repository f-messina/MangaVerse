const likeButton = $("#like-button");

function toggleLike() {
    const requestData = {
        action: "toggleLike",
        mediaId: media.id,
    };
    $.post(contextPath + "/" + media.type, requestData, () =>
        likeButton.toggleClass("isFavourite")
    ).fail(() => console.error("Error occurred while toggling like"));
}

likeButton.click(() => toggleLike());

/////////////////////
// details section //
/////////////////////

const sectionButtons = $(".link");
const sections = $(".content-section");

sectionButtons.click(function() {
    if ($(this).hasClass("router-link-exact-active")) return;
    sectionButtons.toggleClass("router-link-exact-active router-link-active");
    sections.toggleClass("active")
});

////////////////////
// review section //
////////////////////

function validateRating(input) {
    let value = input.val();
    // Remove any non-numeric characters
    value = value.replace(/[^0-9]/g, '');

    // Convert to number and validate range
    let number = parseInt(value, 10);

    if (isNaN(number) || number < 0) {
        number = '';  // Clear the input if the value is invalid
    } else if (number > 10) {
        number = number % 10;  // Clamp the value to 10
    }

    // Update the input value
    input.val(number);
}

function changeRating(input, delta) {
    let value = parseInt(input.val(), 10);
    if (isNaN(value)) {
        value = 0;
    }
    value += delta;
    if (value < 0) {
        value = 0;
    } else if (value > 10) {
        value = 10;
    }
    input.val(value);
}

function checkChanges() {
    const rating = $('#update-rating').val();
    const comment = $('#update-comment').val();
    return !(rating === userReview.rating && comment === userReview.comment);
}

function addReview() {
    const rating = $('#add-rating').val();
    const comment = $('#add-comment').val();
    const requestData = {
        action: "addReview",
        mediaId: media.id,
        mediaTitle: media.title,
        mediaType: media.type,
        rating: rating,
        comment: comment
    };

    $.post(contextPath + "/" + media.type, requestData, function (data) {
        if (data.success) {
            location.reload();
        }
    }).fail(() => console.error("Error occurred while adding review"));
}

function updateReview() {
    if (!checkChanges()) {
        return;
    }

    const rating = $('#update-rating').val();
    const comment = $('#update-comment').val();
    const requestData = {
        action: "updateReview",
        reviewId: userReview.id,
        mediaId: media.id,
        mediaType: media.type,
        rating: rating,
        comment: comment
    };
    console.log(requestData);

    $.post(contextPath + "/" + media.type, requestData, function (data) {
        if (data.success) {
            console.log(data);
            location.reload();
        }
    }).fail(() => console.error("Error occurred while updating review"));
}

function deleteReview() {
    const requestData = {
        action: "deleteReview",
        reviewId: userReview.id,
        mediaId: media.id,
        mediaType: media.type,
        latestReviewsIds: JSON.stringify(media.latestReviewsIds),
        reviewsIds: JSON.stringify(media.reviewIds)
    };

    $.post(contextPath + "/" + media.type, requestData, function (data) {
        if (data.success) {
            location.reload();
        }
    }).fail(() => console.error("Error occurred while deleting review"));
}

$(document).ready(function() {

    $('.input-rating').on('input', function() {
        validateRating($(this));
    });

    $('.minus-button').on('click', function() {
        const input = $(this).closest('.rating-container').find('.input-rating');
        changeRating(input, -1);
    });

    $('.plus-button').on('click', function() {
        const input = $(this).closest('.rating-container').find('.input-rating');
        changeRating(input, 1);
    });

    $('.review-button.toggle-form').click(function() {
        $(this).closest('.review-container').find('.review-form').toggleClass('active');
    });

    $('.review-button.send').click(function() {
        if ($(this).attr('id') === 'send-new-review') {
            addReview();
        } else {
            updateReview();
        }
    });

    $('#delete-review-button').click(deleteReview);
});


const reviewButton = $("#show-review-button");
let totalPages;
let currentPage;

function showReviews() {
    const latestReviews = $("#latest-reviews");
    const allReviews = $("#all-reviews");
    const pagination = $(".container-pagination");

    latestReviews.toggle();
    if (latestReviews.is(":visible")) {
        reviewButton.text("Show more");
        allReviews.hide();
        pagination.hide();
    } else {
        allReviews.show();
        pagination.show();
        reviewButton.text("Show latest reviews");
        if (allReviews.children().length === 0) {
            getReviews();
        }
    }
}

function getReviews(page = 1) {
    const requestData = {
        action: "getReviews",
        reviewIds: JSON.stringify(media.reviewIds),
        page: page
    };
    console.log(requestData);

    $.post(contextPath + "/" + media.type, requestData, function (data) {
        if (data.success) {
            console.log(data);
            const reviews = $("#all-reviews");
            currentPage = page;

            reviews.empty();

            totalPages = data.reviews.totalPages;
            updatePagination();

            data.reviews.entries.forEach(review => {
                const reviewBox = $("<div>").addClass("review-box");
                const reviewPicture = $("<div>").addClass("review-picture");
                const profilePic = $("<img>").attr("src", review.user.profilePicUrl ? review.user.profilePicUrl : userDefaultImage);
                reviewPicture.append(profilePic);
                const reviewInfo = $("<div>").addClass("review-info");
                const reviewRow = $("<div>").addClass("review-row");
                const reviewMediaTitle = $("<a>").attr("href", contextPath + "/profile?userId=" + review.user.id)
                    .addClass("review-media-title").text(review.user.username);
                const reviewRating = $("<p>").addClass("review-rating").text(review.rating ? review.rating : "N/A");
                reviewRow.append(reviewMediaTitle, reviewRating);
                const reviewComment = $("<p>").addClass("review-comment").text(review.comment ? review.comment : "N/A");
                const reviewDate = $("<p>").addClass("review-date").text(review.date);
                reviewInfo.append(reviewRow, reviewComment, reviewDate);
                reviewBox.append(reviewPicture, reviewInfo);
                reviews.append(reviewBox);
            });
        }
    }).fail(() => console.error("Error occurred while fetching reviews"));
}

function updatePagination() {
    const pagination = $(".pagination");
    pagination.empty();
    if (totalPages === 1) return;

    // Add class to display the pagination correctly
    if (totalPages > 999) pagination.addClass("x-large");
    else if (totalPages > 99) pagination.addClass("large");
    else if (totalPages > 9) pagination.addClass("medium");
    else pagination.removeClass("medium large x-large");

    const createPageButton = (pageNumber, isActive = false) => {
        const btn = $("<li>").addClass("page__numbers").text(pageNumber);
        if (isActive) btn.addClass("active");
        btn.click(() => getReviews(pageNumber));
        return btn;
    };

    const createArrowButton = (direction, enabled, targetPage) => {
        const arrow = $("<li>").addClass("page__btn").html(`<span class="material-icons">chevron_${direction}</span>`);
        if (enabled) arrow.addClass("active").click(() => getReviews(targetPage));
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

    if (totalPages > 1)
        pagination.append(createArrowButton("right", currentPage < totalPages, currentPage + 1));
}

reviewButton.click(() => {
    showReviews();
});