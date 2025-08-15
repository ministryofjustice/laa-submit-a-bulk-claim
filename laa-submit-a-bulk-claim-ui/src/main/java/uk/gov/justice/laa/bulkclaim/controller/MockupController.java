package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Mock up controller, not for production. This controller is used to mock up the UI for mockup
 * purposes.
 *
 * @author Jamie Briggs
 */
@Controller
@Profile("local")
public class MockupController {

  @GetMapping("/submission")
  public String viewSubmission() {
    return "pages/_submission";
  }

  @GetMapping("/claim-search")
  public String search() {
    return "pages/_search-results";
  }
}
