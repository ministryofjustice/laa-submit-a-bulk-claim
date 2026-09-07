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

## Generating DBUnit test data for E2E tests
For some E2E tests, the DB is populated directly using DBUnit. This is done by exporting data
from an already populated DB in DBeaver. 

To get data out of DBeaver, right click the tables you
wish to export and click export.

![DBUnit_Export_1.png](../docs/images/DBUnit_Export_1.png)

On the resulting pop up, ensure that:
1. On `Export Target` step, the export format is set to `DBUnit`. 
2. On `Format Settings` step, disable `Include NULL values in export`
3. On `Output` step, specify the output directory for the exported data, as a separate file will be created for each table.

Once ready, click `Proceed`.

Finally, condense all of those files manually into a single file, and place it in the `e2e/src/test/resources/datasets` directory.

You should be able to include this dataset in your E2E tests by referencing it
in the test configuration by extending `DbUnitBaseTest` and overriding the `getDataSetPath()` method:
```java
class MyE2ETest extends DbUnitBaseTest {
    @Override
    protected String getDataSetPath() {
        return "datasets/my-dataset.xml";
    }
}
```