# Contributing to Java E2E Tests (Idiot-Proof Guide)

This guide is intentionally simple. Follow it exactly and you will be productive quickly.

## 1) Before you start

You need:
- Java 21
- Docker (for local app stack when needed)
- Access to test credentials/tokens for the target environment

Working directory conventions:
- Repo root: `laa-submit-a-bulk-claim`
- E2E module: `laa-submit-a-bulk-claim-e2e-tests`

## 2) First-time setup

1. Copy env template
2. Choose auth mode (`mock` is easiest locally)
3. Generate Playwright storage state
4. Run tests

```bash
cd laa-submit-a-bulk-claim/laa-submit-a-bulk-claim-e2e-tests
cp .env.example .env
```

Set at least these in `.env` for local mock auth:

```dotenv
E2E_BASE_URL=http://localhost:8082/
E2E_AUTH_MODE=mock
MOCK_USERNAME=provider.user@provider.com
MOCK_PASSWORD=password
CUCUMBER_TAGS=@smoke
CUCUMBER_PARALLEL_ENABLED=false
CUCUMBER_PARALLELISM=1
```

## 3) Run commands you will use every day

From repo root:

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:e2eLogin --no-daemon
./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
```

Optional cleanup:

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:e2eCleanup --no-daemon
```

Generate Allure report (if Allure CLI is installed):

```bash
cd laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-e2e-tests:allureGenerateE2eReport --no-daemon
```

## 4) Run only what you need

Use tags to avoid running everything.

```bash
cd laa-submit-a-bulk-claim
CUCUMBER_TAGS='@smoke' ./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
CUCUMBER_TAGS='@duplicateChecks and not @ignore' ./gradlew :laa-submit-a-bulk-claim-e2e-tests:cucumberTest --no-daemon
```

## 5) Where to put new code

- Feature files: `laa-submit-a-bulk-claim-e2e-tests/src/test/resources/features/ui/BulkSubmission`
- Step definitions: `laa-submit-a-bulk-claim-e2e-tests/src/test/java/uk/gov/justice/laa/bulkclaim/e2e/steps`
- Page objects: `laa-submit-a-bulk-claim-e2e-tests/src/test/java/uk/gov/justice/laa/bulkclaim/e2e/pages`
- Shared hooks/state: `.../hooks`, `.../state`
- Data generators: `.../utils/files`

## 6) How to write a new test (safe pattern)

1. Add a clear scenario in a `.feature` file
2. Reuse existing step phrases if possible
3. If missing step, add it in the right step class
4. Keep browser interactions in page objects (not directly in steps unless trivial)
5. Use generator steps for test data instead of hardcoded files
6. Run only your tag locally

Minimal example:

```gherkin
@myTag
Scenario: Upload valid legal help file
  Given I start from a clean logged-in state
  And I generate "Legal help" "csv" file with "1" outcomes
  When I upload the generated file
  Then I should see the submission summary for "Legal help"
```

## 7) Rules for good step definitions

Do:
- Keep one responsibility per step
- Put reusable logic in `BaseUiSteps` or helper classes
- Throw clear failure messages
- Use `TestContext` to pass state between steps

Do not:
- Duplicate existing step text with tiny wording changes
- Put complex selector logic in step files
- Hide flaky timing with random sleeps when a wait condition exists

## 8) Rules for page objects

Do:
- Keep selectors centralized in page classes
- Wait for deterministic states (`waitForURL`, `waitForSelector`, `waitForLoadState`)
- Add focused logging when debugging navigation

Do not:
- Mix assertion logic for multiple pages in one class
- Keep stale selectors after HTML changes

## 9) Data generator tips

Use generator-driven steps whenever possible:
- `I generate "<AreaOfLaw>" "<format>" file with "<n>" outcomes`
- `I generate "<AreaOfLaw>" "<format>" file with the following claims`
- `I override the generated file field "..." with value "..."`

If you touch generators, read:
- `DATA_GENERATORS_HANDOVER.md`

## 10) Parallelism policy (important)

If you want one scenario at a time:

```dotenv
CUCUMBER_PARALLEL_ENABLED=false
CUCUMBER_PARALLELISM=1
```

If parallel is enabled later, your test must be thread-safe:
- no shared mutable static state
- no reused storage state files between threads
- use `TestContext` properly

## 11) Common failures and what to check first

### "Cannot navigate to /upload"
- Check `E2E_BASE_URL`
- Check app/container health on expected port
- Check login state exists at `E2E_STORAGE_STATE_PATH`

### "Could not find two valid submission periods"
- Check `PROVIDER_API_KEY`
- Check provider schedules API response
- Check office/period assumptions in the scenario

### Login works locally but fails in pipeline
- Confirm auth mode (`E2E_AUTH_MODE`) is intended
- Confirm env secrets are configured in CI
- Confirm base URL is reachable from runner

## 12) Definition of done for E2E changes

Before opening MR:
- [ ] Scenario(s) pass locally with targeted tags
- [ ] Existing relevant tags are still green
- [ ] No hardcoded secrets in feature/code/docs
- [ ] Step text is readable to non-dev testers
- [ ] New selectors live in page objects
- [ ] Generator changes documented (if any)

## 13) Suggested MR template snippet

Use this in your merge request description:

```markdown
## E2E change summary
- Added/updated scenarios:
- Added/updated step definitions:
- Added/updated page objects:
- Data generator impact:
- Tags covered locally:

## Validation
- Commands run:
- Evidence (report/log/screenshot):
- Known risks/follow-ups:
```

