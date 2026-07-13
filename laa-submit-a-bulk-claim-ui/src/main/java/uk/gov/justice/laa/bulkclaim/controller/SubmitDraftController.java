package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID})
public class SubmitDraftController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionSummaryBuilder submissionSummaryBuilder;
  private final DraftSubmissionService draftSubmissionService;

  @GetMapping("/submit-draft-submission")
  public String getSubmitSubmissionDraft(Model model,
      @Valid SubmissionViewQuery submissionViewQuery) {
    final SubmissionResponse submissionResponse =
        dataClaimsRestClient
            .getSubmission(submissionViewQuery.getSubmissionId())
            .blockOptional()
            .orElseThrow(
                () ->
                    new SubmitBulkClaimException(
                        "Submission %s does not exist"
                            .formatted(submissionViewQuery.getSubmissionId().toString())));
    model.addAttribute("submissionSummary", submissionSummaryBuilder.build(submissionResponse));

    return "pages/confirm-submit-draft-submission";
  }

  @PostMapping("/submit-draft-submission")
  public String postSubmitDraft(
      @SessionAttribute(value = SUBMISSION_ID) UUID submissionId) {
    draftSubmissionService.submitDraftSubmission(submissionId);
    return "redirect:/submission/%s".formatted(submissionId);
  }
}
