# Java Cucumber E2E Scaffold

This module is a ready-made Java E2E scaffold for Playwright + Cucumber + Gradle.

It is intentionally minimal so developers can add scenarios, step definitions, and page objects safely.

Current skeleton journey: navigate through the bulk submission flow using an existing Playwright storage state.

## What this scaffold already provides

- Cucumber + JUnit suite runner (`CucumberE2ETest`)
- Playwright browser lifecycle hooks (`PlaywrightHooks`)
- Thread-local scenario state container (`TestContext`)
- Login bootstrap task for storage state (`e2eLogin`)
- Database cleanup task (`e2eCleanup`)
- Allure result generation (`allureGenerateE2eReport`)
- Dotenv loading from root `.env` and module `.env`
- A minimal skeleton feature path selected by default: `src/test/resources/features/ui/skeleton`
- Skeleton flow reuses stored auth state from `E2E_STORAGE_STATE_PATH` (created once via `e2eLogin`)

## Project layout

- Runner: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/CucumberE2ETest.java`
- Hooks: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/hooks/`
- Context: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/state/TestContext.java`
- Steps: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/steps/`
- Pages: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/pages/`
- Features: `src/test/resources/features/`

### Folder tree (what lives where)

```text
laa-submit-a-bulk-claim-e2e-tests/
  build.gradle                              # E2E dependencies and Gradle tasks
  .env.example                              # Safe env template
  README.md                                 # Scaffold setup and usage
  CONTRIBUTING_E2E_TESTS.md                 # Contributor workflow
  DATA_GENERATORS_HANDOVER.md               # Generator handover notes
  src/
    test/
      java/
        uk/gov/justice/laa/bulkclaim/e2e/
          CucumberE2ETest.java              # Cucumber suite entry point
          hooks/                            # Playwright setup/teardown, screenshots, cleanup
          state/                            # Thread-local scenario context (`TestContext`)
          steps/                            # Cucumber step definitions
          pages/                            # Page object model classes
          utils/                            # Shared helper utilities/scripts
      resources/
        features/
          ui/
            skeleton/                       # Default scaffold feature path
```

## Quick setup

```bash
cd laa-submit-a-bulk-claim/laa-submit-a-bulk-claim-e2e-tests
cp .env.example .env
```

Set the minimum required values in `.env`:

```dotenv
E2E_BASE_URL=http://localhost:8082/
E2E_AUTH_MODE=mock
E2E_HEADLESS=false
E2E_STORAGE_STATE_PATH=build/e2e/storageState.json
CUCUMBER_TAGS=@smoke 
CUCUMBER_PARALLEL_ENABLED=false
CUCUMBER_PARALLELISM=1
MOCK_USERNAME=provider.user@provider.com
MOCK_PASSWORD=password
```

If you already have a valid storage state file at `E2E_STORAGE_STATE_PATH`, you can skip `e2eLogin` and run `cucumberTest` directly.

## Run locally

From repo root:

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:e2eLogin --no-daemon
./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
```

Reuse existing storage state only:

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
```

Optional tasks:

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:e2eCleanup --no-daemon
./gradlew :laa-submit-a-bulk-claim-e2e-tests:allureGenerateE2eReport --no-daemon
```

## Allure reports

The scaffold is already wired to publish Allure raw results during `cucumberTest`.

- Raw results location: `laa-submit-a-bulk-claim-e2e-tests/build/allure-results`
- Static HTML report location: `laa-submit-a-bulk-claim-e2e-tests/build/reports/allure-report`

Generate report:

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:allureGenerateE2eReport --no-daemon
```

Open report locally:

```bash
open laa-submit-a-bulk-claim-e2e-tests/build/reports/allure-report/index.html
```

Note: `allureGenerateE2eReport` checks that Allure CLI is installed.

## Core Gradle tasks

- `e2eLogin`: create Playwright storage state JSON
- `cucumberTest`: run Cucumber suite (`CucumberE2ETest`)
- `e2eCleanup`: delete recent test submissions from DB
- `allureGenerateE2eReport`: build static Allure report from results

## Tags (how to organize and run tests)

Use tags to control scope and avoid running everything while developing.

### Recommended conventions

- `@skeleton`: framework sanity checks only
- `@smoke`: fast, high-value core paths
- `@regression`: broader suite
- `@wip`: work in progress (exclude in CI)
- `@ignore`: temporarily disabled scenarios

### Run by tag

```bash
cd laa-submit-a-bulk-claim
CUCUMBER_TAGS='@skeleton' ./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
CUCUMBER_TAGS='@smoke and not @ignore and not @wip' ./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
CUCUMBER_TAGS='@regression and not @ignore' ./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
```

You can set `CUCUMBER_TAGS` in `.env` for local default behavior.

## Configuration reference

### Required in most environments

- `E2E_BASE_URL`
- `E2E_AUTH_MODE` (`mock` or `aad`)
- `E2E_STORAGE_STATE_PATH`

### Optional test execution controls

- `CUCUMBER_TAGS`
- `CUCUMBER_PARALLEL_ENABLED`
- `CUCUMBER_PARALLELISM`
- `E2E_HEADLESS`

### AAD login mode only

- `USERNAME`
- `PASSWORD`
- `MFA_SECRET`

### Mock login mode only

- `MOCK_USERNAME`
- `MOCK_PASSWORD`

### Optional service/data variables

- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `DB_NAME`, `DB_SSL`
- `DSTEW_API_BASE_URL`, `DSTEW_API_TOKEN`
- `FSP_API_BASE_URL`, `FSP_API_TOKEN`
- `PROVIDER_API_BASE_URL`, `PROVIDER_API_KEY`

## Extending the scaffold

1. Add a new feature file under `src/test/resources/features/ui/`
2. Add matching step definitions under `.../steps/`
3. Add/extend page objects under `.../pages/`
4. Keep selectors and navigation logic in page objects, not in step definitions
5. Use tags to run only your scenarios during development

## Notes

- Hooks remain active for every scenario, including screenshot-on-failure and cleanup behavior.
- `TestContext` is thread-local and safe for parallel execution when tests are written without shared mutable globals.
- For handover docs, see:
  - `CONTRIBUTING_E2E_TESTS.md`
  - `DATA_GENERATORS_HANDOVER.md`
