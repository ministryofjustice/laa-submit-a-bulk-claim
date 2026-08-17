package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus.VALIDATION_IN_PROGRESS;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus.VALIDATION_SUCCEEDED;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.util.CurrencyUtil;
import uk.gov.justice.laa.bulkclaim.util.DateTimeUtil;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafUtils;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;

@WebMvcTest(SubmissionDetailController.class)
@AutoConfigureMockMvc
@Import({
  PaginationLinksBuilder.class,
  ThymeleafHrefUtils.class,
  CurrencyUtil.class,
  DateTimeUtil.class,
  ThymeleafUtils.class
})
@DisplayName("Submission detail controller test")
class SubmissionDetailControllerTest extends BaseControllerTest {

  private static final UUID SUBMISSION_ID = UUID.fromString("bceac49c-d756-4e05-8e28-3334b84b6fe8");
  private static final String OFFICE_CODE = "123456";

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean private SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean private SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private PaginationUtil paginationUtil;
  @MockitoBean private SubmissionService submissionService;

  @Nested
  @DisplayName("GET: /submission/{submissionId}")
  class GetSubmission {

    @Test
    @DisplayName("Should redirect to import in progress when submission validation is running")
    void shouldRedirectToImportInProgressWhenValidationInProgress() {
      var submission =
          SubmissionResponse.builder()
              .submissionId(SUBMISSION_ID)
              .status(VALIDATION_IN_PROGRESS)
              .officeAccountNumber(OFFICE_CODE)
              .build();
      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER)).thenReturn(submission);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));

      MvcTestResult result =
          mockMvc.perform(
              get("/submission/" + SUBMISSION_ID).with(oidcLogin().oidcUser(OIDC_USER)));

      assertThat(result).hasStatus3xxRedirection().hasRedirectedUrl("/upload-is-being-checked");
    }
  }

  @Nested
  @DisplayName("GET: /submissions/{submissionId}")
  class GetSubmissionDetail {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      var submissionResponse =
          SubmissionResponse.builder()
              .status(VALIDATION_SUCCEEDED)
              .officeAccountNumber(OFFICE_CODE)
              .build();
      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER))
          .thenReturn(submissionResponse);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
      when(submissionClaimDetailsBuilder.build(eq(submissionResponse), anyInt(), anyInt(), any()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));
      when(submissionMatterStartsDetailsBuilder.build(any()))
          .thenReturn(List.of(new SubmissionMatterStartsRow("Description", 34)));

      assertThat(
              mockMvc.perform(
                  get("/submissions/%s?sort=line_number,desc".formatted(SUBMISSION_ID))
                      .with(oidcLogin().oidcUser(OIDC_USER))
                      .sessionAttr("submissionId", SUBMISSION_ID)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail-accepted");

      verify(submissionClaimDetailsBuilder, times(1)).build(any(), anyInt(), anyInt(), any());
      verify(submissionMessagesBuilder, times(1))
          .build(SUBMISSION_ID, null, ValidationMessageType.WARNING, 0, 50, null);
      verify(submissionMatterStartsDetailsBuilder, times(1)).build(any());
    }

    @Test
    @DisplayName("Should return expected result with claims")
    void shouldReturnExpectedResultWithClaims() {
      var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      var submissionResponse =
          SubmissionResponse.builder()
              .status(SubmissionStatus.VALIDATION_FAILED)
              .officeAccountNumber(OFFICE_CODE)
              .build();
      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER))
          .thenReturn(submissionResponse);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Invalid",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  "Legal aid",
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));
      when(submissionMessagesBuilder.buildErrors(any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
      when(submissionMatterStartsDetailsBuilder.build(any()))
          .thenReturn(List.of(new SubmissionMatterStartsRow("Description", 34)));
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/submissions/%s?sort=line_number,desc".formatted(SUBMISSION_ID))
                      .with(oidcLogin().oidcUser(OIDC_USER))
                      .sessionAttr("submissionId", SUBMISSION_ID)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail-invalid");

      verify(submissionMessagesBuilder, times(1)).buildErrors(SUBMISSION_ID, 0, 50, null);
      verify(submissionMatterStartsDetailsBuilder, times(1)).build(any());
    }

    @Test
    @DisplayName("Should return expected result with matter starts")
    void shouldReturnExpectedResultWithMatterStarts() {
      var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      var submission =
          SubmissionResponse.builder()
              .status(VALIDATION_SUCCEEDED)
              .officeAccountNumber(OFFICE_CODE)
              .areaOfLaw(AreaOfLaw.LEGAL_HELP)
              .build();
      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER)).thenReturn(submission);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  AreaOfLaw.LEGAL_HELP.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));
      List<SubmissionMatterStartsRow> matterTypes = new ArrayList<>();
      matterTypes.add(new SubmissionMatterStartsRow("Description", 34));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
      when(submissionMatterStartsDetailsBuilder.build(any())).thenReturn(matterTypes);

      assertThat(
              mockMvc.perform(
                  get("/submissions/%s?sort=line_number,desc&navTab=MATTER_STARTS"
                          .formatted(SUBMISSION_ID))
                      .with(oidcLogin().oidcUser(OIDC_USER))
                      .sessionAttr("submissionId", SUBMISSION_ID)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-detail-accepted");

      verify(submissionClaimDetailsBuilder).build(any(), anyInt(), anyInt(), any());
      verify(submissionMatterStartsDetailsBuilder, times(1)).build(any());
    }

    @Test
    @DisplayName("Should populate summary data for accepted submission")
    void shouldPopulateSummaryForAcceptedSubmission() {
      var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      var submissionResponse =
          SubmissionResponse.builder()
              .submissionId(SUBMISSION_ID)
              .status(VALIDATION_SUCCEEDED)
              .officeAccountNumber(OFFICE_CODE)
              .areaOfLaw(AreaOfLaw.CRIME_LOWER)
              .build();
      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER))
          .thenReturn(submissionResponse);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  null,
                  AreaOfLaw.CRIME_LOWER.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.TEN));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));

      MvcTestResult response =
          mockMvc.perform(
              get("/submissions/%s?sort=line_number,desc".formatted(SUBMISSION_ID))
                  .with(oidcLogin().oidcUser(OIDC_USER))
                  .sessionAttr("submissionId", SUBMISSION_ID));

      assertThat(response).hasStatusOk().hasViewName("pages/view-submission-detail-accepted");
      verify(submissionClaimDetailsBuilder).build(any(), anyInt(), anyInt(), any());
      verify(submissionMessagesBuilder)
          .build(SUBMISSION_ID, null, ValidationMessageType.WARNING, 0, 50, null);
    }

    @Test
    @DisplayName("Should display voided tag for voided claim in claims table")
    void shouldDisplayVoidedTagForVoidedClaimInClaimsTable() {
      var pagination = Page.builder().totalPages(3).totalElements(21).number(0).size(10).build();
      var submissionResponse =
          SubmissionResponse.builder()
              .submissionId(SUBMISSION_ID)
              .status(VALIDATION_SUCCEEDED)
              .officeAccountNumber(OFFICE_CODE)
              .areaOfLaw(AreaOfLaw.CRIME_LOWER)
              .build();
      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER))
          .thenReturn(submissionResponse);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));
      when(submissionSummaryBuilder.build(any()))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  BigDecimal.ONE,
                  AreaOfLaw.CRIME_LOWER.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new SubmissionClaimsDetails(
                  List.of(
                      new SubmissionClaimRow(
                          UUID.fromString("5146e93f-92c8-4c56-bd25-0cb6953f534d"),
                          1,
                          "UFN123",
                          "UCN123",
                          "Client",
                          "Name",
                          null,
                          null,
                          null,
                          "cat",
                          "matter",
                          LocalDate.of(2025, 1, 1),
                          0,
                          "VOIDED",
                          "feeType",
                          "feeCode",
                          new SubmissionClaimRowCostsDetails(
                              BigDecimal.ZERO,
                              BigDecimal.ZERO,
                              BigDecimal.ZERO,
                              BigDecimal.ZERO,
                              BigDecimal.ZERO,
                              BigDecimal.ZERO,
                              BigDecimal.ZERO),
                          Boolean.FALSE,
                          BigDecimal.ONE,
                          BigDecimal.ONE)),
                  pagination,
                  BigDecimal.ONE));
      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));

      assertThat(
              mockMvc.perform(
                  get("/submissions/%s".formatted(SUBMISSION_ID))
                      .with(oidcLogin().oidcUser(OIDC_USER))
                      .sessionAttr("submissionId", SUBMISSION_ID)))
          .hasStatusOk()
          .body()
          .asString()
          .contains("govuk-tag govuk-tag--red")
          .contains("Voided");
    }

    @Test
    @DisplayName("Should call submissions with sort parameter")
    void shouldCallWithSortParam() {
      var submissionResponse =
          SubmissionResponse.builder()
              .status(VALIDATION_SUCCEEDED)
              .officeAccountNumber(OFFICE_CODE)
              .build();

      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER))
          .thenReturn(submissionResponse);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));

      when(submissionSummaryBuilder.build(eq(submissionResponse)))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  AreaOfLaw.LEGAL_HELP.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));

      var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));

      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));

      when(submissionMatterStartsDetailsBuilder.build(any()))
          .thenReturn(List.of(new SubmissionMatterStartsRow("Description", 34)));
      mockMvc.perform(
          get("/submissions/%s?page=0&sort=line_number,desc".formatted(SUBMISSION_ID))
              .with(oidcLogin().oidcUser(OIDC_USER))
              .sessionAttr("submissionId", SUBMISSION_ID));

      verify(submissionClaimDetailsBuilder)
          .build(eq(submissionResponse), eq(0), anyInt(), eq("line_number,desc"));
    }

    @Test
    @DisplayName("Should uses defaults sort parameter when sort parameter is absent")
    void shouldUseDefaultSortParameter() {
      var submissionResponse =
          SubmissionResponse.builder()
              .status(VALIDATION_SUCCEEDED)
              .officeAccountNumber(OFFICE_CODE)
              .build();

      when(submissionService.getSubmission(SUBMISSION_ID, OIDC_USER))
          .thenReturn(submissionResponse);
      when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));

      when(submissionSummaryBuilder.build(eq(submissionResponse)))
          .thenReturn(
              new SubmissionSummary(
                  SUBMISSION_ID,
                  "Submitted",
                  LocalDate.of(2025, 5, 1),
                  "AQ2B3C",
                  new BigDecimal("100.50"),
                  AreaOfLaw.LEGAL_HELP.getValue(),
                  OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
                  false));

      var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
      when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));

      when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
          .thenReturn(
              new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));

      when(submissionMatterStartsDetailsBuilder.build(any()))
          .thenReturn(List.of(new SubmissionMatterStartsRow("Description", 34)));
      mockMvc.perform(
          get("/submissions/%s?page=0".formatted(SUBMISSION_ID))
              .with(oidcLogin().oidcUser(OIDC_USER))
              .sessionAttr("submissionId", SUBMISSION_ID));

      verify(submissionClaimDetailsBuilder)
          .build(eq(submissionResponse), eq(0), anyInt(), eq("line_number,asc"));
    }
  }
}
