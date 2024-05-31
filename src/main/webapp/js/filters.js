
// filter functions

const titleFilter = $("#title-filter");
const selectWrap = $(".select-wrap");
const selectInputFilters = $(".select-wrap input");
const genreCheckboxType = $("#genre-checkbox-type");
const removeSelectionButton = $(".filter-select .close");

removeSelectionButton.on("click", function (event) {
    event.stopPropagation();
    if ($(this).is("#title-remove-button")) {
        titleFilter.val("");
        $(this).hide();
    } else {
        const selectWrap = $(this).closest(".select-wrap");
        selectWrap.find(".option").removeClass("selected");
        selectWrap.find(".option").removeClass("avoided");
        selectWrap.find(".options").hide();
        updateValueWrap(selectWrap);
    }
    $(this).closest(".filter-select").removeClass("active");
    toggleClearAllButton();
    getMediaContent();
});

titleFilter.on("input", function () {
    if ($(this).val() !== "") {
        titleFilter.closest(".filter-select").addClass("active");
        $("#title-remove-button").show();
        $(".filter.clear-all").show();
    } else {
        titleFilter.closest(".filter-select").removeClass("active");
    }
    toggleClearAllButton();
    getMediaContent();
});

selectWrap.click(function () {
    // Hide options of all other select-wraps
    $(".options").hide();

    // Show options of the clicked select-wrap
    $(this).find(".options").show();
});

$("#extra-filters-button").click(function () {
    $("#extra-filters").toggle();
});

$(document).ready(function () {
    genreCheckboxType.prop("checked", false);

    $(document).click(function (event) {
        if (!$(event.target).closest('.select-wrap').length) {
            $(".options").hide();
        }

        if (!$(event.target).closest("#extra-filters-button").length && !$(event.target).closest("#extra-filters").length) {
            $("#extra-filters").hide();
        }
    });

    $('.range-wrap').each(function () {
        const rangeWrap = $(this);
        const handles = rangeWrap.find('.handle');
        const rail = rangeWrap.find('.rail');

        let isDragging = false;
        let currentHandle = null;

        handles.on('mousedown', function (e) {
            isDragging = true;
            currentHandle = $(this);
            currentHandle.addClass('active');
            e.preventDefault(); // Prevent text selection
        });

        $(document).on('mousemove', function (e) {
            if (!isDragging) return;

            const railOffset = rail.offset();
            const railWidth = rail.width();
            const handleIndex = handles.index(currentHandle);
            const otherHandlePos = parseInt(rangeWrap.css("--handle-" + (1 - handleIndex) + "-position"), 10);
            let newPosition = e.pageX - railOffset.left;

            // Constrain the handle within the rail and prevent overlap
            if (newPosition < 0) newPosition = 0;
            else if (newPosition > railWidth) newPosition = railWidth;
            if (handleIndex === 0 && newPosition > otherHandlePos - 3) newPosition = otherHandlePos - 3;
            else if (handleIndex === 1 && newPosition < otherHandlePos + 3) {
                newPosition = otherHandlePos + 3;
            }

            if (currentHandle.hasClass('handle-0')) {
                rangeWrap.css('--handle-0-position', newPosition + 'px');
            } else {
                rangeWrap.css('--handle-1-position', newPosition + 'px');
            }
            rangeWrap.css('--active-region-width', parseInt(rangeWrap.css("--handle-1-position"), 10) - parseInt(rangeWrap.css("--handle-0-position"), 10) + 'px');
            const offset = Math.round(((parseInt(currentHandle.css('left'), 10) + 6) / railWidth) * (rangeWrap.css('--max-val') - rangeWrap.css('--min-val')));
            const min = parseInt(rangeWrap.css('--min-val'), 10);
            currentHandle.attr('value', min + offset);
        });

        $(document).on('mouseup', function () {
            if (isDragging) {
                isDragging = false;
                $(".handle").removeClass('active');
                currentHandle = null;
                const header = rangeWrap.find(".header-filters");
                header.find('.values').remove();
                if (handles.eq(0).attr("value") !== rangeWrap.css("--min-val") || handles.eq(1).attr("value") !== rangeWrap.css("--max-val")) {
                    const valuesDiv = $('<div>', { class: 'values', text: handles.eq(0).attr("value") + " - " + handles.eq(1).attr("value") });
                    const timesIcon = $('<svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="clear-btn close svg-inline--fa fa-times fa-w-11"><path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z"></path></svg>');
                    rangeWrap.closest(".filter").addClass("active");
                    valuesDiv.append(timesIcon).on("click", function (event) {
                        event.stopPropagation();
                        handles.eq(0).attr("value", rangeWrap.css("--min-val"));
                        handles.eq(1).attr("value", rangeWrap.css("--max-val"));
                        rangeWrap.css('--handle-0-position', 0);
                        rangeWrap.css('--handle-1-position', rail.width() + 'px');
                        rangeWrap.css('--active-region-width', rail.width() + 'px');
                        header.find('.values').remove();
                        rangeWrap.closest(".filter").removeClass("active");
                        toggleClearAllButton();
                        getMediaContent();
                    });
                    header.append(valuesDiv);
                } else {
                    rangeWrap.closest(".filter").removeClass("active");
                }
                toggleClearAllButton();
                getMediaContent();
            }
        });
    });
});

