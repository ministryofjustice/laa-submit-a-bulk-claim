package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.view.SubmissionViewQuery;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.DraftSubmissionService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Controller
@RequiredArgsConstructor
@SessionAttributes(SUBMISSION_ID)
public class DiscardDraftController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionSummaryBuilder submissionSummaryBuilder;
  private final DraftSubmissionService draftSubmissionService;

  @GetMapping("/discard-draft-submission")
  public String getDiscardDraft(Model model, @Valid SubmissionViewQuery query) {
    var submission =
        dataClaimsRestClient
            .getSubmission(query.getSubmissionId())
            .blockOptional()
            .orElseThrow(() -> new SubmitBulkClaimException("Submission does not exist"));
    if (submission.getStatus() != SubmissionStatus.READY_FOR_SUBMISSION) {
      return "redirect:/submission/%s".formatted(query.getSubmissionId());
    }
    model.addAttribute(SUBMISSION_ID, query.getSubmissionId());
    model.addAttribute("submissionSummary", submissionSummaryBuilder.build(submission));
    return "pages/confirm-discard-draft-submission";
  }

  @PostMapping("/discard-draft-submission")
  public String postDiscardDraft(@SessionAttribute(SUBMISSION_ID) UUID submissionId) {
    draftSubmissionService.discardDraftSubmission(submissionId);
    return "redirect:/submission/%s".formatted(submissionId);
  }
}
