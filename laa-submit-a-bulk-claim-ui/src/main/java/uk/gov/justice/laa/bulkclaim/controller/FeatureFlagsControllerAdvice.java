package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;

@RequiredArgsConstructor
@ControllerAdvice
public class FeatureFlagsControllerAdvice {

  private final FeatureFlagsConfig featureFlagsConfig;

  @ModelAttribute("featureFlagsConfig")
  public FeatureFlagsConfig featureFlagsConfig() {
    return featureFlagsConfig;
  }
}
