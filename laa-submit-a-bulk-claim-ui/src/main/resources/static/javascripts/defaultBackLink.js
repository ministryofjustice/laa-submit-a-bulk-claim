document.addEventListener('DOMContentLoaded', function() {
    const backLinkContainer = document.getElementById('govuk-back-link-container');
    const mainContent = document.getElementById('main-content');

    if (!backLinkContainer || !mainContent) {
        return;
    }

    const backLinkHref = mainContent.getAttribute('data-back-link-href');
    const backLinkText = mainContent.getAttribute('data-back-link-text');

    if (!backLinkHref || !backLinkText) {
        return;
    }

    backLinkContainer.replaceChildren();

    const backLink = document.createElement('a');
    backLink.className = 'govuk-back-link';
    backLink.href = backLinkHref;
    backLink.textContent = backLinkText;

    backLinkContainer.appendChild(backLink);
});