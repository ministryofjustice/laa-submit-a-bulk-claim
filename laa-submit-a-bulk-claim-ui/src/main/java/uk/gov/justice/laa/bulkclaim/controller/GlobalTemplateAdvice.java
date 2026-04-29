package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * A controller advice that provides global attributes to be used across the application. This class
 * exposes a model attribute which can be accessed in views.
 */
@ControllerAdvice
public class GlobalTemplateAdvice {
  @Value("${links.laa-homepage}")
  private String laaHomepageLink;

  @Value("${links.bulk-upload-amendments}")
  private String laaAmendmentsLink;

  @Value("${links.bulk-upload-guidance}")
  private String laaBulkUploadGuidanceText;

  @ModelAttribute("laaHomepageLink")
  public String laaHomepageLink() {
    return laaHomepageLink;
  }

  @ModelAttribute("laaAmendmentsLink")
  public String laaAmendmentsLink() {
    return laaAmendmentsLink;
  }

  @ModelAttribute("laaBulkUploadGuidanceText")
  public String laaBulkUploadGuidanceText() {
    return laaBulkUploadGuidanceText;
  }
}
