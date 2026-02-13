import * as GOVUKFrontend
  from '/webjars/govuk-frontend/dist/govuk/govuk-frontend.min.js'
import * as MOJFrontend
  from '/webjars/ministryofjustice__frontend/moj/moj-frontend.min.js'
import {
  FilterToggleButton
} from '/webjars/ministryofjustice__frontend/moj/moj-frontend.min.js'

document.body.className += ' js-enabled' + ('noModule'
in HTMLScriptElement.prototype ? ' govuk-frontend-supported' : '');

GOVUKFrontend.initAll()
MOJFrontend.initAll()

const $filter = document.querySelector('[data-module="moj-filter"]')

if ($filter) {

  new FilterToggleButton($filter, {
    bigModeMediaQuery: '(min-width: 48.0625em)',
    startHidden: false,
    toggleButton: {
      showText: 'Show filter',
      hideText: 'Hide filter',
      classes: 'govuk-button--secondary',
    },
    closeButton: {
      text: 'Close'
    }
  })

}
