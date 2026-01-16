package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Renders the view and related views for the accessibility pages. */
@Controller
public class AccessibilityPageController {

  @GetMapping("/accessibility-statement")
  public String getAccessibilityPage() {
    return "pages/accessibility-statement";
  }
}
