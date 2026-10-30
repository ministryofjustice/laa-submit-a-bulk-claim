package uk.gov.justice.laa.payments.submit.builder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class LatestAssessmentResolver {

  private static final int ASSESSMENT_PAGE_SIZE = 1;

  private final DataClaimsRestClient dataClaimsRestClient;

  public Optional<AssessmentGet> resolveLatestAssessment(
      UUID claimId, DerivedClaimStatus derivedClaimStatus) {

    if (derivedClaimStatus != DerivedClaimStatus.ASSESSED
        && derivedClaimStatus != DerivedClaimStatus.VOIDED) {
      return Optional.empty();
    }
    AssessmentResultSet resultSet =
        dataClaimsRestClient
            .getClaimAssessments(claimId, 0, ASSESSMENT_PAGE_SIZE, "createdOn,desc")
            .block();

    // Only return the assessment related to the derived claim status
    Optional<AssessmentGet> latestAssessment =
        resultSet.getAssessments().stream().filter(Objects::nonNull).findFirst();

    if (latestAssessment.isEmpty()) {
      log.warn("No assessment found for claim {}", claimId);
    }

    latestAssessment.ifPresent(
        assessment -> logMismatchDerivedClaimStatus(claimId, derivedClaimStatus, assessment));

    return latestAssessment;
  }

  /**
   * Logs a warning if the derived claim status does not match the assessment type.
   *
   * <p>Matching rules:
   *
   * <ul>
   *   <li>DerivedClaimStatus.VOIDED must match AssessmentType.VOID
   *   <li>DerivedClaimStatus.ASSESSED must match either AssessmentType.ESCAPE_CASE_ASSESSMENT or
   *       AssessmentType.STAGE_DISBURSEMENT_ASSESSMENT
   * </ul>
   *
   * @param claimId the UUID of the claim being checked
   * @param derivedClaimStatus the derived status of the claim
   * @param assessment the assessment containing the assessment type to validate
   */
  private static void logMismatchDerivedClaimStatus(
      UUID claimId, DerivedClaimStatus derivedClaimStatus, AssessmentGet assessment) {
    boolean statusMatchesAssessmentType =
        derivedClaimStatus == DerivedClaimStatus.VOIDED
            ? assessment.getAssessmentType() == AssessmentType.VOID
            : assessment.getAssessmentType() == AssessmentType.ESCAPE_CASE_ASSESSMENT
                || assessment.getAssessmentType() == AssessmentType.STAGE_DISBURSEMENT_ASSESSMENT;
    if (!statusMatchesAssessmentType) {
      log.warn(
          "Derived claim status {} doesn't align with assessment type {} for claim {}",
          derivedClaimStatus,
          assessment.getAssessmentType(),
          claimId);
    }
  }
}
