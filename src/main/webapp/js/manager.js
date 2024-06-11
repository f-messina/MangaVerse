let mangaSectionAccessed = false;
let animeSectionAccessed = false;
let animeSelectedId;
let mangaSelectedId;

function fetchData(inputData, chartId, chartType) {
    $.post(contextPath + "/manager", inputData, function(data) {
        if (data.success) {
            let max;
            const labels = Object.keys(data.results);
            const values = Object.values(data.results);
            console.log(data);
            // Destroy the existing chart
            const existingChart = Chart.getChart(chartId);
            if (existingChart) {
                existingChart.destroy();
            }

            if (chartType === 'bar' && chartId === 'app-rating-chart') {
                max = 5;
            } else if (chartType === 'bar' && chartId === 'user-distribution-chart') {
                max = null;
            } else {
                max = 10;
            }

            createChart(chartId, labels, values, chartType, max);
        }
    });
}

function createChart(chartId, labels, values, chartType, max) {
    const ctx = $('#' + chartId)[0].getContext('2d');

    new Chart(ctx, {
        type: chartType,
        data: {
            labels: labels,
            datasets: [{
                label: chartId === 'user-distribution-chart' ? 'Distribution' : 'Average Rating',
                data: values,
                backgroundColor: chartType === 'bar' ? '#36A2EB' : ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0'],
            }]
        },
        options: {
            y: {
                duration: 1000,
                easing: 'easeOutQuad',
                from: chartType === 'bar' ? 0 : 10, // Start from 0 for bar chart, or from the maximum value for other types
                loop: false
            },
            responsive: true,
            plugins: {
                legend: {
                    position: "top"
                },
            },
            scales: chartType !== 'pie' ? {
                y: {
                    beginAtZero: true,
                    max: max
                }
            } : {}
        }
    });
}

function search(type, searchTerm = '') {
    const inputData = {
        title: searchTerm,
        action: 'searchByTitle'
    }
    $.post(contextPath + "/" + type, inputData, function(data) {
        if (data.success) {
            const mediaList = type === 'anime' ? $('#anime-list') : $('#manga-list');
            mediaList.empty(); // Clear previous search results
            data.results.entries.forEach(function(entry) {
                const mediaItem = $('<div class="media-item"></div>');
                const image = $('<img src="' + entry.imageUrl + '" alt="' + entry.title + '" class="media-image">')
                    .on("error", () =>  setDefaultCover(image, type));
                const title = $('<div class="media-title">' + entry.title + '</div>').click(function() {
                    type === 'anime' ? animeSelectedId = entry.id : mangaSelectedId = entry.id;
                    $('#' + type + '-selected').text("Selected " + type + ": " + entry.title);
                    const inputData = {
                        mediaId: entry.id,
                        action: 'getAverageRatingByMonth',
                        type: type,
                        year: new Date().getFullYear()
                    }
                    fetchData(inputData, type + '-chart-month', 'line');
                    $('#single-' + type + '-analytics').show();
                });
                const link = $('<a>').attr('href', contextPath + '/' + type + '?mediaId=' + entry.id).text('View Details');
                mediaItem.append(image, title, link);
                mediaList.append(mediaItem);
            });
        } else if (data.noResults) {
            const mediaList = type === 'anime' ? $('#anime-list') : $('#manga-list');
            mediaList.empty(); // Clear previous search results
            mediaList.append('<div class="no-results">No results found</div>');
        } else {
            console.log('Failed to search');
        }
    }).fail(function() {
        console.log('Failed to search');
    });
}

function setDefaultCover(image, type) {
    image.off("error");
    image.attr("src", type === "anime" ? animeDefaultImage : mangaDefaultImage);
}

function resetPage() {
    $("select").prop('selectedIndex', 0);
    $("input").each(function() {
        if ($(this).attr("name") === "startYear") {
            $(this).val(new Date().getFullYear() - 1);
        } else if ($(this).attr("type") === "search") {
            $(this).val("");
        } else {
            $(this).val(new Date().getFullYear());
        }
    });
}

