<%--
  Created by IntelliJ IDEA.
  User: messi
  Date: 28/05/2024
  Time: 16:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/filters.css">
    <script src="https://code.jquery.com/jquery-3.6.4.min.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/filters.js" defer></script>
    <title>Title</title>
</head>
<body>

<div class="container">
    <!-- filters div -->
    <div class="filters-wrap primary-filters">
        <!-- primary filters (shown) -->
        <div class="filters">
            <!-- title -->
            <div class="filter filter-select">
                <div class="filter-name">Search</div>
                <div class="search-wrap">
                    <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="search" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="icon left svg-inline--fa fa-search fa-w-16">
                        <path fill="currentColor" d="M505 442.7L405.3 343c-4.5-4.5-10.6-7-17-7H372c27.6-35.3 44-79.7 44-128C416 93.1 322.9 0 208 0S0 93.1 0 208s93.1 208 208 208c48.3 0 92.7-16.4 128-44v16.3c0 6.4 2.5 12.5 7 17l99.7 99.7c9.4 9.4 24.6 9.4 33.9 0l28.3-28.3c9.4-9.4 9.4-24.6.1-34zM208 336c-70.7 0-128-57.2-128-128 0-70.7 57.2-128 128-128 70.7 0 128 57.2 128 128 0 70.7-57.2 128-128 128z" class=""></path>
                    </svg>
                    <label>
                        <input id="title-filter" type="search" autocomplete="off" class="search">
                    </label>
                    <svg id="title-remove-button" aria-hidden="true" focusable="false" data-prefix="fas" data-icon="times" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 352 512" class="icon right close svg-inline--fa fa-times fa-w-11">
                        <path fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z" class=""></path>
                    </svg>
                </div>
            </div>

            <!-- genres -->
            <div class="filter filter-select">
                <div class="filter-name">Genres</div>
                <div class="select-wrap">
                    <div class="select">
                        <div class="value-wrap">
                            <div class="placeholder">Any</div>
                            <label>
                                <input type="search" autocomplete="off" class="filter">
                            </label>
                        </div>
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                            <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                        </svg>
                    </div>
                    <div class="options">
                        <div class="scroll-wrap">
                            <div class="option-group">
                                <div class="option">
                                    <div class="label">
                                        <div class="name">Action</div>
                                        <div class="selected-icon circle">
                                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                            </svg>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- year -->
            <div class="filter filter-select">
                <div class="filter-name">Year</div>
                <div class="select-wrap">
                    <div class="select">
                        <div class="value-wrap">
                            <div class="placeholder">Any</div>
                            <label>
                                <input type="search" autocomplete="off" class="filter">
                            </label>
                        </div>
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                            <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                        </svg>
                    </div>
                    <div class="options">
                        <div class="scroll-wrap">
                            <div class="option-group">
                                <div class="option">
                                    <div class="label">
                                        <div class="name">2024</div>
                                        <div class="selected-icon circle">
                                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                            </svg>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- season -->
            <div class="filter filter-select">
                <div class="filter-name">Season</div>
                <div class="select-wrap">
                    <div class="select">
                        <div class="value-wrap">
                            <div class="placeholder">Any</div>
                            <label>
                                <input type="search" autocomplete="off" class="filter">
                            </label>
                        </div>
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                            <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                        </svg>
                    </div>
                    <div class="options">
                        <div class="scroll-wrap">
                            <div class="option-group">
                                <div class="option">
                                    <div class="label">
                                        <div class="name">Winter</div>
                                        <div class="selected-icon circle">
                                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                            </svg>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- airing status -->
            <div class="filter filter-select">
                <div class="filter-name">Airing Status</div>
                <div class="select-wrap">
                    <div class="select">
                        <div class="value-wrap">
                            <div class="placeholder">Any</div>
                            <label>
                                <input type="search" autocomplete="off" class="filter">
                            </label>
                        </div>
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                            <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                        </svg>
                    </div>
                    <div class="options">
                        <div class="scroll-wrap">
                            <div class="option-group">
                                <div class="option">
                                    <div class="label">
                                        <div class="name">Completed</div>
                                        <div class="selected-icon circle">
                                            <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                            </svg>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- extra filters (hidden)-->
        <div class="extra-filters-wrap">

            <!-- extra filters button -->
            <div id="extra-filters-button" class="open-btn active">
                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="sliders-h" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="icon svg-inline--fa fa-sliders-h fa-w-16">
                    <path fill="currentColor" d="M496 384H160v-16c0-8.8-7.2-16-16-16h-32c-8.8 0-16 7.2-16 16v16H16c-8.8 0-16 7.2-16 16v32c0 8.8 7.2 16 16 16h80v16c0 8.8 7.2 16 16 16h32c8.8 0 16-7.2 16-16v-16h336c8.8 0 16-7.2 16-16v-32c0-8.8-7.2-16-16-16zm0-160h-80v-16c0-8.8-7.2-16-16-16h-32c-8.8 0-16 7.2-16 16v16H16c-8.8 0-16 7.2-16 16v32c0 8.8 7.2 16 16 16h336v16c0 8.8 7.2 16 16 16h32c8.8 0 16-7.2 16-16v-16h80c8.8 0 16-7.2 16-16v-32c0-8.8-7.2-16-16-16zm0-160H288V48c0-8.8-7.2-16-16-16h-32c-8.8 0-16 7.2-16 16v16H16C7.2 64 0 71.2 0 80v32c0 8.8 7.2 16 16 16h208v16c0 8.8 7.2 16 16 16h32c8.8 0 16-7.2 16-16v-16h208c8.8 0 16-7.2 16-16V80c0-8.8-7.2-16-16-16z" class=""></path>
                </svg>
            </div>

            <!-- extra filters popup -->
            <div id="extra-filters" class="dropdown anime">
                <div class="filters-wrap">

                    <!-- format -->
                    <div class="filter filter-select">
                        <div class="filter-name">Format</div>
                        <div class="select-wrap">
                            <div class="select">
                                <div class="value-wrap">
                                    <div class="placeholder">Any</div>
                                    <label>
                                        <input type="search" autocomplete="off" class="filter">
                                    </label>
                                </div>
                                <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="chevron-down" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="icon svg-inline--fa fa-chevron-down fa-w-14 fa-fw">
                                    <path data-v-e3e1e202="" fill="currentColor" d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" class=""></path>
                                </svg>
                            </div>
                            <div class="options">
                                <div class="scroll-wrap">
                                    <div class="option-group">
                                        <div class="option">
                                            <div class="label">
                                                <div class="name">Movie</div>
                                                <div class="selected-icon circle">
                                                    <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="check" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-check fa-w-16">
                                                        <path fill="currentColor" d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z" class=""></path>
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- year range -->
                    <div class="filter">
                        <div class="range-wrap" style="--handle-0-position: 0px; --handle-1-position: 171px; --active-region-width: 171px; --min-val: 1930; --max-val: 2024;">
                            <div class="header">
                                <div class="label">year range</div>
                            </div>
                            <div class="range">
                                <div class="rail">
                                    <div val="1930" class="handle handle-0"></div>
                                    <div class="active-region"></div>
                                    <div val="2024" class="handle handle-1"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- rating range -->
                    <div class="filter">
                        <div class="range-wrap" style="--handle-0-position: 0px; --handle-1-position: 171px; --active-region-width: 171px; --min-val: 0; --max-val: 10;">
                            <div class="header">
                                <div class="label">rating range</div>
                            </div>
                            <div class="range">
                                <div class="rail">
                                    <div val="0" class="handle handle-0"></div>
                                    <div class="active-region"></div>
                                    <div val="10" class="handle handle-1"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- results -->
    <div class="results">
        <div class="media-card">
            <a href="#" class="cover">
                <img src="images/anime-image-default.png" class="image loaded"  alt="image"/>
            </a>
            <a href="#" class="title">Kimetsu no Yaiba: Hashira Geiko-hen</a>
            <div class="hover-data right">
                <div class="header">
                    <div class="score">Score: 8.5</div>
                    <div class="likes">
                        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="heart" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="icon svg-inline--fa fa-heart fa-w-16 fa-xs">
                            <path fill="currentColor" d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z" class=""></path>
                        </svg>
                        <span class="n-likes">300</span>
                    </div>
                </div>
                <div class="header">
                    <div class="season">SUMMER</div>
                    <div class="year">2024</div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
