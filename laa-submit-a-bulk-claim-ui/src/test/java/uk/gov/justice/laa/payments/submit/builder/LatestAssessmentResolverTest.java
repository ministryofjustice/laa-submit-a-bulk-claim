package uk.gov.justice.laa.payments.submit.builder;

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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentType;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;

@DisplayName("Latest assessment resolver test")
class LatestAssessmentResolverTest {

  private final DataClaimsRestClient dataClaimsRestClient = mock(DataClaimsRestClient.class);
  private final LatestAssessmentResolver resolver =
      new LatestAssessmentResolver(dataClaimsRestClient);
  private final UUID claimId = UUID.randomUUID();

  @Test
  @DisplayName("Should return the most recent assessment")
  void shouldReturnTheLatestAssessment() {
    AssessmentGet latest =
        new AssessmentGet().assessmentType(AssessmentType.ESCAPE_CASE_ASSESSMENT);
    stubAssessments(latest);

    Optional<AssessmentGet> result = resolver.resolveLatestAssessment(claimId);

    assertThat(result).contains(latest);
  }

  @Test
  @DisplayName("Should return the most recent assessment when multiple assessments exist")
  void shouldReturnTheLatestAssessmentWhenMultipleExist() {
    AssessmentGet latest =
        new AssessmentGet().assessmentType(AssessmentType.ESCAPE_CASE_ASSESSMENT);
    AssessmentGet secondLatest =
        new AssessmentGet().assessmentType(AssessmentType.ESCAPE_CASE_ASSESSMENT);
    stubAssessments(latest, secondLatest);

    Optional<AssessmentGet> result = resolver.resolveLatestAssessment(claimId);

    assertThat(result).contains(latest);
  }

  private void stubAssessments(AssessmentGet... assessmentsInOrder) {
    when(dataClaimsRestClient.getClaimAssessments(eq(claimId), any(), any(), any()))
        .thenReturn(Mono.just(new AssessmentResultSet().assessments(List.of(assessmentsInOrder))));
  }
}