function getDefaultAnalytics(type) {
    const action = type === 'anime' ? 'getAnimeDefaultAnalytics' : 'getMangaDefaultAnalytics';
    $.post(contextPath + "/manager", {action: action}, function(data) {
        if (data.success) {
            const chartId = type === 'anime' ? 'anime-criteria-rating-chart' : 'manga-criteria-rating-chart';
            const labels = Object.keys(data.bestCriteria);
            const values = Object.values(data.bestCriteria);
            createChart(chartId, labels, values, 'bar', 10);
            if (type === 'anime')
                animeSectionAccessed = true;
            else
                mangaSectionAccessed = true;
        }
    });
}

function getTrendMediaContent(type) {
    const inputData = {
        year: $("#" + type + "-trend-year").val(),
        action: 'getTrendMediaContentByYear',
        type: type
    }
    $.post(contextPath + "/manager", inputData, function(data) {
        console.log(data);
        if (data.success) {
            const mediaList = $("#" + type + "-trend-list");
            mediaList.empty(); // Clear previous search results

            Object.entries(data.results).forEach(function([key, value]) {
                console.log(key);
                // Parse the key (which is a JSON string representing MediaContentDTO) and value (which is the integer)
                const entry = JSON.parse(key)

                const mediaItem = $('<div>').addClass('media-item');
                const imgLink = $('<a>').attr('href', contextPath + '/' + type + '?mediaId=' + entry.id);
                const image = $('<img src="' + entry.imageUrl + '" alt="' + entry.title + '" class="media-image">')
                    .on("error", () => setDefaultCover(image, type));
                imgLink.append(image);
                const title = $('<a>').attr('href', contextPath + '/' + type + '?mediaId=' + entry.id).addClass("media-title").text(entry.title);
                const link = $('<p>').text(value); // Set the integer value in the paragraph
                mediaItem.append(imgLink, title, link);
                mediaList.append(mediaItem);
            });
        } else {
            console.log('Failed to get trend');
        }
    }).fail(function() {
        console.log('Failed to get trend');
    });
}

function showPage(pageId, button) {
    $('.page').removeClass('selected');
    $(pageId).addClass('selected');
    $('.options').removeClass('active');
    $(button).addClass('active');
}

