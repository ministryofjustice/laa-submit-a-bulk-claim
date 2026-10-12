package uk.gov.justice.laa.payments.submit.builder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class LatestAssessmentResolver {

  private static final int ASSESSMENT_PAGE_SIZE = 5;

  private final DataClaimsRestClient dataClaimsRestClient;

  public Optional<AssessmentGet> resolveLatestAssessment(
      UUID claimId, DerivedClaimStatus derivedClaimStatus) {

    // Escape early to avoid additional call to Claims API
    if (!List.of(DerivedClaimStatus.AMENDED, DerivedClaimStatus.ASSESSED, DerivedClaimStatus.VOIDED)
        .contains(derivedClaimStatus)) {
      return Optional.empty();
    }
    AssessmentResultSet resultSet =
        dataClaimsRestClient
            .getClaimAssessments(claimId, 0, ASSESSMENT_PAGE_SIZE, "createdOn,desc")
            .block();

    Optional<AssessmentGet> latestAssessment =
        resultSet.getAssessments().stream().filter(Objects::nonNull).findFirst();

    if (latestAssessment.isEmpty()) {
      log.warn("No assessment found for claim {}", claimId);
    }

    return latestAssessment;
  }
}
