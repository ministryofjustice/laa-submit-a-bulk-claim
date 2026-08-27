package uk.gov.justice.laa.payments.submit.builder;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimStatusBanner;
import uk.gov.justice.laa.payments.submit.util.DateTimeUtil;

@Component
@RequiredArgsConstructor
public class ClaimStatusBannerBuilder {

  private static final Map<DerivedClaimStatus, ClaimHistoryEventType> BANNER_EVENT_TYPES =
      Map.of(
          DerivedClaimStatus.VOIDED, ClaimHistoryEventType.VOID,
          DerivedClaimStatus.ASSESSED, ClaimHistoryEventType.ASSESSMENT,
          DerivedClaimStatus.AMENDED, ClaimHistoryEventType.AMENDMENT);

  private final DateTimeUtil dateTimeUtil;

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
            lastEdited == null ? "" : dateTimeUtil.displayDateTimeDateValue(lastEdited),
            lastEdited == null ? "" : dateTimeUtil.displayDateTimeTimeValue(lastEdited)));
  }
}
