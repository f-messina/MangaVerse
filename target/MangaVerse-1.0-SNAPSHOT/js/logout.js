initLogout();
function initLogout() {
    $("#logoutBtn").on("click", function(event) {
        event.preventDefault();
        const logoutHref = $(this).attr("href");
        const inputData = { action: "logout", targetJSP: logoutHref };
        $.post(authURI, inputData).fail(function(xhr, status, error) { console.error("Logout request failed:", status, error)});
    });
}