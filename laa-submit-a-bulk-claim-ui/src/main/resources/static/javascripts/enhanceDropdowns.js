import '/webjars/accessible-autocomplete/dist/accessible-autocomplete.min.js'

var selectDropdowns = document.querySelectorAll('[data-module="make-autocomplete"]');

// For each dropdown
selectDropdowns.forEach(function(select) {
  var whiteBackgroundClass = 'govuk-extension__background_white';
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
