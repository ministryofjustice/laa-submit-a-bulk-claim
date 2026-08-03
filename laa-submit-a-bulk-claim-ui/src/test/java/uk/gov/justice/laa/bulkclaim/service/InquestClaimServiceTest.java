package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.inquest.ClaimInquestData;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class InquestClaimServiceTest {

  private final DataClaimsRestClient client = Mockito.mock(DataClaimsRestClient.class);
  private final InquestClaimService service = new InquestClaimService(client, Set.of("INQUEST"));

  @Test
  void promptsOnlyForIncompleteCivilInquestClaimsOnDrafts() {
    UUID incompleteClaim = UUID.randomUUID();
    UUID completeClaim = UUID.randomUUID();
    when(client.getClaimInquestData(incompleteClaim)).thenReturn(ResponseEntity.notFound().build());
    when(client.getClaimInquestData(completeClaim))
        .thenReturn(
            ResponseEntity.ok(
                new ClaimInquestData(null, null, null, null, null, null, null, null, true)));

    assertThat(
            service.status(
                incompleteClaim,
                "INQUEST",
                AreaOfLaw.LEGAL_HELP,
                SubmissionStatus.READY_FOR_SUBMISSION))
        .isEqualTo(InquestClaimService.Status.INCOMPLETE);
    assertThat(
            service.status(
                completeClaim,
                "INQUEST",
                AreaOfLaw.LEGAL_HELP,
                SubmissionStatus.READY_FOR_SUBMISSION))
        .isEqualTo(InquestClaimService.Status.COMPLETE);
    assertThat(
            service.status(
                UUID.randomUUID(),
                "OTHER",
                AreaOfLaw.LEGAL_HELP,
                SubmissionStatus.READY_FOR_SUBMISSION))
        .isEqualTo(InquestClaimService.Status.NOT_INQUEST);
  }
}
