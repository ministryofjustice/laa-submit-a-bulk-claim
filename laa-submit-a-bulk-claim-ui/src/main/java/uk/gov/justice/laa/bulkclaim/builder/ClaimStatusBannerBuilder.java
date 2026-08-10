package uk.gov.justice.laa.bulkclaim.builder;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimStatusBanner;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

@Component
public class ClaimStatusBannerBuilder {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private static final Map<DerivedClaimStatus, ClaimHistoryEventType> BANNER_EVENT_TYPES =
      Map.of(
          DerivedClaimStatus.VOIDED, ClaimHistoryEventType.VOID,
          DerivedClaimStatus.ASSESSED, ClaimHistoryEventType.ASSESSMENT,
          DerivedClaimStatus.AMENDED, ClaimHistoryEventType.AMENDMENT);

  public Optional<ClaimStatusBanner> build(
      DerivedClaimStatus derivedClaimStatus, List<ClaimHistoryEvent> historyEvents) {
    ClaimHistoryEventType matchingEventType = BANNER_EVENT_TYPES.get(derivedClaimStatus);
    if (matchingEventType == null) {
      return Optional.empty();
    }

    OffsetDateTime lastEdited =
        historyEvents.stream()
            .filter(event -> event.getEventType() == matchingEventType)
            .map(ClaimHistoryEvent::getEventTimestamp)
            .max(Comparator.naturalOrder())
            .orElse(null);

    return Optional.of(
        new ClaimStatusBanner(
            derivedClaimStatus,
            lastEdited == null ? "" : DATE_FORMATTER.format(lastEdited),
            lastEdited == null ? "" : TIME_FORMATTER.format(lastEdited)));
  }
}
