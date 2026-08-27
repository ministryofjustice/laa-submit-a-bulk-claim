package uk.gov.justice.laa.payments.submit.builder;

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

  @Test
  @DisplayName("Should identify only the half of the matter type code that changed")
  void shouldIdentifyOnlyChangedHalfOfMatterTypeCode() {
    List<ClaimHistoryEvent> events = List.of(matterTypeAmendment("FAMA:FPET", "FAMA:FPRO"));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder("claim.matterTypeCode", "claim.matterTypeCode#1");
  }

  @Test
  @DisplayName("Should identify the first half of the matter type code when it changed")
  void shouldIdentifyFirstHalfOfMatterTypeCode() {
    List<ClaimHistoryEvent> events = List.of(matterTypeAmendment("FAMA:FPET", "FAMB:FPET"));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder("claim.matterTypeCode", "claim.matterTypeCode#0");
  }

  @Test
  @DisplayName("Should identify both halves of the matter type code when both changed")
  void shouldIdentifyBothHalvesOfMatterTypeCode() {
    List<ClaimHistoryEvent> events = List.of(matterTypeAmendment("FAMA:FPET", "FAMB:FPRO"));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder(
            "claim.matterTypeCode", "claim.matterTypeCode#0", "claim.matterTypeCode#1");
  }

  @Test
  @DisplayName("Should treat a matter type code delimited by a plus the same as a colon")
  void shouldTreatPlusDelimiterAsColon() {
    List<ClaimHistoryEvent> events = List.of(matterTypeAmendment("FAMA+FPET", "FAMA+FPRO"));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder("claim.matterTypeCode", "claim.matterTypeCode#1");
  }

  @Test
  @DisplayName("Should identify a half that the matter type code gained")
  void shouldIdentifyGainedHalfOfMatterTypeCode() {
    List<ClaimHistoryEvent> events = List.of(matterTypeAmendment("FAMA", "FAMA:FPET"));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder("claim.matterTypeCode", "claim.matterTypeCode#1");
  }

  @Test
  @DisplayName("Should identify no half when the matter type code is reported without a change")
  void shouldIdentifyNoHalfWhenMatterTypeCodeUnchanged() {
    List<ClaimHistoryEvent> events = List.of(matterTypeAmendment("FAMA:FPET", "FAMA:FPET"));

    assertThat(AmendedFieldsBuilder.build(events)).containsExactly("claim.matterTypeCode");
  }

  @Test
  @DisplayName("Should identify halves across separate amendments to the matter type code")
  void shouldIdentifyHalvesAcrossSeparateAmendments() {
    List<ClaimHistoryEvent> events =
        List.of(
            matterTypeAmendment("FAMA:FPET", "FAMB:FPET"),
            matterTypeAmendment("FAMB:FPET", "FAMB:FPRO"));

    assertThat(AmendedFieldsBuilder.build(events))
        .containsExactlyInAnyOrder(
            "claim.matterTypeCode", "claim.matterTypeCode#0", "claim.matterTypeCode#1");
  }

  private static ClaimHistoryEvent matterTypeAmendment(String before, String after) {
    return amendmentEvent(change("claim.matterTypeCode", "REQUESTED", before, after));
  }

  @SafeVarargs
  private static ClaimHistoryEvent amendmentEvent(Map<String, Object>... changes) {
    ClaimHistoryEvent event = new ClaimHistoryEvent();
    event.setEventType(ClaimHistoryEventType.AMENDMENT);
    event.setMetadata(Map.of("changes", List.of(changes)));
    return event;
  }

  private static Map<String, Object> change(String fieldIdentifier, String changeSource) {
    return change(fieldIdentifier, changeSource, null, null);
  }

  private static Map<String, Object> change(
      String fieldIdentifier, String changeSource, Object before, Object after) {
    Map<String, Object> change = new LinkedHashMap<>();
    change.put("field_identifier", fieldIdentifier);
    change.put("change_source", changeSource);
    change.put("before", before);
    change.put("after", after);
    return change;
  }
}
