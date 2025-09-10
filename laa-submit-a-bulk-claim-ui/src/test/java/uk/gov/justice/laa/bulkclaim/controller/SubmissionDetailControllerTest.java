package uk.gov.justice.laa.bulkclaim.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionCostsSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@WebMvcTest(SubmissionDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Submission detail controller test")
class SubmissionDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean private SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean private SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  @MockitoBean private DataClaimsRestService dataClaimsRestService;

  @Nested
  @DisplayName("GET: /submission/{submissionId}")
  class GetSubmissionReference {

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

  @Nested
  @DisplayName("GET: /submission/{submissionId}/detail")
  class GetSubmissionDetail {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      when(dataClaimsRestService.getSubmission(submissionReference))
          .thenReturn(Mono.just(SubmissionResponse.builder().build()));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  LocalDateTime.of(2025, 1, 1, 10, 10, 10)));
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
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail");
      verify(submissionClaimDetailsBuilder, times(1)).build(any());
      verify(submissionMatterStartsDetailsBuilder, times(0)).build(any());
    }

    @Test
    @DisplayName("Should return expected result with claims")
    void shouldReturnExpectedResultWithClaims() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      when(dataClaimsRestService.getSubmission(submissionReference))
          .thenReturn(Mono.just(SubmissionResponse.builder().build()));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  LocalDateTime.of(2025, 1, 1, 10, 10, 10)));
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
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?navTab=CLAIM_DETAILS")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail");
      verify(submissionClaimDetailsBuilder, times(1)).build(any());
      verify(submissionMatterStartsDetailsBuilder, times(0)).build(any());
    }

    @Test
    @DisplayName("Should return expected result with matter starts")
    void shouldReturnExpectedResultWithMatterStarts() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      when(dataClaimsRestService.getSubmission(submissionReference))
          .thenReturn(Mono.just(SubmissionResponse.builder().build()));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  LocalDateTime.of(2025, 1, 1, 10, 10, 10)));
      HashMap<SubmissionMatterStartsRow, Long> matterTypes = new HashMap<>();
      matterTypes.put(new SubmissionMatterStartsRow("Description"), 1L);
      when(submissionMatterStartsDetailsBuilder.build(any()))
          .thenReturn(new SubmissionMatterStartsDetails(matterTypes));
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?navTab=MATTER_STARTS")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail");
      verify(submissionClaimDetailsBuilder, times(0)).build(any());
      verify(submissionMatterStartsDetailsBuilder, times(1)).build(any());
    }

    @Test
    @DisplayName("Should throw exception when submission reference is null")
    void shouldThrowExceptionWhenSubmissionDoesNotExist() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      when(dataClaimsRestService.getSubmission(submissionReference)).thenReturn(Mono.empty());
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?navTab=MATTER_STARTS")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .failure()
          .hasMessageEndingWith("Submission bceac49c-d756-4e05-8e28-3334b84b6fe8 does not exist");
    }
  }
}
