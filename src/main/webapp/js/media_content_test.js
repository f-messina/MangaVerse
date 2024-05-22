const likeButton = $("#like-button");

function toggleLike() {
    console.log("Toggling like");
    const requestData = {
        action: "toggleLike",
        mediaId: mediaId
    };
    $.post(servletURI, requestData, () =>
        likeButton.toggleClass("liked")
    ).fail(() => console.error("Error occurred while toggling like"));
}

likeButton.click(() => toggleLike());