$(".primary-filters .option").on("click", function (event) {
    event.stopPropagation(); // Stop event propagation

    const option = $(this);
    const selectWrap = option.closest(".select-wrap");

    if (option.find(".circle").length === 1) {
        if (selectWrap.hasClass("multi-choice")) {
            option.toggleClass("selected");
            updateValueWrap(selectWrap);
        } else {
            selectWrap.find(".option").removeClass("selected");
            option.addClass("selected");
            selectWrap.find(".options").eq(0).hide();
            // Update value-wrap with selected tag
            updateValueWrap(selectWrap);
        }
    } else {
        if ($(event.target).closest(option.find(".circle").eq(1)).length) {
            option.toggleClass("avoided");
        } else if (option.hasClass("avoided") || option.hasClass("selected")) {
            option.removeClass("avoided");
            option.removeClass("selected");
        } else {
            option.addClass("selected");
        }
        // Update value-wrap with selected and avoided tags
        updateValueWrap(selectWrap);
    }
    getMediaContent();
});

function updateValueWrap(selectWrap) {
    const selectedOptions = selectWrap.find(".option.selected");
    const avoidedOptions = selectWrap.find(".option.avoided");
    const valueWrap = selectWrap.find(".value-wrap");
    valueWrap.find(".tags").remove(); // Clear existing tags
    valueWrap.find(".value").remove(); // Clear existing tags
    selectWrap.find(".placeholder").hide();
    selectWrap.find("input").val("");
    selectWrap.closest(".filter-select").eq(0).addClass("active");
    toggleClearAllButton();

    if (selectWrap.hasClass("multi-choice") && (selectedOptions.length > 0 || avoidedOptions.length > 0)) {
        const tagsDiv = $("<div class='tags'></div>");
        if (selectedOptions.length > 0) {
            const firstSelectedOption = selectedOptions.first();
            tagsDiv.append("<div class='tag'>" + firstSelectedOption.find(".name").text() + "</div>");
            if (selectedOptions.length > 1) {
                tagsDiv.append("<div class='tag'>+" + (selectedOptions.length - 1) + "</div>");
            }
        }
        if (avoidedOptions.length > 0) {
            tagsDiv.append("<div class='tag avoided'>" + avoidedOptions.length + "</div>");
        }
        valueWrap.prepend(tagsDiv);
    } else if (selectedOptions.length > 0) {
        const div = $("<div>").addClass("value").text(selectedOptions.first().find(".name").text());
        valueWrap.prepend(div);
    } else {
        selectWrap.closest(".filter-select").removeClass("active");
        selectWrap.find(".placeholder").show();
    }
}

