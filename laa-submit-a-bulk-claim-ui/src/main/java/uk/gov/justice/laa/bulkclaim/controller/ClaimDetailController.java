package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimDetailsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, CLAIM_ID})
public class ClaimDetailController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final SubmissionClaimDetailsMapper submissionClaimDetailsMapper;

  @GetMapping("/submission/claim/{claimReference}")
  public String getClaimDetail(
      @PathVariable("claimReference") UUID claimReference, HttpSession httpSession) {
    httpSession.setAttribute(CLAIM_ID, claimReference);
    return "redirect:/view-claim-detail";
  }

  @GetMapping("/view-claim-detail")
  public String getClaimDetail(
      Model model,
      @ModelAttribute(SUBMISSION_ID) UUID submissionId,
      @ModelAttribute(CLAIM_ID) UUID claimId) {
    ClaimResponse submissionClaim =
        dataClaimsRestClient
            .getSubmissionClaim(submissionId, claimId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new SubmitBulkClaimException(
                        "Claim %s does not exist for submission %s"
                            .formatted(claimId.toString(), submissionId.toString())));
    SubmissionClaimDetails submissionClaimDetails =
        submissionClaimDetailsMapper.toSubmissionClaimDetails(submissionClaim);
    model.addAttribute("claimDetails", submissionClaimDetails);
    return "pages/view-claim-detail";
  }
}
