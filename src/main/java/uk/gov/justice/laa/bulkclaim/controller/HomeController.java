package uk.gov.justice.laa.cwa.bulkupload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling the entry point of the Submit a bulk claim application.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
public class HomeController {

  /**
   * Renders the home page.
   *
   * @return the home page template.
   */
  @GetMapping("/")
  public String getHome() {

    return "pages/home";
  }
}
