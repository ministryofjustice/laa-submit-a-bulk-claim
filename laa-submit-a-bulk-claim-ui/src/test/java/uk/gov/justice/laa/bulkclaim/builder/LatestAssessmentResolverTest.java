package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentType;

@DisplayName("Latest assessment resolver test")
class LatestAssessmentResolverTest {

  private final DataClaimsRestClient dataClaimsRestClient = mock(DataClaimsRestClient.class);
  private final LatestAssessmentResolver resolver =
      new LatestAssessmentResolver(dataClaimsRestClient);
  private final UUID claimId = UUID.randomUUID();

  @Test
  @DisplayName("Should return the most recent non-VOID assessment")
  void shouldReturnTheLatestNonVoidAssessment() {
    AssessmentGet latest =
        new AssessmentGet().assessmentType(AssessmentType.ESCAPE_CASE_ASSESSMENT);
    stubAssessments(latest);

    Optional<AssessmentGet> result = resolver.resolveLatestNonVoid(claimId);

    assertThat(result).contains(latest);
  }

  @Test
  @DisplayName("Should skip a VOID assessment record and fall back to the next non-VOID assessment")
  void shouldFallBackPastAVoidRecord() {
    AssessmentGet voided = new AssessmentGet().assessmentType(AssessmentType.VOID);
    AssessmentGet previous =
        new AssessmentGet().assessmentType(AssessmentType.STAGE_DISBURSEMENT_ASSESSMENT);
    stubAssessments(voided, previous);

    Optional<AssessmentGet> result = resolver.resolveLatestNonVoid(claimId);

    assertThat(result).contains(previous);
  }

  @Test
  @DisplayName("Should degrade to empty, not throw, when every assessment is VOID")
  void shouldDegradeToEmptyWhenAllAssessmentsAreVoid() {
    stubAssessments(new AssessmentGet().assessmentType(AssessmentType.VOID));

    Optional<AssessmentGet> result = resolver.resolveLatestNonVoid(claimId);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should degrade to empty, not throw, when the claim has no assessments at all")
  void shouldDegradeToEmptyWhenThereAreNoAssessments() {
    when(dataClaimsRestClient.getClaimAssessments(eq(claimId), any(), any(), any()))
        .thenReturn(Mono.just(new AssessmentResultSet().assessments(List.of())));

    Optional<AssessmentGet> result = resolver.resolveLatestNonVoid(claimId);

    assertThat(result).isEmpty();
  }

  private void stubAssessments(AssessmentGet... assessmentsInOrder) {
    when(dataClaimsRestClient.getClaimAssessments(eq(claimId), any(), any(), any()))
        .thenReturn(Mono.just(new AssessmentResultSet().assessments(List.of(assessmentsInOrder))));
  }
}
