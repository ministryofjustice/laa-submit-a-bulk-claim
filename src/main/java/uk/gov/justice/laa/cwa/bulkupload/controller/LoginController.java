package uk.gov.justice.laa.cwa.bulkupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller for handling user sign-out functionality. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

  @GetMapping("/login")
  public String login() {
    return "pages/login";
  }
}
