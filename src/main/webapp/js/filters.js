const titleFilter = $("#title-filter");
const selectInputFilters = $(".select-wrap input");
const genreCheckboxType = $("#genre-checkbox-type");
const removeSelectionButton = $(".filter-select .close");

removeSelectionButton.on("click", function (event) {
    event.stopPropagation();
    const selectWrap = $(this).closest(".select-wrap");
    selectWrap.find(".option").removeClass("selected");
    selectWrap.find(".option").removeClass("avoided");
    selectWrap.find(".options").hide();
    updateValueWrap(selectWrap);
    getMediaContent();
});

titleFilter.on("input", function () {
    if ($(this).val() !== "") {
        titleFilter.closest(".filter-select").addClass("active");
    } else {
        titleFilter.closest(".filter-select").removeClass("active");
    }
    getMediaContent();
});

$("#title-remove-button").on("click", function () {
    titleFilter.val("");
    $(this).hide();
    getMediaContent();
});

$(document).ready(function () {
    genreCheckboxType.prop("checked", false);

    $(".select-wrap").click(function () {
        // Hide options of all other select-wraps
        $(".options").hide();

        // Show options of the clicked select-wrap
        $(this).find(".options").show();
    });

    $("#extra-filters-button").click(function () {
        $("#extra-filters").toggle();
    });

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
            $(this).addClass('active');
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
            isDragging = false;
            $(".handle").removeClass('active');
            currentHandle = null;
        });
    });
});

$(".option").on("click", function (event) {
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
    selectWrap.closest(".filter-select").eq(0).addClass("active");

    if (selectWrap.hasClass("multi-choice") && (selectedOptions.length > 0 || avoidedOptions.length > 0)) {
        const tagsDiv = $("<div class='tags'></div>");
        if (selectedOptions.length > 0) {
            const firstSelectedOption = selectedOptions.first();
            tagsDiv.append("<div class='tag'>" + firstSelectedOption.find(".name").text() + "</div>").click(function (event) {
                event.stopPropagation();
                selectedOptions.eq(0).removeClass("selected");
                updateValueWrap(selectWrap);
            });
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
    console.log(options);
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
});

function createFilterParams() {
    const params = {
        "action": "search",
        "mediaType": mediaType,
        "genreSelectMode": genreCheckboxType.val()
    };

    if (titleFilter.val() !== "") {
        params["title"] = titleFilter.val();
    }

    $(".select-wrap").each(function () {
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

    console.log(params);
    return params;
}
