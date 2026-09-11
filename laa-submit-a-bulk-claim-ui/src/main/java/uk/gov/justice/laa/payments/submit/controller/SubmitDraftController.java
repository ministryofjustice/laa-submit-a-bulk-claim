package uk.gov.justice.laa.payments.submit.controller;

import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.SUBMISSION_ID;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.payments.submit.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.payments.submit.dto.submission.view.SubmissionViewQuery;
import uk.gov.justice.laa.payments.submit.service.DraftSubmissionService;

@Slf4j
@Controller
@RequiredArgsConstructor
@SubmissionControllerAdvice.Enabled
@SessionAttributes({SUBMISSION_ID})
public class SubmitDraftController {

  private final SubmissionSummaryBuilder submissionSummaryBuilder;
  private final DraftSubmissionService draftSubmissionService;

  @GetMapping("/submission/submit-draft")
  public String getSubmitSubmissionDraft(
      @ModelAttribute("submissionResponse") SubmissionResponse submissionResponse,
      Model model,
      @Valid SubmissionViewQuery submissionViewQuery) {

    model.addAttribute("submissionSummary", submissionSummaryBuilder.build(submissionResponse));

    return "pages/confirm-submit-draft-submission";
  }

  @PostMapping("/submission/submit-draft")
  public String postSubmitDraft(
      @ModelAttribute(name = SUBMISSION_ID) UUID submissionId,
      @AuthenticationPrincipal OidcUser oidcUser) {
    draftSubmissionService.submitDraftSubmission(submissionId, oidcUser);
    return "redirect:/submission/%s".formatted(submissionId);
  }
}
