$(document).ready(function() {
    $(".landing-section img").on("error", function() {
        setDefaultCover($(this));
    });
    // Check if the image has already failed to load after a delay
    setTimeout(function() {
        $(".landing-section img").each(function() {
            if (!this.complete || this.naturalWidth === 0) {
                setDefaultCover($(this));
            }
        });
    }, 500); // Adjust the delay time (in milliseconds) as needed
});

function setDefaultCover(image, type = null) {
    image.off("error");
    if (type === null)
        type = mediaType;
    image.attr("src", type === "manga" ? mangaDefaultImage : animeDefaultImage);
}