package uk.gov.justice.laa.payments.submit.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class SubmissionDetailsCrimeLowerViewTest extends SubmissionDetailsViewTestBase {

  private static final UUID ESCAPED_CLAIM_ID = UUID.randomUUID();
  private static final UUID FIXED_FEE_CLAIM_ID = UUID.randomUUID();

  @BeforeEach
  void beforeEach() {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(CRIME_LOWER)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);

    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Submitted",
                LocalDate.of(2025, 5, 1),
                OFFICE_CODE,
                BigDecimal.ZERO,
                CRIME_LOWER.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));

    SubmissionClaimRow escapedClaim =
        SubmissionClaimRow.builder()
            .id(ESCAPED_CLAIM_ID)
            .lineNumber(1)
            .ufn("011015/125")
            .clientForename("First")
            .clientSurname("Escaped")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 10))
            .feeCode("CLIN")
            .status("ACCEPTED")
            .escapeCase(true)
            .calculatedValue(new BigDecimal("250.00"))
            .build();
    SubmissionClaimRow fixedFeeClaim =
        SubmissionClaimRow.builder()
            .id(FIXED_FEE_CLAIM_ID)
            .lineNumber(2)
            .ufn("011015/126")
            .clientForename("FirstTwo")
            .clientSurname("Unescaped")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 12))
            .feeCode("IBVN")
            .status("AMENDED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("95.50"))
            .build();
    Page claimsPagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(2).build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(escapedClaim, fixedFeeClaim), claimsPagination, new BigDecimal("345.50")));

    Page messagesPagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(1).build();
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(
                    MessageRow.builder()
                        .claimReference(Optional.of(ESCAPED_CLAIM_ID))
                        .clientSurname("Escaped")
                        .clientForename("First")
                        .ufn("011015/125")
                        .message("This claim is escaped")
                        .build()),
                1,
                1,
                messagesPagination,
                MessagesSource.CLAIM));

    when(submissionMatterStartsDetailsBuilder.build(any())).thenReturn(List.of());
  }

  @Test
  void renderCrimeLowerSubmissionDetails() {
    Document doc = renderDocument();

    // Summary
    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(6);
    assertRowContainsValues(summaryList.get(2), "Area of law", "Crime lower");
    assertRowContainsValues(summaryList.get(5), "Calculated bulk claim value", "£345.50");

    // Warning count banner
    assertPageHasContent(doc, "1 claim has a warning message");

    // Crime lower has no matter starts
    var navItemTexts = doc.select(".moj-sub-navigation__item").eachText();
    assertThat(navItemTexts).contains("Claims (2)", "Messages (1)");
    assertThat(navItemTexts).noneMatch(text -> text.contains("Matter starts"));

    // Claims table headers
    assertThat(doc.select(".govuk-table__header").eachText())
        .containsExactly(
            "Client name",
            "UFN",
            "Fee code",
            "Date work concluded",
            "Initial calculated value",
            "Escape case",
            "Status");

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(2);

    var escapedRowCells = rows.get(0).select("td");
    assertThat(escapedRowCells.get(0).text()).isEqualTo("First Escaped");
    assertThat(escapedRowCells.get(1).text()).isEqualTo("011015/125");
    assertThat(escapedRowCells.get(2).text()).isEqualTo("CLIN");
    assertThat(escapedRowCells.get(4).text()).isEqualTo("£250.00");
    assertThat(escapedRowCells.get(5).text()).isEqualTo("Yes");
    assertThat(selectFirst(rows.get(0), ".govuk-tag--green").text()).isEqualTo("Accepted");
    assertThat(selectFirst(rows.get(0), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, ESCAPED_CLAIM_ID));

    var fixedFeeRowCells = rows.get(1).select("td");
    assertThat(fixedFeeRowCells.get(0).text()).isEqualTo("FirstTwo Unescaped");
    assertThat(fixedFeeRowCells.get(1).text()).isEqualTo("011015/126");
    assertThat(fixedFeeRowCells.get(2).text()).isEqualTo("IBVN");
    assertThat(fixedFeeRowCells.get(4).text()).isEqualTo("£95.50");
    assertThat(fixedFeeRowCells.get(5).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(1), ".govuk-tag--yellow").text()).isEqualTo("Amended");
    assertThat(selectFirst(rows.get(1), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, FIXED_FEE_CLAIM_ID));
  }

  @Test
  void checkMessagesTabContainsEscapeMessage() {
    Document doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(1);

    var messageCells = rows.get(0).select("td");
    assertThat(messageCells.get(1).text()).isEqualTo("Escaped");
    assertThat(messageCells.get(2).text()).isEqualTo("First");
    assertThat(messageCells.get(3).text()).isEqualTo("011015/125");
    assertThat(messageCells.get(4).text()).isEqualTo("This claim is escaped");
  }
}
