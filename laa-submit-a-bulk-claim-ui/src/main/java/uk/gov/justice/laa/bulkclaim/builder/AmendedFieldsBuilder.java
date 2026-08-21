package uk.gov.justice.laa.bulkclaim.builder;

import static java.util.stream.Collectors.toSet;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType.AMENDMENT;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;

@UtilityClass
public class AmendedFieldsBuilder {

  private static final String CHANGES = "changes";
  private static final String CHANGE_SOURCE = "change_source";
  private static final String FIELD_IDENTIFIER = "field_identifier";
  private static final String REQUESTED = "REQUESTED";

  public static Set<String> build(List<ClaimHistoryEvent> historyEvents) {
    return historyEvents.stream()
        .filter(event -> event.getEventType() == AMENDMENT)
        .map(AmendedFieldsBuilder::getChanges)
        .flatMap(Collection::stream)
        .filter(AmendedFieldsBuilder::isRequested)
        .map(change -> change.get(FIELD_IDENTIFIER))
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .collect(toSet());
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
    return REQUESTED.equals(change.get(CHANGE_SOURCE));
  }
}
