package uk.gov.justice.laa.payments.submit.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimStatusBanner;

class ClaimStatusBannerBuilderTest {

  @Test
  void shouldBuildVoidedBannerFromMostRecentVoidEvent() {
    List<ClaimHistoryEvent> events =
        List.of(
            historyEvent(ClaimHistoryEventType.VOID, OffsetDateTime.parse("2026-01-01T09:15:00Z")),
            historyEvent(ClaimHistoryEventType.VOID, OffsetDateTime.parse("2026-03-17T14:32:00Z")),
            historyEvent(
                ClaimHistoryEventType.SUBMISSION, OffsetDateTime.parse("2026-04-01T10:00:00Z")));

    Optional<ClaimStatusBanner> banner =
        ClaimStatusBannerBuilder.build(DerivedClaimStatus.VOIDED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().status()).isEqualTo(DerivedClaimStatus.VOIDED);
    assertThat(banner.get().label()).isEqualTo("Voided");
    assertThat(banner.get().lastEditedDate()).isEqualTo("17/03/2026");
    assertThat(banner.get().lastEditedTime()).isEqualTo("14:32");
    assertThat(banner.get().error()).isTrue();
  }

  @Test
  void shouldBuildAssessedBannerFromMostRecentAssessmentEvent() {
    List<ClaimHistoryEvent> events =
        List.of(
            historyEvent(
                ClaimHistoryEventType.ASSESSMENT, OffsetDateTime.parse("2026-02-05T08:00:00Z")));

    Optional<ClaimStatusBanner> banner =
        ClaimStatusBannerBuilder.build(DerivedClaimStatus.ASSESSED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().label()).isEqualTo("Assessed");
    assertThat(banner.get().lastEditedDate()).isEqualTo("05/02/2026");
    assertThat(banner.get().lastEditedTime()).isEqualTo("08:00");
    assertThat(banner.get().error()).isFalse();
  }

  @Test
  void shouldBuildAmendedBannerFromMostRecentAmendmentEvent() {
    List<ClaimHistoryEvent> events =
        List.of(
            historyEvent(
                ClaimHistoryEventType.AMENDMENT, OffsetDateTime.parse("2026-06-30T23:59:00Z")));

    Optional<ClaimStatusBanner> banner =
        ClaimStatusBannerBuilder.build(DerivedClaimStatus.AMENDED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().label()).isEqualTo("Amended");
    assertThat(banner.get().lastEditedDate()).isEqualTo("30/06/2026");
    assertThat(banner.get().lastEditedTime()).isEqualTo("23:59");
    assertThat(banner.get().error()).isFalse();
  }

  @Test
  void shouldReturnEmptyForStatusesWithNoBanner() {
    List<ClaimHistoryEvent> events =
        List.of(historyEvent(ClaimHistoryEventType.SUBMISSION, OffsetDateTime.now()));

    assertThat(ClaimStatusBannerBuilder.build(DerivedClaimStatus.ACCEPTED, events)).isEmpty();
    assertThat(ClaimStatusBannerBuilder.build(DerivedClaimStatus.INVALID, events)).isEmpty();
    assertThat(ClaimStatusBannerBuilder.build(DerivedClaimStatus.READY_TO_PROCESS, events))
        .isEmpty();
  }

  @Test
  void shouldReturnBlankTimestampWhenNoMatchingHistoryEventExists() {
    List<ClaimHistoryEvent> events =
        List.of(historyEvent(ClaimHistoryEventType.SUBMISSION, OffsetDateTime.now()));

    Optional<ClaimStatusBanner> banner =
        ClaimStatusBannerBuilder.build(DerivedClaimStatus.ASSESSED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().lastEditedDate()).isEmpty();
    assertThat(banner.get().lastEditedTime()).isEmpty();
  }

  private ClaimHistoryEvent historyEvent(ClaimHistoryEventType type, OffsetDateTime timestamp) {
    return ClaimHistoryEvent.builder().eventType(type).eventTimestamp(timestamp).build();
  }
}
