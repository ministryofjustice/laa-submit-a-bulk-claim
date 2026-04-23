# Accessibility Test Suite

This source set contains the Java Playwright accessibility suite for Submit a Bulk Claim.

## Adding A New Page Scenario

1. Add/update Claims API/OIDC payload templates in `resources/wiremock/...` as needed.
2. Register matching stubs in `AbstractAccessibilityTest`.
3. Add a scenario method in the relevant `...AccessibilityTest` class.
4. Use `AccessibilityAxeHelper.assertAccessible(...)` with only justified rule exclusions.
5. Run the suite and inspect generated reports for diagnostics.

## Run

Run all accessibility tests:

```sh
./gradlew :laa-submit-a-bulk-claim-ui:accessibilityTest
```

Optional arguments:

| Argument | Default | Purpose | Example |
|---|---|---|---|
| `-Daccessibility.headless=<true\|false>` | `true` | Runs Playwright in headless or headed mode. | `-Daccessibility.headless=false` |
| `-Daccessibility.maxParallelForks=<n>` | `max(1, availableProcessors / 2)` | Controls Gradle test JVM parallel forks for faster execution. | `-Daccessibility.maxParallelForks=4` |
| `--tests "<pattern>"` | all tests | Runs a specific class or method pattern. | `--tests "*UploadProcessAccessibilityTest.uploadValidationErrorAccessibilityChecks*"` |

Environment variable equivalents:

| Variable | Equivalent argument |
|---|---|
| `ACCESSIBILITY_HEADLESS` | `-Daccessibility.headless` |
| `ACCESSIBILITY_MAX_PARALLEL_FORKS` | `-Daccessibility.maxParallelForks` |

Implementation note: properties are forwarded by the `accessibilityTest` task in
`laa-submit-a-bulk-claim-ui/build.gradle`.

## Layout

- `java/.../tests`: feature-style test classes and scenario methods.
- `java/.../helpers`: shared base class, WireMock setup, and axe helper.
- `java/.../config`: test-only Spring Security and token provider config.
- `java/.../pages`: page object models (POMs) for navigation and UI interactions.
- `resources/wiremock/claim-api`: Claims API stub payloads.
- `resources/wiremock/oidc`: OIDC discovery/JWKS stub payloads.

## Page Object Models

The accessibility suite uses lightweight POMs in `java/.../pages` to keep selectors and
navigation logic out of test classes.

Current POMs:

- `LandingPage`
- `SubmissionSearchPage`
- `UploadPage`
- `SubmissionDetailPage`
- `ClaimDetailPage`

Guidelines:

- Keep assertions in test classes.
- Keep navigation/click/upload interactions in page objects.
- Add or extend a page object when multiple scenarios use the same selectors or flow.

## How The Suite Works

- Tests boot the Spring application on a random port (`@SpringBootTest`).
- Playwright drives the rendered UI and `AccessibilityAxeHelper` runs axe checks.
- A local WireMock server is started in the test base class and injected via dynamic properties:
  - `CLAIM_API_URL`
  - OIDC issuer URIs for SILAS/MOJ providers
- Virus and file validators are mocked for deterministic test setup.

## Reports

- JUnit XML:
  - `laa-submit-a-bulk-claim-ui/build/test-results/accessibilityTest`
- HTML test report:
  - `laa-submit-a-bulk-claim-ui/build/reports/tests/accessibilityTest`
- Axe violation detail files:
  - `laa-submit-a-bulk-claim-ui/build/reports/accessibility`
