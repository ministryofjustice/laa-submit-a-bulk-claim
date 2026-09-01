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
import uk.gov.justice.laa.payments.submit.util.DateTimeUtil;

class ClaimStatusBannerBuilderTest {

  private final ClaimStatusBannerBuilder claimStatusBannerBuilder =
      new ClaimStatusBannerBuilder(new DateTimeUtil());

  @Test
  void shouldBuildVoidedBannerFromMostRecentVoidEvent() {
    List<ClaimHistoryEvent> events =
        List.of(
            historyEvent(ClaimHistoryEventType.VOID, OffsetDateTime.parse("2026-01-01T09:15:00Z")),
            historyEvent(ClaimHistoryEventType.VOID, OffsetDateTime.parse("2026-03-17T14:32:00Z")),
            historyEvent(
                ClaimHistoryEventType.SUBMISSION, OffsetDateTime.parse("2026-04-01T10:00:00Z")));

    Optional<ClaimStatusBanner> banner =
        claimStatusBannerBuilder.build(DerivedClaimStatus.VOIDED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().status()).isEqualTo(DerivedClaimStatus.VOIDED);
    assertThat(banner.get().label()).isEqualTo("voided");
    assertThat(banner.get().lastEditedDate()).isEqualTo("17 March 2026");
    assertThat(banner.get().lastEditedTime()).isEqualTo("2:32pm");
    assertThat(banner.get().error()).isTrue();
  }

  @Test
  void shouldBuildAssessedBannerFromMostRecentAssessmentEvent() {
    List<ClaimHistoryEvent> events =
        List.of(
            historyEvent(
                ClaimHistoryEventType.ASSESSMENT, OffsetDateTime.parse("2026-02-05T08:00:00Z")));

    Optional<ClaimStatusBanner> banner =
        claimStatusBannerBuilder.build(DerivedClaimStatus.ASSESSED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().label()).isEqualTo("assessed");
    assertThat(banner.get().lastEditedDate()).isEqualTo("5 February 2026");
    assertThat(banner.get().lastEditedTime()).isEqualTo("8:00am");
    assertThat(banner.get().error()).isFalse();
  }

  @Test
  void shouldBuildAmendedBannerFromMostRecentAmendmentEvent() {
    List<ClaimHistoryEvent> events =
        List.of(
            historyEvent(
                ClaimHistoryEventType.AMENDMENT, OffsetDateTime.parse("2026-06-30T23:59:00Z")));

    Optional<ClaimStatusBanner> banner =
        claimStatusBannerBuilder.build(DerivedClaimStatus.AMENDED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().label()).isEqualTo("amended");
    assertThat(banner.get().lastEditedDate()).isEqualTo("1 July 2026");
    assertThat(banner.get().lastEditedTime()).isEqualTo("12:59am");
    assertThat(banner.get().error()).isFalse();
  }

  @Test
  void shouldReturnEmptyForStatusesWithNoBanner() {
    List<ClaimHistoryEvent> events =
        List.of(historyEvent(ClaimHistoryEventType.SUBMISSION, OffsetDateTime.now()));

    assertThat(claimStatusBannerBuilder.build(DerivedClaimStatus.ACCEPTED, events)).isEmpty();
    assertThat(claimStatusBannerBuilder.build(DerivedClaimStatus.INVALID, events)).isEmpty();
    assertThat(claimStatusBannerBuilder.build(DerivedClaimStatus.READY_TO_PROCESS, events))
        .isEmpty();
  }

  @Test
  void shouldReturnBlankTimestampWhenNoMatchingHistoryEventExists() {
    List<ClaimHistoryEvent> events =
        List.of(historyEvent(ClaimHistoryEventType.SUBMISSION, OffsetDateTime.now()));

    Optional<ClaimStatusBanner> banner =
        claimStatusBannerBuilder.build(DerivedClaimStatus.ASSESSED, events);

    assertThat(banner).isPresent();
    assertThat(banner.get().lastEditedDate()).isEmpty();
    assertThat(banner.get().lastEditedTime()).isEmpty();
  }

  private ClaimHistoryEvent historyEvent(ClaimHistoryEventType type, OffsetDateTime timestamp) {
    return ClaimHistoryEvent.builder().eventType(type).eventTimestamp(timestamp).build();
  }
}
