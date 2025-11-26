package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;

@WebMvcTest(SubmissionDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Submission detail controller test")
class SubmissionDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean private SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean private SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private PaginationUtil paginationUtil;

  @Nested
  @DisplayName("GET: /submission/{submissionId}")
  class GetSubmissionReference {

    @Test
    @DisplayName("Should store submission and redirect to detail view when submission exists")
    void shouldStoreSubmissionsAndRedirectToDetail() {
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      SubmissionBase submission = mock(SubmissionBase.class);
      when(submission.getSubmissionId()).thenReturn(submissionReference);
      when(submission.getStatus()).thenReturn(SubmissionStatus.VALIDATION_SUCCEEDED);

      SubmissionsResultSet submissions = new SubmissionsResultSet();
      submissions.setContent(List.of(submission));

      MockHttpSession session = new MockHttpSession();
      session.setAttribute("submissions", submissions);

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/submission/" + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .session(session)))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/view-submission-detail?submissionId=" + submissionReference);
    }

    @Test
    @DisplayName("Should redirect to import in progress when submission validation is running")
    void shouldRedirectToImportInProgressWhenValidationInProgress() throws Exception {
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      SubmissionBase submission = mock(SubmissionBase.class);
      when(submission.getSubmissionId()).thenReturn(submissionReference);
      when(submission.getStatus()).thenReturn(SubmissionStatus.VALIDATION_IN_PROGRESS);

      SubmissionsResultSet submissions = new SubmissionsResultSet();
      submissions.setContent(List.of(submission));

      MockHttpSession session = new MockHttpSession();
      session.setAttribute("submissions", submissions);

      MvcTestResult result =
          mockMvc.perform(
              get("/submission/" + submissionReference)
                  .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                  .session(session));

      assertThat(result).hasStatus3xxRedirection().hasRedirectedUrl("/upload-is-being-checked");
    }

    @Test
    @DisplayName("Should return forbidden when submission does not belong to current session user")
    void shouldReturnForbiddenWhenSubmissionMissing() {
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      SubmissionBase submission = mock(SubmissionBase.class);
      when(submission.getSubmissionId()).thenReturn(UUID.randomUUID());
      when(submission.getStatus()).thenReturn(SubmissionStatus.VALIDATION_SUCCEEDED);

      SubmissionsResultSet submissions = new SubmissionsResultSet();
      submissions.setContent(List.of(submission));

      MockHttpSession session = new MockHttpSession();
      session.setAttribute("submissions", submissions);

      assertThat(
              mockMvc.perform(
                  get("/submission/" + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .session(session)))
          .failure()
          .hasMessageContaining("403 FORBIDDEN");
    }
  }

  @Nested
  @DisplayName("GET: /submission/{submissionId}/detail")
  class GetSubmissionDetail {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      // Given
      final UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      final Page pagination =
          Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      SubmissionResponse submissionResponse =
          SubmissionResponse.builder().status(SubmissionStatus.VALIDATION_SUCCEEDED).build();
      when(dataClaimsRestClient.getSubmission(submissionReference))
          .thenReturn(Mono.just(submissionResponse));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?submissionId=" + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail-accepted");
      verify(submissionClaimDetailsBuilder, times(1)).build(any(), anyInt(), anyInt());
      verifyNoInteractions(submissionMatterStartsDetailsBuilder);
    }

    @Test
    @DisplayName("Should return expected result with claims")
    void shouldReturnExpectedResultWithClaims() {
      // Given
      final UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      final Page pagination =
          Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      SubmissionResponse submissionResponse =
          SubmissionResponse.builder().status(SubmissionStatus.VALIDATION_FAILED).build();
      when(dataClaimsRestClient.getSubmission(submissionReference))
          .thenReturn(Mono.just(submissionResponse));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Invalid",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));
      when(submissionMessagesBuilder.buildErrors(any(), anyInt(), anyInt()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?navTab=CLAIM_DETAILS&submissionId="
                          + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail-invalid");

      verify(submissionClaimDetailsBuilder, times(1)).build(any(), anyInt(), anyInt());
      verify(submissionMessagesBuilder, times(1)).buildErrors(submissionReference, 0, 10);
      verifyNoInteractions(submissionMatterStartsDetailsBuilder);
    }

    @Test
    @DisplayName("Should return expected result with matter starts")
    void shouldReturnExpectedResultWithMatterStarts() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      Page pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      when(dataClaimsRestClient.getSubmission(submissionReference))
          .thenReturn(
              Mono.just(
                  SubmissionResponse.builder()
                      .status(SubmissionStatus.VALIDATION_SUCCEEDED)
                      .areaOfLaw(AreaOfLaw.LEGAL_HELP)
                      .build()));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  AreaOfLaw.LEGAL_HELP.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
      List<SubmissionMatterStartsRow> matterTypes = new ArrayList<>();
      matterTypes.add(new SubmissionMatterStartsRow("Description", 34));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
      when(submissionMatterStartsDetailsBuilder.build(any())).thenReturn(matterTypes);
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?navTab=MATTER_STARTS&submissionId="
                          + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail-accepted");
      verify(submissionClaimDetailsBuilder).build(any(), anyInt(), anyInt());
      verify(submissionMatterStartsDetailsBuilder, times(1)).build(any());
    }

    @Test
    @DisplayName("Should populate summary data for accepted submission")
    void shouldPopulateSummaryForAcceptedSubmission() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      Page pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      SubmissionResponse submissionResponse =
          SubmissionResponse.builder()
              .submissionId(submissionReference)
              .status(SubmissionStatus.VALIDATION_SUCCEEDED)
              .areaOfLaw(AreaOfLaw.CRIME_LOWER)
              .build();
      when(dataClaimsRestClient.getSubmission(submissionReference))
          .thenReturn(Mono.just(submissionResponse));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  submissionReference,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  null,
                  AreaOfLaw.CRIME_LOWER.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.TEN));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));

      // When
      MvcTestResult response =
          mockMvc.perform(
              get("/view-submission-detail?submissionId=" + submissionReference)
                  .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                  .sessionAttr("submissionId", submissionReference));

      // Then
      assertThat(response).hasStatusOk().hasViewName("pages/view-submission-detail-accepted");
      verify(submissionClaimDetailsBuilder).build(any(), anyInt(), anyInt());
      verify(submissionMessagesBuilder)
          .build(submissionReference, null, ValidationMessageType.WARNING, 0, 10);
    }

    @Test
    @DisplayName("Should throw exception when submission does not exist")
    void shouldThrowExceptionWhenSubmissionDoesNotExist() {
      // Given
      UUID submissionReference = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
      when(dataClaimsRestClient.getSubmission(submissionReference)).thenReturn(Mono.empty());
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-detail?navTab=MATTER_STARTS&submissionId="
                          + submissionReference)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("submissionId", submissionReference)))
          .failure()
          .hasMessageEndingWith("Submission bceac49c-d756-4e05-8e28-3334b84b6fe8 does not exist");
    }
  }
}
