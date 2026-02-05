import * as GOVUKFrontend from '/webjars/accessible-autocomplete/dist/accessible-autocomplete.min.js'
import accessibleAutocomplete from "./wrapper";


var selectDropdowns = document.querySelectorAll('[data-module="make-autocomplete"]');

// For each dropdown
selectDropdowns.forEach(function(select) {
  accessibleAutocomplete.enhanceSelectElement({
    autoselect: true,
    defaultValue: select.options[select.options.selectedIndex].innerHTML,
    selectElement: select
  });
});
