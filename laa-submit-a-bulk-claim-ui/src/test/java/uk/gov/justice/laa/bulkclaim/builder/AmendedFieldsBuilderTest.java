package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType;

@DisplayName("Amended fields builder test")
class AmendedFieldsBuilderTest {

  @Test
  @DisplayName("Should return the field identifiers of provider requested changes")
  void shouldReturnRequestedChangeFieldIdentifiers() {
    List<ClaimHistoryEvent> events =
        List.of(
            amendmentEvent(change("client.clientForename", "REQUESTED")),
            amendmentEvent(change("claim.feeCode", "REQUESTED")));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder("client.clientForename", "claim.feeCode");
  }

  @Test
  @DisplayName("Should ignore changes sourced from fee scheme platform repricing")
  void shouldIgnoreFspChanges() {
    List<ClaimHistoryEvent> events =
        List.of(
            amendmentEvent(change("claim.feeCode", "REQUESTED"), change("fee.totalAmount", "FSP")));

    assertThat(AmendedFieldsBuilder.build(events)).containsExactly("claim.feeCode");
  }

  @Test
  @DisplayName("Should ignore events that are not amendments")
  void shouldIgnoreNonAmendmentEvents() {
    ClaimHistoryEvent assessment = new ClaimHistoryEvent();
    assessment.setEventType(ClaimHistoryEventType.ASSESSMENT);
    assessment.setMetadata(Map.of("changes", List.of(change("claim.feeCode", "REQUESTED"))));

    assertThat(AmendedFieldsBuilder.build(List.of(assessment))).isEmpty();
  }

  @Test
  @DisplayName("Should return an empty set when the claim has never been amended")
  void shouldReturnEmptySetWhenNeverAmended() {
    assertThat(AmendedFieldsBuilder.build(List.of())).isEmpty();
  }

  @Test
  @DisplayName("Should return an empty set when an amendment has no change metadata")
  void shouldReturnEmptySetWhenAmendmentHasNoMetadata() {
    ClaimHistoryEvent amendment = new ClaimHistoryEvent();
    amendment.setEventType(ClaimHistoryEventType.AMENDMENT);

    assertThat(AmendedFieldsBuilder.build(List.of(amendment))).isEmpty();
  }

  @SafeVarargs
  private static ClaimHistoryEvent amendmentEvent(Map<String, Object>... changes) {
    ClaimHistoryEvent event = new ClaimHistoryEvent();
    event.setEventType(ClaimHistoryEventType.AMENDMENT);
    event.setMetadata(Map.of("changes", List.of(changes)));
    return event;
  }

  private static Map<String, Object> change(String fieldIdentifier, String changeSource) {
    Map<String, Object> change = new LinkedHashMap<>();
    change.put("field_identifier", fieldIdentifier);
    change.put("change_source", changeSource);
    return change;
  }
}
