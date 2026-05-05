package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class DefaultController {

  @GetMapping("/logged-out")
  public String loggedOut() {
    return "pages/logged-out";
  }
}
