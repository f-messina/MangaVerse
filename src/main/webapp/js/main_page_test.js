function setDefaultCover(image) {
    image.off("error");
    image.attr("src", mediaType === "manga" ? mangaDefaultImage : animeDefaultImage);
}

$(document).ready(function () {
    const navBar = $("#navbar");
    const sectionHome = $('#welcome-section');

    $('#down-arrow').click(function() {
        navBar[0].scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
    });

    $('#logo').click(function() {
        const scrollPoint = $('#main-section');
        window.scrollTo({ top: scrollPoint.offset().top, behavior: 'smooth' });
        sectionHome[0].scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
    });
});

let currentPage, totalPages;

// get the query results based on the selected filters
function getMediaContent(page = 1) {
    const filters = createFilterParams();
    filters.page = page;
    const resultsContainer = $(".results");
    $.post(contextPath + "/mainPage/" + mediaType, filters, function (data) {
        if (data.success) {
            currentPage = page;
            totalPages = data.mediaPage.totalPages;
            updatePagination();
            resultsContainer.empty();
            data.mediaPage.entries.forEach(media => {
                const mediaCard = $("<div>").addClass("media-card");
                const mediaImgLink = $("<a>").addClass("cover").attr("href", contextPath + "/" + mediaType + "?mediaId=" + media.id);
                const mediaImg = $("<img>").attr("src", media.imageUrl).attr("alt", media.title).addClass("image loaded")
                    .on("error", () => setDefaultCover(mediaImg, mediaType));
                mediaImgLink.append(mediaImg);
                const title = $("<a>").attr("href", contextPath + "/" + mediaType + "?mediaId=" + media.id).addClass("title").text(media.title);
                const hoverBox = $("<div>").addClass("hover-data right");
                const scoreValue = media.averageRating === undefined ? "N/A" : media.averageRating;
                const score = $("<div>").addClass("score header").text("Score: " + scoreValue);
                const likes = $("<div>").addClass("likes header");
                const heartIcon = $('<svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="heart" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="icon svg-inline--fa fa-heart fa-w-16 fa-xs"><path fill="currentColor" d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z" class=""></path></svg>');
                const nLikes = $("<span>").addClass("n-likes").text(media.likes);
                likes.append(heartIcon, nLikes);
                if (mediaType === "manga") {
                    const startDate = $("<div>").addClass("start-date").text(media.startDate);
                    const separator = $("<div>").addClass("separator").text("-");
                    const endDate = $("<div>").addClass("end-date").text(media.endDate);
                    if (media.startDate === null) {
                        startDate.text("N/A");
                    }
                    if (media.endDate === null) {
                        endDate.text("N/A");
                    }
                    hoverBox.append(score, likes, startDate, separator, endDate);
                } else {
                    const season = $("<div>").addClass("season").text("Season: " + media.season);
                    const year = $("<div>").addClass("year").text("Year: " + media.year);
                    hoverBox.append(score, likes, season, year);
                }

                mediaCard.append(mediaImgLink, title, hoverBox);
                resultsContainer.append(mediaCard);
                if (hoverBox.offset().left + hoverBox.width() > resultsContainer.offset().left + resultsContainer.width()) {
                    hoverBox.removeClass("right");
                    hoverBox.addClass("left");
                }
            });
        } else {
            console.log(data.error);
            alert(data.error);
        }
    }).fail(function () {
        alert("Error occurred during the asynchronous request");
    });
}

function updatePagination() {
    const pagination = $(".pagination");
    const action = "search";

    pagination.empty();
    if (totalPages === 1) return;

    const createPageButton = (pageNumber, isActive = false) => {
        const btn = $("<li>").addClass("page__numbers").text(pageNumber);
        if (isActive) btn.addClass("active");
        else btn.click(() => getMediaContent(pageNumber));
        return btn;
    };

    const createArrowButton = (direction, enabled, targetPage) => {
        const arrow = $("<li>").addClass("page__btn").html(`<span class="material-icons">chevron_${direction}</span>`);
        if (enabled) arrow.addClass("active").click(() => getMediaContent(targetPage));
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