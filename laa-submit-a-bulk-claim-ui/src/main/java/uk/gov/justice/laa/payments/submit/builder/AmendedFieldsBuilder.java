package uk.gov.justice.laa.payments.submit.builder;

import static uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType.AMENDMENT;
import static uk.gov.justice.laa.payments.submit.util.MatterTypeUtil.MATTER_TYPE_CODE;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.payments.submit.util.MatterTypeUtil;

@UtilityClass
public class AmendedFieldsBuilder {

  private static final String CHANGES = "changes";
  private static final String CHANGE_SOURCE = "change_source";
  private static final String FIELD_IDENTIFIER = "field_identifier";
  private static final String BEFORE = "before";
  private static final String AFTER = "after";
  private static final String REQUESTED = "REQUESTED";

  public static Set<String> build(List<ClaimHistoryEvent> historyEvents) {
    Set<String> amendedFields = new LinkedHashSet<>();
    historyEvents.stream()
        .filter(event -> event.getEventType() == AMENDMENT)
        .map(AmendedFieldsBuilder::getChanges)
        .flatMap(Collection::stream)
        .filter(AmendedFieldsBuilder::isRequested)
        .forEach(change -> addIdentifiers(change, amendedFields));
    return amendedFields;
  }

  private static void addIdentifiers(
      final Map<String, Object> change, final Set<String> amendedFields) {
    final Object fieldIdentifier = change.get(FIELD_IDENTIFIER);
    if (fieldIdentifier == null) {
      return;
    }
    final String fieldName = String.valueOf(fieldIdentifier);
    amendedFields.add(fieldName);
    if (MATTER_TYPE_CODE.equals(fieldName)) {
      amendedFields.addAll(
          MatterTypeUtil.changedPartIdentifiers(
              asString(change.get(BEFORE)), asString(change.get(AFTER))));
    }
  }

  private static String asString(final Object value) {
    return value == null ? null : String.valueOf(value);
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> getChanges(final ClaimHistoryEvent event) {
    final var metadata = event.getMetadata();
    if (metadata == null) {
      return List.of();
    }
    return (List<Map<String, Object>>) metadata.getOrDefault(CHANGES, List.of());
  }

  private static boolean isRequested(final Map<String, Object> change) {
    return Objects.equals(REQUESTED, change.get(CHANGE_SOURCE));
  }
}
