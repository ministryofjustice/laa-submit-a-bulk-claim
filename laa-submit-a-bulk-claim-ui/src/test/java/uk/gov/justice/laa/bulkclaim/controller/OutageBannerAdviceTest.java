package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Maintenance Banner Advice Test")
class OutageBannerAdviceTest {

  @Mock DateWrapperUtil dateWrapperUtil;

  ZonedDateTime rootTime =
      ZonedDateTime.of(LocalDate.of(2026, 1, 1), LocalTime.of(0, 0), ZoneId.systemDefault());

  @Nested
  @DisplayName("Get Maintenance Banner Enabled Tests")
  class GetMaintenanceBannerEnabledTests {

    @Test
    @DisplayName("Should be disabled if current time passed disabled time")
    void shouldBeDisabledIfCurrentTimePassed() {
      // Given
      ZonedDateTime disableAtTime = rootTime.minusDays(1);
      ZonedDateTime currentTime = rootTime;
      when(dateWrapperUtil.timeNow()).thenReturn(LocalDateTime.from(currentTime));
      OutageBannerAdvice outageBannerAdvice =
          new OutageBannerAdvice(disableAtTime, "Banner text", dateWrapperUtil);
      // When
      var result = outageBannerAdvice.getOutageBannerEnabled();
      // Then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should be enabled if current time not passed disabled time")
    void shouldBeDisabledIfCurrentTimeNotPassed() {
      // Given
      ZonedDateTime disableAtTime = rootTime.plusDays(1);
      ZonedDateTime currentTime = rootTime;
      when(dateWrapperUtil.timeNow()).thenReturn(LocalDateTime.from(currentTime));
      OutageBannerAdvice outageBannerAdvice =
          new OutageBannerAdvice(disableAtTime, "Banner text", dateWrapperUtil);
      // When
      var result = outageBannerAdvice.getOutageBannerEnabled();
      // Then
      assertThat(result).isTrue();
    }
  }

  @Nested
  @DisplayName("Get Outage Banner Message Tests")
  class GetOutageBannerMessageTests {

    @Test
    @DisplayName("Should return outage banner message")
    void shouldReturnMessage() {
      // Given
      String message = "Outage message";
      OutageBannerAdvice outageBannerAdvice =
          new OutageBannerAdvice(null, message, dateWrapperUtil);
      // When
      var result = outageBannerAdvice.getOutageBannerMessage();
      // Then
      assertThat(result).isEqualTo(message);
    }
  }
}
