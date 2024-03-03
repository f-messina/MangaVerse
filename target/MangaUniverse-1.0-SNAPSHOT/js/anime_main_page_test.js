function toggleYearRangeInputs() {
    const singleYearDiv = document.getElementById('singleYearDiv');
    const yearRangeDiv = document.getElementById('yearRangeDiv');
    const minYearInput = document.getElementById('minYear');
    const maxYearInput = document.getElementById('maxYear');
    const yearInput = document.getElementById('year');
    const seasonInput = document.getElementById('season');
    const yearRangeCheckbox = document.getElementById('yearRangeCheckbox');

    if (yearRangeCheckbox.checked) {
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

document.addEventListener('DOMContentLoaded', function () {
    const yearRangeCheckbox = document.getElementById('yearRangeCheckbox');
    toggleYearRangeInputs(); // Initial state
    yearRangeCheckbox.addEventListener('change', toggleYearRangeInputs);
});