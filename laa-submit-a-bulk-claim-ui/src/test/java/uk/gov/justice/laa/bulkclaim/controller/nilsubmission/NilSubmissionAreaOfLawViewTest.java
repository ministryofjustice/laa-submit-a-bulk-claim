package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

@WebMvcTest(NilSubmissionAreaOfLawController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class NilSubmissionAreaOfLawViewTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FeatureFlagsConfig featureFlagsConfig;

  @Test
  void invalidAreaOfLawShowsInlineError() throws Exception {
    var response =
        mockMvc
            .perform(
                post("/nil-submission/areaoflaw")
                    .sessionAttr("nilSubmissionForm", new NilSubmissionForm())
                    .param("areaOfLaw", "potato"))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(response.getContentAsString());

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(document.selectFirst(".govuk-form-group").className())
        .contains("govuk-form-group--error");
    assertThat(document.select(".govuk-error-message")).hasSize(1);
    assertThat(document.selectFirst(".govuk-error-message").text())
        .contains("Select a valid area of law.");
  }

  @Test
  void areaOfLawNotSelectedShowsInlineError() throws Exception {
    var response =
        mockMvc
            .perform(
                post("/nil-submission/areaoflaw")
                    .sessionAttr("nilSubmissionForm", new NilSubmissionForm()))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(response.getContentAsString());

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(document.select(".govuk-error-message")).hasSize(1);
    assertThat(document.selectFirst(".govuk-error-message").text())
        .contains("Select the area of law");
  }
}
