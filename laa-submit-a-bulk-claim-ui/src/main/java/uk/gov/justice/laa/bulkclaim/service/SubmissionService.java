package uk.gov.justice.laa.bulkclaim.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

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
}
