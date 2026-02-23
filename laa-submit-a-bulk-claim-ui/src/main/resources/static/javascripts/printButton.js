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
    const button = `<button type="button" class="govuk-button govuk-button--secondary"
                  data-module="govuk-button" id="print-button">
            Print this page
          </button>`;

    // If container section detailed
    const secondaryContainer = laaPrintButton.getAttribute(
        'data-print-action-container');
    const actionContainer = document.getElementById(secondaryContainer);

    if (actionContainer) {
      // Put print button inside container at the end.
      actionContainer.insertAdjacentHTML('beforeend', button);
    } else {
      const printButtonSection = `
          <div class="govuk-grid-column-one-quarter govuk-!-text-align-right sabc-container__align_right">
            ${button}
          </div>
      `
      // Make column width 3/4s to fit new button section
      laaPrintButton.classList = 'govuk-grid-column-three-quarters';
      // Put new section after original section
      laaPrintButton.insertAdjacentHTML('afterend', printButtonSection);
    }
    document.getElementById('print-button').addEventListener('click',
        printPage);
  }

});