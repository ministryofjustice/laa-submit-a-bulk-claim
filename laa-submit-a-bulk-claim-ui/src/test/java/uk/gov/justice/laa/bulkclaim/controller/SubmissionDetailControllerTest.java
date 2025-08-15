package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;

@WebMvcTest(SubmissionDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Submission detail controller test")
class SubmissionDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @Nested
  @DisplayName("GET: /submission/{submissionId}")
  class GetSubmission {

    @Test
    @DisplayName("Should expect redirect")
    void shouldExpectRedirect() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      // When
      assertThat(
              mockMvc.perform(
                  get("/submission/" + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/view-submission-detail");
    }
  }
}
