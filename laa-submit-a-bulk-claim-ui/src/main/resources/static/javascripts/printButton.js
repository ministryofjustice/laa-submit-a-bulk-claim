document.addEventListener('DOMContentLoaded', () => {
  const laaPrintButton = document.querySelector(
      '[data-component="laa-print-button"]')
  if (laaPrintButton) {
    const printButtonSection = `
        <div class="govuk-grid-column-one-half govuk-!-text-align-right">
          <button type="button" class="govuk-button govuk-button--secondary"
                  data-module="govuk-button" id="print-button"
                  onclick="window.print()">
            Print this page
          </button>
        </div>
    `
    laaPrintButton.classList = 'govuk-grid-column-one-half';
    laaPrintButton.insertAdjacentHTML('afterend', printButtonSection);
  }
});