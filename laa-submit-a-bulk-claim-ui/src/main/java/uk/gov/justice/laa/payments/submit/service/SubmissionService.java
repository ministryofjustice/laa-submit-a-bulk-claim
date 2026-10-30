package uk.gov.justice.laa.payments.submit.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.util.OidcAttributeUtils;

@RequiredArgsConstructor
@Service
@Slf4j
public class SubmissionService {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final OidcAttributeUtils oidcAttributeUtils;

  public SubmissionResponse getSubmission(UUID submissionId, OidcUser user) {
    var submission =
        dataClaimsRestClient
            .getSubmission(submissionId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submission %s does not exist".formatted(submissionId)));

    oidcAttributeUtils.checkOfficeAccess(user, submission.getOfficeAccountNumber());
    return submission;
  }

  public void approveSubmission(UUID submissionId, OidcUser user) {
    var submission = getSubmission(submissionId, user);
    if (submission.getStatus() != SubmissionStatus.VALIDATED_PENDING_APPROVAL) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Submission %s is not pending approval".formatted(submissionId));
    }

    var patch =
        new SubmissionPatch()
            .status(SubmissionStatus.READY_FOR_FINAL_VALIDATION)
            .officeAccountNumber(submission.getOfficeAccountNumber())
            .createdByUserId(user.getPreferredUsername());

    dataClaimsRestClient.patchSubmission(submissionId, patch).block();
  }
}
