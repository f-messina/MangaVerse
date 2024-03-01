<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 28/02/2024
  Time: 14:15
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>MAIN PAGE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/range_input.css">
    <script src="${pageContext.request.contextPath}/js/range_input.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
</head>
<body>
<form id="searchForm" action="${pageContext.request.contextPath}/mainPage/anime" method="post">
    <input type="hidden" name="action" value="search">
    <label for="search">Title:</label>
    <input type="search" id="search" name="searchTerm" placeholder="Title">
    <input type="submit" value="SEARCH">
</form>
<form id="filterForm" action="${pageContext.request.contextPath}/mainPage/anime" method="post" style="width: 15rem">
    <input type="hidden" name="action" value="search">

    <%-- This are the radios for the tags --%>
    <label>Genres:</label><br/>
    <c:forEach items="${requestScope.animeTags}" var="tag">
        <div>
            <input type="radio" name="${tag}" style="color: green" onclick="toggleRadio(this)" value="select">
            <input type="radio" name="${tag}" style="color: red" onclick="toggleRadio(this)" value="avoid">
            <label>${tag}</label>
        </div>
    </c:forEach>
    <div>
        <label>Operator:</label>
        <input type="radio" name="genreOperator" checked value="and">and
        <input type="radio" name="genreOperator" value="or">or
    </div>

    <%-- This are the checkboxes for the types --%>
    <div>
        <label>Type:</label>
        <c:forEach var="entry" items="${requestScope.animeTypes}">
            <c:if test="${entry.name() != 'UNKNOWN'}">
                <div>
                    <input type="checkbox" id="${entry.name()}" name="animeTypes" value="${entry.name()}">
                    <label for="${entry.name()}">${entry.toString()}</label>
                </div>
            </c:if>
        </c:forEach>
    </div>

    <%-- This are the checkboxes for the status --%>
    <div>
        <label>Publishing status:</label>
        <c:forEach var="entry" items="${requestScope.animeStatus}">
            <div>
                <input type="checkbox" id="${entry.name()}" name="status" value="${entry.name()}">
                <label for="${entry.name()}">${entry.toString()}</label>
            </div>
        </c:forEach>
    </div>
    <%-- This are the range inputs for the min and max score --%>
    <div>
        <label>Rating:</label>
        <div class="range-slider container">
            <span class="output outputOne"></span>
            <span class="output outputTwo"></span>
            <span class="full-range"></span>
            <span class="incl-range"></span>
            <input name="minScore" value="0" min="0" max="10" step="0.1" type="range">
            <input name="maxScore" value="10" min="0" max="10" step="0.1" type="range">
        </div>
    </div>

    <div>
        <label>
            <input type="checkbox" id="yearRangeCheckbox"> Choose Year Range
        </label>
    </div>

    <%-- This is the selection of an anime season --%>
    <div id="singleYearDiv">
        <label for="season">Season:</label>
        <select name="season" id="season">
            <option value="WINTER">Winter</option>
            <option value="SPRING">Spring</option>
            <option value="SUMMER">Summer</option>
            <option value="FALL">Fall</option>
        </select>
        <br/>
        <label for="year">Year:</label>
        <input type="number" id="year" name="year" step="1">
    </div>

    <%-- This are the range inputs for the min and max start year --%>
    <div id="yearRangeDiv" class="year-range">
        <label for="minYear">Start Year:</label>
        <input type="number" id="minYear" name="minYear" step="1" >
        <br/>
        <label for="maxYear">End Year:</label>
        <input type="number" id="maxYear" name="maxYear" step="1">
    </div>

    <div>
        <label for="orderBy">Order By:</label>
        <select name="orderBy" id="orderBy">
            <option value="title 1">Title enc</option>
            <option value="title -1">Title dec</option>
            <option value="average_rating 1">Average Rating enc</option>
            <option value="average_rating -1">Average Rating dec</option>
            <option value="anime_season.year 1">Year enc</option>
            <option value="anime_season.year -1">Year dec</option>
        </select>
    </div>

    <input type="submit" value="SEARCH">
</form>

<section id="resultsSection"></section>

<!-- page bar -->
<div>
    <form action="${pageContext.request.contextPath}/mainPage/anime" method="post">
        <input type="hidden" name="action" value="sortAndPaginate">

        <c:if test="${requestScope.page > 1}">
            <button type="submit" class="navigation-button" name="page" value="${requestScope.page - 1}">Previous Page</button>
        </c:if>
        <c:if test="${requestScope.page < requestScope.mediaContentPage.getTotalPages()}">
            <button type="submit" class="navigation-button" name="page" value="${requestScope.page + 1}">Next Page</button>
        </c:if>
    </form>
