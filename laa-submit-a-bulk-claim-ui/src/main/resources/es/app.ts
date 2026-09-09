import * as GOVUKFrontend from './govuk-frontend.min.js';
import * as MOJFrontend from './moj-frontend.min.js';

document.documentElement.classList.remove('no-js');
document.body.className += ` js-enabled${'noModule' in HTMLScriptElement.prototype ? ' govuk-frontend-supported' : ''}`;

GOVUKFrontend.initAll();
MOJFrontend.initAll();

document.querySelectorAll<HTMLAnchorElement>('[data-module="logout-link"]').forEach(function (link: HTMLAnchorElement): void {
  link.addEventListener('click', function (event: MouseEvent): void {
    event.preventDefault();
    (document.forms.namedItem('logoutForm') as HTMLFormElement | null)?.requestSubmit();
  });
});

const printPage = (): void => {
  // Hide content on screen
  const elements = document.querySelectorAll<HTMLElement>('[data-module="laa-hide-on-print-button"]');

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
  const laaPrintButton = document.querySelector('[data-module="laa-print-button"]');
  if (laaPrintButton) {
    const button = `<button type="button" class="govuk-button govuk-button--secondary"
                  data-module="govuk-button" id="print-button">
            Print this page
          </button>`;

    // If container section detailed
    const secondaryContainer = laaPrintButton.getAttribute('data-print-action-container');
    const actionContainer = secondaryContainer ? document.getElementById(secondaryContainer) : null;

    if (actionContainer) {
      // Put print button inside container at the end.
      actionContainer.insertAdjacentHTML('beforeend', button);
    } else {
      const printButtonSection = `
          <div class="govuk-grid-column-one-quarter govuk-!-text-align-right sabc-container__align_right">
            ${button}
          </div>
      `;
      // Make column width 3/4s to fit new button section
      laaPrintButton.className = 'govuk-grid-column-three-quarters';
      // Put new section after original section
      laaPrintButton.insertAdjacentHTML('afterend', printButtonSection);
    }
    document.getElementById('print-button')?.addEventListener('click', printPage);
  }
});

const selectDropdowns = document.querySelectorAll<HTMLSelectElement>('[data-module="make-autocomplete"]');

// For each dropdown
selectDropdowns.forEach(function (select: HTMLSelectElement): void {
  const whiteBackgroundClass = 'govuk-extension__background_white';
  // Not white as default when it should be
  accessibleAutocomplete.enhanceSelectElement({
    element: select,
    id: select.id,
    defaultValue: select.options[select.options.selectedIndex].innerHTML,
    selectElement: select,
    inputClasses: whiteBackgroundClass,
    allowEmpty: true
  });
});
