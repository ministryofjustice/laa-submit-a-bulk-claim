package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionCostsSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

@WebMvcTest(SubmissionDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Submission detail controller test")
class SubmissionDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean private SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean private DataClaimsRestService dataClaimsRestService;

  @Nested
  @DisplayName("GET: /submission/{submissionId}")
  class GetSubmission {

    @Test
    @DisplayName("Should expect redirect")
    void shouldExpectRedirect() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      when(dataClaimsRestService.getSubmission(submissionReference))
          .thenReturn(Mono.just(GetSubmission200Response.builder().build()));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  LocalDate.of(2025, 1, 1)));
      when(submissionClaimDetailsBuilder.build(any()))
          .thenReturn(
              new SubmissionClaimDetails(
                  new SubmissionCostsSummary(
                      new BigDecimal("100.00"),
                      new BigDecimal("100.50"),
                      new BigDecimal("100.85"),
                      new BigDecimal("100.90"),
                      new BigDecimal("123.45")),
                  Collections.emptyList()));
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
