package uk.gov.justice.laa.bulkclaim.controller;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;

@ControllerAdvice
public class MaintenanceBannerAdvice {


  private final ZonedDateTime disableAtTime;
  private final String maintenanceBannerMessage;

  private final DateWrapperUtil dateWrapperUtil;

  public MaintenanceBannerAdvice(
      @Value("${app.maintenance.disable-at-time}") ZonedDateTime disableAtTime,
      @Value("${app.maintenance.outage-banner-message}")
      String outageBannerMessage,
      DateWrapperUtil dateWrapperUtil) {
    this.disableAtTime = disableAtTime;
    this.maintenanceBannerMessage = outageBannerMessage;
    this.dateWrapperUtil = dateWrapperUtil;
  }

  @ModelAttribute("maintenanceBannerEnabled")
  public boolean getMaintenanceBannerEnabled() {
    var currentTime = dateWrapperUtil.timeNow();
    return currentTime.isBefore(ChronoLocalDateTime.from(disableAtTime));
  }

  @ModelAttribute("maintenanceBannerMessage")
  public String getMaintenanceBannerMessage(){
    return maintenanceBannerMessage;
  }
}
