package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Controller
@RequiredArgsConstructor
@SessionAttributes(SUBMISSION_ID)
public class AddClaimController {

  private static final String CREATED_BY_USER_ID = "Submit-a-bulk-claim";
  private static final String PLACEHOLDER_MATTER_TYPE_CODE = "TBC";

  private final DataClaimsRestClient dataClaimsRestClient;

  @PostMapping("/add-claim")
  public String addClaim(@SessionAttribute(SUBMISSION_ID) UUID submissionId) {
    var submission =
        dataClaimsRestClient
            .getSubmission(submissionId)
            .blockOptional()
            .orElseThrow(() -> new SubmitBulkClaimException("Submission does not exist"));

    if (submission.getStatus() == SubmissionStatus.READY_FOR_SUBMISSION) {
      dataClaimsRestClient.createClaim(
          submissionId,
          ClaimPost.builder()
              .status(ClaimStatus.INVALID)
              .lineNumber(nextLineNumber(submission.getOfficeAccountNumber(), submissionId))
              .matterTypeCode(PLACEHOLDER_MATTER_TYPE_CODE)
              .createdByUserId(CREATED_BY_USER_ID)
              .build());
    }

    return "redirect:/submission/%s".formatted(submissionId);
  }

  private int nextLineNumber(String officeAccountNumber, UUID submissionId) {
    var claims =
        dataClaimsRestClient.getClaims(officeAccountNumber, submissionId, 0, 10_000).getBody();
    return claims.getContent().stream()
            .map(ClaimResponse::getLineNumber)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0)
        + 1;
  }
}
