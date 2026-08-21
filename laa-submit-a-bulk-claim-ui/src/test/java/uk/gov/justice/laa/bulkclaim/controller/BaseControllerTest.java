package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

@WebMvcTest(DefaultController.class)
@Import({WebMvcTestConfig.class})
public abstract class BaseControllerTest {

  @MockitoBean protected DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean protected FeatureFlagsConfig featureFlagsConfig;
  @MockitoBean protected OidcAttributeUtils oidcAttributeUtils;
  @MockitoBean protected SubmissionService submissionService;
}
