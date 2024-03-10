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
    const x = document.getElementById("filtersFormContainer");
    x.style.display = x.style.display === "none" ? "block" : "none";
}

function performAsyncSearch(formId, containerId) {
    const form = $("#" + formId);
    const url = form.attr("action");
    const formData = form.serialize();

    $.post(url, formData, function (data) {
        const container = $("#" + containerId).empty();
        container.append(
            $("<h1>").text("Total results: " + data.mediaContentList.totalCount),
            $("<div>").attr("id", "orderSelection"),
            $("<div>").attr("id", "mediaContentContainer"),
            $("<div>").attr("id", "pageSelection")
        );

        updateOrderSelection(data, formId);
        updateMediaContent(data, "mediaContentContainer");
        updatePageBar(data, formId);
    }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
}

function performAsyncOrderChange(formId, containerId, selectedOrder) {
    const form = $("#" + formId);
    const formData = form.serialize().replace(/&orderBy=[^&]*/, '') + "&orderBy=" + selectedOrder;

    $.post(form.attr("action"), formData, function (data) {
        $("#orderBy").val(selectedOrder);
        updateMediaContent(data, containerId);
        updatePageBar(data, formId);
    }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
}

function performAsyncPagination(formId, containerId, page) {
    const $form = $("#" + formId);
    const formData = $form.serialize() + "&page=" + page;

    $.post($form.attr("action"), formData, function (data) {
        updateMediaContent(data, containerId);
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
function updateMediaContent(data, containerId) {
    const mediaContentContainer = $("#" + containerId).empty();
    mediaContentContainer.append(data.mediaContentList.entries.map(media => createArticleElement(media)));
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
        articleElement.append(createLikeButton(media), createListButton(media));
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

// Create list button for a media article
function createListButton(media) {
    return $("<button>").addClass("list-button").text("Add to list").on("click", () => showListPopup(media));
}

// Show a popup with list options for a media article
function showListPopup(media) {
    const popupContainer = $("<div>").addClass("popup-container");
    const popup = $("<div>").addClass("list-popup");

    // Add list option buttons
    lists.forEach(list => popup.append($("<button>").addClass("list-option-button").text(list[1]).on("click", () => addElementToList(media, list[0]).then(() => {
        console.log("Element added to the list:", list);
        popupContainer.empty().hide();
    }).catch(error => console.error("Error adding element to the list:", error)))));

    popupContainer.empty().append(popup).appendTo("body").show();

    // Close the popup when clicking outside of it
    popupContainer.on("click", e => e.target === popupContainer[0] && popupContainer.empty().hide());
}

// Add a media element to a specified list
function addElementToList(element, listId) {
    const input = { action: "addToList", mediaId: element.id, mediaTitle: element.title, mediaImageUrl: element.imageUrl, listId };
    console.log("Adding element to list:", input);
    return $.post(servletURI, input, data => data.alreadyInList ? console.log("Element already in list") : console.log("Element added to list"));
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
        performAsyncSearch("searchForm", "resultsSection");
    });

    // Bind the filterForm submission to a different function
    $("#filterForm").submit(function (event) {
        event.preventDefault(); // Prevent the default form submission
        performAsyncSearch("filterForm", "resultsSection");
    });
});