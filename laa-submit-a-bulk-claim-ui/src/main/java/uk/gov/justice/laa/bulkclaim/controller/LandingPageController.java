package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller for rendering the service landing page. */
@Controller
public class LandingPageController {

  /**
   * Renders the landing page and clears the session.
   *
   * @return the landing page
   */
  @GetMapping("/")
  public String getLandingPage() {
    return "pages/landing";
  }
}
