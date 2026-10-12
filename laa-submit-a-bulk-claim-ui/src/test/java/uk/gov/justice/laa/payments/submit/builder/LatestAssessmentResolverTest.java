package uk.gov.justice.laa.payments.submit.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;

@DisplayName("Latest assessment resolver test")
class LatestAssessmentResolverTest {

  private final DataClaimsRestClient dataClaimsRestClient = mock(DataClaimsRestClient.class);
  private final LatestAssessmentResolver resolver =
      new LatestAssessmentResolver(dataClaimsRestClient);
  private final UUID claimId = UUID.randomUUID();

  @ParameterizedTest
  @EnumSource(
      value = DerivedClaimStatus.class,
      names = {"ASSESSED", "VOIDED"})
  @DisplayName("Should return the most recent assessment")
  void shouldReturnTheLatestAssessment(DerivedClaimStatus derivedClaimStatus) {
    AssessmentGet latest = assessmentForStatus(derivedClaimStatus);
    stubAssessments(latest);

    Optional<AssessmentGet> result = resolver.resolveLatestAssessment(claimId, derivedClaimStatus);

    assertThat(result).contains(latest);
    verify(dataClaimsRestClient).getClaimAssessments(claimId, 0, 5, "createdOn,desc");
  }

  @ParameterizedTest
  @EnumSource(
      value = DerivedClaimStatus.class,
      names = {"ASSESSED", "VOIDED"})
  @DisplayName("Should return the most recent assessment when multiple assessments exist")
  void shouldReturnTheLatestAssessmentWhenMultipleExist(DerivedClaimStatus derivedClaimStatus) {
    AssessmentGet latest = assessmentForStatus(derivedClaimStatus);
    AssessmentGet secondLatest = assessmentForStatus(derivedClaimStatus);
    AssessmentGet mismatched =
        assessmentForStatus(
            derivedClaimStatus == DerivedClaimStatus.VOIDED
                ? DerivedClaimStatus.ASSESSED
                : DerivedClaimStatus.VOIDED);
    stubAssessments(null, new AssessmentGet(), mismatched, latest, secondLatest);

    Optional<AssessmentGet> result = resolver.resolveLatestAssessment(claimId, derivedClaimStatus);

    assertThat(result).contains(latest);
  }

  @ParameterizedTest
  @EnumSource(
      value = DerivedClaimStatus.class,
      names = {"ASSESSED", "VOIDED"})
  @DisplayName("Should return empty when no assessments exist for an eligible status")
  void shouldReturnEmptyWhenNoAssessmentsExist(DerivedClaimStatus derivedClaimStatus) {
    stubAssessments();

    Optional<AssessmentGet> result = resolver.resolveLatestAssessment(claimId, derivedClaimStatus);

    assertThat(result).isEmpty();
    verify(dataClaimsRestClient).getClaimAssessments(claimId, 0, 5, "createdOn,desc");
  }

  @ParameterizedTest
  @EnumSource(
      value = DerivedClaimStatus.class,
      names = {"ASSESSED", "VOIDED"},
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("Should return empty without calling Claims API for an ineligible status")
  void shouldSkipAssessmentsForIneligibleStatus(DerivedClaimStatus derivedClaimStatus) {
    Optional<AssessmentGet> result = resolver.resolveLatestAssessment(claimId, derivedClaimStatus);

    assertThat(result).isEmpty();
    verifyNoInteractions(dataClaimsRestClient);
  }

  @ParameterizedTest
  @EnumSource(
      value = AssessmentType.class,
      names = {"ESCAPE_CASE_ASSESSMENT", "STAGE_DISBURSEMENT_ASSESSMENT"})
  @DisplayName("Should return either assessment type for an assessed claim")
  void shouldReturnAssessmentForAssessedClaim(AssessmentType assessmentType) {
    AssessmentGet assessment = new AssessmentGet().assessmentType(assessmentType);
    stubAssessments(assessment);

    Optional<AssessmentGet> result =
        resolver.resolveLatestAssessment(claimId, DerivedClaimStatus.ASSESSED);

    assertThat(result).contains(assessment);
  }

  @ParameterizedTest
  @EnumSource(
      value = AssessmentType.class,
      names = {"ESCAPE_CASE_ASSESSMENT", "STAGE_DISBURSEMENT_ASSESSMENT"})
  @DisplayName("Should return empty when assessment types do not match the claim status")
  void shouldReturnEmptyWhenAssessmentTypesDoNotMatch(AssessmentType assessmentType) {
    stubAssessments(new AssessmentGet().assessmentType(assessmentType));
    assertThat(resolver.resolveLatestAssessment(claimId, DerivedClaimStatus.VOIDED)).isEmpty();

    stubAssessments(new AssessmentGet().assessmentType(AssessmentType.VOID));
    assertThat(resolver.resolveLatestAssessment(claimId, DerivedClaimStatus.ASSESSED)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(
      value = DerivedClaimStatus.class,
      names = {"ASSESSED", "VOIDED"})
  @DisplayName("Should return empty when assessments or their types are null")
  void shouldReturnEmptyWhenAssessmentsHaveNoType(DerivedClaimStatus derivedClaimStatus) {
    stubAssessments(null, new AssessmentGet());

    assertThat(resolver.resolveLatestAssessment(claimId, derivedClaimStatus)).isEmpty();
  }

  private AssessmentGet assessmentForStatus(DerivedClaimStatus derivedClaimStatus) {
    return new AssessmentGet()
        .assessmentType(
            derivedClaimStatus == DerivedClaimStatus.VOIDED
                ? AssessmentType.VOID
                : AssessmentType.ESCAPE_CASE_ASSESSMENT);
  }

  private void stubAssessments(AssessmentGet... assessmentsInOrder) {
    when(dataClaimsRestClient.getClaimAssessments(claimId, 0, 5, "createdOn,desc"))
        .thenReturn(
            Mono.just(new AssessmentResultSet().assessments(Arrays.asList(assessmentsInOrder))));
  }
}
