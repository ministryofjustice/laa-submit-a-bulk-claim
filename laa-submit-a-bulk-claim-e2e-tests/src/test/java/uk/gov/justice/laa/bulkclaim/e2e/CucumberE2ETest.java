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
        value = "pretty,html:build/reports/cucumber/cucumber.html,json:build/reports/cucumber/cucumber.json"
)
//@ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "true")
//@ConfigurationParameter(key = "cucumber.execution.parallel.config.strategy", value = "fixed")
//@ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.parallelism", value = "4")
public class CucumberE2ETest {

//    static {
//        String tags = System.getenv("CUCUMBER_TAGS");
//
//        if (tags != null && !tags.isBlank()) {
//            System.setProperty("cucumber.filter.tags", tags);
//        }
//
//        String parallelEnabled = System.getenv("CUCUMBER_PARALLEL_ENABLED");
//        String parallelism = System.getenv("CUCUMBER_PARALLELISM");
//
//        System.setProperty("cucumber.execution.parallel.enabled", parallelEnabled);
//        System.setProperty("cucumber.execution.parallel.config.strategy", "fixed");
//        System.setProperty("cucumber.execution.parallel.config.fixed.parallelism", parallelism);
//    }
}