$(document).ready(function() {
    resetPage();
    createChart('user-distribution-chart', distributionLabels, distributionData, 'pie');
    createChart('app-rating-chart', averageAppRatingLabels, averageAppRatingData, 'bar', 5);

    $('#user-distribution-type').change(function () {
        const criteria = $('#user-distribution-type').val();
        let chartType = 'bar';

        const inputData = {
            criteria: criteria,
            action: 'getDistribution'
        }
        if (criteria === 'gender') {
            chartType = 'pie';
            $('#user-distribution-chart').addClass('small');
        } else {
            $('#user-distribution-chart').removeClass('small');
        }

        fetchData(inputData, 'user-distribution-chart', chartType);
    });

    $('#user-rating-criteria').change(function () {
        const inputData = {
            criteria: $('#user-rating-criteria').val(),
            action: 'getAverageAppRatingByCriteria'
        }
        fetchData(inputData, 'app-rating-chart', 'bar');
    });

    $('#manga-analytics-type').change(function () {
        const inputData = {
            criteria: $('#manga-analytics-type').val(),
            action: 'getBestCriteria',
            type: 'manga',
            page: 1
        }
        fetchData(inputData, 'manga-criteria-rating-chart', 'bar');
    });

    $('#anime-analytics-type').change(function () {
        const inputData = {
            criteria: $('#anime-analytics-type').val(),
            action: 'getBestCriteria',
            type: 'anime',
            page: 1
        }
        fetchData(inputData, 'anime-criteria-rating-chart', 'bar');
    });

    // page triggers

    $('#user-button').click(function () {
        showPage('#user-page', $(this));
    });

    $('#manga-button').click(function () {
        if (!mangaSectionAccessed) {
            getDefaultAnalytics('manga');
            getTrendMediaContent('manga');
            search('manga');
        }
        showPage('#manga-page', $(this));
    });

    $('#anime-button').click(function () {
        if (!animeSectionAccessed) {
            getDefaultAnalytics('anime');
            getTrendMediaContent('anime');
            search('anime');
        }
        showPage('#anime-page', $(this));
    });


    // search triggers
    mediaTypes = ['manga', 'anime'];
    mediaTypes.forEach(function(type) {
        const yearInput = $('#' + type + '-year');
        const startYearInput = $('#' + type + '-start-year');
        const endYearInput = $('#' + type + '-end-year');
        const trendYearInput = $("#" + type + "-trend-year");

        $('#' + type + '-search').on('input', function () {
            const searchTerm = $('#' + type + '-search').val();
            search(type, searchTerm);
        });

        yearInput.on('input', function () {
            if (yearInput.val() < 1900 || yearInput.val() > new Date().getFullYear()) {
                return;
            }
            const inputData = {
                mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                action: 'getAverageRatingByMonth',
                type: type,
                year: yearInput.val()
            }
            fetchData(inputData, type + '-chart-month', 'line');
        });

        startYearInput.on('input', function () {
            const startYear = startYearInput.val();
            let endYear = endYearInput.val();
            if (startYear < 1900 || startYear > new Date().getFullYear()) {
                return;
            }
            if (startYear > endYear) {
                $('#' + type + '-end-year').val(startYear);
                endYear = startYear;
            }
            const inputData = {
                mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                action: 'getAverageRatingByYear',
                type: type,
                startYear: startYear,
                endYear: endYear
            }
            fetchData(inputData, type + '-chart-year', 'line');
        });

        endYearInput.on('input', function () {
            let startYear = startYearInput.val();
            const endYear = endYearInput.val();
            if (endYear < 1900 || endYear > new Date().getFullYear()) {
                return;
            }
            if (endYear < startYear) {
                $('#' + type + '-start-year').val(endYear);
                startYear = endYear;
            }

            const inputData = {
                mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                action: 'getAverageRatingByYear',
                type: type,
                startYear: startYear,
                endYear: endYear
            }
            fetchData(inputData, type + '-chart-year', 'line');
        });

        function isCanvasPopulated(canvasId) {
            const canvas = $('#' + canvasId)[0];
            const offscreen = new OffscreenCanvas($(canvas).prop('width'), $(canvas).prop('height'));
            const ctx = offscreen.getContext('2d');
            ctx.drawImage(canvas, 0, 0);
            return ctx.getImageData(0, 0, canvas.width, canvas.height).data.some(alpha => alpha !== 0);
        }

        $('#' + type + '-period-selection').change(function () {
            const period = $('#' + type + '-period-selection').val();

            $("#single-" + type + "-analytics").find(".diagram-parameter").toggleClass("active");
            const inputData = {
                mediaId: type === 'anime' ? animeSelectedId : mangaSelectedId,
                action: 'getAverageRatingBy' + period.charAt(0).toUpperCase() + period.slice(1),
                type: type,
            }
            if (period === 'year') {
                inputData.startYear = startYearInput.val();
                inputData.endYear = endYearInput.val();
            } else {
                inputData.year = yearInput.val();
            }
            $('#' + type + '-chart-year').toggleClass("active");
            $('#' + type + '-chart-month').toggleClass("active");

            if (!isCanvasPopulated(type + '-chart-' + period)) {
                fetchData(inputData, type + '-chart-' + period, 'line');
            }
        });

        trendYearInput.on('input', function() {
            if (trendYearInput.val() < 1900 || trendYearInput.val() > new Date().getFullYear()) {
                return;
            }
            getTrendMediaContent(type);
        });
    });
});