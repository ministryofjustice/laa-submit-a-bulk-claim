package uk.gov.justice.laa.bulkclaim.service;

import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Service
public class InquestClaimService {

  public enum Status {
    NOT_INQUEST,
    INCOMPLETE,
    COMPLETE
  }

  private final DataClaimsRestClient dataClaimsRestClient;
  private final Set<String> inquestMatterTypeCodes;

  public InquestClaimService(
      DataClaimsRestClient dataClaimsRestClient,
      @Value("${app.inquest.matter-type-codes:INQUEST}") Set<String> inquestMatterTypeCodes) {
    this.dataClaimsRestClient = dataClaimsRestClient;
    this.inquestMatterTypeCodes = inquestMatterTypeCodes;
  }

  public Status status(
      UUID claimId, String matterTypeCode, AreaOfLaw areaOfLaw, SubmissionStatus submissionStatus) {
    if (submissionStatus != SubmissionStatus.READY_FOR_SUBMISSION
        || areaOfLaw != AreaOfLaw.LEGAL_HELP
        || !inquestMatterTypeCodes.contains(matterTypeCode)) {
      return Status.NOT_INQUEST;
    }
    try {
      var response = dataClaimsRestClient.getClaimInquestData(claimId);
      return response.getStatusCode().is2xxSuccessful()
              && response.getBody() != null
              && Boolean.TRUE.equals(response.getBody().complete())
          ? Status.COMPLETE
          : Status.INCOMPLETE;
    } catch (WebClientResponseException.NotFound exception) {
      return Status.INCOMPLETE;
    }
  }
}
