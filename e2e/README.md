# Submit a Bulk Claim E2E Tests

## Running tests locally

Download the .env file from the team's 1Password vault and place it in the root of the e2e module.
By default this configuration will run the tests against a local instance of the app using silas,
but this can be changed for an app using mock OIDC also.

Ensure that the app is running locally before executing the tests. (see the main 
[README](../README.md) for instructions on how to run the app locally)

Run the tests in the command line using the following command:

```bash
./gradlew :e2e:test
```

To run a single test, specify the test class and method name. e.g:

```bash
./gradlew :e2e:test --tests "*BulkSubmissionE2ETest.happyPath"
```

## Generating Allure reports locally

Allure results are collected in the `src/e2e/build/allure-results` directory. To generate a report, 
run the following command:

```bash
./gradlew :e2e:allureReport
```
