import '/webjars/accessible-autocomplete/dist/accessible-autocomplete.min.js'

function enhanceDropdown(select) {
  if (select.dataset.autocompleteEnhanced === 'true') {
    return;
  }

  var whiteBackgroundClass = 'govuk-extension__background_white';
  accessibleAutocomplete.enhanceSelectElement({
    element: select,
    id: select.id,
    defaultValue: select.options[select.options.selectedIndex].innerHTML,
    selectElement: select,
    inputClasses: whiteBackgroundClass,
    allowEmpty: true
  });
  select.dataset.autocompleteEnhanced = 'true';
}

document.querySelectorAll('[data-module="make-autocomplete"]').forEach(enhanceDropdown);

// MOJ Add Another clones its first item. Rebuild an autocomplete from the cloned
// select so generated markup and IDs are not duplicated.
document.addEventListener('click', function(event) {
  if (!event.target.closest('.moj-add-another__add-button')) {
    return;
  }

  var addAnother = event.target.closest('.moj-add-another');
  var item = addAnother.querySelector('.moj-add-another__items').lastElementChild;
  var select = item.querySelector('select[data-module="make-autocomplete"]');
  if (!select || !item.querySelector('.autocomplete__wrapper')) {
    return;
  }

  item.querySelectorAll('.autocomplete__wrapper').forEach(function(wrapper) {
    wrapper.remove();
  });
  select.classList.remove('autocomplete__select--hidden');
  select.removeAttribute('aria-hidden');
  select.removeAttribute('tabindex');
  delete select.dataset.autocompleteEnhanced;
  enhanceDropdown(select);
});
