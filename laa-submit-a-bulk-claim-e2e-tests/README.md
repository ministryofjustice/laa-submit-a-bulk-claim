# Java Cucumber E2E (Real Systems)

This module hosts Java Cucumber E2E journeys for bulk submission.

## Scope

- Real-system E2E layer (no WireMock in this module)
- Migrated feature files from `bulk-submission-and-fee-scheme-tests-/tests/features/ui/BulkSubmission`
- Java step mapping is centralized in `BulkSubmissionSteps`
- Page-object layer is in `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/pages`

## Current Gradle Tasks (Scanned)

The `laa-submit-a-bulk-claim-e2e-tests` `build.gradle` currently exposes:

- `e2eLogin` - creates Playwright storage state
- `cucumberTest` - runs the JUnit Platform suite class `CucumberE2ETest`
- `e2eCleanup` - removes recent test submissions from DB

There is no active `cucumberTestSmoke` task at the moment.

## Run

Create a local env file first:

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim/laa-submit-a-bulk-claim-e2e-tests
cp .env.example .env
```

Then update `.env` with your real credentials, tokens, and environment-specific URLs.

Create storage state first (equivalent to `npm run login`):

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
./gradlew laa-submit-a-bulk-claim-e2e-tests:e2eLogin
```

Run E2E suite:

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
./gradlew laa-submit-a-bulk-claim-e2e-tests:cucumberTest
```

Clean test data:

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
./gradlew laa-submit-a-bulk-claim-e2e-tests:e2eCleanup
```

## Tag Filtering

`cucumberTest` supports tag filtering using `CUCUMBER_TAGS`.

Example (run only `@stable` and exclude `@ignore`):

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
CUCUMBER_TAGS='@stable and not @ignore' ./gradlew laa-submit-a-bulk-claim-e2e-tests:cucumberTest
```

You can also set `CUCUMBER_TAGS` in `.env`.

## Configuration

- `E2E_BASE_URL` (default `http://localhost:8080`)
- `E2E_HEADLESS` (default `true`)
- `E2E_STORAGE_STATE_PATH` (default `build/e2e/storageState.json`)
- `E2E_AUTH_MODE` (`aad` or `mock`, default `aad`)
- `CUCUMBER_TAGS` (optional, overrides default suite selection)

AAD login mode variables:

- `USERNAME`
- `PASSWORD`
- `MFA_SECRET`

Mock login mode variables:

- `MOCK_USERNAME` (default `provider.user@provider.com`)
- `MOCK_PASSWORD` (default `password`)

## Example `.env`

A safe example is provided in `.env.example`.

Example values:

```dotenv
UI_BASE_URL=https://uat-submit-a-bulk-claim-<env>.apps.live.cloud-platform.service.justice.gov.uk/
USERNAME=your.test.user@justice.gov.uk
PASSWORD=your-password
MFA_SECRET=your-mfa-secret
DB_HOST=localhost
DB_PORT=5432
DB_USER=your-db-user
DB_PASS=your-db-password
DB_NAME=your-db-name
DB_SSL=true
DSTEW_API_BASE_URL=https://main-laa-data-claims-api-<env>.cloud-platform.service.justice.gov.uk
DSTEW_API_TOKEN=your-dstew-api-token
BROWSERSTACK_USERNAME=your-browserstack-username
BROWSERSTACK_ACCESS_KEY=your-browserstack-access-key
BROWSERSTACK_LOCAL_IDENTIFIER=your-browserstack-local-identifier
GITHUB_RUN_NUMBER=local
PORT_FORWARD=false
PROVIDER_API_KEY=your-provider-api-key
FSP_API_BASE_URL=https://laa-fee-scheme-api-<env>.apps.live.cloud-platform.service.justice.gov.uk/
FSP_API_TOKEN=your-fsp-api-token
CUCUMBER_TAGS=@matterStarts
E2E_BASE_URL=https://uat-submit-a-bulk-claim-<env>.apps.live.cloud-platform.service.justice.gov.uk/
E2E_HEADLESS=false
E2E_AUTH_MODE=aad
E2E_STORAGE_STATE_PATH=build/e2e/storageState.json
```

## Notes

- `CucumberE2ETest` is the suite class selected by `cucumberTest`.
- Steps not yet implemented in Java throw `PendingException` to keep migration gaps visible.
- Coverage notes are tracked in `BulkSubmissionStepCoverage.md`.
