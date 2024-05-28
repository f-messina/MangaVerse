function toggleRadio(element) {
    if (element.classList.contains("active")) {
        element.checked = false;
    }
    element.classList.toggle("active");
    let radios = document.getElementsByName(element.name);
    radios.forEach(radio => {
        if (radio !== element) {
            radio.classList.remove("active");
        }
    });
}

function toggleFiltersDisplay() {
    const filtersFormContainer = $("#filtersFormContainer");
    filtersFormContainer.css("display", filtersFormContainer.css("display") === "none" ? "flex" : "none");
}

function performAsyncSearch(formId) {
    const form = $("#" + formId);
    const url = form.attr("action");
    const formData = form.serialize();

    $.post(url, formData, function (data) {
        $("#totalResults").text("Total Results: " + data.mediaContentList.totalCount);
        $("#orderSelection").empty();
        $("#mediaContentContainer").empty();
        $("#pageSelection").empty();

        updateOrderSelection(data, formId);
        updateMediaContent(data.mediaContentList.entries);
        updatePageBar(data, formId);
    }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
}

function performAsyncOrderChange(formId, containerId, selectedOrder) {
    const form = $("#" + formId);
    const formData = form.serialize().replace(/&orderBy=[^&]*/, '') + "&orderBy=" + selectedOrder;

    $.post(form.attr("action"), formData, function (data) {
        $("#orderBy").val(selectedOrder);
        updateMediaContent(data.mediaContentList.entries);
        updatePageBar(data, formId);
    }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
}

function performAsyncPagination(formId, containerId, page) {
    const $form = $("#" + formId);
    const formData = $form.serialize() + "&page=" + page;

    $.post($form.attr("action"), formData, function (data) {
        updateMediaContent(data.mediaContentList.entries);
        updatePageBar(data, formId);
    }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
}

function updateOrderSelection(data, formId) {
    const options = [
        {value: "title 1", text: "Title enc"},
        {value: "title -1", text: "Title dec"},
        {value: "average_rating 1", text: "Average Rating enc"},
        {value: "average_rating -1", text: "Average Rating dec"}
    ];
    let servletPath;
    if (servletURI.includes("manga")) {
        options.push({value: "start_date 1", text: "Start Date enc"});
        options.push({value: "start_date -1", text: "Start Date dec"});
        servletPath = "mainPage/manga";

    } else {
        options.push({value: "anime_season.season 1", text: "Season enc"});
        options.push({value: "anime_season.season -1", text: "Season dec"});
        servletPath = "mainPage/anime";
    }

    const orderContainer = $("#orderSelection").empty();
    $("<form>").attr({ id: "orderForm", action: servletPath, method: "post" }).on("change", () =>
        performAsyncOrderChange(formId, "mediaContentContainer", $("#orderResults").val())
    ).append(
        $("<input>").attr({ type: "hidden", name: "action", value: "sortAndPaginate" }),
        $("<label>").attr("for", "orderResults").text("Order By:"),
        $("<select>").attr({ name: "orderBy", id: "orderResults" }).append(
            options.map(option => $("<option>").attr("value", option.value).text(option.text).prop("selected", data.orderBy === option.value))
        )
    ).appendTo(formId === "filterForm" || isSearchFormEmpty(formId) ? orderContainer : "");
}

function isSearchFormEmpty(formId) {
    return formId === "searchForm" && $("#search").val().trim() === "";
}

// Update media content in the specified container
function updateMediaContent(mediaList) {
    const mediaContentContainer = $("#mediaContentContainer").empty();
    mediaList.forEach(media => {
        const mediaWrapper = $("<div>").addClass("project-box-wrapper");
        const mediaBox = $("<div>").addClass("project-box");
        const picture = $("<img>").attr("src", media.imageUrl).attr("alt", media.title)
            .addClass("box-image")
            .on("error", () => setDefaultCover(this));
        const title = $("<a>").attr("href", `${contextPath}/${isAnime ? "anime" : "manga"}?mediaId=${media.id}`)
            .addClass("box-title").text(media.title);

        mediaBox.append(picture, title);
        mediaWrapper.append(mediaBox);
        likeList.append(mediaWrapper);
    });
}

function setDefaultCover(image) {
    $(image).off("error");
    $(image).attr("src", mediaType === "anime" ? animeDefaultImage : mangaDefaultImage);
}

// Create HTML element for a media article
function createArticleElement(media) {
    const articleElement = $("<article>").append(
        $("<a>").text(media.title).attr("href", mediaDetailHRef + media.id),
        $("<img>").attr({ src: media.imageUrl, alt: "No image" }),
        (media.averageRating !== null ? [$("<p>").text("Score: " + media.averageRating)] : [])
    );

    if (servletURI.includes("manga")) {
        articleElement.append(
            (media.startDate !== null ? [$("<p>").text("Start Date: " + media.startDate)] : []),
            (media.endDate !== null ? [$("<p>").text("End Date: " + media.endDate)] : [])
        );
    } else {
        articleElement.append(
            (media.season !== null ? [$("<p>").text("Season: " + media.season)] : []),
            (media.year !== null ? [$("<p>").text("Year: " + media.year)] : [])
        );
    }

    // Add interaction buttons if authenticated user
    if (authenticatedUser) {
        articleElement.append(createLikeButton(media));
    }

    return articleElement;
}

// Create like button for a media article
function createLikeButton(media) {
    const likeButton = $("<button>").addClass("like-button").on("click", () => toggleLike(media, likeButton));
    likeButton[media.isLiked ? "addClass" : "removeClass"]("liked").append(createHeartSVG());
    return likeButton;
}

// Create heart SVG element
function createHeartSVG() {
    return $(`<svg class="heart" width="24" height="24" viewBox="0 0 24 24"><path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"></path></svg>`);
}

// Toggle like interaction for a media article
function toggleLike(media, likeButton) {
    const requestData = { action: "toggleLike", mediaId: media.id, mediaTitle: media.title, mediaImageUrl: media.imageUrl };
    $.post(servletURI, requestData, () => likeButton.toggleClass("liked"), "json").fail(() => console.error("Error occurred during the asynchronous request"));
}

function updatePageBar(data, formId) {
    const pageSelection = $("#pageSelection").empty();
    const form = $("<form>", { action: servletURI, method: "post" }).appendTo(pageSelection);

    $("<input>", { type: "hidden", name: "action", value: "sortAndPaginate" }).appendTo(form);

    const createButton = (value, text) =>
        $("<button>", { type: "button", class: "navigation-button", name: "page", value })
            .text(text)
            .on("click", () => performAsyncPagination(formId, "mediaContentContainer", value))
            .appendTo(form);

    if (data.page > 1) createButton(data.page - 1, "Previous Page");
    if (data.page < data.mediaContentList.totalPages) createButton(data.page + 1, "Next Page");
}

$(document).ready(function () {
    // Bind the searchForm submission to the performAsyncSearch function
    $("#searchForm").submit(function (event) {
        event.preventDefault(); // Prevent the default form submission
        performAsyncSearch("searchForm");
    });

    // Bind the filterForm submission to a different function
    $("#filterForm").submit(function (event) {
        event.preventDefault(); // Prevent the default form submission
        performAsyncSearch("filterForm");
    });
});

$(document).ready(function () {
    const navBar = $("#navbar");
    const sectionHome = $('#welcome-section');
    const searchDiv = $('#search-div');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                // Element is in the viewport
                navBar.removeClass("fixed-nav");
                searchDiv.css('margin-top', '0');
            } else {
                // Element is out of the viewport
                navBar.addClass("fixed-nav");
                searchDiv.css('margin-top', '100px');
            }
        });
    });

    // Start observing changes in the sectionHome
    observer.observe(sectionHome[0]);

    $('#down-arrow').click(function() {
        navBar[0].scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
    });

    $('#up-arrow').click(function() {
        const scrollPoint = $('#main-section');
        window.scrollTo({ top: scrollPoint.offset().top, behavior: 'smooth' });
        sectionHome[0].scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
    });
});

///////////////////
// MEDIA SECTION //
///////////////////

let mangaPage, totalMangaPages;
let animePage, totalAnimePages;

function fetchData(action, page = 1) {
    $.post(`${contextPath}/profile`, { action, userId, page }, (data) => {
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
        const picture = $("<img>").attr("src", media.imageUrl).attr("alt", media.title)
            .addClass("box-image")
            .on("error", () => setDefaultCover(this, action));
        const title = $("<a>").attr("href", `${contextPath}/${isAnime ? "anime" : "manga"}?mediaId=${media.id}`)
            .addClass("box-title").text(media.title);

        mediaBox.append(picture, title);
        mediaWrapper.append(mediaBox);
        likeList.append(mediaWrapper);
    });
}