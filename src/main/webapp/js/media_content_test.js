const likeButton = $("#like-button");

function toggleLike() {
    const requestData = {
        action: "toggleLike",
        mediaId: mediaId
    };
    $.post(contextPath + "/" + mediaType, requestData, () =>
        likeButton.toggleClass("isFavourite")
    ).fail(() => console.error("Error occurred while toggling like"));
}

likeButton.click(() => toggleLike());