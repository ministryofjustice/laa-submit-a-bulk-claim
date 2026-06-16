# UI testing with Playwright

This module now has a lightweight Playwright UI-test framework in `src/uiTest/java`.

## Design goals

- reuse the existing accessibility browser wiring instead of creating a second heavy framework
- keep page objects simple and focused on navigation/interactions
- keep assertions in the test classes
- keep backend dependencies deterministic via the existing WireMock and test-auth setup

## Structure

- `src/uiTest/java/.../helpers/AbstractUiTest` – base class for UI flow tests
- `src/uiTest/java/.../pages` – simple Playwright page objects for user journeys
- `src/accessibilityTest/java` – existing accessibility suite and shared browser plumbing reused by UI tests

## How to add a new UI test

1. Create a test class under `src/uiTest/java`
2. Extend `AbstractUiTest`
3. Instantiate the page object you need using the shared `page`
4. Drive the journey and assert behaviour

Example shape:

```java
class UploadFlowUiTest extends AbstractUiTest {

  private UploadPage uploadPage;

  @BeforeEach
  void setUpPageObject() {
    uploadPage = new UploadPage(page);
  }
}
```

## Run the suite

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-ui:uiTest
```

The UI suite now defaults to 4 Gradle test forks. You can override that when needed:

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-ui:uiTest -Dui.maxParallelForks=2
```

Run with a visible browser:

```bash
cd /Users/temilayo.oluwaseunkufeji/Documents/laa-data-claims-parent/laa-submit-a-bulk-claim
./gradlew :laa-submit-a-bulk-claim-ui:uiTest -Dui.headless=false
```

