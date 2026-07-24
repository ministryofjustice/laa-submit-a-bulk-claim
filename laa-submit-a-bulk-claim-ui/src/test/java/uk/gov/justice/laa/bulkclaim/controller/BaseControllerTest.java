package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;

@WebMvcTest(DefaultController.class)
@Import({WebMvcTestConfig.class})
public abstract class BaseControllerTest {

  @MockitoBean protected FeatureFlagsConfig featureFlagsConfig;
}