</div>

<script>
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

    function toggleYearRangeInputs(checked) {
        const singleYearDiv = document.getElementById('singleYearDiv');
        const yearRangeDiv = document.getElementById('yearRangeDiv');
        const minYearInput = document.getElementById('minYear');
        const maxYearInput = document.getElementById('maxYear');
        const yearInput = document.getElementById('year');
        const seasonInput = document.getElementById('season');

        if (checked) {
            singleYearDiv.style.display = 'none';
            yearRangeDiv.style.display = 'block';
            minYearInput.disabled = false;
            maxYearInput.disabled = false;
            yearInput.disabled = true;
            seasonInput.disabled = true;
        } else {
            singleYearDiv.style.display = 'block';
            yearRangeDiv.style.display = 'none';
            minYearInput.disabled = true;
            maxYearInput.disabled = true;
            yearInput.disabled = false;
            seasonInput.disabled = false;
        }
    }

    const yearRangeCheckbox = document.getElementById('yearRangeCheckbox');
    toggleYearRangeInputs(yearRangeCheckbox.checked);
    yearRangeCheckbox.addEventListener('change', function () {
        toggleYearRangeInputs(this.checked);
    });

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
        const formData = $("#" + formId).serialize() + "&page=" + page;

        $.post($("#" + formId).attr("action"), formData, function (data) {
            updateMediaContent(data, containerId);
            updatePageBar(data, formId);
        }, "json").fail(() => console.error("Error occurred during the asynchronous request"));
    }

    function updateOrderSelection(data, formId) {
        const options = [
            { value: "title 1", text: "Title enc" },
            { value: "title -1", text: "Title dec" },
            { value: "average_rating 1", text: "Average Rating enc" },
            { value: "average_rating -1", text: "Average Rating dec" },
            { value: "anime_season.year 1", text: "Year enc" },
            { value: "anime_season.year -1", text: "Year dec" }
        ];
        const orderContainer = $("#orderSelection").empty();
        $("<form>").attr({ id: "orderForm", action: "mainPage/anime", method: "post" }).on("change", () =>
            performAsyncOrderChange(formId, "mediaContentContainer", $("#orderResults").val())
        ).append(
            $("<input>").attr({ type: "hidden", name: "action", value: "sortAndPaginate" }),
            $("<label>").attr("for", "orderResults").text("Order By:"),
            $("<select>").attr({ name: "orderBy", id: "orderResults" }).append(
                options.map(option => $("<option>").attr("value", option.value).text(option.text).prop("selected", data.orderBy === option.value))
            )
        ).appendTo(formId === "filterForm" || isSearchFormEmpty(formId) ? orderContainer : "");
    }

    function updateMediaContent(data, containerId) {
        const mediaContentPage = data.mediaContentList;
        const mediaContentContainer = $("#" + containerId).empty();

        mediaContentContainer.append(
            mediaContentPage.entries.map(anime => $("<article>").append(
                $("<h2>").text(anime.title),
                $("<img>").attr({ src: anime.imageUrl, alt: "No image" }),
                anime.averageRating !== null ? $("<p>").text("Score: " + anime.averageRating) : "",
                anime.season !== null ? $("<p>").text("Season: " + anime.season) : "",
                anime.year !== null ? $("<p>").text("Year: " + anime.year) : ""
            ))
        );
    }

    function updatePageBar(data, formId) {
        const pageSelection = $("#pageSelection").empty();
        const form = $("<form>", { action: "mainPage/anime", method: "post" }).appendTo(pageSelection);

        $("<input>", { type: "hidden", name: "action", value: "sortAndPaginate" }).appendTo(form);

        const createButton = (value, text) =>
            $("<button>", { type: "button", class: "navigation-button", name: "page", value })
                .text(text)
                .on("click", () => performAsyncPagination(formId, "mediaContentContainer", value))
                .appendTo(form);

        if (data.page > 1) createButton(data.page - 1, "Previous Page");
        if (data.page < data.mediaContentList.totalPages) createButton(data.page + 1, "Next Page");
    }

    function isSearchFormEmpty(formId) {
        return formId === "searchForm" && $("#search").val().trim() === "";
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
</script>
</body>
</html>
