package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentType;


@Slf4j
@Component
@RequiredArgsConstructor
public class LatestAssessmentResolver {

  private static final int ASSESSMENT_PAGE_SIZE = 5;

  private final DataClaimsRestClient dataClaimsRestClient;

  public Optional<AssessmentGet> resolveLatestNonVoid(UUID claimId) {
    AssessmentResultSet resultSet =
        dataClaimsRestClient
            .getClaimAssessments(claimId, 0, ASSESSMENT_PAGE_SIZE, "createdOn,desc")
            .blockOptional()
            .orElse(null);

    Optional<AssessmentGet> latestNonVoid =
        Optional.ofNullable(resultSet)
            .map(AssessmentResultSet::getAssessments)
            .orElseGet(List::of)
            .stream()
            .filter(Objects::nonNull)
            .filter(assessment -> assessment.getAssessmentType() != AssessmentType.VOID)
            .findFirst();

    if (latestNonVoid.isEmpty()) {
      log.warn(
          "No non-VOID assessment found for claim {} despite an AMENDED/ASSESSED derived status",
          claimId);
    }

    return latestNonVoid;
  }
}
