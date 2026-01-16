import * as GOVUKFrontend from '/webjars/govuk-frontend/dist/govuk/govuk-frontend.min.js'
import * as MOJFrontend from '/webjars/ministryofjustice__frontend/moj/moj-frontend.min.js'

document.body.className += ' js-enabled' + ('noModule' in HTMLScriptElement.prototype ? ' govuk-frontend-supported' : '');

GOVUKFrontend.initAll()
MOJFrontend.initAll()