package uk.gov.justice.laa.bulkclaim.e2e;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/ui/BulkSubmission")
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "uk.gov.justice.laa.bulkclaim.e2e"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty,html:build/reports/cucumber/cucumber.html,json:build/reports/cucumber/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
)

public class CucumberE2ETest {
}