function toggleClearAllButton() {
    const clearAllButton = $(".filter.clear-all");

    if ($(".filter.active").length > 0) {
        clearAllButton.show();
    } else {
        clearAllButton.hide();
    }
}

$(".filter.clear-all").on("click", function () {
    $(".filter .close").click();
    $(this).hide();
    getMediaContent();
});

// input event listeners for select filters where the options can be searched
selectInputFilters.on("focus", function () {
    $(this).closest(".select-wrap").find(".placeholder").hide();
});

selectInputFilters.on("blur", function () {
    if ($(this).val() === "") {
        $(this).closest(".select-wrap").find(".placeholder").show();
    }
});

selectInputFilters.on("input", function () {
    const filter = $(this).val();
    const options = $(this).closest(".select-wrap").find(".option");
    options.each(function () {
        const option = $(this);
        if (option.text().toUpperCase().indexOf(filter.toUpperCase()) > -1) {
            option.show();
        } else {
            option.hide();
        }
    });
});

genreCheckboxType.change(function () {
    if (this.checked) {
        $(this).val("or");
    } else {
        $(this).val("and");
    }
    const genresFilter = $("#genres-filter");
    if (genresFilter.find(".options").css("display") === "none")
        genresFilter.find(".options").show();

    getMediaContent();
});


// sorting functions

const sortType = $("#sort");
const sortDirection = $(".sort-wrap .icon");
const sortDropdown = $(".secondary-filters .dropdown");
const sortOptions = $(".secondary-filters .option");

sortType.click(function () {
    $(this).closest(".selects-wrap").find(".dropdown").toggle();
});

sortOptions.on("click", function (event) {
    event.stopPropagation(); // Stop event propagation
    if (!$(this).hasClass("active")) {
        $(this).addClass("active").siblings().removeClass("active");
        getMediaContent();
        sortType.text($(this).text());
    }
    sortDropdown.hide();
});

sortDirection.on("click", function (event) {
    event.stopPropagation()
    const icon = $(this);
    icon.toggleClass("down");
    icon.attr("value", icon.hasClass("down") ? "-1" : "1");
    getMediaContent();
});

$(document).click(function (event) {
    if (!$(event.target).closest(sortType).length) {
        sortDropdown.hide();
    }
});

// create the filter parameters and sorting parameters to send to the server

function createFilterParams() {
    const params = {
        "action": "search",
        "mediaType": mediaType,
        "genreSelectMode": genreCheckboxType.val(),
        "sortParam": sortOptions.filter(".active").attr("value"),
        "sortDirection": sortDirection.attr("value"),
    };

    if (titleFilter.val() !== "") {
        params["title"] = titleFilter.val();
    }

    selectWrap.each(function () {
        const selectWrap = $(this);
        const filterName = selectWrap.attr("name");
        if (selectWrap.hasClass("multi-choice")) {
            const selectedOptions = selectWrap.find(".option.selected");

            params[filterName === "genre" ? "genreSelected" : filterName] = JSON.stringify(selectedOptions.map(function () {
                return $(this).find(".name").attr("value");
            }).get());

            if (filterName === "genre") {
                const avoidedOptions = selectWrap.find(".option.avoided");
                params[filterName + "Avoided"] = JSON.stringify(avoidedOptions.map(function () {
                    return $(this).find(".name").attr("value");
                }).get());
            }
        } else {
            const selectedOption = selectWrap.find(".option.selected");
            if (selectedOption.length > 0) {
                params[filterName] = selectedOption.find(".name").attr("value");
            }
        }
    });

    $(".range-wrap").each(function () {
        const rangeWrap = $(this);
        const rangeName = rangeWrap.attr("name");
        const minVal = rangeWrap.find(".handle-0").attr("value");
        const maxVal = rangeWrap.find(".handle-1").attr("value");
        params[rangeName + "Min"] = minVal;
        params[rangeName + "Max"] = maxVal;
    });

    return params;
}