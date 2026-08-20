package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageQuery;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageSortField;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.view.SubmissionViewQuery;
import uk.gov.justice.laa.bulkclaim.dto.submission.view.SubmissionViewSortField;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SubmissionDetailController {

  private final SubmissionSummaryBuilder submissionSummaryBuilder;
  private final SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  private final SubmissionMessagesBuilder submissionMessagesBuilder;
  private final SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  private final PaginationLinksBuilder paginationLinksBuilder;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final SubmissionService submissionService;

  @GetMapping({"/submission/{submissionId}", "/submissions/{submissionId}"})
  public String getSubmissionReference(
      Model model,
      @PathVariable UUID submissionId,
      @Valid SubmissionViewQuery submissionViewQuery,
      @Valid MessageQuery messageQuery,
      @AuthenticationPrincipal OidcUser user,
      HttpSession session) {

    var submission = submissionService.getSubmission(submissionId, user);

    // Redirect based on submission status
    if (submission != null && submission.getStatus() == SubmissionStatus.VALIDATION_IN_PROGRESS) {
      session.setAttribute(SUBMISSION_ID, submissionId);
      session.setAttribute(BULK_SUBMISSION_ID, submission.getBulkSubmissionId());
      return "redirect:/upload-is-being-checked";
    }

    model.addAttribute("submissionViewQuery", submissionViewQuery);
    model.addAttribute("SubmissionViewSortField", SubmissionViewSortField.class);

    model.addAttribute("messageQuery", messageQuery);
    model.addAttribute("MessageSortField", MessageSortField.class);

    model.addAttribute("claimDetailsTab", ViewSubmissionNavigationTab.CLAIM_DETAILS);

    model.addAttribute(
        "showUpdatedCalculatedValueColumn",
        featureFlagsConfig.getIsUpdatedCalculatedValueAvailable());

    SubmissionSummary submissionSummary = submissionSummaryBuilder.build(submission);
    boolean submissionAccepted = submission.getStatus() == SubmissionStatus.VALIDATION_SUCCEEDED;

    if (submissionAccepted) {
      submissionSummary =
          handleAcceptedSubmission(
              model, user, submissionSummary, submission, submissionViewQuery, messageQuery);
      addCommonSubmissionAttributes(model, submissionSummary, submission, submissionViewQuery);
      return "pages/view-submission-detail-accepted";
    } else {
      handleInvalidSubmission(model, user, submission, messageQuery);
      addCommonSubmissionAttributes(model, submissionSummary, submission, submissionViewQuery);
      return "pages/view-submission-detail-invalid";
    }
  }

  private SubmissionSummary handleAcceptedSubmission(
      Model model,
      OidcUser user,
      SubmissionSummary submissionSummary,
      SubmissionResponse submissionResponse,
      SubmissionViewQuery submissionViewQuery,
      MessageQuery messageQuery) {

    SubmissionClaimsDetails claimDetails =
        submissionClaimDetailsBuilder.build(
            submissionResponse,
            submissionViewQuery.getPage(),
            submissionViewQuery.getSize(),
            submissionViewQuery.getSort().toString());
    model.addAttribute("claimDetails", claimDetails);
    model.addAttribute(
        "claimDetailsPaginationLinks",
        paginationLinksBuilder.build(
            "/submissions/%s#claims-table".formatted(submissionViewQuery.getSubmissionId()),
            claimDetails.pagination(),
            "page",
            "navTab",
            ViewSubmissionNavigationTab.CLAIM_DETAILS,
            "sort",
            submissionViewQuery.getSort().toString()));

    if (claimDetails.totalClaimValue() != null) {
      submissionSummary =
          new SubmissionSummary(
              submissionSummary.submissionReference(),
              submissionSummary.status(),
              submissionSummary.submissionPeriod(),
              submissionSummary.officeAccount(),
              claimDetails.totalClaimValue(),
              submissionSummary.areaOfLaw(),
              submissionSummary.submitted());
    }

    MessagesSummary messagesSummary =
        submissionMessagesBuilder.build(
            user,
            messageQuery.getSubmissionId(),
            null,
            ValidationMessageType.WARNING,
            messageQuery.getPage(),
            messageQuery.getSize(),
            messageQuery.getSort().toString());
    model.addAttribute("messagesSummary", messagesSummary);
    model.addAttribute(
        "messagesPaginationLinks",
        paginationLinksBuilder.build(
            "/submissions/%s".formatted(submissionViewQuery.getSubmissionId()),
            messagesSummary.pagination(),
            "messagesPage",
            "navTab",
            ViewSubmissionNavigationTab.CLAIM_MESSAGES,
            "messagesSort",
            messageQuery.getSort().toString()));

    List<SubmissionMatterStartsRow> matterStartsDetails =
        submissionMatterStartsDetailsBuilder.build(submissionResponse);
    model.addAttribute("matterStartsDetails", matterStartsDetails);

    boolean isCrimeLower =
        Optional.ofNullable(submissionResponse.getAreaOfLaw())
            .map(AreaOfLaw::getValue)
            .map(String::toLowerCase)
            .map(area -> area.contains("crime"))
            .orElse(false);

    model.addAttribute("isCrimeLower", isCrimeLower);

    addCounts(model, claimDetails, messagesSummary, matterStartsDetails);

    return submissionSummary;
  }

  private void handleInvalidSubmission(
      Model model, OidcUser user, SubmissionResponse submissionResponse, MessageQuery messageQuery) {

    MessagesSummary messagesSummary =
        submissionMessagesBuilder.buildErrors(
            user,
            messageQuery.getSubmissionId(),
            messageQuery.getPage(),
            messageQuery.getSize(),
            messageQuery.getSort().toString());
    model.addAttribute("messagesSummary", messagesSummary);
    model.addAttribute(
        "messagesPaginationLinks",
        paginationLinksBuilder.build(
            "/submissions/%s".formatted(messageQuery.getSubmissionId()),
            messagesSummary.pagination(),
            "messagesPage",
            "messagesSort",
            messageQuery.getSort().toString()));

    List<SubmissionMatterStartsRow> matterStartsDetails =
        submissionMatterStartsDetailsBuilder.build(submissionResponse);
    model.addAttribute("matterStartsDetails", matterStartsDetails);

    addCounts(model, messagesSummary, matterStartsDetails);
  }

  private void addCommonSubmissionAttributes(
      Model model,
      SubmissionSummary submissionSummary,
      SubmissionResponse submissionResponse,
      SubmissionViewQuery submissionViewQuery) {

    model.addAttribute("submissionSummary", submissionSummary);
    model.addAttribute("submissionStatus", submissionResponse.getStatus());
    model.addAttribute("navTab", submissionViewQuery.getNavTab().toString());
    model.addAttribute(SUBMISSION_ID, submissionViewQuery.getSubmissionId());
  }

  private void addCounts(
      Model model,
      SubmissionClaimsDetails claimDetails,
      MessagesSummary messagesSummary,
      List<SubmissionMatterStartsRow> matterStartsDetails) {
    int claimCount =
        Optional.ofNullable(claimDetails)
            .map(SubmissionClaimsDetails::pagination)
            .map(Page::getTotalElements)
            .orElse(0);
    model.addAttribute("claimCount", claimCount);

    addCounts(model, messagesSummary, matterStartsDetails);
  }

  private void addCounts(
      Model model,
      MessagesSummary messagesSummary,
      List<SubmissionMatterStartsRow> matterStartsDetails) {
    int messageCount =
        Optional.ofNullable(messagesSummary).map(MessagesSummary::totalMessageCount).orElse(0);

    long matterStartsCount =
        matterStartsDetails.stream()
            .mapToLong(SubmissionMatterStartsRow::numberOfMatterStarts)
            .sum();

    model.addAttribute("messageCount", messageCount);
    model.addAttribute("matterStartsCount", matterStartsCount);
  }
}
