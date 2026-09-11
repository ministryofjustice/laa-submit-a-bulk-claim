package uk.gov.justice.laa.payments.submit.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.payments.submit.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.payments.submit.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.payments.submit.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.payments.submit.controller.SubmissionDetailController;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.payments.submit.service.SubmissionService;
import uk.gov.justice.laa.payments.submit.util.CurrencyUtil;
import uk.gov.justice.laa.payments.submit.util.DateTimeUtil;
import uk.gov.justice.laa.payments.submit.util.PaginationLinksBuilder;
import uk.gov.justice.laa.payments.submit.util.ThymeleafHrefUtils;
import uk.gov.justice.laa.payments.submit.util.ThymeleafUtils;

@WebMvcTest(SubmissionDetailController.class)
@Import({
  PaginationLinksBuilder.class,
  ThymeleafHrefUtils.class,
  CurrencyUtil.class,
  DateTimeUtil.class,
  ThymeleafUtils.class
})
class ViewSubmissionDetailAcceptedPageViewTest extends ViewTestBase {

  ViewSubmissionDetailAcceptedPageViewTest() {
    this.mapping = "/submissions/%s".formatted(submissionId);
  }

  @MockitoBean private SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean private SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean private SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private SubmissionService submissionService;

  @Test
  void acceptedSubmissionPageShowsSuccessBannerAndSummary() {
    var pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
    var submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .officeAccountNumber("ABC123")
            .areaOfLaw(AreaOfLaw.LEGAL_HELP)
            .build();

    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Accepted",
                LocalDate.of(2025, 5, 1),
                "ABC123",
                new BigDecimal("100.50"),
                AreaOfLaw.LEGAL_HELP.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(new MessagesSummary(List.of(), 0, 0, pagination, MessagesSource.CLAIM));
    when(submissionClaimDetailsBuilder.build(eq(submissionResponse), anyInt(), anyInt(), any()))
        .thenReturn(new SubmissionClaimsDetails(List.of(), pagination, BigDecimal.ZERO));

    when(submissionMatterStartsDetailsBuilder.build(any())).thenReturn(List.of());

    var doc = renderDocument();

    assertPageHasTitle(doc, "Submission details");
    assertPageHasHeading(doc, "Submission summary");
    assertPageHasContent(doc, "Accepted");
    assertPageHasContent(doc, "Your submission has been accepted.");
    assertPageHasContent(
        doc, "You cannot make changes. See messages for any actions you may want to take.");
    assertPageHasContent(doc, "request an amendment");
  }
}
