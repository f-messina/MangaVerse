// Get DOM elements
const rangeOne = document.querySelector('input[name="minScore"]');
const rangeTwo = document.querySelector('input[name="maxScore"]');
const outputOne = document.querySelector('.outputOne');
const outputTwo = document.querySelector('.outputTwo');
const inclRange = document.querySelector('.incl-range');

// Function to update the view
function updateView(element) {
    const value = element.value;
    const max = element.getAttribute('max');
    const name = element.getAttribute('name');

    if (name === 'minScore') {
        outputOne.innerHTML = value;
        outputOne.style.left = (value / max) * 100 + '%';
    } else {
        outputTwo.innerHTML = value;
        outputTwo.style.left = (value / max) * 100 + '%';
    }

    const rangeOneValue = parseInt(rangeOne.value);
    const rangeTwoValue = parseInt(rangeTwo.value);

    inclRange.style.width = Math.abs(rangeOneValue - rangeTwoValue) / max * 100 + '%';
    inclRange.style.left = Math.min(rangeOneValue, rangeTwoValue) / max * 100 + '%';
}

// Event listeners
document.addEventListener('DOMContentLoaded', function () {
    updateView(rangeOne);
    updateView(rangeTwo);

    const rangeInputs = document.querySelectorAll('input[type="range"]');

    rangeInputs.forEach((range) => {
        range.addEventListener('mouseup', function () {
            this.blur();
        });

        range.addEventListener('mousedown', function () {
            updateView(this);
        });

        range.addEventListener('input', function () {
            updateView(this);
        });
    });
});