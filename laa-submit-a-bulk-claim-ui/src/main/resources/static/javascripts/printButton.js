const printPage = () => {
  // Hide content on screen
  let elements = document.querySelectorAll(
      '[data-module="laa-hide-on-print-button"]');

  elements.forEach(element => {
    const originalDisplay = element.style.display;
    element.style.display = 'none';
    setTimeout(function () {
      element.style.display = originalDisplay;
    }, 2000);
  });

  window.print();
};

document.addEventListener('DOMContentLoaded', () => {
  const laaPrintButton = document.querySelector(
      '[data-module="laa-print-button"]')
  if (laaPrintButton) {
    const printButtonSection = `
        <div class="govuk-grid-column-quarter govuk-!-text-align-right">
          <button type="button" class="govuk-button govuk-button--secondary"
                  data-module="govuk-button" id="print-button">
            Print this page
          </button>
        </div>
    `
    laaPrintButton.classList = 'govuk-grid-column-three-quarters';
    laaPrintButton.insertAdjacentHTML('afterend', printButtonSection);
    document.getElementById('print-button').addEventListener('click',
        printPage);
  }
  
  
});