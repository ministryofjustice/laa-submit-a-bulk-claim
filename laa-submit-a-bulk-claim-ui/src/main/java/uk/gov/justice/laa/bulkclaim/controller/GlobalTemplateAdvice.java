package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
